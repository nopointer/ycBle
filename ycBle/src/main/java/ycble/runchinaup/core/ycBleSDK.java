package ycble.runchinaup.core;

import android.content.Context;

public class ycBleSDK {

    /**
     * 初始化蓝牙
     *
     * @param context
     */
    public static void initSDK(Context context) {
        BleScanner.init(context);
        AbsBleManager.initSDK(context);
    }




    public static void setScanLog(boolean enable) {
        BleScanner.isShowScanLog = enable;
    }


}
