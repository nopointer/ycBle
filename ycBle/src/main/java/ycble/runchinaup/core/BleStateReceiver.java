package ycble.runchinaup.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import ycble.runchinaup.log.ycBleLog;

import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED;
import static android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED;
import static ycble.runchinaup.core.BleStateReceiver.SystemBluetoothState.StateClosingBle;
import static ycble.runchinaup.core.BleStateReceiver.SystemBluetoothState.StateConn;
import static ycble.runchinaup.core.BleStateReceiver.SystemBluetoothState.StateOffBle;
import static ycble.runchinaup.core.BleStateReceiver.SystemBluetoothState.StateOnBle;
import static ycble.runchinaup.core.BleStateReceiver.SystemBluetoothState.StateOpeningBle;


/**
 * Created by nopointer on 2017/12/3.
 * 监听蓝牙的状态，打开，关闭 做出相应的操作
 */

public class BleStateReceiver extends BroadcastReceiver {

    /**
     * 这个广播当然也是不所有的设备都要去监听的，只监听指定的设备
     */
    private String listenerMac = null;

    public void setListenerMac(String listenerMac) {
        ycBleLog.e("监听此设备相关的蓝牙广播==>" + listenerMac);
        this.listenerMac = listenerMac;
    }

    private static BleStateReceiver instance = new BleStateReceiver();

    private BleStateReceiver() {
    }

    public static BleStateReceiver getInstance() {
        return instance;
    }

    /**
     * 部分手机断开后不会不会走回调广播,所以这里才会tm的出现这个标志位，
     * 在gatt回调里面拿到断开的回调后，如果走广播的话为true,不走广播的话，为false,然后延时
     * 1200毫秒后再强行认为是断开了的
     */
    private boolean hasCover = false;

    public boolean isHasCover() {
        return hasCover;
    }

    public void setHasCover(boolean hasCover) {
        this.hasCover = hasCover;
    }

    //创建一个蓝牙状态的过滤器
    private static IntentFilter createSateFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        return intentFilter;
    }

    public void startListen(Context context) {
        context.registerReceiver(this, createSateFilter());
    }

    public BleStateListener bleStateListener;

    public void setBleStateListener(BleStateListener bleStateListener) {
        this.bleStateListener = bleStateListener;
    }

    public static interface BleStateListener {
        void onBleState(SystemBluetoothState systemBluetoothState, BluetoothDevice bluetoothDevice);
    }


    @Override
    public void onReceive(final Context context, Intent intent) {


        String action = intent.getAction();

        ycBleLog.e("BleStateReceiver 广播的action:===>" + action);

        if (action.equals(ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    ycBleLog.e("蓝牙正在打开......");
                    onState(StateOpeningBle, null);
                    break;

                case BluetoothAdapter.STATE_ON:
                    ycBleLog.e("手机蓝牙开启状态");
                    onState(StateOnBle, null);
                    break;

                case BluetoothAdapter.STATE_TURNING_OFF:
                    ycBleLog.e("蓝牙正在关闭......");
                    onState(StateClosingBle, null);
                    break;

                case BluetoothAdapter.STATE_OFF:
                    ycBleLog.e("手机蓝牙关闭状态");
                    onState(StateOffBle, null);
                    break;
            }
        } else {

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null) {
                ycBleLog.e("设备为空，不需要往后执行......");
                return;
            }

            ycBleLog.e("相关的设备===>" + device.getName() + "/" + device.getAddress());

            if (TextUtils.isEmpty(listenerMac) || !device.getAddress().equalsIgnoreCase(listenerMac)) {
                ycBleLog.e("监听设备为空或者不属于本项目的设备，不需要往后执行......");
                return;
            }
            switch (action) {
                case ACTION_ACL_CONNECTED: //有设备连接上了
                {
                    onState(StateConn, device);
                }
                break;
                case ACTION_ACL_DISCONNECTED: //有设备断开了
                {
//                    hasCover = true;
//                    onState(StateDisConn, device);
                }
                break;
            }
        }
    }


    public void onState(SystemBluetoothState systemBluetoothState, BluetoothDevice bluetoothDevice) {
        if (bleStateListener != null) {
            bleStateListener.onBleState(systemBluetoothState, bluetoothDevice);
        }
    }

    public enum SystemBluetoothState {
        /**
         * 连接上了
         */
        StateConn,
        /**
         * 断开了
         */
        StateDisConn,
        /**
         * 手机系统蓝牙打开
         */
        StateOnBle,
        /**
         * 手机系统蓝牙关闭
         */
        StateOffBle,
        /**
         * 正在打开手机系统蓝牙
         */
        StateOpeningBle,
        /**
         * 正在关闭手机系统蓝牙
         */
        StateClosingBle;
    }

}
