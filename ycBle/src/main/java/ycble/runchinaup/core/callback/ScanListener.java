package ycble.runchinaup.core.callback;

import ycble.runchinaup.device.BleDevice;

/**
 * Created by nopointer on 2018/8/3.
 * 扫描设备的回调
 */

public interface ScanListener<T extends BleDevice> {
    void onScan(T t);
}
