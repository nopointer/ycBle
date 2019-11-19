package ycble.runchinaup.ota;

import android.content.Context;

import no.nordicsemi.android.dfu.DfuBaseService;
import ycble.runchinaup.device.BleDevice;
import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.ota.absimpl.freqchip.FreqchipOTAHelper;
import ycble.runchinaup.ota.absimpl.htx.HTXOTAHelper;
import ycble.runchinaup.ota.absimpl.nordic.DfuHelper;
import ycble.runchinaup.ota.absimpl.telink.TeLinkOTAHelper;
import ycble.runchinaup.ota.absimpl.ti.TIOTAHelper;
import ycble.runchinaup.ota.callback.OTACallback;

/**
 * OTA 助手
 */
public class OTAHelper {

    private static final OTAHelper ourInstance = new OTAHelper();

    public static OTAHelper getInstance() {
        return ourInstance;
    }

    /**
     * 设置DfuService
     */
    public Class<? extends DfuBaseService> dfuBaseService;

    public void setDfuBaseService(Class<? extends DfuBaseService> dfuBaseService) {
        this.dfuBaseService = dfuBaseService;
    }

    private OTAHelper() {
    }

    public void startOTA(Context context, String filePath, BleDevice bleDevice, FirmType firmType, OTACallback otaCallback) {
        ycBleLog.e("startOTA======>");
        ycBleLog.e("firmType======>" + firmType);
        ycBleLog.e("filePath======>" + filePath);
        ycBleLog.e("bleDevice======>" + bleDevice);
        ycBleLog.e("otaCallback======>" + otaCallback);
        startOTA(context, filePath, bleDevice.getMac(), bleDevice.getName(), firmType, otaCallback);
    }

    public void startOTA(Context context, String filePath, String mac, String name, FirmType firmType, OTACallback otaCallback) {
        switch (firmType) {
            //nordic的ota 也是默认的ota
            case NORDIC:
                DfuHelper.getDfuHelper().start(context, filePath, mac, name, otaCallback, dfuBaseService);
                break;
            case HTX://汉天下的OTA
                ycBleLog.e("开始汉天下的ota======>");
                HTXOTAHelper htxotaHelper = HTXOTAHelper.getInstance();
                htxotaHelper.setAppFilePath(filePath);
                htxotaHelper.setDeviceMac(mac);
                htxotaHelper.setOtaCallback(otaCallback);
                htxotaHelper.startOTA(context);
                break;
            case TELINK:
                TeLinkOTAHelper.getInstance().startOTA(context, mac, filePath, otaCallback);
                break;
            case FREQCHIP:
                FreqchipOTAHelper.getInstance().startOTA(context, mac, filePath, otaCallback);
                break;
            case TI:
                TIOTAHelper.getInstance().startOTA(context, mac, filePath, otaCallback);
                break;
            default:
                ycBleLog.e("暂无合适的固件");
                break;
        }
    }

    /**
     * 释放资源
     */
    public void free() {
        HTXOTAHelper.getInstance().free();
    }

}
