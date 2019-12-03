package ycble.runchinaup.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import ycble.runchinaup.core.callback.ScanListener;
import ycble.runchinaup.device.BleDevice;
import ycble.runchinaup.exception.BleUUIDNullException;
import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.util.BleUtil;

import static ycble.runchinaup.BleCfg.npBleTag;

/**
 * 抽象的ble连接管理
 */
public final class AbsBleConnManger {


    private BluetoothAdapter bluetoothAdapter = null;

    private Context context;
    private BluetoothGatt bluetoothGatt;

    public AbsBleConnManger(Context context) {
        this.context = context;
        initBleAdapter();
    }


    /**
     * 开始连接的时间
     */
    private long startConnTime = 0;

    private void initBleAdapter() {
        if (context == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        } else {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            } else {
                bluetoothAdapter = bluetoothManager.getAdapter();
            }
        }
    }

    /**
     * 必须得uuid
     */
    private HashSet<UUID> mustUUIDList = new HashSet<>();

    protected void addMustUUID(UUID uuid) {
        mustUUIDList.add(uuid);
    }

    protected void clearMUstUUID() {
        if (mustUUIDList != null) {
            mustUUIDList.clear();
        }
    }


    /**
     * 连接设备
     *
     * @param mac
     */
    protected void connect(String mac) {
        if (TextUtils.isEmpty(mac)) return;
        ycBleLog.e("发起连接请求的mac:" + mac);
        initBleAdapter();
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
        connect(bluetoothDevice);
    }

    /**
     * 连接设备
     *
     * @param bluetoothDevice
     */
    protected synchronized void connect(final BluetoothDevice bluetoothDevice) {
        ycBleLog.e("当前实际连接设备是:" + new Gson().toJson(new String[]{
                bluetoothDevice.getAddress(), bluetoothDevice.getName()
        }));
        boolIsInterceptConn = false;
        isHandDisConn = false;
        startConnTime = System.currentTimeMillis();

        if (!TextUtils.isEmpty(bluetoothDevice.getName())) {
            if (bluetoothGatt != null) {
                ycBleLog.e("已经有了缓存了=======");
            }
            refreshCache(context, bluetoothGatt);
            bluetoothGatt = bluetoothDevice.connectGatt(context, false, gattCallback);
        } else {
            ycBleLog.e("名称为空，需要开启一下扫描来缓存一下设备名称");
            hadScanDeviceFlag = true;
            BleScanner.getInstance().registerScanListener(new ScanListener() {
                @Override
                public void onScan(BleDevice bleDevice) {
                    ycBleLog.i("hadScanDeviceFlag=====>" + hadScanDeviceFlag + "///扫描到的设备:" + new Gson().toJson(bleDevice));
                    if (hadScanDeviceFlag) {
                        if (bleDevice != null && bleDevice.getMac().equalsIgnoreCase(bluetoothDevice.getAddress())) {
                            BleScanner.getInstance().unRegisterScanListener(this);
                            hadScanDeviceFlag = false;
                            BleScanner.getInstance().stopScan();
                            ycBleLog.e("扫描到设备了，停止扫描，然后再连接");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    bluetoothGatt = bluetoothDevice.connectGatt(context, false, gattCallback);
                                }
                            }, 2000);
                        }
                    }
                }

                @Override
                public void onFailure(int code) {

                }
            });
            BleScanner.getInstance().startScan();
        }
    }

    private Handler handler = new Handler();


    //是否拦截中途拦截蓝牙的连接
    private boolean boolIsInterceptConn = false;

    /**
     * 手动断开连接,同时也做成拦截（防止蓝牙在发出连接请求后，后续连接上的情况）
     */
    public void disConnect() {
        ycBleLog.e("=====>手动断开指令");
        isHandDisConn = true;
        if (bluetoothGatt != null && isConnected) {
            ycBleLog.e("已经在连接中，就不发出拦截请求了，直接断开");
            bluetoothGatt.disconnect();
        } else {
            ycBleLog.e("没有在连接中，发出拦截请求即连接后立马断开）");
            boolIsInterceptConn = true;
        }
    }

    protected BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }


    //到当前为止，是否是连接上的
    private boolean isConnected = false;
    //是否是扫描到了服务，国产的蓝牙ic，有时候检测不到
    private boolean hasServicesDiscovered = false;

    /**
     * 扫描后的连接标志位
     */
    private boolean hadScanDeviceFlag = true;

    public void setHadScanDeviceFlag(boolean hadScanDeviceFlag) {
        this.hadScanDeviceFlag = hadScanDeviceFlag;
    }

    //是否是手动断开的
    private boolean isHandDisConn = false;

    public synchronized void setHandDisConn(boolean handDisConn) {
        ycBleLog.e("handDisConn===>" + handDisConn);
        isHandDisConn = handDisConn;
    }

    public boolean isHandDisConn() {
        return isHandDisConn;
    }

    //在断开的时候之前是否连接过,这个用于关闭的时候再刷新
    private boolean hasConn = false;


    /**
     * 清除标志位
     */
    private void clearFlag() {
        hasServicesDiscovered = false;
    }


    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        /**
         * 连接成功，只有一个情况，但是失败 有很多场景
         * 连接上就断开的情况 会先走133/0,0 ===>连接异常  会走广播
         * 手动断开的情况 会走133/0,0 ===>手动断开       会走广播（假如有其他调试的app在后台连接的话，不会走广播，这里要特别注意一下）
         * 连接成功的情况 会先走2,0 ===>连接成功          会走广播
         * 没有连接上的情况，会手133//0,0===>连接失败     不会走广播
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            ycBleLog.e("===========================================" + this);
            ycBleLog.e(status + ":" + newState);
            ycBleLog.e(PhoneBleExceptionCode.getPhoneCode(status));
            ycBleLog.e("===========================================");

            //系统蓝牙挂壁的情况，真不好判断，无法通过代码判断，只能人为通过手动试验，才能知道，目前就收集到一个魅族的手机 ble异常码
            if (PhoneBleExceptionCode.isPhoneBleExcepiton(status)) {
                ycBleLog.e("系统蓝牙挂壁了");
                if (absBleConnCallback != null) {
                    absBleConnCallback.connResult(BleConnState.PHONEBLEANR);
                }
                return;
            }

            //连接上了
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {

                long useTime = (System.currentTimeMillis() - startConnTime) / 1000L;
                ycBleLog.e("================设备连接上了，耗时:" + useTime + "S");

                hasConn = true;


                //如果有拦截蓝牙连接的请求，此时一定要断开
                if (boolIsInterceptConn) {
                    ycBleLog.e("================有拦截请求，此处断开");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gatt.disconnect();
                        }
                    }, 500);
                    return;
                }

                bluetoothGatt = gatt;
                isConnected = true;

                if (absBleConnCallback != null) {
                    absBleConnCallback.connResult(BleConnState.CONNECTED);
                }
                ycBleLog.e("先移除所有的关于一次连接的消息队列");
                hasServicesDiscovered = false;
                handler.removeCallbacksAndMessages(null);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isConnected && bluetoothGatt != null) {
                            //500毫秒后，如果还连接上的，则开始扫描服务
                            ycBleLog.e("500毫秒后，如果还连接上的，则开始扫描服务");
                            boolean boolResult = bluetoothGatt.discoverServices();
                            ycBleLog.e("discoverServices结果:" + boolResult);
                            if (boolResult) {
                                handler.postDelayed(discoverServiceRunnable, 1000);
                            } else {
                                if (gatt != null) {
                                    gatt.disconnect();
                                }
                            }
                        } else {
                            ycBleLog.e("500毫秒后，不再连接是情况，移除discoverService");
                            handler.removeCallbacks(discoverServiceRunnable);
                        }
                    }
                }, 500);
            } else {
                ycBleLog.e("没有连接的所有情况====>>>>>>>>");

                //只要没有连接上，那么所有的情况，都视为连接失败，这里有区分
//                boolean boolTmp = isHandDisConn;
                isConnected = false;
                //已经断开了，就不存在拦截连接的说法了，置为false

                if (hasConn) {
                    //如果是拦截中断的话
                    if (boolIsInterceptConn) {
                        if (absBleConnCallback != null) {
                            absBleConnCallback.connResult(BleConnState.HANDDISCONN);
                        }
                    } else {
                        if (absBleConnCallback != null) {
                            absBleConnCallback.connResult(isHandDisConn ? BleConnState.HANDDISCONN : BleConnState.CONNEXCEPTION);
                        }
                    }
                } else {
                    ycBleLog.e("如果是断开之前没有连接，很明显，异常连接");
                    if (absBleConnCallback != null) {
                        absBleConnCallback.connResult(BleConnState.CONNEXCEPTION);
                    }
                }

                boolIsInterceptConn = false;
                bluetoothGatt.disconnect();
                close(bluetoothGatt);

                if (hasConn) {
                    hasConn = false;
                }
                //刷新缓存
                refreshCache(context, bluetoothGatt);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            ycBleLog.e("onServicesDiscovered==status=>" + status + new Gson().toJson(mustUUIDList));
            int count = 0;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
                    for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                        for (UUID uuid : mustUUIDList) {
                            if (uuid.equals(bluetoothGattCharacteristic.getUuid())) {
                                count++;
                            }
                        }
                    }
                }

                hasServicesDiscovered = true;
                handler.removeCallbacks(discoverServiceRunnable);

                if (count == mustUUIDList.size()) {
                    if (absBleConnCallback != null) {
                        absBleConnCallback.onLoadCharas(gatt);
                    }
                } else {
                    ycBleLog.e("uuid对不上，情况不对");
                    gatt.disconnect();
                }
            } else {
                if (absBleConnCallback != null) {
                    absBleConnCallback.onException(ErrCode.ERR_DISCOVER_SERVICE, "onServicesDiscovered");
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (absBleConnCallback != null) {
                    absBleConnCallback.onDataWrite(true, characteristic.getValue(), characteristic.getUuid());
                }
            } else {
                if (absBleConnCallback != null) {
                    absBleConnCallback.onException(ErrCode.ERR_WRITE_CHARA, "onCharacteristicWrite", characteristic.getUuid());
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (absBleConnCallback != null) {
                    absBleConnCallback.onDataWrite(false, descriptor.getValue(), descriptor.getCharacteristic().getUuid(), descriptor.getUuid());
                }
            } else {
                if (absBleConnCallback != null) {
                    absBleConnCallback.onException(ErrCode.ERR_WRITE_DESCR, "onDescriptorWrite");
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (absBleConnCallback != null) {
                    absBleConnCallback.onDataRead(true, characteristic.getValue(), characteristic.getUuid());
                }
            } else {
                if (absBleConnCallback != null) {
                    absBleConnCallback.onException(ErrCode.ERR_READ_CHARA, "onCharacteristicRead");
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (absBleConnCallback != null) {
                    absBleConnCallback.onDataRead(false, descriptor.getValue(), descriptor.getCharacteristic().getUuid(), descriptor.getUuid());
                }
            } else {
                if (absBleConnCallback != null) {
                    absBleConnCallback.onException(ErrCode.ERR_READ_DESCR, "onDescriptorRead");
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (absBleConnCallback != null) {
                absBleConnCallback.onDataChange(characteristic.getUuid(), characteristic.getValue());
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (absBleConnCallback != null) {
                    absBleConnCallback.onRssi(rssi);
                }
            } else {
                if (absBleConnCallback != null) {
                    absBleConnCallback.onException(ErrCode.ERR_RSSI, "onReadRemoteRssi");
                }
            }
        }
    };

    //写数据
    public boolean writeData(UUID serViceUUID, UUID charaUUID, byte[] data) throws BleUUIDNullException {
        BluetoothGattService service = getService(serViceUUID);
        BluetoothGattCharacteristic characteristic = getChara(service, charaUUID);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        characteristic.setValue(data);
        boolean writeResult = bluetoothGatt.writeCharacteristic(characteristic);
        String dataLenString = String.format(" [%d] ", data.length);
        ycBleLog.e("默认写:" + serViceUUID.toString() + "/" + charaUUID.toString() + ">" + dataLenString + writeResult + "< " + BleUtil.byte2HexStr(data) + " >");
        return writeResult;
    }

    //写数据
    public boolean writeDataWithOutResponse(UUID serViceUUID, UUID charaUUID, byte[] data) throws BleUUIDNullException {
        BluetoothGattService service = getService(serViceUUID);
        BluetoothGattCharacteristic characteristic = getChara(service, charaUUID);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        characteristic.setValue(data);
        boolean writeResult = bluetoothGatt.writeCharacteristic(characteristic);
        String dataLenString = String.format(" [%d] ", data.length);
        ycBleLog.e("无响应写:" + serViceUUID.toString() + "/" + charaUUID.toString() + ">" + dataLenString + writeResult + "< " + BleUtil.byte2HexStr(data) + " >");
        return writeResult;
    }

    //写数据,写描述符，很少用
    public boolean writeData(UUID serViceUUID, UUID charaUUID, UUID descrUUID, byte[] data) throws BleUUIDNullException {
        BluetoothGattService service = getService(serViceUUID);
        BluetoothGattCharacteristic characteristic = getChara(service, charaUUID);
        BluetoothGattDescriptor descriptor = getDescriptor(characteristic, descrUUID);
        descriptor.setValue(data);
        boolean writeResult = bluetoothGatt.writeDescriptor(descriptor);
        ycBleLog.e(npBleTag + "->write:" + serViceUUID.toString() + "/" + charaUUID.toString() + "/" + writeResult + "{ " + BleUtil.byte2HexStr(data) + " }");
        return writeResult;
    }

    //读取数据
    public boolean readData(UUID serViceUUID, UUID charaUUID) throws BleUUIDNullException {
        ycBleLog.i(npBleTag + "->read:" + serViceUUID.toString() + "/" + charaUUID.toString());
        BluetoothGattService service = getService(serViceUUID);
        BluetoothGattCharacteristic characteristic = getChara(service, charaUUID);
        return bluetoothGatt.readCharacteristic(characteristic);
    }

    //读取数据,读取描述符
    public boolean readData(UUID serViceUUID, UUID charaUUID, UUID descrUUID) throws BleUUIDNullException {
        BluetoothGattService service = getService(serViceUUID);
        BluetoothGattCharacteristic characteristic = getChara(service, charaUUID);
        BluetoothGattDescriptor descriptor = getDescriptor(characteristic, descrUUID);
        return bluetoothGatt.readDescriptor(descriptor);
    }

    //使能通知
    public boolean enableNotity(UUID serViceUUID, UUID charaUUID) throws BleUUIDNullException {
        return enableNotifyOrIndication(serViceUUID, charaUUID, true, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    }

    //使能指示
    public boolean enableIndication(UUID serViceUUID, UUID charaUUID) throws BleUUIDNullException {
        return enableNotifyOrIndication(serViceUUID, charaUUID, true, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
    }


    //关闭通知/指示
    public boolean disAbleNotifyOrIndication(UUID serViceUUID, UUID charaUUID) throws BleUUIDNullException {
        return enableNotifyOrIndication(serViceUUID, charaUUID, false, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
    }

    //设置监听
    public void setNotifyListen(UUID serViceUUID, UUID charaUUID, boolean enable) throws BleUUIDNullException {
        BluetoothGattService service = getService(serViceUUID);
        BluetoothGattCharacteristic characteristic = getChara(service, charaUUID);
        bluetoothGatt.setCharacteristicNotification(characteristic, enable);
    }

    //关闭蓝牙
    private void close(BluetoothGatt gatt) {
        ycBleLog.e("close Gatt");
        if (gatt != null) {
            gatt.close();
        }
    }

    //处理使能
    private boolean enableNotifyOrIndication(UUID serViceUUID, UUID charaUUID, boolean enable, byte[] data) throws BleUUIDNullException {
        BluetoothGattService service = getService(serViceUUID);
        BluetoothGattCharacteristic characteristic = getChara(service, charaUUID);
        BluetoothGattDescriptor descriptor = getDescriptor(characteristic, UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        bluetoothGatt.setCharacteristicNotification(characteristic, enable);
        descriptor.setValue(data);
        boolean writeResult = bluetoothGatt.writeDescriptor(descriptor);
        ycBleLog.i(npBleTag + "notify/indication:" + serViceUUID.toString() + "-->" + charaUUID.toString() + "-->" + descriptor + "-->" + writeResult + "{ " + BleUtil.byte2HexStr(data) + " }");
        return writeResult;
    }

    //=====================================================
    //雷打不动的方法，不需要修改什么==================================================
    //=====================================================
    //刷新缓存
    public static void refreshCache(Context context, BluetoothGatt gatt) {
        final BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        final List<BluetoothDevice> devices = manager.getConnectedDevices(BluetoothProfile.GATT);
        ycBleLog.e(npBleTag + "当前连接列表里面，设备连接个数:" + devices.size());
        for (BluetoothDevice b : devices) {
            ycBleLog.d(npBleTag + b.getAddress());
        }
        try {
            if (gatt == null) {
                ycBleLog.e("gatt======null");
                return;
            }
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                localMethod.invoke(gatt, new Object[0]);
                ycBleLog.e(npBleTag + "刷新BLE缓存");
            }
        } catch (Exception localException) {
            localException.printStackTrace();
            ycBleLog.e(npBleTag + "An exception occured while refreshing device");
        }
    }


    //判断设备是否在连接列表里面
    public static BluetoothDevice isInConnList(String mac, Context context) {
        //打印一下连接的蓝牙设备
        List<BluetoothDevice> devices = connDeviceList(context);
        if (devices == null || devices.size() < 1) return null;
        for (BluetoothDevice device : devices) {
            if (device.getAddress().equalsIgnoreCase(mac)) {
                return device;
            }
        }
        return null;
    }

    //获取设备的连接列表
    public static List<BluetoothDevice> connDeviceList(Context context) {
        ycBleLog.d(npBleTag + "读取连接的蓝牙设备");
        if (context == null) {
            return null;
        }
        final BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null || manager.getAdapter() == null || !manager.getAdapter().isEnabled()) {
            return null;
        }
        final List<BluetoothDevice> devices = manager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice device : devices) {
            ycBleLog.d("debug==>device：===>" + device.getAddress() + "==" + device.getName());
        }
        return devices;
    }


    private BluetoothGattService getService(UUID U_service) throws BleUUIDNullException {
        if (bluetoothGatt == null) {
            throw new BleUUIDNullException(String.format("not find this bluetoothGatt", U_service.toString()));
        }
        BluetoothGattService result = bluetoothGatt.getService(U_service);
        if (result == null) {
            throw new BleUUIDNullException(String.format("not find this uuid %s for BluetoothGattService", U_service.toString()));
        }
        return result;
    }

    private BluetoothGattCharacteristic getChara(BluetoothGattService service, UUID U_chara) throws BleUUIDNullException {
        BluetoothGattCharacteristic result = service.getCharacteristic(U_chara);
        if (result == null) {
            throw new BleUUIDNullException(String.format("not find this uuid %s for BluetoothGattCharacteristic please check service  or charateristic uuid", U_chara.toString()));
        }
        return result;
    }

    private BluetoothGattDescriptor getDescriptor(BluetoothGattCharacteristic characteristic, UUID U_descriptor) throws BleUUIDNullException {
        BluetoothGattDescriptor result = characteristic.getDescriptor(U_descriptor);
        if (result == null) {
            throw new BleUUIDNullException(String.format("not find this uuid %s for BluetoothGattCharacteristic please check service  or charateristic or descriptor uuid", U_descriptor.toString()));
        }
        return result;

    }

    private AbsBleConnCallback absBleConnCallback = null;

    public void setAbsBleConnAndStateCallback(AbsBleConnCallback absBleConnCallback) {
        this.absBleConnCallback = absBleConnCallback;
    }

    public static abstract class AbsBleConnCallback {
        protected abstract void connResult(BleConnState bleConnState);

        protected abstract void onLoadCharas(BluetoothGatt gatt);

        //数据上报回调
        protected void onDataChange(UUID uuid, byte[] data) {

        }

        //数据读取回调,读描述符和特性都会回调这个函数
        protected void onDataRead(boolean isCharacteristic, byte[] data, UUID... uuid) {

        }

        //数据写入回调,写描述符和特性都会回调这个函数
        protected void onDataWrite(boolean isCharacteristic, byte[] data, UUID... uuid) {

        }

        protected void onRssi(int rssi) {

        }

        protected void onException(ErrCode errCode, String msg, UUID... uuids) {
            ycBleLog.e(npBleTag + "on ERROR " + msg);
        }
    }


    /**
     * 延时扫描ble下的服务
     */
    private Runnable discoverServiceRunnable = new Runnable() {
        @Override
        public void run() {
            ycBleLog.e("1000毫秒后，如果还连接上的，检测服务有没有被扫描到");
            if (!hasServicesDiscovered) {
                ycBleLog.e("服务没有被检测到");
                if (bluetoothGatt != null) {
                    bluetoothGatt.disconnect();
                }
            } else {
                handler.removeCallbacks(discoverServiceRunnable);
            }
        }
    };


    public int getClientIf(BluetoothGatt bluetoothGatt) {
        int result = 0;
        Class<?> clazz = bluetoothGatt.getClass();
        try {
            Field field = clazz.getDeclaredField("mClientIf");
            field.setAccessible(true);
            result = (int) field.get(bluetoothGatt);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    protected void requestConnectionPriority(int connectionPriority) {
        if (bluetoothGatt != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            }
        }
    }


}
