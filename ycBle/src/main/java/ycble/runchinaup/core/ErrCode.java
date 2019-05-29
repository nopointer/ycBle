package ycble.runchinaup.core;

public enum ErrCode {

    //没有检测到服务
    ERR_DISCOVER_SERVICE,
    //写特征异常
    ERR_WRITE_CHARA,
    //写描述符异常
    ERR_WRITE_DESCR,
    //读取特征异常
    ERR_READ_CHARA,
    //读取描述符异常
    ERR_READ_DESCR,
    //获取RSSI异常
    ERR_RSSI,
    //检测服务异常
    ERR_DISCOVER;

}
