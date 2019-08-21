package com.gurunzhixun.watermeter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

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

public class UPMeterDataService extends Service {


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
	String rechargeId="";
	String rechargeData="";
	String setData="";
	String clearData="";
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String meterBack = PreferenceUtils.getInstance(UPMeterDataService.this).getString("getDataMeterBack");
		clearData = PreferenceUtils.getInstance(UPMeterDataService.this).getString(meterBack+"clearDataBack");
		setData = PreferenceUtils.getInstance(UPMeterDataService.this).getString(meterBack+"setDataBack");
		rechargeData = PreferenceUtils.getInstance(UPMeterDataService.this).getString(meterBack+"rechargeDataBack");
		rechargeId = PreferenceUtils.getInstance(UPMeterDataService.this).getString(meterBack+"rechargeIdBack");
//		Toast.makeText(UPMeterDataService.this,"clearData:"+clearData, Toast.LENGTH_LONG).show();
//		Toast.makeText(UPMeterDataService.this,"setData:"+setData, Toast.LENGTH_LONG).show();
//		Toast.makeText(UPMeterDataService.this,"rechargeData:"+rechargeData, Toast.LENGTH_LONG).show();
//		Toast.makeText(UPMeterDataService.this,"rechargeId:"+rechargeId, Toast.LENGTH_LONG).show();
		if(!"".equals(clearData)){
//			Toast.makeText(UPMeterDataService.this,"开始上传换表数据", Toast.LENGTH_LONG).show();
			upMeterData(meterBack,clearData,"3");
		}else{
			if(!rechargeData.equals("")){
				if(setData.equals("")){
					upRechargeStatus(rechargeId,rechargeData,"2");
				}else{
					upRechargeStatus(rechargeId,setData,"1");
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	public void upRechargeStatus(String rechargeId,String data,String type) {
		FormBody body = new FormBody.Builder()
				.add("client", "android"+ ToolKit.getLocalVersionName(MainApplicaton.getContext()))
				.add("rechargeId",rechargeId)
				.add("status",type)
				.add("data",data).build();
		SBCZDataRepository.getInstance().upRechargeStatus(body).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread()).compose(RxSchedulerUtils.normalSchedulersTransformer()).subscribe(new Observer<CuscResultVo<String>>() {
					@Override
					public void onNext(CuscResultVo<String> message) {
						if (message.getSuccess()) {
							//T.showToastSafe(message.getMsg());
						} else {
							//T.showToastSafe(message.getMsg());
						}
						if(type.equals("1")){
							callBack(1);
						}else if(type.equals("2")){
							callBack(2);
						}else if(type.equals("3")){
							callBack(3);
						}else if(type.equals("4")){
							callBack(4);
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
	public void upMeterData(String metercode,String data,String type) {
		FormBody body = new FormBody.Builder()
				.add("metercode",metercode)
				.add("type",type)
				.add("readtime", DateUtil.getDateString(new Date().getTime(),"yyyy-MM-dd HH:mm:ss"))
				.add("client", "android"+ ToolKit.getLocalVersionName(MainApplicaton.getContext()))
				.add("data",data).build();
		Subscription subscription = SBCZDataRepository.getInstance().upMeterData(body).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread()).compose(RxSchedulerUtils.normalSchedulersTransformer()).subscribe(new Observer<CuscResultVo<String>>() {
					@Override
					public void onNext(CuscResultVo<String> message) {
						if (message.getSuccess()) {
							if(!rechargeData.equals("")&&!setData.equals("")){
								upRechargeStatus(rechargeId,setData,"3");
							}
						} else {
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
	public void callBack(int index) {
		if(index==1){
			PreferenceUtils.getInstance(UPMeterDataService.this).setString("setDataBack","");
			upRechargeStatus(rechargeId,rechargeData,"2");
		}else if(index==2){
			PreferenceUtils.getInstance(UPMeterDataService.this).setString("rechargeIdBack","");
			PreferenceUtils.getInstance(UPMeterDataService.this).setString("rechargeDataBack","");
		}else if(index==3){
			PreferenceUtils.getInstance(UPMeterDataService.this).setString("setDataBack","");
			upRechargeStatus(rechargeId,rechargeData,"4");
		}else if(index==4){
			PreferenceUtils.getInstance(UPMeterDataService.this).setString("rechargeIdBack","");
			PreferenceUtils.getInstance(UPMeterDataService.this).setString("rechargeDataBack","");
			PreferenceUtils.getInstance(UPMeterDataService.this).setString("clearDataBack","");
		}
	}

}
