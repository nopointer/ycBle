package ycble.runchinaup.ota.absimpl.htx;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.otalib.boads.Constant;
import com.example.otalib.boads.Utils;
import com.example.otalib.boads.WorkOnBoads;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppOTA {


    private Thread mTransThread = null;

    private WorkOnBoads do_work_on_boads;

    private boolean mIsWorcking = false;

    private static final String tag = "ota";

    public BluetoothLeService mBLE;

    public static final int MSG_BURN_APP_SUCCESS = 0x00;
    public static final int MSG_BURN_CFG_SUCCESS = 0x01;
    public static final int MSG_BURN_PATCH_SUCCESS = 0x02;
    public static final int MSG_OTA_COMPLETE = 0X04;
    public static final int MSG_FLASH_EMPTY = 0x05;
    public static final int MSG_HANDS_UP_FAILED = 0x08;
    public static final int MSG_OTA_RESEPONSE = 0x09;
    public static final int MSG_DISCONNECT_BLE = 0x10;
    public static final int MSG1_NO_FILE = 0x11;
    public static final int MSG1_BLE_ERROR = 0x12;


    private BluetoothAdapter mBluetoothAdapter;
    public static boolean mConnected = false;
    private List<BluetoothGattService> BluetoothGattServices;
    public BluetoothGattCharacteristic ota_tx_dat_charac = null;
    public BluetoothGattCharacteristic ota_rx_dat_charac = null;

    public BluetoothGattCharacteristic ota_tx_cmd_charac = null;
    public BluetoothGattCharacteristic ota_rx_cmd_charac = null;

    private String mDeviceAddress;
    private String appFileStringPath;

    public void setmDeviceAddress(String mDeviceAddress) {
        this.mDeviceAddress = mDeviceAddress;
    }

    public void setAppFileStringPath(String appFileStringPath) {
        this.appFileStringPath = appFileStringPath;
    }


    private OTACallback otaCallback = null;

    public void setOtaCallback(OTACallback otaCallback) {
        this.otaCallback = otaCallback;
    }

    protected void startOTA(Context context) {
        //start broadcast-service
        do_work_on_boads = new WorkOnBoads(context, handler);
        Intent i = new Intent(context, BluetoothLeService.class);
        context.startService(i);
        boolean res = context.bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }


    private void loadData() {
        mIsWorcking = true;
        mTransThread = new Thread(new Runnable() {

            @Override
            public void run() {
                int response;
                // 设置为加密传输
                do_work_on_boads.setEncrypt(false);

//                        int readPartresponse = do_work_on_boads.ReadPart(Constant.PATCHTYPE);
//                        do_work_on_boads.ReadPart(Constant.APPTYPE);
//                        do_work_on_boads.ReadPart(Constant.CONFGTYPE);
//
//                        if (readPartresponse < 0) {
//                            Message msg = Message.obtain();
//                            msg.what = 10;
//                            msg.obj = "read part error!";
//                            handler.sendMessage(msg);
//                        }

                byte[] tmp_read;
                Utils op = new Utils();
                try {
                    //do_work_on_boads.app_buf = op.readSDFile(file_path);
                    tmp_read = op.readSDFile(appFileStringPath);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    mIsWorcking = false;
                    return;
                }
                response = do_work_on_boads.LoadBinary(tmp_read, Constant.APPTYPE);
                SendFileRseponse(response);
//                //burn patch
//                // 复位硬件
//                do_work_on_boads.ResetTarget();
//
//                Message msg = Message.obtain();
//                msg.arg1 = MSG_DISCONNECT_BLE;
//                handler.sendMessage(msg);
            }
        });//.start();
        mTransThread.start();
    }


    private void SendFileRseponse(int response) {
//        Log.e("peng", "response:" + response);

        String detail;
        switch (response) {
            case Constant.NORESPONSEERROR:
                detail = "OTA is not response!";
                break;
            case Constant.INVALIDPARAMETERERROR:
                detail = "Send parameter error!";
                break;
            case Constant.FILETOBIGERROR:
                detail = "Too large a file to send!";
                break;
            case Constant.LOADBINARYFILEERROR:
                detail = "Send load binary file error!";
                break;
            case Constant.EXECFORMATERROR:
                detail = "Error sending upgrade package format!";
                break;
            case Constant.USERADDRERROR:
                detail = "User Addr error !";
                break;
            case 0:
                detail = "OTA has been successful!";
                break;
            default:
                detail = "返回值是：" + response;
                break;
        }

//        LogUtils.e("detail:" + detail);
        Message msg = handler.obtainMessage();
        msg.arg1 = MSG_OTA_RESEPONSE;
        msg.obj = detail;
        handler.sendMessage(msg);
        mIsWorcking = false;
    }


    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg == null)
                return;

            Message msg2 = msg;
            Log.e(tag, "handleMessage:MsgWhat===========> " + msg.what);
            if (msg2.what == Constant.MSG_WHAT_READ_PART) {
                int addr = msg2.arg1;
                int length = msg2.arg2;
                switch ((Integer) msg2.obj) {
                    case Constant.APPTYPE:
                        break;
                    case Constant.CONFGTYPE:
                        break;
                    case Constant.PATCHTYPE:
                        break;
                }

//                Log.e("huntersun", "hand addr:" + addr + " length:" + length);
            } else if (msg2.what == 10) {
                if (msg2.obj != null) {
                    String str = (String) msg2.obj;
                }
            }


            switch (msg2.arg1) {
                case MSG_OTA_RESEPONSE:
                    if (msg.obj != null) {
//                        LogUtils.e("=====MSG_OTA_RESEPONSE==>" + MSG_OTA_RESEPONSE);
                        String toaststr = (String) msg.obj;
                        do_work_on_boads.ResetTarget();
                        if (otaCallback != null) {
                            otaCallback.onSuccess();
                        }
                    }
                    break;
                case MSG_BURN_APP_SUCCESS:

                    break;
                case MSG1_NO_FILE:
                    break;
                case Constant.MSG_ARG1_KBS:
                    float kbs = (Float) msg2.obj;
                    Log.e("ota", kbs + "kB/s");
                    break;
                case MSG_DISCONNECT_BLE:
                    break;
                case MSG_BURN_CFG_SUCCESS:

                    break;
                case MSG1_BLE_ERROR:
                    Log.e(tag, "Bluetooth connection failed,Please scan bluetooth agai ");
                    mBLE.disconnect();
                    break;
                case MSG_BURN_PATCH_SUCCESS:

                    break;
                case MSG_FLASH_EMPTY:
                    break;
                case Constant.MSG_ARG1_SEND_OTA_DATA:

                    int pos = 0;
                    int len = msg2.arg2;
                    int tmp = len % 20;
                    byte[] senddat = (byte[]) msg2.obj;
                    boolean res;
                    if (ota_tx_dat_charac == null) {
                        Log.e(tag, " 发数据 OTA has not discover the right character!");
                        return;
                    }
                    for (int i = 0; i < len / 20; i++) {
                        byte[] packet_data = new byte[20];
                        System.arraycopy(senddat, pos, packet_data, 0, 20);

                        if (mConnected == true && mBLE != null) {
                            Log.e("DEBUG_OTA", "ota send lenth:" + packet_data.length);
                            res = ota_tx_dat_charac.setValue(packet_data);
                            ota_tx_dat_charac.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                            res = mBLE.writeCharacteristic(ota_tx_dat_charac);
                            if (!res) {
                                Log.e(tag, "writeCharacteristic() failed!!!");
                                return;
                            }
                            pos = pos + 20;
                        } else {
                            return;
                        }
                    }
                    if (tmp != 0) {
                        byte[] packet_data = new byte[tmp];
                        System.arraycopy(senddat, pos, packet_data, 0, tmp);
                        if (mConnected == true && mBLE != null) {
                            Log.e("peng", "send data:" + Utils.bytesToHexString(packet_data));
                            res = ota_tx_dat_charac.setValue(packet_data);
                            if (!res) {
                                Log.e(tag, "setValue() failed!!!");
                            }
                            ota_tx_dat_charac.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                            res = mBLE.writeCharacteristic(ota_tx_dat_charac);
                            if (!res) {
                                Log.e(tag, "writeCharacteristic() failed!!!");
                                return;
                            }
                        } else {
                            return;
                        }
                    }
                    break;

                case MSG_OTA_COMPLETE:
                    mIsWorcking = false;
                    Log.e(tag, "OTA has done and success!\"");
                    if (mConnected && mBLE != null) {
                        mBLE.disconnect();
                    }
                    break;
                case Constant.MSG_ARG1_PROGRESS_BAR_MAX:
                    int len1 = msg2.arg2;
                    Log.e(tag, "len==>1 " + len1);
                    break;
                case Constant.MSG_ARG1_PROGRESS_BAR_UPDATA:
                    int len2 = msg2.arg2;
                    Log.e(tag, "len==>2 " + len2);
                    if (otaCallback != null) {
                        otaCallback.onProgress(len2);
                    }
                    break;
                case MSG_HANDS_UP_FAILED:
                    Log.e(tag, "Hands up to the boads failed before OTA!");
                    break;
                case Constant.MSG_ARG1_OTA_ENCRPT_KEY_FAILED:
                    Log.e(tag, "OTA exchange key please try again");
                    mBLE.disconnect();
                    break;
                case Constant.MSG_ARG1_SEND_OTA_CMD: {
                    int pos1 = 0;
                    int len3 = msg2.arg2;
                    int tmp1 = len3 % 20;
                    byte[] sendcmd = (byte[]) msg2.obj;
                    boolean res1 = false;
                    if (ota_tx_cmd_charac == null) {
                        Log.e(tag, "OTA has not discover the right character!");
                        return;
                    }
                    for (int i = 0; i < len3 / 20; i++) {
                        byte[] packet_data = new byte[20];
                        System.arraycopy(sendcmd, pos1, packet_data, 0, 20);
                        if (mConnected == true && mBLE != null) {
                            ota_tx_cmd_charac.setValue(packet_data);
                            res1 = mBLE.writeCharacteristic(ota_tx_cmd_charac);
                            if (!res1) {
                                Log.e(tag, "writeCharacteristic() failed!!!");
                                return;
                            }
                            pos1 = pos1 + 20;
                            Log.e(tag, packet_data.toString());
                        } else {
                            return;
                        }
                    }

                    if (tmp1 != 0) {
                        byte[] packet_data = new byte[tmp1];
                        System.arraycopy(sendcmd, pos1, packet_data, 0, tmp1);
                        if (mConnected == true && mBLE != null) {

                            Log.e("peng", "cmd data:" + Utils.bytesToHexString(packet_data));
                            boolean b = ota_tx_cmd_charac.setValue(packet_data);
                            if (!b) {
                                Log.e(tag, "setValue failed!");
                                return;
                            }
                            res1 = mBLE.writeCharacteristic(ota_tx_cmd_charac);
                            if (!res1) {
                                Log.e(tag, "writeCharacteristic() failed!!!");
                                return;
                            }
                            Log.e(tag, packet_data.toString());
                        } else {
                            return;
                        }
                    }
                }
                break;
            }
        }
    };


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.e("peng", "disconnection");
                if (mIsWorcking) {
                    Log.e("tag", "The connection is lost while OTA is working!");
                    mIsWorcking = false;
                }
            } else if (BluetoothLeService.ACTION_GATT_STATUS_133.equals(action)) {
                if (mBluetoothAdapter != null) {
                    mBluetoothAdapter.disable();
                    Log.e("tag", "Bluetooth connection status is 133,reset the bluetooth now,please wait");
                    mBluetoothAdapter.enable();
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                BluetoothGattServices = mBLE.getSupportedGattServices();

                Log.e(tag, "已经连接上了 扫描服务: ");
                if (BluetoothGattServices == null) return;
                String uuid = null;
                final Message msg = Message.obtain();
                // Loops through available GATT Services.
                for (BluetoothGattService gattService : BluetoothGattServices) {

                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

                    // Loops through available Characteristics.
                    for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                        Log.e(tag, "gattCharacteristic: " + gattCharacteristic.getUuid());

                        if (gattCharacteristic.getUuid().toString().contains("00002")) {
                            continue;
                        }

                        //charas.add(gattCharacteristic);
                        if (SampleGattAttributes.otas_tx_dat_uuid.equals(gattCharacteristic.getUuid().toString())) {
                            ota_tx_dat_charac = gattCharacteristic;
                            Log.e(tag, "不为空吧？: " + ota_tx_dat_charac);
                        } else if (SampleGattAttributes.otas_rx_dat_uuid.equals(gattCharacteristic.getUuid().toString())) {
                            ota_rx_dat_charac = gattCharacteristic;
                            if ((ota_rx_dat_charac.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean res = mBLE.setCharacteristicNotification(gattCharacteristic, true);
                                        if (res) {
                                            Log.e(tag, "Set Notify Success");

                                        } else {
                                            msg.arg1 = MSG1_BLE_ERROR;
                                            handler.sendMessage(msg);
                                            Log.e(tag, "Notify failed!!");
                                            return;
                                        }
                                    }
                                }, 500);


                            }
                        } else if (SampleGattAttributes.otas_rx_cmd_uuid.equals(gattCharacteristic.getUuid().toString())) {

                            ota_rx_cmd_charac = gattCharacteristic;
                            if ((ota_rx_cmd_charac.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {

                                boolean res = mBLE.setCharacteristicNotification(
                                        gattCharacteristic, true);
                                if (res) {
                                    Log.e(tag, "Set Notify Success");

                                } else {
                                    msg.arg1 = MSG1_BLE_ERROR;
                                    handler.sendMessage(msg);
                                    Log.e(tag, "Notify failed!!");
                                    return;
                                }
                            }

                        } else if (SampleGattAttributes.otas_tx_cmd_uuid.equals(gattCharacteristic.getUuid().toString())) {
                            ota_tx_cmd_charac = gattCharacteristic;

                        } else if (SampleGattAttributes.otas_tx_ips_cmd_uuid.equals(gattCharacteristic.getUuid().toString())) {
                            ota_tx_cmd_charac = gattCharacteristic;

                        } else if (SampleGattAttributes.otas_rx_ips_cmd_uuid.equals(gattCharacteristic.getUuid().toString())) {
                            ota_rx_cmd_charac = gattCharacteristic;
                            if (ota_rx_cmd_charac.getProperties() == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                                boolean res = mBLE.setCharacteristicNotification(
                                        gattCharacteristic, true);
                                if (res) {
                                    Log.e(tag, "Set Notify Success");
                                } else {
                                    msg.arg1 = MSG1_BLE_ERROR;
                                    handler.sendMessage(msg);
                                    Log.e(tag, "Notify failed!!");
                                    return;
                                }
                            }
                        }
                    }
                }
                //ShowGattServices(mBLE.getSupportedGattServices());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                    }
                }, 1000);

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //read peer char
                intent.getStringExtra(BluetoothLeService.EXTRA_DATA);

            } else if (BluetoothLeService.ACTION_GATT_CHARACTER_NOTIFY.equals(action)) {
                //intent.get
                intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            }
            // HS OTA
            else if (BluetoothLeService.OTA_RX_DAT_ACTION.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.ARRAY_BYTE_DATA);
