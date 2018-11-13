package ycble.runchinaup.aider.phone;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import ycble.runchinaup.log.ycBleLog;


/**
 * Created by nopointer on 2017/5/9.
 * 手机来电状态广播接收器
 */
public final class NPPhoneStateReceiver extends BroadcastReceiver {

    private static NPPhoneStateListener npPhoneStateListener = new NPPhoneStateListener();
    private static final String PHONE_STATE = "android.intent.action.PHONE_STATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equalsIgnoreCase(PHONE_STATE)) {
            //如果是来电状态，把状态交给监听器处理
            ycBleLog.e("NPPhoneStateListener==>电话状态有改变");
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(npPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        } else if (action.equalsIgnoreCase(Intent.ACTION_NEW_OUTGOING_CALL)) {
            ycBleLog.e("NPPhoneStateListener==>拨打电话出去");
        }
    }

    public static IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        intentFilter.setPriority(1000);
        return intentFilter;
    }

}


