package com.gurunzhixun.watermeter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.baronzhang.android.library.util.RxSchedulerUtils;
import com.gurunzhixun.watermeter.data.entity.CuscResultVo;
import com.gurunzhixun.watermeter.modules.yhcz.model.repository.SBCZDataRepository;
import com.gurunzhixun.watermeter.util.PreferenceUtils;
import com.gurunzhixun.watermeter.util.ToolKit;
import com.gurunzhixun.watermeter.util.utils.DateUtil;
import com.gurunzhixun.watermeter.util.utils.T;

import java.util.Date;

import okhttp3.FormBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UPCBMeterDataService extends Service {


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	String data="";
	String metercode="";
	String readtime="";
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		metercode = MainApplicaton.meterVo.getMetercode();
		readtime = PreferenceUtils.getInstance(UPCBMeterDataService.this).getString(metercode+"readtime");
		data = PreferenceUtils.getInstance(UPCBMeterDataService.this).getString(metercode+"cb");
		if(!"".equals(data)){
			upMeterData(metercode,data,readtime,"1");
		}
		return super.onStartCommand(intent, flags, startId);
	}
	public void upMeterData(String metercode,String data,String readtime,String type) {
		FormBody body = new FormBody.Builder()
				.add("metercode",metercode)
				.add("type",type)
				.add("readtime",readtime)
				.add("client", "android"+ ToolKit.getLocalVersionName(MainApplicaton.getContext()))
				.add("data",data).build();
		Subscription subscription = SBCZDataRepository.getInstance().upMeterData(body).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread()).compose(RxSchedulerUtils.normalSchedulersTransformer()).subscribe(new Observer<CuscResultVo<String>>() {
					@Override
					public void onNext(CuscResultVo<String> message) {
						if (message.getSuccess()) {
							//T.showToastSafe(message.getMsg());
							PreferenceUtils.getInstance(UPCBMeterDataService.this).setString(metercode+"cb","");
							PreferenceUtils.getInstance(UPCBMeterDataService.this).setString(metercode+"readtime","");
						} else {
							//T.showToastSafe(message.getMsg());
						}
					}

					@Override
					public void onCompleted() {

					}

					@Override
					public void onError(Throwable e) {
					}
				});
	}


}
