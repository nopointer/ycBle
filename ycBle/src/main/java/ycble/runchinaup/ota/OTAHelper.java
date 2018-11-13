package ycble.runchinaup.ota;

import android.content.Context;

import ycble.runchinaup.device.BleDevice;
import ycble.runchinaup.ota.absimpl.nordic.DfuHelper;
import ycble.runchinaup.ota.callback.OTACallback;

/**
 * OTA 助手
 */
public class OTAHelper {

    private static final OTAHelper ourInstance = new OTAHelper();

    public static OTAHelper getInstance() {
        return ourInstance;
    }

    private OTAHelper() {
    }

    public void startOTA(Context context, String filePath, BleDevice bleDevice, FirmType firmType, OTACallback otaCallback) {
        startOTA(context, filePath, bleDevice.getMac(), bleDevice.getName(), firmType, otaCallback);
    }

    public void startOTA(Context context, String filePath, String mac, String name, FirmType firmType, OTACallback otaCallback) {
        switch (firmType) {
            case NORDIC:
                DfuHelper.getDfuHelper().start(context, filePath, mac, name, otaCallback);
                break;
        }
    }

}
