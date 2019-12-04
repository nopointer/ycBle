package ycble.runchinaup.core;

import android.content.Context;

import ycble.runchinaup.aider.PushAiderHelper;

public class ycBleSDK {

    /**
     * 初始化蓝牙
     *
     * @param context
     */
    public static void initSDK(Context context) {
        initSDK(context, true);
    }



    public static void initSDK(Context context, boolean enablePushAider) {
        BleScanner.init(context);
        AbsBleManager.initSDK(context);
        if (enablePushAider) {
            PushAiderHelper.getAiderHelper().startListeningForNotifications(context);
            PushAiderHelper.getAiderHelper().registerCallAndSmsReceiver(context);
        }else {
            PushAiderHelper.getAiderHelper().stopListeningForNotifications(context);
            PushAiderHelper.getAiderHelper().unRegisterCallAndSmsReceiver(context);
        }
    }

    public static void setScanLog(boolean enable) {
        BleScanner.isShowScanLog = enable;
    }


}
