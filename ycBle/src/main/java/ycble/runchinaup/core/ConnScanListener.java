package ycble.runchinaup.core;

import ycble.runchinaup.device.BleDevice;

/**
 * 针对连接而设计的回调
 */
interface ConnScanListener {

    void scanMyDevice(BleDevice bleDevice);

}
