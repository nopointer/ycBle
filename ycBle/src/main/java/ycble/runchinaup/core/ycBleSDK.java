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
        AbsBleManager.initSDK(context);
        AiderHelper.init(context);
    }

}
