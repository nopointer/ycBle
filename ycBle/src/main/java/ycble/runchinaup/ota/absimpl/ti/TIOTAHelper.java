package ycble.runchinaup.ota.absimpl.ti;

import android.content.Context;

import ycble.runchinaup.ota.callback.OTACallback;

public class TIOTAHelper {

    TiOTAImpl tiOTA = new TiOTAImpl();
    private static final TIOTAHelper ourInstance = new TIOTAHelper();

    public static TIOTAHelper getInstance() {
        return ourInstance;
    }

    private TIOTAHelper() {
    }


    public void startOTA(Context context, String mac, final String filePath, final OTACallback otaCallback) {

        tiOTA.setOtaCallback(otaCallback);
        tiOTA.setFilePath(filePath);
        tiOTA.startOTA(mac);
    }


    public void startOTA(Context context, String mac, final byte imageByes[], final OTACallback otaCallback) {
        tiOTA.setOtaCallback(otaCallback);
        tiOTA.setImageByes(imageByes);
        tiOTA.startOTA(mac);
    }

    public void stopOTA() {
        if (tiOTA != null) {
            tiOTA.stopOTA();
        }
    }


}
