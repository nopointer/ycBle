package ycble.runchinaup.core;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import ycble.runchinaup.core.callback.ScanListener;
import ycble.runchinaup.device.BleDevice;
import ycble.runchinaup.log.ycBleLog;

import static ycble.runchinaup.BleCfg.npBleTag;

/**
 * Created by nopointer on 2018/8/3.
 * 蓝牙扫描
 */

public class BleScanner {

    private static BleScanner bleScanner = new BleScanner();

    public static BleScanner getInstance() {
        return bleScanner;
    }

    private boolean isShowScanLog = true;

    public boolean isShowScanLog() {
        return isShowScanLog;
    }

    public void setShowScanLog(boolean showScanLog) {
        isShowScanLog = showScanLog;
    }

    protected static Context mContext;

    /**
     * 设置扫描间隔,单位毫秒
     */
    private int scanRefreshTime = 1500;

    public int getScanRefreshTime() {
        return scanRefreshTime;
    }

    public void setScanRefreshTime(int scanRefreshTime) {
        this.scanRefreshTime = scanRefreshTime;
    }

    protected static void init(Context context) {
        mContext = context;
    }

    //线程池
    private ExecutorService cachedThreadPool = Executors.newScheduledThreadPool(10);

    //========================================
    //  单例模板              =================
    //========================================


    private BleScanner() {
        init();
    }

    //蓝牙适配器
    private BluetoothAdapter adapter = null;
    //5.0以后的蓝牙扫描
    private ScanCallback scanCallback = null;

    //========================================
    //  扫描部分              =================
    //========================================
    /**
     * 是否是在扫描中
     */
    private boolean isScan = false;

    public boolean isScan() {
        return isScan;
    }

    private HashSet<ScanListener> scanListenerHashSet = new HashSet<>();

    public boolean isEnabled() {
        return adapter.isEnabled();
    }

    public void closeSysBLE() {
        if (adapter == null) {
            adapter = BluetoothAdapter.getDefaultAdapter();
        }
        adapter.disable();
    }

    public void openSysBLE() {
        if (adapter == null) {
            adapter = BluetoothAdapter.getDefaultAdapter();
        }
        adapter.enable();
    }

    private void init() {

        if (adapter == null) {
            adapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (scanCallback == null) {
            scanCallback = new ScanCallback() {

                //单个,在扫描的时候已经配置过了从批量里面去拿结果，暂时不需要单个扫描的结果了
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    super.onScanResult(callbackType, result);
                    ycBleLog.e("====onScanResult====>单个==>" + result.toString());
                }

                @Override
                public void onBatchScanResults(@NonNull final List<ScanResult> results) {
                    super.onBatchScanResults(results);
                    ycBleLog.e("====onScanResult====>批量==>" + results.size());
                    cachedThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            for (ScanResult result : results) {
                                BleDevice bleDevice = BleDevice.parserFromScanData(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
                                if (isShowScanLog) {
                                    ycBleLog.e("====onScanResult====>" + bleDevice.toString() + (bleDeviceFilter == null));
                                }
                                if (bleDeviceFilter != null) {
                                    if (bleDeviceFilter.filter(bleDevice)) {
                                        onScan(bleDevice);
                                    }
                                } else {
                                    onScan(bleDevice);
                                }
                            }
                        }
                    });
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    ycBleLog.e("onScanFailed====>" + errorCode);
                    onFailure(errorCode);
                }
            };
        }

    }

    public void startScan() {
        init();
        if (!isEnabled()) {
            ycBleLog.e(npBleTag + "蓝牙没有打开，请先打开手机蓝牙，再进行扫描");
            return;
        }
        ycBleLog.e("要求开始扫描设备,当前扫描状态:" + isScan);
        if (isScan) {
            return;
        }
        isScan = true;
        judgeScanOrStop();
    }


    public void stopScan() {
        init();
        if (!isEnabled()) {
            ycBleLog.w(npBleTag + " 蓝牙没有打开--");
            return;
        }
        if (!isScan) {
            return;
        }
        isScan = false;
        judgeScanOrStop();
    }


    //设置蓝牙设备的过滤器
    private BleDeviceFilter bleDeviceFilter = null;

    public void setBleDeviceFilter(BleDeviceFilter bleDeviceFilter) {
        this.bleDeviceFilter = bleDeviceFilter;
    }

    public void registerScanListener(ScanListener scanListener) {
        if (!scanListenerHashSet.contains(scanListener)) {
            scanListenerHashSet.add(scanListener);
        }
    }

    public void unRegisterScanListener(ScanListener scanListener) {
        if (scanListenerHashSet.contains(scanListener)) {
            scanListenerHashSet.remove(scanListener);
        }
    }


    private void onScan(BleDevice bleDevice) {
        for (ScanListener scanListener : scanListenerHashSet) {
            scanListener.onScan(bleDevice);
        }
    }

    private void onFailure(int code) {
        for (ScanListener scanListener : scanListenerHashSet) {
            scanListener.onFailure(code);
        }
    }


    private void judgeScanOrStop() {
        try {
            if (isScan) {
                if (scanRefreshTime < 0) {
                    scanRefreshTime = 1500;
                }
                final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                final ScanSettings settings = new ScanSettings.Builder()
                        .setLegacy(false)
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setReportDelay(scanRefreshTime)
                        .setUseHardwareBatchingIfSupported(false).build();
                final List<ScanFilter> filters = new ArrayList<>();
                filters.add(new ScanFilter.Builder().build());
                scanner.startScan(filters, settings, scanCallback);

            } else {
                final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                scanner.stopScan(scanCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

