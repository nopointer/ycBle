package ycble.runchinaup.ota;

public class OTAErrCode {

    /**
     * 连接异常断开了
     */
    public static final int LOST_CONN =1;

    public static final int UUID_ERROR =2;



    /**
     * 连接中断了 nrf
     */
    public static final int NRF_ABORTED=11;
    /**
     * 泰凌微失败，原因未知
     */
    public static final int TELINK_ERROR = 30;


    /**
     * 富萵坤 文件校验不通过
     */
    public static final int FWK_FILE_INVALIDE =40;


    public static final int TI_FAILURE =50;



    public static final int FAILURE =-1;
}