//                Log.e("peng", "DATA notify:" + Utils.bytesToHexString(data));

                if (data != null) {
                    do_work_on_boads.setBluetoothNotifyData(data, Constant.DATCHARC);
                }
            }
            //OTA CMD
            else if (BluetoothLeService.OTA_RX_CMD_ACTION.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.ARRAY_BYTE_DATA);
                Log.e("peng", "CMD notify:" + Utils.bytesToHexString(data));
                if (data != null) {
                    do_work_on_boads.setBluetoothNotifyData(data, Constant.CMDCHARC);
                }
            } //OTA isp CMD
            else if (BluetoothLeService.OTA_RX_ISP_CMD_ACTION.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.ARRAY_BYTE_DATA);
                Log.e("peng", "ISP notify:" + Utils.bytesToHexString(data));
                do_work_on_boads.EntryIspMoudle(Constant.MSG_ARG1_ENTRY_ISP);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mBLE != null) {
                            mBLE.disconnect();
                        }
                    }
                }, 500);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CHARACTER_NOTIFY);
        intentFilter.addAction(BluetoothLeService.OTA_RX_CMD_ACTION);
        intentFilter.addAction(BluetoothLeService.OTA_RX_DAT_ACTION);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_STATUS_133);
        intentFilter.addAction(BluetoothLeService.OTA_RX_ISP_CMD_ACTION);
        return intentFilter;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLE = ((BluetoothLeService.LocalBinder) service).getService();
            Log.e(tag, "in onServiceConnected!!!");
            if (!mBLE.initialize()) {
                Log.e(tag, "Unable to initialize Bluetooth");
            }
            mBLE.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLE = null;
        }
    };

}
