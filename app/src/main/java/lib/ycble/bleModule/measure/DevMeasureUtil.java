package lib.ycble.bleModule.measure;

import lib.ycble.bleModule.BleCfg;
import ycble.runchinaup.util.BleUtil;

/**
 * 检测数据的解析对象
 */
public class DevMeasureUtil implements BleCfg {


    private DevMeasureUtil() {
    }

    public static void receiveData(byte data[]) {
        int flag = BleUtil.byte2IntLR(data[3], data[4]);

        if (flag == MEASURE_DATA_FLAG) {
            //心率数据 包含app端测试的 也包含三设备端测试的
        }
    }


}
