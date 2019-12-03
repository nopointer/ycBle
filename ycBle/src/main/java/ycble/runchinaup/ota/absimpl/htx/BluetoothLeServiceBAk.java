//package ycble.runchinaup.ota.absimpl.htx;
//
//
//import android.app.Service;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.BluetoothProfile;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Binder;
//import android.os.IBinder;
//import android.util.Log;
//
//import java.lang.reflect.Method;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.Semaphore;
//
//import ycble.runchinaup.log.ycBleLog;
//
//
//public class BluetoothLeServiceBAk extends Service {
//
//    public static Semaphore write_characer_lock = new Semaphore(1);
//    private static int mPlayCount = 0;
//    private int mRes = 0;
//    private final static String TAG = BluetoothLeServiceBAk.class.getSimpleName();
//
//    private BluetoothManager mBluetoothManager;
//    private BluetoothAdapter mBluetoothAdapter;
//    private String mBluetoothDeviceAddress;
//    private BluetoothGatt mBluetoothGatt;
//
//    //    private int mConnectionState = STATE_DISCONNECTED;
//    private static final int STATE_DISCONNECTED = 0;
//    private static final int STATE_CONNECTING = 1;
//    private static final int STATE_CONNECTED = 2;
//
//    public final static String ACTION_BLE_RECV_DATA =
//            "com.example.bluetooth.le.ACTION_BLE_RECV_DATA";
//
//    public final static String ACTION_THEMOMETER_RECV_VALUE =
//            "com.example.bluetooth.le.ACTION_THEMOMETER_RECV_VALUE";
//
//
//    public final static String ACTION_GATT_CONNECTED =
//            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
//    public final static String ACTION_GATT_DISCONNECTED =
//            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
//
//    public final static String ACTION_GATT_STATUS_133 =
//            "com.example.bluetooth.le.ACTION_GATT_STATUS_133";
//    public final static String ACTION_GATT_SERVICES_DISCOVERED =
//            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
//    public final static String ACTION_DATA_AVAILABLE =
//            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
//
//    public final static String ACTION_GATT_CHARACTER_NOTIFY =
//            "com.example.bluetooth.le.ACTION_GATT_CHARACTER_NOTIFY";
//
//    public final static String EXTRA_DATA =
//            "com.example.bluetooth.le.EXTRA_DATA";
//
//    public final static String ARRAY_BYTE_DATA =
//            "com.example.bluetooth.le.ARRAY_BYTE_DATA";
//
//    public final static String ACTION_GATT_WRITE_RESULT =
//            "com.example.bluetooth.le.ACTION_GATT_WRITE_RESULT";
//
//    public final static String OTA_RX_DAT_ACTION =
//            "com.hs.bluetooth.le.OTA_RX_DAT_ACTION";
//
//    public final static String OTA_RX_CMD_ACTION =
//            "com.hs.bluetooth.le.OTA_RX_CMD_ACTION";
//
//    public final static String OTA_RX_ISP_CMD_ACTION =
//            "com.hs.bluetooth.le.OTA_RX_ISP_CMD_ACTION";
//
//    public final static UUID UUID_HEART_RATE_MEASUREMENT =
//            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
//
//    public final static UUID UUID_RSSI_VALUE =
//            UUID.fromString(SampleGattAttributes.RSSI_VALUE);
//
//    public final static UUID UUID_RSSI_CONFIGARATION =
//            UUID.fromString(SampleGattAttributes.RSSI_CONFIGARATION);
//
//    public final static UUID UUID_BLUE_RECV_VALUE =
//            UUID.fromString(SampleGattAttributes.BLUE_RECV_VALUE);
//
//    public final static UUID UUID_TEMPERATURE_MEASUREMENT =
//            UUID.fromString(SampleGattAttributes.TEMP_MEASUREMENT);
//
//    public final static UUID UUID_OTA_TX_CMD =
//            UUID.fromString(SampleGattAttributes.otas_tx_cmd_uuid);
//
//    public final static UUID UUID_ISP_TX_CMD =
//            UUID.fromString(SampleGattAttributes.otas_tx_ips_cmd_uuid);
//
//
//    public final static UUID UUID_OTA_TX_DAT =
//            UUID.fromString(SampleGattAttributes.otas_tx_dat_uuid);
//
//    public final static UUID UUID_OTA_RX_CMD =
//            UUID.fromString(SampleGattAttributes.otas_rx_cmd_uuid);
//
//    public final static UUID UUID_ISP_RX_CMD =
//            UUID.fromString(SampleGattAttributes.otas_rx_ips_cmd_uuid);
//
//    public final static UUID UUID_OTA_RX_DAT =
//            UUID.fromString(SampleGattAttributes.otas_rx_dat_uuid);
//
//    private Context mContext;
//
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO Auto-generated method stub
//        return mBinder;
//    }
//
//    @Override
//    public boolean onUnbind(Intent intent) {
//        // After using a given device, you should make sure that BluetoothGatt.close() is called
//        // such that resources are cleaned up properly.  In this particular example, close() is
//        // invoked when the UI is disconnected from the Service.
//
//        return super.onUnbind(intent);
//    }
//
//    private final IBinder mBinder = new LocalBinder();
//
//    public class LocalBinder extends Binder {
//        public BluetoothLeServiceBAk getService() {
//            return BluetoothLeServiceBAk.this;
//        }
//    }
//
//
//    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
////            Log.e("peng", "状态码 status:" + status);
//            String intentAction;
//            if (status == 133) {
//                intentAction = ACTION_GATT_STATUS_133;
//                refreshDeviceCache();
//                close();
//                broadcastUpdate(intentAction);
//            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
//                intentAction = ACTION_GATT_CONNECTED;
//                broadcastUpdate(intentAction);
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Log.i(TAG, "Connected to GATT server.");
//                // Attempts to discover services after successful connection.
//                Log.i(TAG, "Attempting to start service discovery:" +
//                        mBluetoothGatt.discoverServices());
//
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                refreshDeviceCache();
//                close();
//                write_characer_lock.release();
//                Log.i(TAG, "Disconnected from GATT server.");
//                intentAction = ACTION_GATT_DISCONNECTED;
//                broadcastUpdate(intentAction);
//            }
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            //if (status == BluetoothGatt.GATT_SUCCESS && mOnServiceDiscoverListener!=null) {
//            String intentAction;
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                //mOnServiceDiscoverListener.onServiceDiscover(gatt);
//                intentAction = ACTION_GATT_SERVICES_DISCOVERED;
//                //ShowGattServices(getSupportedGattServices());
//                broadcastUpdate(intentAction);
//                Log.i(TAG, "onServicesDiscovered");
//            } else {
//                Log.w(TAG, "onServicesDiscovered received: " + status);
//            }
//        }
//
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt,
//                                         BluetoothGattCharacteristic characteristic,
//                                         int status) {
////        	if (mOnDataAvailableListener!=null)
////        		mOnDataAvailableListener.onCharacteristicRead(gatt, characteristic, status);
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//        }
//
//        /**
//         * Callback triggered as a result of a remote characteristic notification.
//         *
//         * @param gatt GATT client the characteristic is associated with
//         * @param characteristic Characteristic that has been updated as a result
//         *                       of a remote notification event.
//         */
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt,
//                                            BluetoothGattCharacteristic characteristic) {
//
//
////        	if(SampleGattAttributes.BLUE_RECV_VALUE.equals(characteristic.getUuid().toString())){
////
////        		broadcastUpdate(ACTION_BLE_RECV_DATA, characteristic);
////        	}else if(SampleGattAttributes.TEMP_MEASUREMENT.equals(characteristic.getUuid().toString())){
////        		broadcastUpdate(ACTION_THEMOMETER_RECV_VALUE, characteristic);
////        	}else{
////        		broadcastUpdate(ACTION_GATT_CHARACTER_NOTIFY, characteristic);
////        	}
//
//            if (SampleGattAttributes.otas_rx_dat_uuid.equals(characteristic.getUuid().toString())) {
//                broadcastUpdate(OTA_RX_DAT_ACTION, characteristic);
//            }
//
//            if (SampleGattAttributes.otas_rx_ips_cmd_uuid.equals(characteristic.getUuid().toString())) {
//                broadcastUpdate(OTA_RX_ISP_CMD_ACTION, characteristic);
//            }
//
//
//            if (SampleGattAttributes.otas_rx_cmd_uuid.equals(characteristic.getUuid().toString())) {
//                broadcastUpdate(OTA_RX_CMD_ACTION, characteristic);
//            }
//        }
//
//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
////            Log.e("peng","gatt1gatt1:"+mGattCallback);
//            Log.i("DEBUG_OTA", "write status: " + status);
//            write_characer_lock.release(1);
////        	if(SampleGattAttributes.otas_tx_dat_uuid.equals(characteristic.getUuid().toString()))
////        	{
////        		broadcastUpdate(ACTION_GATT_WRITE_RESULT, characteristic);
////        	}
//        }
//    };
//
//    public boolean initialize() {
//        // For API level 18 and above, get a reference to BluetoothAdapter through
//        // BluetoothManager.
//        if (mBluetoothManager == null) {
//            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//            if (mBluetoothManager == null) {
//                ycBleLog.e( "Unable to initialize BluetoothManager.");
//                return false;
//            }
//        }
//
//        mBluetoothAdapter = mBluetoothManager.getAdapter();
//        if (mBluetoothAdapter == null) {
//            ycBleLog.e( "Unable to obtain a BluetoothAdapter.");
//            return false;
//        }
//
//        return true;
//    }
//
//    /**
//     * Connects to the GATT server hosted on the Bluetooth LE device.
//     *
//     * @param address The device address of the destination device.
//     * @return Return true if the connection is initiated successfully. The connection result
//     * is reported asynchronously through the
//     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
//     * callback.
//     */
//    public boolean connect(final String address) {
//        if (mBluetoothAdapter == null || address == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
//            return false;
//        }
//
//        // Previously connected device.  Try to reconnect.
//        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            ycBleLog.e( "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                return true;
//            } else {
//                return false;
//            }
//        }
//        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        if (device == null) {
//            Log.w(TAG, "Device not found.  Unable to connect.");
//            return false;
//        }
//        // We want to directly connect to the device, so we are setting the autoConnect
//        // parameter to false.
//        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
//        ycBleLog.e( "Trying to create a new connection.");
//        mBluetoothDeviceAddress = address;
//        return true;
//    }
//
//    /**
//     * Disconnects an existing connection or cancel a pending connection. The disconnection result
//     * is reported asynchronously through the
//     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
//     * callback.
//     */
//    public void disconnect() {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.disconnect();
//    }
//
//    /**
//     * After using a given BLE device, the app must call this method to ensure resources are
//     * released properly.
//     */
//    public void close() {
//        if (mBluetoothGatt == null) {
//            return;
//        }
//        mBluetoothGatt.close();
//        mBluetoothGatt = null;
//    }
//
//    /**
//     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
//     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
//     * callback.
//     *
//     * @param characteristic The characteristic to read from.
//     */
//    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.readCharacteristic(characteristic);
//    }
//
//    /**
//     * Enables or disables notification on a give characteristic.
//     *
//     * @param characteristic Characteristic to act on.
//     * @param enabled        If true, enable notification.  False otherwise.
//     */
//    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
//                                                 boolean enabled) {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return false;
//        }
//        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//        UUID uuid = UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
////        Log.e("peng","descriptor:"+descriptor);
////        if (descriptor != null)
//        if (descriptor == null) {
//            return false;
//        }
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        return mBluetoothGatt.writeDescriptor(descriptor);
//
//
//    }
//
//    public boolean setCharacteristicIndication(BluetoothGattCharacteristic characteristic,
//                                               boolean enabled) {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return false;
//        }
//        UUID uuid = UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
//        byte[] value = new byte[6];
//        value[0] = (byte) 0x02;
//        descriptor.setValue(value);
//        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//        return mBluetoothGatt.writeDescriptor(descriptor);
//    }
//
//
//    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
//        try {
//            write_characer_lock.acquire(1);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        if (mBluetoothGatt == null)
//            return false;
//
////        Log.e("peng","gatt1:"+mGattCallback);
//        boolean response = mBluetoothGatt.writeCharacteristic(characteristic);
//        if (!response) {
//            write_characer_lock.release();
//            disconnect();
//        }
//        return response;
//    }
//
//    /**
//     * Retrieves a list of supported GATT services on the connected device. This should be
//     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
//     *
//     * @return A {@code List} of supported services.
//     */
//    public List<BluetoothGattService> getSupportedGattServices() {
//        if (mBluetoothGatt == null) return null;
//
//        return mBluetoothGatt.getServices();
//    }
//
//
//    private void broadcastUpdate(final String action) {
//        Intent intent = new Intent(action);
//        Log.i(TAG, "in " + action);
//        sendBroadcast(intent);
////        Log.e("peng","send broadcast  action："+action);
//    }
//
//
//    private void broadcastUpdate(final String action,
//                                 final BluetoothGattCharacteristic characteristic) {
//        final Intent intent = new Intent(action);
//
//
//        if (UUID_OTA_RX_DAT.equals(characteristic.getUuid())) {
//            final byte[] ota_rx_dat = characteristic.getValue();
//            if (ota_rx_dat != null && ota_rx_dat.length > 0) {
//                intent.putExtra(ARRAY_BYTE_DATA, ota_rx_dat);
//            }
//        } else if (UUID_OTA_RX_CMD.equals(characteristic.getUuid())) {
//            final byte[] ota_rx_cmd = characteristic.getValue();
//            if (ota_rx_cmd != null && ota_rx_cmd.length > 0) {
//                intent.putExtra(ARRAY_BYTE_DATA, ota_rx_cmd);
//            }
//        } else if (UUID_ISP_RX_CMD.equals(characteristic.getUuid())) {
//            byte[] value = characteristic.getValue();
//            if (value != null && value.length > 0) {
//                intent.putExtra(ARRAY_BYTE_DATA, value);
//            }
//        } else if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            int flag = characteristic.getProperties();
//            int format = -1;
//            if ((flag & 0x01) != 0) {
//                format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                ycBleLog.e( "Heart rate format UINT16.");
//            } else {
//                format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                ycBleLog.e( "Heart rate format UINT8.");
//            }
//            final int heartRate = characteristic.getIntValue(format, 1);
//            ycBleLog.e( String.format("Received heart rate: %d", heartRate));
//            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
//        } else if (UUID_RSSI_VALUE.equals(characteristic.getUuid())) {
//            final byte[] data1 = characteristic.getValue();
//            if (data1 != null && data1.length > 0) {
//                final StringBuilder stringBuilder1 = new StringBuilder(data1.length);
//                for (byte byteChar : data1)
//                    stringBuilder1.append(String.format("%d ", byteChar));
//                intent.putExtra(EXTRA_DATA, new String("RSSI = ") + stringBuilder1.toString());
//            }
//        } else if (UUID_TEMPERATURE_MEASUREMENT.equals(characteristic.getUuid())) {
//            final byte[] data1 = characteristic.getValue();
//            if (data1 != null)
//                intent.putExtra(EXTRA_DATA, data1);
//        } else if (UUID_RSSI_CONFIGARATION.equals(characteristic.getUuid())) {
//            final byte[] data1 = characteristic.getValue();
//            if (data1 != null && data1.length > 0) {
//                final StringBuilder stringBuilder1 = new StringBuilder(data1.length);
//                for (byte byteChar : data1)
//                    stringBuilder1.append(String.format("%02x", byteChar));
//                intent.putExtra(EXTRA_DATA, new String("RSSI_CONFIGARATION = 0x") + stringBuilder1.toString());
//            }
//        } else if (UUID_BLUE_RECV_VALUE.equals(characteristic.getUuid())) {
//            final byte[] data1 = characteristic.getValue();
//            if (data1 != null && data1.length > 0) {
//                intent.putExtra(EXTRA_DATA, new String(data1));
//            }
//
//        } else {
//            // For all other profiles, writes the data formatted in HEX.
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//            }
//        }
//        sendBroadcast(intent);
//    }
//
//    private boolean refreshDeviceCache() {
//        if (mBluetoothGatt != null) {
//            try {
//                // 通过反射调用蓝牙refresh方法
//                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
//                Class<? extends BluetoothGatt> gattClass = localBluetoothGatt.getClass();
//                Method method = gattClass.getMethod("refresh", new Class[0]);
//                if (method != null) {
//                    boolean b = ((Boolean) method.invoke(localBluetoothGatt, new Object[0])).booleanValue();
//                    return b;
//                }
//            } catch (Exception e) {
//                Log.i(TAG, e.toString());
//            }
//        }
//        return false;
//    }
//
//}
