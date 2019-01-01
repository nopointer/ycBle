package ycble.runchinaup.ota.absimpl.htx;

import android.content.Context;

public class OTAHelper {
    private static final OTAHelper ourInstance = new OTAHelper();

    public static OTAHelper getInstance() {
        return ourInstance;
    }

    private OTAHelper() {
        deviceMac = "C1:2F:F2:C3:D5:0F";
    }

    private String appFilePath;
    private String deviceMac;

    private AppOTA appOTA = null;

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
            appOTA = new AppOTA();
        }
        appOTA.setOtaCallback(otaCallback);
        appOTA.setAppFileStringPath(appFilePath);
        appOTA.setmDeviceAddress(deviceMac);
        appOTA.startOTA(context);
    }


}
