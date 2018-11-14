package ycble.runchinaup.core;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ycble.runchinaup.core.callback.ScanListener;
import ycble.runchinaup.device.BleDevice;
import ycble.runchinaup.log.ycBleLog;

import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY;
import static ycble.runchinaup.BleCfg.npBleTag;

/**
 * Created by nopointer on 2018/8/3.
 * 蓝牙扫描
 */

public class BleScaner {

    //线程池
    private ExecutorService cachedThreadPool = Executors.newScheduledThreadPool(10);

    //========================================
    //  单例模板              =================
    //========================================

    private BleScaner() {
        init();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = adapter.getBluetoothLeScanner();
            }
            if (scanSettings == null) {
                ScanSettings.Builder builder = new ScanSettings.Builder();
                builder.setScanMode(SCAN_MODE_LOW_LATENCY);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    builder.setMatchMode(MATCH_MODE_AGGRESSIVE);
//                    builder.setCallbackType(CALLBACK_TYPE_FIRST_MATCH);
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    builder.setLegacy(true);
//                    builder.setPhy(PHY_LE_ALL_SUPPORTED);
//                }
                scanSettings = builder.build();
//                new ScanSettings.Builder().setScanMode()
//                        .setReportDelay(0)
//                        .setMatchMode(1)
//                        .setCallbackType(1)
//                        .build();
            }
        }
    }

    private static Context mContext = null;

    private static BleScaner bleScaner = new BleScaner();

    public static BleScaner getBleScaner() {
        return bleScaner;
    }

    private BluetoothAdapter adapter = null;

    //5.0以后的蓝牙扫描
    private ScanCallback scanCallback50 = null;

    //5.0以前的蓝牙扫描
    private BluetoothAdapter.LeScanCallback scanCallback43 = null;

    //========================================
    //  扫描部分              =================
    //========================================
    private boolean isScan = false;
    private HashSet<ScanListener> scanListenerHashSet = new HashSet<>();

    public boolean isEnabled() {
        return adapter.isEnabled();
    }

    //初始化蓝牙设备
    public static void initSDK(Context context) {
        mContext = context;
    }

    private void init() {
        if (mContext == null) {
            adapter = BluetoothAdapter.getDefaultAdapter();
        } else {
            BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            adapter = bluetoothManager.getAdapter();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (scanCallback50 == null) {
                scanCallback50 = new ScanCallback() {

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onScanResult(int callbackType, final ScanResult result) {
                        super.onScanResult(callbackType, result);
                        cachedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                ycBleLog.e("====onScanResult====>" + result.toString() + (bleDeviceFilter == null));
                                if (bleDeviceFilter != null) {
                                    BleDevice bleDevice = bleDeviceFilter.parserDevice(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
                                    if (bleDeviceFilter.filter(bleDevice)) {
                                        onScan(bleDevice);
                                    }
                                } else {
                                    BleDevice bleDevice = BleDevice.parserFromScanData(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
                                    onScan(bleDevice);
                                }
                            }
                        });
                    }

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        super.onBatchScanResults(results);
                        ycBleLog.e("====onBatchScanResults====>" + results.size());
                        for (ScanResult result : results) {
                            if (bleDeviceFilter != null) {
                                BleDevice bleDevice = bleDeviceFilter.parserDevice(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
                                if (bleDeviceFilter.filter(bleDevice)) {
                                    onScan(bleDevice);
                                }
                            } else {
                                BleDevice bleDevice = BleDevice.parserFromScanData(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
                                onScan(bleDevice);
                            }
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        ycBleLog.e("onScanFailed====>" + errorCode);
                    }
                };
            }
            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = adapter.getBluetoothLeScanner();
            }
        }
        if (scanCallback43 == null) {
            scanCallback43 = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    cachedThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (bleDeviceFilter != null) {
                                BleDevice bleDevice = bleDeviceFilter.parserDevice(device, scanRecord, rssi);
                                if (bleDeviceFilter.filter(bleDevice)) {
                                    onScan(bleDevice);
                                }
                            } else {
                                BleDevice bleDevice = (BleDevice) BleDevice.parserFromScanData(device, scanRecord, rssi);
                                onScan(bleDevice);
                            }
                        }
                    });
                }
            };
        }
    }

    BluetoothLeScanner bluetoothLeScanner = null;
    ScanSettings scanSettings = null;

    public void startScan() {
        init();
        if (!isEnabled()) {
            ycBleLog.w(npBleTag + " 蓝牙没有打开--");
            return;
        }
        ycBleLog.e("要求开始扫描设备");
        if (!isScan) {
            if (Build.VERSION.SDK_INT < 21) {
                adapter.startLeScan(scanCallback43);
            } else {
                ycBleLog.e("======>扫描设备===>");
                bluetoothLeScanner.startScan(null, scanSettings, scanCallback50);
            }
            isScan = true;
        }
    }

    public void stopScan() {
        init();
        if (!isEnabled()) {
            ycBleLog.w(npBleTag + " 蓝牙没有打开--");
            return;
        }
        ycBleLog.e("要求停止扫描设备");
        if (isScan) {
            if (Build.VERSION.SDK_INT < 21) {
                adapter.stopLeScan(scanCallback43);
            } else {
                bluetoothLeScanner.stopScan(scanCallback50);
            }
            isScan = false;
        }
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
        ycBleLog.e("===处理回调===>" + scanListenerHashSet.size());
        for (ScanListener scanListener : scanListenerHashSet) {
            scanListener.onScan(bleDevice);
        }
    }

}
