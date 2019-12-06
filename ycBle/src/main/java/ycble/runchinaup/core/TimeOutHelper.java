package ycble.runchinaup.core;

import java.io.Serializable;

//超时助手
final class TimeOutHelper implements Serializable {

    //发送的数据
    private String strData;
    //延时多少毫秒后判断，是否是超时
    private int milliSecond;
    //重发指令次数
    public int reSendCount = 0;

    public TimeOutHelper(String strData, int milliSecond) {
        this.strData = strData;
        this.milliSecond = milliSecond;
    }

    public String getStrData() {
        return strData;
    }

    public int getMilliSecond() {
        return milliSecond;
    }

}