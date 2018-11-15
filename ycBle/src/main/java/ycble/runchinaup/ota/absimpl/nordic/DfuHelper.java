package ycble.runchinaup.ota.absimpl.nordic;

import android.content.Context;
import android.os.Build;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.ota.OTAState;
import ycble.runchinaup.ota.callback.OTACallback;

public class DfuHelper {

    private DfuHelper() {
    }

    public static DfuHelper dfuHelper = null;

    public static DfuHelper getDfuHelper() {
        synchronized (Void.class) {
            if (dfuHelper == null) {
                synchronized (Void.class) {
                    dfuHelper = new DfuHelper();
                }
            }
        }
        return dfuHelper;
    }

    private OTACallback otaCallback = null;

    public void start(Context context, String zipFilePath, String mac, String name, OTACallback otaCallback) {
        ycBleLog.e("context===>" + context);
        ycBleLog.e("zipFilePath===>" + zipFilePath);
        ycBleLog.e("mac===>" + mac);
        ycBleLog.e("name===>" + name);
        ycBleLog.e("otaCallback===>" + otaCallback);

        this.otaCallback = otaCallback;
        final DfuServiceInitiator starter = new DfuServiceInitiator(mac)
                .setDeviceName(name)
                .setKeepBond(false)
                .setForceDfu(true)
                .setPacketsReceiptNotificationsEnabled(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                .setPacketsReceiptNotificationsValue(12)
                .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);

        starter.setZip(zipFilePath);

        starter.start(context, DfuService.class);
        DfuServiceListenerHelper.registerProgressListener(context, mDfuProgressListener);
    }

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            ycBleLog.e("R.string.dfu_status_connecting");

            if (otaCallback != null) {
                otaCallback.onCurrentState(OTAState.connecting);
            }
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {

            ycBleLog.e("R.string.dfu_status_starting");

            if (otaCallback != null) {
                otaCallback.onCurrentState(OTAState.starting);
            }
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            ycBleLog.e("R.string.dfu_status_switching_to_dfu");

            if (otaCallback != null) {
                otaCallback.onCurrentState(OTAState.switching_to_dfu);
            }

        }
//
        @Override
        public void onFirmwareValidating(final String deviceAddress) {
//            mProgressBar.setIndeterminate(true);
//            mTextPercentage.setText(R.string.dfu_status_validating);
            ycBleLog.e("R.string.dfu_status_validating");
        }
//
        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
//            mProgressBar.setIndeterminate(true);
//            mTextPercentage.setText(R.string.dfu_status_disconnecting);
            ycBleLog.e("R.string.dfu_status_disconnecting");
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            ycBleLog.e("R.string.dfu_status_completed");
            if (otaCallback != null) {
                otaCallback.onSuccess();
            }
        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            ycBleLog.e("R.string.dfu_status_aborted");

            if (otaCallback != null) {
                otaCallback.onFailure("");
            }
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            ycBleLog.e("R.string.dfu_uploading_percentage" + percent);
            if (otaCallback != null) {
                otaCallback.onProgress(percent);
            }
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            ycBleLog.e("onError==" + error + ";" + message);
            if (otaCallback != null) {
                otaCallback.onFailure(message);
            }
        }
    };


}
