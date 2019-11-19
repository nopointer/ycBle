package ycble.runchinaup.ota;

/**
 * 设备类别
 */
public enum FirmType {
    /**
     * DA
     */
    DIALOG("dialog"),
    NORDIC("nordic"),
    /**
     * TI的oad 貌似有2种，目前待验证，默认的就用cc254x系列
     */
    TI("ti"),
    SYD("盛源达"),
    TELINK("泰凌微"),
    HTX("汉天下"),
    FREQCHIP("富芮坤");

    private FirmType(String name) {

    }


}
