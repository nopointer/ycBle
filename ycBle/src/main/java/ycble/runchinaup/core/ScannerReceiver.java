package ycble.runchinaup.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import ycble.runchinaup.log.ycBleLog;

public class ScannerReceiver extends BroadcastReceiver {

        public ScannerReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) {
                ycBleLog.e("intent.getAction() = null");
                return;
            }

            //获取返回的错误码
            int errorCode = intent.getIntExtra(BluetoothLeScannerCompat.EXTRA_ERROR_CODE, -1);//ScanSettings.SCAN_FAILED_*
            //获取到的蓝牙设备的回调类型
            int callbackType = intent.getIntExtra(BluetoothLeScannerCompat.EXTRA_CALLBACK_TYPE, -1);//ScanSettings.CALLBACK_TYPE_*

            List<ScanResult> scanResults = (List<ScanResult>) intent.getSerializableExtra(BluetoothLeScannerCompat.EXTRA_LIST_SCAN_RESULT);
            if (scanResults == null || scanResults.size() < 1) return;

            ycBleLog.i("====MyReceiver==" + errorCode + "/" + callbackType + "==>批量==>" + scanResults.size());
        }
    }
