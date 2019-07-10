package ycble.runchinaup.aider;

import android.content.Context;

import ycble.runchinaup.aider.callback.MsgCallback;
import ycble.runchinaup.aider.phone.NPPhoneStateReceiver;
import ycble.runchinaup.aider.sms.NPSmsReciver;
import ycble.runchinaup.log.ycBleLog;

/**
 * Created by nopointer on 2018/7/26.
 * 推送辅助工具助手，在里面可以定义是否需要启用通知栏监听,来电监听，以及短信监听
 */

public final class PushAiderHelper {

    private MsgNotifyHelper notifyHelper = MsgNotifyHelper.getMsgNotifyHelper();



    public void start(Context context) {
        try {
            //常用的系统广播
            context.registerReceiver(npLiveReceiver, ReStartNotificationReceiver.createIntentFilter());
            NotificationMsgUtil.reBindService(context);
            registerReceiver(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(Context context) {
        try {
            context.unregisterReceiver(npLiveReceiver);
            NotificationMsgUtil.closeService(context);
            unregisterReceiver(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PushAiderHelper() {
        ycBleLog.e("new PushAiderHelper()");
    }


    private static PushAiderHelper aiderHelper = new PushAiderHelper();

    public static PushAiderHelper getAiderHelper() {
        return aiderHelper;
    }

    //是否有通知栏监听消息的权限
    public boolean isNotifyEnable(Context context) {
        return NotificationMsgUtil.isEnabled(context);
    }

    //前往设置通知权限授权界面
    public void goToSettingNotificationAccess(Context context) {
        NotificationMsgUtil.goToSettingNotificationAccess(context);
    }

    //前往打开辅助功能
    public void goToSettingAccessibility(Context context) {
        NotificationMsgUtil.goToSettingAccessibility(context);
    }

    /**
     * 设置消息回调通知
     *
     * @param msgReceiveCakkback
     */
    public void setMsgReceiveCallback(MsgCallback msgReceiveCakkback) {
        notifyHelper.setMsgCallback(msgReceiveCakkback);
    }

    //接收一些常用的广播接收器，用来激活通知栏，不让他挂掉
    private ReStartNotificationReceiver npLiveReceiver = new ReStartNotificationReceiver();
    //来电状态广播接收器
    private NPPhoneStateReceiver npPhoneStateReceiver = new NPPhoneStateReceiver();
    //短信接收广播接收器
    private NPSmsReciver npSmsReciver = new NPSmsReciver();

    //注册广播
    private void registerReceiver(Context context) {
        //注册来电状态的广播
        context.registerReceiver(npPhoneStateReceiver, NPPhoneStateReceiver.createIntentFilter());
        //注册短信接收的广播
        context.registerReceiver(npSmsReciver, NPSmsReciver.createIntentFilter());
    }


    private void unregisterReceiver(Context context) {
        try {
            context.unregisterReceiver(npPhoneStateReceiver);
            context.unregisterReceiver(npSmsReciver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}