package lib.ycble.backgroundLive.extra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.HashSet;

/**
 * 后台存活时间久一点的监听广播
 */
public class AliveBroadcastReceiver extends BroadcastReceiver {
    private static final AliveBroadcastReceiver ourInstance = new AliveBroadcastReceiver();

    public static AliveBroadcastReceiver getInstance() {
        return ourInstance;
    }

    IntentFilter intentFilter = new IntentFilter();


    private HashSet<OnBroadcastReceiveCallback> callbackHashSet = new HashSet<>();


    private AliveBroadcastReceiver() {
        //监听时间变化广播
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        //监听锁屏广播
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        //监听开锁广播
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        for (OnBroadcastReceiveCallback callback : callbackHashSet) {
            callback.onBroadcastReceive(intent.getAction());
        }
    }


    public void register(Context context, OnBroadcastReceiveCallback callback) {
        try {
            if (!callbackHashSet.contains(callback)) {
                callbackHashSet.add(callback);
            }
            context.registerReceiver(this, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unRegister(Context context,OnBroadcastReceiveCallback callback) {
        try {
            context.unregisterReceiver(this);
            if (callbackHashSet.contains(callback)) {
                callbackHashSet.remove(callback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnBroadcastReceiveCallback {

        void onBroadcastReceive(String action);
    }


}
