package ycble.runchinaup.aider;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

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

    private Handler handler = new Handler();

    /**
     * 开始监听通知栏消息
     *
     * @param context
     */
    public void startListeningForNotifications(final Context context) {
        try {

            NotificationMsgUtil.reStartNotifyListenService(context);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (NotificationMsgUtil.isServiceExisted(context, NPNotificationService.class)) {
                        ycBleLog.e("如果3秒后没有启动服务，那么就开启service");
                        Intent intent = new Intent(context, NPNotificationService.class);
                        context.startService(intent);
                        NotificationMsgUtil.reStartNotifyListenService(context);
                    } else {
                        ycBleLog.e("监听服务已经开启");
                    }
                }
            }, 3 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否允许通知监听栏运行久一点
     *
     * @param context
     * @param enable
     */
    public void enablePushAiderAlive(Context context, boolean enable) {
        if (context == null) return;
        //常用的系统广播
        if (enable) {
            context.registerReceiver(npLiveReceiver, ReStartNotificationReceiver.createIntentFilter());
        } else {
            context.unregisterReceiver(npLiveReceiver);
        }
    }


    /**
     * 停止监听通知栏消息
     *
     * @param context
     */
    public void stopListeningForNotifications(Context context) {
        try {
            Intent intent = new Intent(context, NPNotificationService.class);
            context.stopService(intent);
            handler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PushAiderHelper() {
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

    /**
     * 注册来电广播和短信广播
     *
     * @param context
     */
    public void registerCallAndSmsReceiver(Context context) {
        registerCallReceiver(context);
        registerSmsReceiver(context);
    }


    /**
     * 注册来电广播
     *
     * @param context
     */
    public void registerCallReceiver(Context context) {
        //注册来电状态的广播
        context.registerReceiver(npPhoneStateReceiver, NPPhoneStateReceiver.createIntentFilter());
    }

    /**
     * 注册短信广播
     *
     * @param context
     */
    public void registerSmsReceiver(Context context) {
        //注册短信接收的广播
        context.registerReceiver(npSmsReciver, NPSmsReciver.createIntentFilter());
    }

    /**
     * 注销来电广播和短信广播
     *
     * @param context
     */
    public void unRegisterCallAndSmsReceiver(Context context) {
        unRegisterCallReceiver(context);
        unRegisterSmsReceiver(context);
    }


    /**
     * 注销来电广播
     *
     * @param context
     */
    public void unRegisterCallReceiver(Context context) {
        try {
            context.unregisterReceiver(npPhoneStateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注销短信广播
     *
     * @param context
     */
    public void unRegisterSmsReceiver(Context context) {
        try {
            context.unregisterReceiver(npSmsReciver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}