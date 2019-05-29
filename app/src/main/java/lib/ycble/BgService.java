package lib.ycble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import ycble.runchinaup.log.ycBleLog;

public class BgService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ycBleLog.e("3分钟后,开始扫描");
                conn();
            }
        }, 1000 * 60 * 5);

        return super.onStartCommand(intent, flags, startId);
    }

    private void conn() {
        String mac = "D2:A6:3E:E0:B6:1E";
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
        bluetoothDevice.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                ycBleLog.e("status:" + status + "/newState:" + newState);
            }
        });
    }
}
