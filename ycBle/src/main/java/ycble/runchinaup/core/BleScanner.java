package ycble.runchinaup.core;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

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

    protected static boolean isShowScanLog = true;


    protected static Context mContext;

    protected static void init(Context context) {
        mContext = context;
    }

    //线程池
    private ExecutorService cachedThreadPool = Executors.newScheduledThreadPool(30);

    //========================================
    //  单例模板              =================
    //========================================

    //是否是在做常规的扫描
    private boolean isScanForNormal = false;
    //是否是在做为了连接的扫描
    private boolean isScanForConn = false;

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
    private boolean isScan = false;
    private HashSet<ScanListener> scanListenerHashSet = new HashSet<>();

    public boolean isEnabled() {
        return adapter.isEnabled();
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
                public void onBatchScanResults(@NonNull List<ScanResult> results) {
                    super.onBatchScanResults(results);
                    ycBleLog.i("====onScanResult====>批量==>" + results.size());
                    if (results != null && results.size() == 0) {
//                        mContext.sendBroadcast(new Intent(Intent.ACTION_SCREEN_ON));
                    }
                    for (final ScanResult result : results) {
                        cachedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                BleDevice bleDevice = BleDevice.parserFromScanData(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
                                if (isShowScanLog) {
                                    ycBleLog.i("====onScanResult====>" + bleDevice.toString() + (bleDeviceFilter == null));
                                }
                                if (isScanForNormal) {
                                    if (bleDeviceFilter != null) {
                                        if (bleDeviceFilter.filter(bleDevice)) {
                                            onScan(bleDevice);
                                        }
                                    } else {
                                        onScan(bleDevice);
                                    }
                                }
                                //如果用户需要管理的设备 还在的话,是要回调回去的
                                if (!TextUtils.isEmpty(scanForMyDeviceMac) && bleDevice.getMac().equals(scanForMyDeviceMac) && connScanListener != null) {
                                    connScanListener.scanMyDevice(bleDevice);
                                }
                            }
                        });
                    }
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
        isScanForNormal = true;
        if (isScan) {
            return;
        }
        judgeScanOrStop();
    }


    public void stopScan() {
        init();
        if (!isEnabled()) {
            ycBleLog.w(npBleTag + " 蓝牙没有打开--");
            return;
        }
        isScanForNormal = false;
        if (!isScan) {
            return;
        }
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


    //针对连接的回调，不需要个普通扫描去做切换了
    private ConnScanListener connScanListener = null;

    protected void setConnScanListener(ConnScanListener connScanListener) {
        this.connScanListener = connScanListener;
    }

    //针对我的设备的mac地址
    private String scanForMyDeviceMac = null;

    public void setScanForMyDeviceMac(String scanForMyDeviceMac) {
        this.scanForMyDeviceMac = scanForMyDeviceMac;
    }


    //连接的扫描 不影响普通的扫描
    protected void startScanForConn() {
        init();
        if (!isEnabled()) {
            ycBleLog.e(npBleTag + " 蓝牙没有打开--");
            return;
        }
        if (isScanForConn) return;
        isScanForConn = true;
        judgeScanOrStop();
    }

    protected void stopScanForConn() {
        init();
        if (!isEnabled()) {
            ycBleLog.w(npBleTag + " 蓝牙没有打开--");
            return;
        }
        if (!isScanForConn) return;
        isScanForConn = false;
        judgeScanOrStop();
    }

    private void judgeScanOrStop() {
        ycBleLog.e(" 当前扫描状态:==>isScanForNormal:" + isScanForNormal);
        ycBleLog.e(" 当前扫描状态:==>isScanForConn:" + isScanForConn);

        isScan = isScanForConn || isScanForNormal;
        if (isScan) {
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            final ScanSettings settings = new ScanSettings.Builder()
                    .setLegacy(false)
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(1200)
                    .setUseHardwareBatchingIfSupported(false).build();
            final List<ScanFilter> filters = new ArrayList<>();
            filters.add(new ScanFilter.Builder().build());
            if (isScanForConn && isScanForNormal) {
                scanner.stopScan(scanCallback);
            }
//            scanner.startScan(filters, settings, scanCallback);


            Intent intent = new Intent(mContext, MyReceiver.class);
            intent.setAction("com.example.ACTION_FOUND");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            scanner.startScan(filters, settings, mContext, pendingIntent);
        } else {
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);
        }
    }


}
