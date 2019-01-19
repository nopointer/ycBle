package lib.ycble.bleModule;

import java.util.UUID;

/**
 * 静态变量
 */
public interface BleCfg {


    /**
     * 主服务service
     */
    UUID dataServiceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

    /**
     * 数据写特征
     */
    UUID dataWriteUUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");


    /**
     * 数据通知特征
     */
    UUID dataNotifyUUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");


    /**
     * 表盘图片写数据特征
     */
    UUID imageDataWriteUUID = UUID.fromString("6e400005-b5a3-f393-e0a9-e50e24dcca9e");

    /**
     * 表盘图片设备端端通知数据特征
     */
    UUID imageDataNotifyUUID = UUID.fromString("6e400006-b5a3-f393-e0a9-e50e24dcca9e");

    /**
     * 测试数据，包括心率 血压 血氧 App端测试的 或者设备端测试的
     */
    public static final int MEASURE_DATA_FLAG = 0XFF02;

}
