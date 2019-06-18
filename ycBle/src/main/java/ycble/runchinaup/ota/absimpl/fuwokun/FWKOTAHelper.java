package ycble.runchinaup.ota.absimpl.fuwokun;

import android.content.Context;

import ycble.runchinaup.core.ycBleSDK;
import ycble.runchinaup.ota.callback.OTACallback;

/**
 * 富窝坤OTA
 */
public class FWKOTAHelper {
    private static final FWKOTAHelper ourInstance = new FWKOTAHelper();

    public static FWKOTAHelper getInstance() {
        return ourInstance;
    }

    private FWKOTAHelper() {
    }

    private OTAImpl otaImpl = new OTAImpl();

    public void startOTA(Context context, String mac, String filePath, OTACallback otaCallback) {
        ycBleSDK.initSDK(context);
        otaImpl = new OTAImpl();
        otaImpl.setFilePath(filePath);
        otaImpl.setOtaCallback(otaCallback);
        otaImpl.scanAndConn(mac, 30);
    }
}
