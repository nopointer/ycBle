package ycble.runchinaup.aider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by nopointer on 2018/7/26.
 */

class NPLiveReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        LogUtil.e("NPLiveReceiver==>" + intent.getAction());
        NotificationMsgUtil.ensureCollectorRunning(context);
    }

    /**
     * 创建系统常用的广播接收器
     *
     * @return
     */
    public static IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        return intentFilter;
    }

}
