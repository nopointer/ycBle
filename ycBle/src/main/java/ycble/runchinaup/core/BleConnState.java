package ycble.runchinaup.core;

/**
 * 蓝牙连接状态
 * 20181112 14:33
 */
public enum BleConnState {

    /**
     * 搜索设备中
     */
    SEARCH_ING,
    /**
     * 请求连接中
     */
    CONNECTING,

    /**
     * 连接失败，包括连接超时，或者连接不上设备
     */
    CONNFAILURE,
    /**
     * 连接上设备，
     */
    CONNECTED,
    /**
     * 手动断开，设备
     */
    HANDDISCONN,

    /**
     * 连接异常，设备端主动断开连接，或者是连接中断，都算是异常
     */
    CONNEXCEPTION,

    /**
     * 手机系统的蓝牙崩溃了,无响应
     */
    PHONEBLEANR;


}