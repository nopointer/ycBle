package ycble.runchinaup.ota.absimpl.freqchip;

import java.util.UUID;

interface FreqBleCfg {

    /**
     * OTA数据交互的服务
     */
    final static UUID UUID_OTA_SERVICE = UUID.fromString("0000fe00-0000-1000-8000-00805f9b34fb");

    /**
     * 发送数据的特征通道
     */
    final static UUID UUID_OTA_SEND_DATA = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    /**
     * 接收数据的特征通道
     */
    final static UUID UUID_OTA_RECV_DATA = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");

}
