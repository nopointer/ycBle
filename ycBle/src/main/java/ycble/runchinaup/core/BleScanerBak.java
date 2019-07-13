//package ycble.runchinaup.core;
//
//import android.annotation.TargetApi;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.le.BluetoothLeScanner;
//import android.bluetooth.le.ScanCallback;
//import android.bluetooth.le.ScanFilter;
//import android.bluetooth.le.ScanResult;
//import android.bluetooth.le.ScanSettings;
//import android.content.Context;
//import android.os.Build;
//import android.text.TextUtils;
//
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import ycble.runchinaup.core.callback.ScanListener;
//import ycble.runchinaup.device.BleDevice;
//import ycble.runchinaup.log.ycBleLog;
//
//import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY;
//import static ycble.runchinaup.BleCfg.npBleTag;
//
///**
// * Created by nopointer on 2018/8/3.
// * 蓝牙扫描
// */
//
//public class BleScanerBak {
//
//    private static BleScanerBak bleScaner = new BleScanerBak();
//
//    public static BleScanerBak getBleScaner() {
//        return bleScaner;
//    }
//
//
//    protected static boolean isShowScanLog = true;
//
//    //线程池
//    private ExecutorService cachedThreadPool = Executors.newScheduledThreadPool(10);
//
//    //========================================
//    //  单例模板              =================
//    //========================================
//
//    //是否是在做常规的扫描
//    private boolean isScanForNormal = false;
//    //是否是在做为了连接的扫描
//    private boolean isScanForConn = false;
//
//    private BleScanerBak() {
//        init();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//            if (bluetoothLeScanner == null) {
//                bluetoothLeScanner = adapter.getBluetoothLeScanner();
//            }
//            if (scanSettings == null) {
//                ScanSettings.Builder builder = new ScanSettings.Builder();
//                builder.setScanMode(SCAN_MODE_LOW_LATENCY);
//                scanSettings = builder.build();
//            }
//        }
//    }
//
//    private static Context mContext = null;
//    //蓝牙适配器
//    private BluetoothAdapter adapter = null;
//    //5.0以后的蓝牙扫描
//    private ScanCallback scanCallback50 = null;
//
//    //5.0以前的蓝牙扫描
//    private BluetoothAdapter.LeScanCallback scanCallback43 = null;
//
//    //========================================
//    //  扫描部分              =================
//    //========================================
//    private boolean isScan = false;
//    private HashSet<ScanListener> scanListenerHashSet = new HashSet<>();
//
//    public boolean isEnabled() {
//        return adapter.isEnabled();
//    }
//
//    //初始化蓝牙设备
//    public static void initSDK(Context context) {
//        mContext = context;
//    }
//
//    private void init() {
//        if (mContext == null) {
//            adapter = BluetoothAdapter.getDefaultAdapter();
//        } else {
//            BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
//            if (bluetoothManager == null) {
//                adapter = BluetoothAdapter.getDefaultAdapter();
//            } else {
//                adapter = bluetoothManager.getAdapter();
//            }
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (scanCallback50 == null) {
//                scanCallback50 = new ScanCallback() {
//                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//                    @Override
//                    public void onScanResult(int callbackType, final ScanResult result) {
//                        super.onScanResult(callbackType, result);
//                        cachedThreadPool.execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                BleDevice bleDevice = BleDevice.parserFromScanData(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
//                                if (isShowScanLog) {
//                                    ycBleLog.e("====onScanResult====>" + bleDevice.toString() + (bleDeviceFilter == null));
//                                }
//                                if (isScanForNormal) {
//                                    if (bleDeviceFilter != null) {
//                                        if (bleDeviceFilter.filter(bleDevice)) {
//                                            onScan(bleDevice);
//                                        }
//                                    } else {
//                                        onScan(bleDevice);
//                                    }
//                                }
//                                //如果用户需要管理的设备 还在的话,是要回调回去的
//                                if (!TextUtils.isEmpty(scanForMyDeviceMac) && bleDevice.getMac().equals(scanForMyDeviceMac) && connScanListener != null) {
//                                    connScanListener.scanMyDevice(bleDevice);
//                                }
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onScanFailed(int errorCode) {
//                        super.onScanFailed(errorCode);
//                        ycBleLog.e("onScanFailed====>" + errorCode);
//                        onFailure(errorCode);
////                        try {
////                            bluetoothLeScanner.stopScan(scanCallback50);
////                            bluetoothLeScanner.startScan(null, scanSettings, scanCallback50);
////                        } catch (Exception e) {
////                            e.printStackTrace();
////                        } finally {
////
////                        }
//                    }
//                };
//            }
//            if (adapter == null) {
//                adapter = BluetoothAdapter.getDefaultAdapter();
//            }
//            if (bluetoothLeScanner == null) {
//                bluetoothLeScanner = adapter.getBluetoothLeScanner();
//            }
//        }
//        if (scanCallback43 == null) {
//            scanCallback43 = new BluetoothAdapter.LeScanCallback() {
//                @Override
//                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
//                    cachedThreadPool.execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            BleDevice bleDevice = BleDevice.parserFromScanData(device, scanRecord, rssi);
//                            //如果是常规的扫描正在进行的话
//                            if (isScanForNormal) {
//                                if (bleDeviceFilter != null) {
//                                    if (bleDeviceFilter.filter(bleDevice)) {
//                                        onScan(bleDevice);
//                                    }
//                                } else {
//                                    onScan(bleDevice);
//                                }
//                            }
//                            //如果用户需要管理的设备 还在的话,是要回调回去的
//                            if (!TextUtils.isEmpty(scanForMyDeviceMac) && bleDevice.getMac().equals(scanForMyDeviceMac) && connScanListener != null) {
//                                connScanListener.scanMyDevice(bleDevice);
//                            }
//                        }
//                    });
//                }
//            };
//        }
//    }
//
//    BluetoothLeScanner bluetoothLeScanner = null;
//    ScanSettings scanSettings = null;
//
//    public void startScan() {
//        init();
//        if (!isEnabled()) {
//            ycBleLog.w(npBleTag + " 蓝牙没有打开--");
//            return;
//        }
//        ycBleLog.e("要求开始扫描设备,当前扫描状态:" + isScan);
//        isScanForNormal = true;
//        if (isScan) {
//            return;
//        }
//        judgeScanOrStop();
//    }
//
//
//    public void stopScan() {
//        init();
//        if (!isEnabled()) {
//            ycBleLog.w(npBleTag + " 蓝牙没有打开--");
//            return;
//        }
//        isScanForNormal = false;
//        if (!isScan) {
//            return;
//        }
//        judgeScanOrStop();
//    }
//
//
//    //设置蓝牙设备的过滤器
//    private BleDeviceFilter bleDeviceFilter = null;
//
//    public void setBleDeviceFilter(BleDeviceFilter bleDeviceFilter) {
//        this.bleDeviceFilter = bleDeviceFilter;
//    }
//
//    public void registerScanListener(ScanListener scanListener) {
//        if (!scanListenerHashSet.contains(scanListener)) {
//            scanListenerHashSet.add(scanListener);
//        }
//    }
//
//    public void unRegisterScanListener(ScanListener scanListener) {
//        if (scanListenerHashSet.contains(scanListener)) {
//            scanListenerHashSet.remove(scanListener);
//        }
//    }
//
//
//    private void onScan(BleDevice bleDevice) {
//        for (ScanListener scanListener : scanListenerHashSet) {
//            scanListener.onScan(bleDevice);
//        }
//    }
//
//    private void onFailure(int code) {
//        for (ScanListener scanListener : scanListenerHashSet) {
//            scanListener.onFailure(code);
//        }
//    }
//
//
//    //针对连接的回调，不需要个普通扫描去做切换了
//    private ConnScanListener connScanListener = null;
//
//    protected void setConnScanListener(ConnScanListener connScanListener) {
//        this.connScanListener = connScanListener;
//    }
//
//    //针对我的设备的mac地址
//    private String scanForMyDeviceMac = null;
//
//    public void setScanForMyDeviceMac(String scanForMyDeviceMac) {
//        this.scanForMyDeviceMac = scanForMyDeviceMac;
//    }
//
//
//    //连接的扫描 不影响普通的扫描
//    protected void startScanForConn() {
//        init();
//        if (!isEnabled()) {
//            ycBleLog.e(npBleTag + " 蓝牙没有打开--");
//            return;
//        }
//        if (isScanForConn) return;
//        isScanForConn = true;
//        judgeScanOrStop();
//    }
//
//    protected void stopScanForConn() {
//        init();
//        if (!isEnabled()) {
//            ycBleLog.w(npBleTag + " 蓝牙没有打开--");
//            return;
//        }
//        if (!isScanForConn) return;
//        isScanForConn = false;
//        judgeScanOrStop();
//    }
//
//    private void judgeScanOrStop() {
//        ycBleLog.e(" 当前扫描状态:==>isScanForNormal:" + isScanForNormal);
//        ycBleLog.e(" 当前扫描状态:==>isScanForConn:" + isScanForConn);
//
//        isScan = isScanForConn || isScanForNormal;
//        if (isScan) {
//            if (Build.VERSION.SDK_INT < 21) {
//                adapter.startLeScan(scanCallback43);
//            } else {
//                List<ScanFilter> filters = Collections.singletonList(new ScanFilter.Builder().build());
//                bluetoothLeScanner.startScan(filters, scanSettings, scanCallback50);
//            }
//        } else {
//            if (Build.VERSION.SDK_INT < 21) {
//                adapter.stopLeScan(scanCallback43);
//            } else {
//                bluetoothLeScanner.stopScan(scanCallback50);
//            }
//        }
//    }
//
//}
