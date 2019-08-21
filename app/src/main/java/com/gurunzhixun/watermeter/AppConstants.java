package com.gurunzhixun.watermeter;

import com.gurunzhixun.watermeter.util.PreferenceUtils;
import com.gurunzhixun.watermeter.util.utils.Constant;
import com.socks.library.KLog;

import java.util.UUID;

import cn.droidlover.xdroidmvp.kit.Kits;

/**
 * Created by vigss on 2018/3/13.
 */

public class AppConstants {
    public static final String BRANDCODE = "DPAD";
    public static String APPID = "0";
    public static final String APPVERSIONNO = Kits.Package.getVersionCode(MainApplicaton.getContext()) + "";
    public static final String DEVICENO = "869718026776358";
    public static final String DEVICETYPE = "00";

    //生成UUID
    public static String getMyUUID() {
        String uniqueId = "";
        if (Kits.Empty.check(PreferenceUtils.getInstance(MainApplicaton.getContext()).getString(Constant.DEVICEID_KEY))) {
            UUID uuid = UUID.randomUUID();
            uniqueId = uuid.toString();
            PreferenceUtils.getInstance(MainApplicaton.getContext()).setString(Constant.DEVICEID_KEY, uniqueId);
        } else {
            uniqueId = PreferenceUtils.getInstance(MainApplicaton.getContext()).getString(Constant.DEVICEID_KEY);
        }
        KLog.e(uniqueId + "UUID_________________");
        return uniqueId;
    }
}
