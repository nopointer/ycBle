package ycble.runchinaup.core.callback;

import ycble.runchinaup.core.BleConnState;

/**
 * Ble连接回调接口
 */
public interface BleConnCallback {
    void onConnState(BleConnState bleConnState);
}
