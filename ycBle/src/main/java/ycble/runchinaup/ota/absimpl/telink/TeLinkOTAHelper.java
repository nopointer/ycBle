package ycble.runchinaup.ota.absimpl.telink;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ycble.runchinaup.device.BleDevice;
import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.ota.callback.OTACallback;
import ycble.runchinaup.util.BleUtil;

public class TeLinkOTAHelper {
    private static final TeLinkOTAHelper ourInstance = new TeLinkOTAHelper();

    public static TeLinkOTAHelper getInstance() {
        return ourInstance;
    }

    private TeLinkOTAHelper() {
    }


    private long startTime = 0;
    private long endTime = 0;


    public void startOTA(Context context, String mac, final String filePath, final OTACallback otaCallback) {
        BleDevice bleDevice = new BleDevice("", mac);
        Device device = new Device(bleDevice);
        device.setCallback(new Device.Callback() {
            @Override
            public void onConnected(final Device device) {

            }

            @Override
            public void onDisconnected(final Device device) {

            }

            @Override
            public void onServicesDiscovered(final Device device) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] firmwareFile = readFirmware(filePath);
                        startTime = System.currentTimeMillis();
                        device.startOta(firmwareFile);
                    }
                }).start();
            }

            @Override
            public void onOtaStateChanged(Device device, int state) {
                switch (state) {
                    case Device.STATE_PROGRESS:
                        if (otaCallback != null) {
                            otaCallback.onProgress(device.getOtaProgress());
                        }
                        break;
                    case Device.STATE_SUCCESS:
                        ycBleLog.e("成功");
                        endTime = System.currentTimeMillis();
                        ycBleLog.e("time:" + (endTime - startTime) / 1000L / 60.0f);
                        if (otaCallback != null) {
                            otaCallback.onSuccess();
                        }
                        break;
                    case Device.STATE_FAILURE:
                        if (otaCallback != null) {
                            otaCallback.onFailure("failure");
                        }
                        break;
                }
            }
        });
        device.connect(context);
    }

    private byte[] readFirmware(String fileName) {
        try {
            InputStream stream = new FileInputStream(fileName);
            Log.e("debug==文件路径=>", fileName.toString());
            int length = stream.available();
            byte[] firmware = new byte[length];
            stream.read(firmware);
            Log.e("debug==文件", BleUtil.byte2HexStr(firmware));
            stream.close();
            return firmware;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
