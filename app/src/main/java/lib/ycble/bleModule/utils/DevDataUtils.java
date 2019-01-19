package lib.ycble.bleModule.utils;

public class DevDataUtils {
    private DevDataUtils() {
    }


    /**
     * 控制心率测试
     *
     * @param isMeasure true 开启 false停止
     * @return
     */
    public static byte[] controlMeasureHr(boolean isMeasure) {
        byte[] data = new byte[7];
        int index = 3;
        data[index++] = (byte) 0xFF;
        data[index++] = (byte) 0x02;
        data[index++] = 0x09;
        data[index++] = (byte) (isMeasure ? 1 : 0);

        setDateHeaderAndLen(data);
        return data;
    }

    /**
     * 设备ui指令
     *
     * @param commandType 1开始更新，请求设备端接收数据，0数据下发完成 请求设备端执行更新ui
     * @return
     */
    public static byte[] controlDevUI(int commandType) {
        byte[] data = new byte[6];
        int index = 3;
        data[index++] = (byte) 0xFF;
        data[index++] = (byte) 0x33;
        data[index++] = (byte) commandType;
        setDateHeaderAndLen(data);
        return data;
    }

    public static byte[] setDevUICfg() {
        byte[] data = new byte[10];
        int index = 3;
        data[index++] = (byte) 0xFF;
        data[index++] = (byte) 0x32;
        data[index++] = (byte) 0x01;
        data[index++] = (byte) 0x02;
        data[index++] = (byte) 0x00;
        data[index++] = (byte) 0x01;
        data[index++] = (byte) 0x03;
        setDateHeaderAndLen(data);
        return data;
    }

    /**
     * 选择表盘
     *
     * @param number
     * @return
     */
    public static byte[] choiceDevUIType(int number) {
        byte[] data = new byte[6];
        int index = 3;
        data[index++] = (byte) 0xFF;
        data[index++] = (byte) 0x29;
        data[index++] = (byte) number;
        setDateHeaderAndLen(data);
        return data;
    }


    /**
     * 封装头部数据 和数据长度
     *
     * @param data
     */
    private static void setDateHeaderAndLen(byte[] data) {
        data[0] = (byte) 0xAB;
        int len = data.length - 3;
        data[1] = (byte) ((len & 0xff00) >> 8);
        data[2] = (byte) (len & 0xff);
    }


}
