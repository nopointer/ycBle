package ycble.runchinaup.core;

import android.content.Context;

import ycble.runchinaup.aider.AiderHelper;

public class ycBleSDK {

    /**
     * 初始化蓝牙
     *
     * @param context
     */
    public static void initSDK(Context context) {
        initSDK(context, true);
    }


    public static void initSDK(Context context, boolean enableAider) {
        AbsBleManager.initSDK(context);
        if (enableAider) {
            AiderHelper.init(context);
        }
    }

    public static void setScanLog(boolean enable) {
        BleScaner.isShowScanLog = enable;
    }


}
