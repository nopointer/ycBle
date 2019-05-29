package ycble.runchinaup.aider.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import ycble.runchinaup.aider.MsgNotifyHelper;
import ycble.runchinaup.log.ycBleLog;

import static android.telephony.TelephonyManager.EXTRA_STATE_IDLE;
import static android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.EXTRA_STATE_RINGING;


/**
 * Created by nopointer on 2017/5/9.
 * 手机来电状态广播接收器
 */
public final class NPPhoneStateReceiver extends BroadcastReceiver {

    /**
     * 手机状态
     */
    private static final String PHONE_STATE = "android.intent.action.PHONE_STATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equalsIgnoreCase(PHONE_STATE)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String extraIncomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            onCallStateChanged(context,state, extraIncomingNumber);
        } else if (action.equalsIgnoreCase(Intent.ACTION_NEW_OUTGOING_CALL)) {
            ycBleLog.e("NPPhoneStateListener==>拨打电话出去");
        }
    }

    public static IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        return intentFilter;
    }


    //来电状态监听
    public void onCallStateChanged(Context context,String state, String incomingNumber) {
        ycBleLog.e("state:" + state + ";incomingNumber:" + incomingNumber);
        if (TextUtils.isEmpty(incomingNumber)) return;
        String name = NPContactsUtil.queryContact(context, incomingNumber);

        if (state.equalsIgnoreCase(EXTRA_STATE_RINGING)) {
            ycBleLog.e("NPPhoneStateListener==>手机铃声响了，来电人:" + name);
            MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 0);
        } else if (state.equalsIgnoreCase(EXTRA_STATE_IDLE)) {
            ycBleLog.e("NPPhoneStateListener==>非通话状态" + name);
            MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 2);
        } else if (state.equalsIgnoreCase(EXTRA_STATE_OFFHOOK)) {
            ycBleLog.e("NPPhoneStateListener==>电话被接通了,可能是打出去的，也可能是接听的" + incomingNumber);
            MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 1);
        }
    }
}


