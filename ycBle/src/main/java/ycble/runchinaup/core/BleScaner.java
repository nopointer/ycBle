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

import java.util.HashSet;
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

public final class BleScaner {

    private static BleScaner bleScaner = new BleScaner();

    public static BleScaner getBleScaner() {
        return bleScaner;
    }


    protected static boolean isShowScanLog = true;

    //线程池
    private ExecutorService cachedThreadPool = Executors.newScheduledThreadPool(33);

    //========================================
    //  单例模板              =================
    //========================================


    /**
     * 构造函数
     */
    private BleScaner() {
    }

    //蓝牙适配器
    private BluetoothAdapter adapter = null;
    //5.0以后的蓝牙扫描
    private ScanCallback scanCallback50 = null;

    //5.0以前的蓝牙扫描
    private BluetoothAdapter.LeScanCallback scanCallback43 = null;

    //========================================
    //  扫描部分              =================
    //========================================
    /**
     * 是否正在扫描
     */
    private boolean isScaning = false;
    private HashSet<ScanListener> scanListenerHashSet = new HashSet<>();

    public boolean isEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }


    private synchronized void init(Context context) {

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            adapter = BluetoothAdapter.getDefaultAdapter();
        } else {
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
                                BleDevice bleDevice = BleDevice.parserFromScanData(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
                                if (isShowScanLog) {
                                    ycBleLog.e("====onScanResult====>" + bleDevice.toString() + (bleDeviceFilter == null));
                                }
                                if (bleDeviceFilter != null) {
                                    if (bleDeviceFilter.filter(bleDevice)) {
                                        notifyScan(bleDevice);
                                    }
                                } else {
                                    notifyScan(bleDevice);
                                }
                            }
                        });
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        ycBleLog.e("onScanFailed====>" + errorCode);
                    }
                };
            }
        }
        if (scanCallback43 == null) {
            scanCallback43 = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    cachedThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            BleDevice bleDevice = BleDevice.parserFromScanData(device, scanRecord, rssi);
                            ycBleLog.e("====onScanResult====>" + bleDevice.toString() + (bleDeviceFilter == null));
                            //如果是常规的扫描正在进行的话
                            if (bleDeviceFilter != null) {
                                if (bleDeviceFilter.filter(bleDevice)) {
                                    notifyScan(bleDevice);
                                }
                            } else {
                                notifyScan(bleDevice);
                            }
                        }
                    });
                }
            };
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = adapter.getBluetoothLeScanner();
            }
            if (scanSettings == null) {
                ScanSettings.Builder builder = new ScanSettings.Builder();
                builder.setScanMode(SCAN_MODE_LOW_LATENCY);
                scanSettings = builder.build();
            }
        }
    }

    BluetoothLeScanner bluetoothLeScanner = null;
    ScanSettings scanSettings = null;

    public void startScan(Context context) {
        init(context);
        if (!isEnabled()) {
            ycBleLog.w(npBleTag + " 蓝牙没有打开--");
            return;
        }
        ycBleLog.e("要求开始扫描设备,当前扫描状态:" + isScaning);
        if (isScaning) {
            return;
        }
        isScaning = true;
        judgeScanOrStop();
    }


    public void stopScan(Context context) {
        init(context);
        if (!isEnabled()) {
            ycBleLog.w(npBleTag + " 蓝牙没有打开--");
            return;
        }
        ycBleLog.e("要求停止扫描设备,当前扫描状态:" + isScaning);
        if (!isScaning) {
            return;
        }
        isScaning = false;
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


    /**
     * 通知扫描回调
     *
     * @param bleDevice
     */
    private void notifyScan(BleDevice bleDevice) {
        for (ScanListener scanListener : scanListenerHashSet) {
            scanListener.onScan(bleDevice);
        }
    }

    private void judgeScanOrStop() {

        if (isScaning) {
            if (Build.VERSION.SDK_INT < 21) {
                adapter.startLeScan(scanCallback43);
            } else {
                bluetoothLeScanner.startScan(null, scanSettings, scanCallback50);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                adapter.stopLeScan(scanCallback43);
            } else {
                bluetoothLeScanner.stopScan(scanCallback50);
            }
        }
    }

}
