package ycble.runchinaup.core;

import java.util.ArrayList;
import java.util.List;

import ycble.runchinaup.util.PhoneDeviceUtil;

/**
 * Created by nopointer on 2018/7/30.
 * 手机ble的蓝牙状态异常收集
 */

public class PhoneBleExceptionCode {


    public static String getPhoneCode(int code) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("手机厂商:" + PhoneDeviceUtil.getDeviceBrand()).append("   ");
        stringBuilder.append("手机型号:" + PhoneDeviceUtil.getSystemModel()).append("   ");
        stringBuilder.append("安卓版本:" + PhoneDeviceUtil.getSystemVersion());
        return stringBuilder.toString();
    }


    private PhoneBleExceptionCode() {
    }

    //目前收集到的就只有魅族手机
    private static List<String> phoneBleExceptionCodeArrList = new ArrayList<>();

    static {
        phoneBleExceptionCodeArrList.add("Meizu_A_MX4 Pro_A_5.1.1_A_257");
    }

    //是否是手机的蓝牙挂壁了
    public static boolean isPhoneBleExcepiton(int code) {
        return phoneBleExceptionCodeArrList.contains(getPhoneCode(code));
    }


}
