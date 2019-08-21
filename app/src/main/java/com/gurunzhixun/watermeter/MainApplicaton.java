package com.gurunzhixun.watermeter;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.gurunzhixun.watermeter.base.basecopy.ActivityManager;
import com.gurunzhixun.watermeter.modules.ble.BLEDevice;
import com.gurunzhixun.watermeter.modules.ble.BleHelper;
import com.gurunzhixun.watermeter.modules.gl.model.entity.GLYHentity;
import com.gurunzhixun.watermeter.modules.hdym.WebViewActivity;
import com.gurunzhixun.watermeter.modules.hy.activity.WelcomeActivity;
import com.gurunzhixun.watermeter.modules.sbgl.model.entity.ADDMeterVO;
import com.gurunzhixun.watermeter.modules.sbgl.model.entity.MyMeterVo;
import com.gurunzhixun.watermeter.modules.yhdl.activity.LoginActivity;
import com.gurunzhixun.watermeter.modules.yhdl.model.entity.LoginResultVBack;
import com.gurunzhixun.watermeter.modules.yhdl.model.entity.LoginResultVO;
import com.gurunzhixun.watermeter.util.PreferenceUtils;
import com.gurunzhixun.watermeter.util.utils.Constant;
import com.gurunzhixun.watermeter.util.utils.NetUtil;
import com.gurunzhixun.watermeter.util.utils.T;
import com.umeng.commonsdk.UMConfigure;

import java.util.ArrayList;
import java.util.List;

import cn.droidlover.xdroidmvp.kit.Kits;
import cn.droidlover.xdroidmvp.net.NetError;
import cn.droidlover.xdroidmvp.net.NetProvider;
import cn.droidlover.xdroidmvp.net.RequestHandler;
import cn.droidlover.xdroidmvp.net.XApi;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * Created by vigss on 2018/3/13.
 */

public class MainApplicaton extends Application {
    /**
     * ble控制类
     */
    public static  BleHelper mBle = new BleHelper();
    private static MainApplicaton mainApplicaton;
    public static LoginResultVO LOGINRESULTVO = new LoginResultVO();

    public static LoginResultVBack loginResultVBack = new LoginResultVBack();
    public static String WDQBActivity_TAG = "1";

    public static MainApplicaton getInstance() {
        return mainApplicaton;
    }

    public static Context mContext;

    public static MyMeterVo meterVo;
    public static final String TAG = MainApplicaton.class.getSimpleName();
    public static String meterType;
    private static Context context;
    private static Thread mainThread;
    private static int mainThreadId;
    private static Handler mainThreadHandler;
    public  static List<Activity> mActivityList;
    public  static List<BluetoothGattService> servicesList;
    public List<Activity> getmActivityList() {
        return mActivityList;
    }
    public static GLYHentity gLYHentity;
    public void setmActivityList(List<Activity> mActivityList) {
        this.mActivityList = mActivityList;
    }

    //用户是否已登录
    public static boolean sIsLogin;
    //是否第一次打开App
    public static boolean sIsFirstOpen;
    //首页是否已加载过
    public static boolean sHasMainLoad;

    public static String clientId = "";
    public static String APP_ID = "wxf52b888d7d85a95f";

    public static String setCmd = "";
    public static String rechargeCmd = "";
    public static String clearCmd = "";
    public static String hbid = "";
    public static String getClientId() {
        if (clientId.isEmpty()) {
            String s = PreferenceUtils.getInstance(MainApplicaton.getContext()).getString(Constant.KEY_CLIENTID);
            clientId = Kits.Empty.check(s) ? clientId : s;//如果缓存中clientId为空,则取clientId
        }
        return clientId;
    }
    public static void setClientId(String clientId) {
        MainApplicaton.clientId = clientId;
        PreferenceUtils.getInstance(MainApplicaton.getContext()).setString(Constant.KEY_CLIENTID, clientId);
    }

    //创建通知的id,可能收到多条消息
    private int notifyId = 1;
    public static int mNetWorkState;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }




    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mainApplicaton = this;

        context = getApplicationContext();
        mainThreadHandler = new Handler();
        mainThread = Thread.currentThread();
        mainThreadId = Process.myTid();
        //存储activity的集合
        mActivityList = new ArrayList<>();
        initActivityLifecycle();
        initNet();

        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE,"");
        //初始化ble的控制类
        mBle.init(this);

    }

    public BleHelper getmBle() {
        return mBle;
    }

    public void setmBle(BleHelper mBle) {
        this.mBle = mBle;
    }
    /**
     * 初始化Activity生命周期管理
     */
    private void initActivityLifecycle() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                ActivityManager.getInstance().setCurrentActivity(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }


    /**
     * 网络框架相关初始化
     */
    private void initNet() {
        XApi.registerProvider(new NetProvider() {
            @Override
            public Interceptor[] configInterceptors() {
                return new Interceptor[0];
            }

            @Override
            public void configHttps(OkHttpClient.Builder builder) {

            }

            @Override
            public CookieJar configCookie() {
                return null;
            }

            @Override
            public RequestHandler configHandler() {
                return null;
            }

            @Override
            public long configConnectTimeoutMills() {
                return 15 * 1000L;
            }

            @Override
            public long configReadTimeoutMills() {
                return 15 * 1000L;
            }

            @Override
            public boolean configLogEnable() {
                return true;
            }

            @Override
            public boolean handleError(NetError error) {
                String message = error.getMessage();
                int errorCode = error.getErrorCode();
                T.showToastSafe(message);
                //如果登陆失效,清除登陆信息,提示登陆
//                if (errorCode == NetErrorCode.CODE_NEED_LOGIN) {
//                    UserDataUtil.clearUserData(context);
//                    //如果当前应用不是在后台 那就跳转登陆页面
//                    if (!SystemUtil.isAppIsInBackground(context)) {
//                        Activity currentActivity = ActivityManager.getInstance().getCurrentActivity();
//                        UserLoginActivity.launch(currentActivity);
//                    }
//                }
                return true;
            }
        });
    }

    public static Context getContext() {
        return context;
    }

    public static Thread getMainThread() {
        return mainThread;
    }

    public static int getMainThreadId() {
        return mainThreadId;
    }

    public static Handler getMainThreadHandler() {
        return mainThreadHandler;
    }

    public void addActivity(Activity activity) {
        mActivityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        if (mActivityList.contains(activity)) {
            mActivityList.remove(activity);
            activity.finish();
        }
    }

    public void removeAllActivities() {
        for (Activity activity : mActivityList) {
            activity.finish();
        }
        mActivityList.clear();
    }

    public void initData() {
        mNetWorkState = NetUtil.getNetworkState(this);
    }
    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        Log.d(TAG, "onTerminate");
//        Intent intentFour = new Intent(this, UPMeterDataService.class);
//        stopService(intentFour);
        super.onTerminate();
    }
    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        Log.d(TAG, "onLowMemory");
        super.onLowMemory();
    }
    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        Log.d(TAG, "onTrimMemory");
        super.onTrimMemory(level);
    }
}
