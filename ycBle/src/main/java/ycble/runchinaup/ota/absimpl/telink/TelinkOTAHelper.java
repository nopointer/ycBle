package ycble.runchinaup.ota.absimpl.telink;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ycble.runchinaup.core.AbsBleManager;
import ycble.runchinaup.exception.BleUUIDNullException;
import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.util.BleUtil;

import static ycble.runchinaup.BleCfg.npBleTag;

/**
 * Created by nopointer on 2018/7/26.
 */

public class TelinkOTAHelper extends AbsBleManager {

    private TelinkOTAHelper() {
    }

    private static TelinkOTAHelper telinkOTAHelper = new TelinkOTAHelper();

    public static TelinkOTAHelper getTelinkOTAHelper() {
        return telinkOTAHelper;
    }


    private UUID U_service = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d1912");
    private UUID U_write = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d2b12");

    //固件包
    private List<byte[]> firmwareBinList = new ArrayList<>();
    //ota开始指令
    private static byte[] ota_start_command = new byte[]{0x01, (byte) 0xFF};
    //重启ota指令
    private static byte[] ota_end_command = new byte[]{0x02, (byte) 0xFF};
    //设备校验
    private static byte[] ota_last_data = new byte[6];


    public void startOTA(File file, String mac, Context context) {
        loadFile(file);
        connDevice(mac);
    }


    @Override
    public void loadCfg() {

    }

    @Override
    public void onConnException() {

    }

    @Override
    public void onConnectSuccess() {

    }

    @Override
    public void onHandDisConn() {

    }

    //当前数据索引
    private int otaDataIndex = 0;
    //ota文件长度
    private int otaDataLen = 0;


    @Override
    public void onFinishTaskAfterConn() {
        writeData();
    }

    private void writeData() {

        if (otaDataIndex < firmwareBinList.size()) {
            try {
                writeData(U_service, U_write, firmwareBinList.get(otaDataIndex));
                otaDataIndex++;
            } catch (BleUUIDNullException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDataWrite(byte[] data, boolean isChara, UUID... uuid) {
        super.onDataWrite(data, isChara, uuid);
        ycBleLog.d(npBleTag + BleUtil.byte2HexStr(data));
//        readData();

        writeData();
//        notifyWait();
//        try {
//            if (index < firmwarePckLen) {
//                writeData(U_service, U_write, firmwareBinList.get(index));
//                index++;
//            } else {
//                if (!hadWriteCheckSumCommand) {
//                    int index = this.index - 1;
//
//                    writeData(U_service, U_write, ota_data_check);
//                    hadWriteCheckSumCommand = true;
//                } else {
//                    if (!hasWriteEndCommand) {
//                        writeData(U_service, U_write, reset_sys_command);
//                        hasWriteEndCommand = true;
//                    }
//                }
//            }
//        } catch (BleUUIDNullException e) {
//            e.printStackTrace();
//        }


    }

    @Override
    public void onDataReceive(byte[] data, UUID uuid) {

    }

    private void loadFile(File file) {
        firmwareBinList.clear();
        firmwareBinList.add(ota_start_command);
        int firmwarePckLen = 0;
        try {
            InputStream inputStream = new FileInputStream(file);
            int fileLen = inputStream.available();
            byte[] firmware = new byte[fileLen];
            inputStream.read(firmware);
            firmwarePckLen = fileLen % 16 == 0 ? fileLen / 16 : fileLen / 16 + 1;
            int index = 0;
            for (; index < firmwarePckLen - 1; index++) {
                byte[] buffer = createByteArr(20);
                buffer[0] = (byte) (index & 0xff);
                buffer[1] = (byte) ((index & 0xff00) >> 8);
                System.arraycopy(firmware, index * 16, buffer, 2, 16);
                int crc = crc16(buffer);
                int offset = buffer.length - 2;
                buffer[offset++] = (byte) (crc & 0xFF);
                buffer[offset] = (byte) (crc >> 8 & 0xFF);
                ycBleLog.e(npBleTag + BleUtil.byte2HexStr(buffer));
                firmwareBinList.add(buffer);
            }
            byte[] buffer = createByteArr(20);
            int lastLen = fileLen - 16 * index;
            buffer[0] = (byte) (index & 0xff);
            buffer[1] = (byte) ((index & 0xff00) >> 8);
            System.arraycopy(firmware, index * 16, buffer, 2, lastLen);
            int crc = crc16(buffer);
            int offset = buffer.length - 2;
            buffer[offset++] = (byte) (crc & 0xFF);
            buffer[offset] = (byte) (crc >> 8 & 0xFF);
            ycBleLog.e(npBleTag + BleUtil.byte2HexStr(buffer));

            firmwareBinList.add(buffer);


            int lastIndex = firmwarePckLen - 1;
            ota_last_data[0] = 0x02;
            ota_last_data[1] = (byte) 0xFF;

            ota_last_data[2] = (byte) (lastIndex & 0xFF);
            ota_last_data[3] = (byte) (lastIndex >> 8 & 0xFF);
            ota_last_data[4] = (byte) (0xFF - lastIndex & 0xFF);
            ota_last_data[5] = (byte) (0xFF - (lastIndex >> 8 & 0xFF));

            firmwareBinList.add(ota_last_data);
            firmwareBinList.add(ota_end_command);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] createByteArr(int len) {
        byte[] result = new byte[len];
        for (int i = 0; i < result.length - 2; i++) {
            result[i] = (byte) 0xFF;
        }
        return result;
    }

    public int crc16(byte[] packet) {

        int length = packet.length - 2;
        short[] poly = new short[]{0, (short) 0xA001};
        int crc = 0xFFFF;
        int ds;

        for (int j = 0; j < length; j++) {

            ds = packet[j];

            for (int i = 0; i < 8; i++) {
                crc = (crc >> 1) ^ poly[(crc ^ ds) & 1] & 0xFFFF;
                ds = ds >> 1;
            }
        }
        return crc;
    }

}
