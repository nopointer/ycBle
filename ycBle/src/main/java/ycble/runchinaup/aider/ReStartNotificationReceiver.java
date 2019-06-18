package ycble.runchinaup.aider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import ycble.runchinaup.log.ycBleLog;

/**
 * Created by nopointer on 2018/7/26.
 * 重新启动通知栏的辅助广播接收器
 */

class ReStartNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        LogUtil.e("NPLiveReceiver==>" + intent.getAction());
        boolean result = NotificationMsgUtil.isServiceExisted(context, NPNotificationService.class);
        if (!result){
            ycBleLog.e("通知没有打开");
        }
    }

    /**
     * 创建系统常用的广播接收器
     *
     * @return
     */
    public static IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        //时间变化
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        //亮屏
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        //黑屏
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        return intentFilter;
    }

}
