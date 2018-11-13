package ycble.runchinaup.aider.phone;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import ycble.runchinaup.aider.MsgNotifyHelper;
import ycble.runchinaup.log.ycBleLog;

/**
 * Created by nopointer on 2017/4/28.
 * 手机状态监听器
 */

final class NPPhoneStateListener extends PhoneStateListener {

    //来电状态监听
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        if (TextUtils.isEmpty(incomingNumber)) return;
        String name = NPContactsUtil.getContactName(incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                ycBleLog.e("NPPhoneStateListener==>手机铃声响了，来电人:" + name);
                MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 0);
                break;
            case TelephonyManager.CALL_STATE_IDLE://非通话状态
                ycBleLog.e("NPPhoneStateListener==>非通话状态" + name);
                MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 2);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK://接通或者去电状态
                ycBleLog.e("NPPhoneStateListener==>电话被接通了,可能是打出去的，也可能是接听的" + incomingNumber);
                MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 1);
                break;
        }
        super.onCallStateChanged(state, incomingNumber);
    }

}