package ycble.runchinaup.device;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.util.BleUtil;

/**
 * 蓝牙对象，所有扫描到的设备都会被封装成这个对象，
 * 可以拿到该设备广播里面的所有数据
 * 数据取决于设备定义的广播数据
 */
public class BleDevice implements Serializable {


    //设备自定义数据
    public static final String adv_manufacturer_data = "FF";
    //十六位的serviceUUID
    public static final String adv_serviceUUID_16bit = "03";
    //设备的简写名称
    public static final String adv_name_short = "08";
    //设备的完整名称
    public static final String adv_name_length = "09";


    public BleDevice(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }

    public BleDevice(String name, String mac, byte[] scanBytes) {
        this.name = name;
        this.mac = mac;
        this.scanBytes = scanBytes;
    }

    //设备的mac地址
    private String mac;
    //设备的名称
    private String name = "null";
    //设备的rssi
    private int rssi;
    //设备的广播数据
    private byte[] scanBytes;

    private String scanHexStr;


    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? "null" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public byte[] getScanBytes() {
        return scanBytes;
    }

    public void setScanBytes(byte[] scanBytes) {
        this.scanBytes = scanBytes;
    }

    //解析蓝牙广播数据
    public synchronized static BleDevice parserFromScanData(BluetoothDevice device, byte[] scanData, int rssi) {
        BleDevice result = new BleDevice(device.getName(), device.getAddress(), scanData);
        result.setRssi(rssi);
        ycBleLog.e(result.toString());
        return result;
    }


    private String getAdvName() {
        HashMap<String, String> getAdvData = getAdvData();
        if (getAdvData == null || !getAdvData.containsKey(adv_name_length) || TextUtils.isEmpty(getAdvData.get(adv_name_length))) {
            return "null";
        } else {
            String nameHexStr = getAdvData.get(adv_name_length);
            String advName = new String(BleUtil.hexStr2Byte(nameHexStr));
            return advName;
        }
    }

    private HashMap<String, String> getAdvData() {
        scanHexStr = scanHexStr.replace(" ", "");
        // advStr = tmp;
        // 先判断长度,如果长度小于1，或者大于124（62个字节）
        if (TextUtils.isEmpty(scanHexStr) || scanHexStr.startsWith("00")) {
            return null;
        }
        if (scanHexStr.length() < 1 || scanHexStr.length() > 124) {
            return null;
        }
        ycBleLog.w(mac + "==>原始广播数据:" + scanHexStr);
        int totalLen = scanHexStr.length();
        HashMap<String, String> advData = new HashMap<>();
        String str = scanHexStr;
        // 索引，这里能知道这段数据的长度
        int tmpIndex = 0;
        // 取出来第一包数据的长度
        int dataLenTmp = Integer.valueOf(str.substring(tmpIndex, tmpIndex + 2), 16);
        if (dataLenTmp <= 0 || dataLenTmp >= 30) {
            return null;
        }
        String subStr = scanHexStr.substring(tmpIndex + 2);
        //数据长度对不上了
        if (subStr.length() < dataLenTmp * 2) {
            return null;
        }
        int dataTypeTmp = Integer.valueOf(str.substring(tmpIndex + 2, tmpIndex + 4), 16);
        String data = str.substring(tmpIndex + 4, tmpIndex + 4 + dataLenTmp * 2 - 2);
        advData.put(String.format("%02X", dataTypeTmp), data);
        while (tmpIndex < totalLen) {
            dataLenTmp = Integer.valueOf(str.substring(tmpIndex, tmpIndex + 2), 16);
            //数据长度有误
            if (dataLenTmp == 0 || dataLenTmp == 0 || dataLenTmp > 60) {
                break;
            }
            subStr = scanHexStr.substring(tmpIndex + 2);
            //数据长度对不上了
            if (subStr.length() < dataLenTmp * 2) {
                break;
            }
            dataTypeTmp = Integer.valueOf(str.substring(tmpIndex + 2, tmpIndex + 4), 16);
            data = str.substring(tmpIndex + 4, tmpIndex + 4 + dataLenTmp * 2 - 2);
            advData.put(String.format("%02X", dataTypeTmp), data);
            tmpIndex += dataLenTmp * 2 + 2;
        }
        return advData;
    }


    //创建一个调试设备，用于调试
    public static BleDevice createADebugDeice(String mac) {
        return new BleDevice("debug", mac);
    }

    @Override
    public String toString() {
        return "BleDevice{" +
                "mac='" + mac + '\'' +
                ", name='" + name + '\'' +
                ", rssi=" + rssi +
                ", scanBytes=" + Arrays.toString(scanBytes) +
                '}';
    }
}
