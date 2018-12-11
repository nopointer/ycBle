package ycble.runchinaup.aider;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

import ycble.runchinaup.log.ycBleLog;

import static ycble.runchinaup.aider.NPNotificationService.NPNotificationServiceCanReceive;

/**
 * Created by wangmingxing on 17-12-25.
 */

public class NPAccessibilityService extends AccessibilityService {

    public static boolean NPAccessibilityServiceCanReceive = false;


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, new Notification()); //这个id不要和应用内的其他同志id一样，不行就写 int.maxValue()        //context.startForeground(SERVICE_ID, builder.getNotification());
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, new Notification()); //这个id不要和应用内的其他同志id一样，不行就写 int.maxValue()        //context.startForeground(SERVICE_ID, builder.getNotification());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onServiceConnected() {
        ycBleLog.e("辅助通知栏服务正常，可以获取到通知信息");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.notificationTimeout = 100;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int type = accessibilityEvent.getEventType();
        if (type != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            return;
        }
        if (accessibilityEvent.getPackageName().equals(getPackageName())) {
            return;
        }
        if (NPNotificationServiceCanReceive) {
            ycBleLog.e("通知栏可以获取到消息，就不用辅助推送了");
            return;
        }

        NPAccessibilityServiceCanReceive = true;
        String pkg = accessibilityEvent.getPackageName().toString();
        Notification notification = (Notification) accessibilityEvent.getParcelableData();
        if (notification == null) {
            ycBleLog.e("通知栏内容为空，不推送消息");
            return;
        }
        String from = "";
        String content = "";
        String tmpString = "";

        if (notification.tickerText != null) {
            tmpString = notification.tickerText.toString();
        } else {
            List<CharSequence> texts = accessibilityEvent.getText();
            if (texts != null) {
                StringBuilder sb = new StringBuilder();
                for (CharSequence text : texts) {
                    sb.append(text);
                }
                tmpString = sb.toString();
            }
        }
        if (!TextUtils.isEmpty(tmpString)) {
            String arr[] = tmpString.split(":");
            if (arr != null && arr.length > 1) {
                from = arr[0];
                content = arr[1];
                handMsg(pkg, from, content);
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    //最后一次数据，为了避免部分手机推送消息的时候，通知栏会收到2次回调
    private String lastMsgStr = null;

    //处理消息，判断消息类型和来源
    public void handMsg(String pkhName, String from, String msgContent) {
        String tmpStr = pkhName + from + msgContent;
        boolean needPush = false;
        MsgType msgType = MsgType.pck2MsgType(pkhName);
        if (TextUtils.isEmpty(lastMsgStr)) {
            needPush = true;
            lastMsgStr = tmpStr;
        } else {
            if (!lastMsgStr.equals(tmpStr)) {
                needPush = true;
                lastMsgStr = tmpStr;
            }
        }
        if (msgType != null) {
            MsgNotifyHelper.getMsgNotifyHelper().onAppMsgReceiver(pkhName, msgType, from, msgContent);
        }
    }
}
