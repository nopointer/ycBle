package lib.ycble.backgroundLive.extra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * 时间变化的监听器
 */
public class TimeChangeReceiver extends BroadcastReceiver {
    private static final TimeChangeReceiver ourInstance = new TimeChangeReceiver();

    public static TimeChangeReceiver getInstance() {
        return ourInstance;
    }

    IntentFilter intentFilter = new IntentFilter();

    private OnTimeChangeCallback onTimeChangeCallback;

    private TimeChangeReceiver() {
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
            if (onTimeChangeCallback != null) {
                onTimeChangeCallback.onTimeChange();
            }
        }
    }


    public void register(Context context, OnTimeChangeCallback onTimeChangeCallback) {
        try {
            this.onTimeChangeCallback = onTimeChangeCallback;
            context.registerReceiver(this, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unRegister(Context context) {
        try {
            context.unregisterReceiver(this);
            if (onTimeChangeCallback != null) {
                onTimeChangeCallback = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnTimeChangeCallback {

        void onTimeChange();
    }


}
