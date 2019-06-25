package ycble.runchinaup.aider.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import ycble.runchinaup.aider.MsgNotifyHelper;
import ycble.runchinaup.aider.phone.NPContactsUtil;
import ycble.runchinaup.log.ycBleLog;


/**
 * 短信接收广播
 * 对于国内的贱商，真的是fuck啊，普通短信可以收到没问题，问题是一个通知类的短信来了，就他娘的拦截了，真恶心
 * 这个广播，还有可能会回调2次
 * <p>
 * 静态注册
 * <receiver
 * android:name=".aider.sms.NPSmsReciver"
 * android:enabled="true"
 * android:exported="true"
 * android:permission="android.permission.BROADCAST_SMS">
 * <intent-filter android:priority="1000">
 * <action android:name="android.provider.Telephony.SMS_DELIVER" />
 * <action android:name="android.provider.Telephony.SMS_RECEIVED" />
 * </intent-filter>
 * </receiver>
 * </p>
 */

public class NPSmsReciver extends BroadcastReceiver {


    private static String strLastContent = null;
    //短信没有内容的时候，应该就是没有权限了，这里提示没有相关的权限
    public static String messageWithNoPermissionText = "请授予app读取短信权限,否则无法显示短信内容";

    /**
     * 操蛋的方法 每个地方最好都判断一次吧 金立的烂手机就会空指针异常
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        ycBleLog.e("debug sms" + "短信来了===>");
        Bundle bundle = intent.getExtras();
        SmsMessage msg = null;
        String number = null;
        StringBuilder messageContentBuilder = new StringBuilder();
        if (null != bundle) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            if (null != smsObj) {
                for (Object object : smsObj) {
                    msg = SmsMessage.createFromPdu((byte[]) object);
                    if (null != msg) {
                        number = msg.getOriginatingAddress();
                        String messageContent = msg.getDisplayMessageBody();
                        if (null == number || null == messageContent || TextUtils.isEmpty(number) || TextUtils.isEmpty(messageContent)) {
                            return;
                        }
                        messageContentBuilder.append(messageContent);
                    }
                }
                if (TextUtils.isEmpty(messageContentBuilder.toString())) {
                    messageContentBuilder.append(messageWithNoPermissionText);
                }

                if (TextUtils.isEmpty(strLastContent) || !strLastContent.equals(messageContentBuilder.toString())) {
                    MsgNotifyHelper.getMsgNotifyHelper().onMessageReceive(number, NPContactsUtil.queryContact(context, number), messageContentBuilder.toString());
                    strLastContent = messageContentBuilder.toString();
                }
            }
        }
    }


    public static IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.setPriority(Integer.MAX_VALUE);
        return intentFilter;
    }


    /**
     * 设置没有短信相关权限的提示语，这个会推送到设备上（如果没有相关权限的话）
     *
     * @param messageWithNoPermissionText
     */
    public static void setMessageWithNoPermissionText(String messageWithNoPermissionText) {
        NPSmsReciver.messageWithNoPermissionText = messageWithNoPermissionText;
    }

    /**
     * 清空上一次的短信内容
     */
    public static void clearLastMessage() {
        if (strLastContent != null && !TextUtils.isEmpty(strLastContent)) {
            strLastContent = null;
        }
    }


}
