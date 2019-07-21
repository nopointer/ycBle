package ycble.runchinaup.aider;

import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import ycble.runchinaup.log.ycBleLog;


/**
 * Created by nopointer on 2018/7/26.
 * 通知栏消息获取
 */

public final class NPNotificationService extends NotificationListenerService {

    /**
     * 通知栏监听是否能收到通知的标志，有的手机收不到，开了辅助帮助后就可以收到，
     * 就怕能收到通知同时又开了辅助的情况，所以加了这个标志位，能收到通知 辅助里面就不处理了
     */
    public static boolean NPNotificationServiceCanReceive = false;

    /**
     * 最后一次数据，为了避免部分手机推送消息的时候，通知栏会收到2次回调
     */
    private static String lastMsgStr = null;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundFuckAndroidP();
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        ycBleLog.e("通知栏服务正常，可以获取到通知信息");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ycBleLog.e("通知栏onStartCommand");
        startForegroundFuckAndroidP();
        return super.onStartCommand(intent, flags, startId);
    }

    //接收到通知消息的回调
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        NPNotificationServiceCanReceive = true;
        if (sbn == null) return;
        //应用包名
        String pckName = sbn.getPackageName();
        if (TextUtils.isEmpty(pckName)) {
            return;
        }
        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        //消息发送方,QQ 来电或者语音是没有发送方的，为空
        String from = "";

        String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
        if (!TextUtils.isEmpty(notificationTitle)) {
            from = notificationTitle;
        }

        //消息内容
        String msgStr;
        try {
            msgStr = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
        } catch (NullPointerException e) {
            msgStr = "";
        }

        ycBleLog.e("通知栏获取到消息==>{" + msgStr + "}===>pckName:" + pckName);

        handMsg(pckName, from, msgStr);
    }

    //通知消息被移除的回调
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

//    @Override
//    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
//        super.onNotificationPosted(sbn, rankingMap);
//        ycBleLog.e("onNotificationPosted(StatusBarNotification sbn,RankingMap rankingMap)");
//    }

    //处理消息，判断消息类型和来源
    public void handMsg(String pkhName, String from, String msgContent) {
        String tmpStr = pkhName + "/from:" + from + "/msgContent:" + msgContent;
        MsgType msgType = MsgType.pck2MsgType(pkhName);
        ycBleLog.e(msgType + "/" + tmpStr);
//        if (TextUtils.isEmpty(lastMsgStr) || !tmpStr.equals(lastMsgStr)) {
        MsgNotifyHelper.getMsgNotifyHelper().onAppMsgReceiver(pkhName, msgType, from, msgContent);
        lastMsgStr = tmpStr;
//        }
    }

    /**
     * 清空上一次的消息内容
     */
    @Deprecated
    public static void clearLastMessage() {
        if (lastMsgStr != null && !TextUtils.isEmpty(lastMsgStr)) {
            lastMsgStr = null;
        }
    }

    /**
     * fuck的 google 9.0各种坑逼问题
     */
    private void startForegroundFuckAndroidP() {
        NotificationMsgUtil.reBindService(this);
    }


}
