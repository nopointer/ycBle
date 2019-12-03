package ycble.runchinaup.ota.absimpl.xc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.ota.OTAErrCode;
import ycble.runchinaup.ota.callback.OTACallback;

public class XcOTAImpl {

    private final String TAG = XcOTAImpl.class.getSimpleName();

    private MeshOTAManager manager;

    private String filePath = null;
    byte[] otaData;

    private OTACallback otaCallback = null;


    public XcOTAImpl() {

    }


    public void startOTA(Context context, String mac,final String filePath, final OTACallback otaCallback) {
        this.filePath = filePath;
        this.otaCallback = otaCallback;
        try {
            otaData = getBytesFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
        manager = new MeshOTAManager(context, otaData);
        manager.setSignData(null);
        manager.setNeedCrc32(true);
        manager.setNeedCheckSign(false);
        manager.setGattCallbacks(new OTAManagerCallbacks() {
            @Override
            public void onOTAStart() {
//                setStartBtnState(false);
//                buffer+("image transport start ")+("\n");
//                progressTv.post(() -> progressTv.setText("0 %"));
            }

            @Override
            public void onProgress(float progress) {
                updateStatusInfo("onProgress :" + progress + "\n");
                if (otaCallback != null) {
                    otaCallback.onProgress((int) (progress * 100));
                }
//                progressTv.post(() -> progressTv.setText(String.valueOf(progress * 100) + "%"));
//                if (progress * 100 >= 100) {
//                    otaCount++;
//                    reOTA(device, ota_data);
//                }

            }

            @Override
            public void onOTARequestStart() {
                updateStatusInfo("onOTARequestStart :" + "\n");
            }

            @Override
            public void print(String toString) {
//                updateStatusInfo(toString)+("\n");
            }

            @Override
            public void onDeviceConnecting(@NonNull BluetoothDevice device) {
                updateStatusInfo("Connecting :" + device.getAddress() + "\n");
            }

            @Override
            public void onDeviceConnected(@NonNull BluetoothDevice device) {
                updateStatusInfo("DeviceConnected :" + device.getAddress() + "\n");
            }

            @Override
            public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
                updateStatusInfo("DeviceDisconnecting :" + device.getAddress() + "\n");

            }

            @Override
            public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
                updateStatusInfo("DeviceDisconnected :" + device.getAddress() + "\n");

                if (manager.isSuccess()) {
                    ycBleLog.e("OTA 成功了");
                    if (otaCallback != null) {
                        otaCallback.onSuccess();
                    }
                } else {
                    ycBleLog.e("OTA 失败了");
                    if (otaCallback != null) {
                        otaCallback.onFailure(OTAErrCode.FAILURE, "failure");
                    }
                }
            }

            @Override
            public void onLinklossOccur(BluetoothDevice device) {

            }

            public void onLinkLossOccurred(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {
                updateStatusInfo("ServicesDiscovered :" + device.getAddress() + ("\n"));
            }

            @Override
            public void onDeviceReady(@NonNull BluetoothDevice device) {

            }

            @Override
            public boolean shouldEnableBatteryLevelNotifications(BluetoothDevice device) {
                return false;
            }

            @Override
            public void onBatteryValueReceived(BluetoothDevice device, int value) {

            }

            @Override
            public void onBondingRequired(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onBonded(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {
                updateStatusInfo("onError :" + message + ("status code:") + (errorCode) + "\n");
                if (message.equals(MeshOTAManager.ERROR_READ_CHARACTERISTIC)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    manager.readAck();
                } else if (message.equals(MeshOTAManager.ERROR_WRITE_DESCRIPTOR)) {
                    manager.writeDescriptor();
                } else if (errorCode == 133) {
                    manager.close();
                }
            }


            @Override
            public void onDeviceNotSupported(@NonNull BluetoothDevice device) {
                updateStatusInfo("DeviceNotSupported :" + device.getAddress() + "\n");

            }

        });
        manager.connect(device);
    }


    private void updateStatusInfo(String message) {
//        statusInfoTv.post(new Runnable() {
//            @Override
//            public void run() {
//                statusInfoTv.setText(buffer.toString());
//            }
//        });
        ycBleLog.e(message);

    }


    private byte[] getBytesFile(String filePath) throws IOException {
        byte[] mOtaBytes = toByteArray(new FileInputStream(new File(filePath)));
        return mOtaBytes;
    }

    public final byte[] toByteArray(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;

    }


}
