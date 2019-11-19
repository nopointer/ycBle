package ycble.runchinaup.ota.absimpl.ti;

import java.util.UUID;

interface TIBleCfg {

    /**
     * OAD数据交互的服务
     */
    final static UUID UUID_OTA_SERVICE = UUID.fromString("f000ffc0-0451-4000-b000-000000000000");

    /**
     * 发送数据的特征通道
     */
    final static UUID UUID_OTA_SEND_DATA = UUID.fromString("f000ffc2-0451-4000-b000-000000000000");
    /**
     * 接收数据的特征通道
     */
    final static UUID UUID_OTA_RECV_DATA = UUID.fromString("f000ffc1-0451-4000-b000-000000000000");

}
