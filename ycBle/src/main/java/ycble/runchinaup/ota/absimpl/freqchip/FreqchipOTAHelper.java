package ycble.runchinaup.ota.absimpl.freqchip;

import android.content.Context;

import ycble.runchinaup.ota.callback.OTACallback;

/**
 * 富窝坤OTA
 */
public class FreqchipOTAHelper {
    private static final FreqchipOTAHelper ourInstance = new FreqchipOTAHelper();

    public static FreqchipOTAHelper getInstance() {
        return ourInstance;
    }

    private FreqchipOTAHelper() {
    }

    private FreqOTAImpl otaImpl = null;

    public void startOTA(Context context, String mac, String filePath, OTACallback otaCallback) {
        otaImpl = new FreqOTAImpl();
        otaImpl.setFilePath(filePath);
        otaImpl.setOtaCallback(otaCallback);
        otaImpl.connDevice(mac);
    }
}
