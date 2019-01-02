package ycble.runchinaup.ota.absimpl.htx;

import android.content.Context;

import ycble.runchinaup.ota.callback.OTACallback;

public class HTXOTAHelper {
    private static final HTXOTAHelper ourInstance = new HTXOTAHelper();

    public static HTXOTAHelper getInstance() {
        return ourInstance;
    }

    private HTXOTAHelper() {
//        deviceMac = "C1:2F:F2:C3:D5:0F";
    }

    private String appFilePath;
    private String deviceMac;

    private HTXAppOTA appOTA = null;

    private OTACallback otaCallback = null;

    public OTACallback getOtaCallback() {
        return otaCallback;
    }

    public void setOtaCallback(OTACallback otaCallback) {
        this.otaCallback = otaCallback;
    }

    public void setAppFilePath(String appFilePath) {
        this.appFilePath = appFilePath;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }


    public void startOTA(Context context) {
        if (appOTA == null) {
            appOTA = new HTXAppOTA();
        }
        appOTA.setOtaCallback(otaCallback);
        appOTA.setAppFileStringPath(appFilePath);
        appOTA.setmDeviceAddress(deviceMac);
        appOTA.startOTA(context);
    }


}
