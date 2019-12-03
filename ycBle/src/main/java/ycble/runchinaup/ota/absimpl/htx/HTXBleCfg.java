package ycble.runchinaup.ota.absimpl.htx;

import java.util.UUID;

interface HTXBleCfg {

    /**
     *
     */
    final static UUID UUID_OTA_SERVICE = UUID.fromString("00001234-0000-1000-8000-00805f9b34fb");

    /**
     *
     */
    final static UUID UUID_OTA_TX_CMD = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");

    /**
     *
     */
    final static UUID UUID_OTA_TX_DATA = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");

    /**
     * 接收指令的通知
     */
    final static UUID UUID_OTA_RX_CMD = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");


    /**
     * 接收数据的通知
     */
    final static UUID UUID_OTA_RX_DATA = UUID.fromString("0000ff04-0000-1000-8000-00805f9b34fb");


}
