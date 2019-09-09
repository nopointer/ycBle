package ycble.runchinaup.ota.absimpl.telink;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.List;
import java.util.UUID;

import ycble.runchinaup.device.BleDevice;
import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.util.BleUtil;

class Device extends Peripheral {

    public static final String TAG = Device.class.getSimpleName();

    public static final UUID SERVICE_UUID = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d1912");

    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d2b12");

    public static final int OTA_START_REQ = 0xFF03;
    public static final int OTA_START_RSP = 0xFF04;
    public static final int OTA_START = 0xFF01;
    public static final int OTA_END = 0xFF02;

    public static final int STATE_SUCCESS = 1;
    public static final int STATE_FAILURE = 0;
    public static final int STATE_PROGRESS = 2;

    private static final int TAG_OTA_WRITE = 1;
    private static final int TAG_OTA_READ = 2;
    private static final int TAG_OTA_LAST = 3;
    private static final int TAG_OTA_LAST_READ = 10;
    private static final int TAG_OTA_PRE_READ = 4;
    private static final int TAG_OTA_START_REQ = 5;
    private static final int TAG_OTA_START_RSP = 6;
    private static final int TAG_OTA_START = 7;
    private static final int TAG_OTA_END = 8;
    private static final int TAG_OTA_ENABLE_NOTIFICATION = 9;

    private final OtaPacketParser mOtaParser = new OtaPacketParser();
    private final OtaCommandCallback mOtaCallback = new OtaCommandCallback();

    private Callback mCallback;

    private boolean isReadSupport = true;
    private long delay = 20;

    public Device(BluetoothDevice device, byte[] scanRecord, int rssi) {
        super(device, scanRecord, rssi);
    }

    public Device(BleDevice device) {
        super(device);
    }

    @Override
    public void connect(Context context) {
        super.connect(context);
        isReadSupport = SharedPreferencesHelper.getReadSupport(context);
        delay = SharedPreferencesHelper.getPktDelay(context);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    protected void onConnect() {
        super.onConnect();
        if (mCallback != null) {
            mCallback.onConnected(this);
        }
    }

    @Override
    protected void onDisconnect() {
        super.onDisconnect();
        resetOta();
        if (mCallback != null) {
            mCallback.onDisconnected(this);
        }
    }

    @Override
    protected void onServicesDiscovered(List<BluetoothGattService> services) {
        super.onServicesDiscovered(services);
        //this.enablePcmNotification();
        if (mCallback != null) {
            mCallback.onServicesDiscovered(this);
        }
    }

    @Override
    protected void onNotify(byte[] data, UUID serviceUUID, UUID characteristicUUID, Object tag) {
        super.onNotify(data, serviceUUID, characteristicUUID, tag);
        ycBleLog.e( " onNotify ==> " + BleUtil.byte2HexStr(data, ":"));
    }


    protected void onOtaSuccess() {
        if (mCallback != null) {
            mCallback.onOtaStateChanged(this, STATE_SUCCESS);
        }
    }

    protected void onOtaFailure() {
        if (mCallback != null) {
            mCallback.onOtaStateChanged(this, STATE_FAILURE);
        }
    }

    protected void onOtaProgress() {
        if (mCallback != null) {
            mCallback.onOtaStateChanged(this, STATE_PROGRESS);
        }
    }

    /********************************************************************************
     * OTA API
     *******************************************************************************/

    public void startOta(byte[] firmware) {

        ycBleLog.d("Start OTA");
        this.resetOta();
        this.mOtaParser.set(firmware);
        //this.enableOtaNotification();
        this.sendOtaStartCommand();
    }

    public int getOtaProgress() {
        return this.mOtaParser.getProgress();
    }

    private void resetOta() {
        this.mDelayHandler.removeCallbacksAndMessages(null);
        this.mOtaParser.clear();
    }

    private void setOtaProgressChanged() {

        if (this.mOtaParser.invalidateProgress()) {
            onOtaProgress();
        }
    }

    private void sendOtaStartReqCommand() {
        Command reqCmd = Command.newInstance();
        reqCmd.serviceUUID = SERVICE_UUID;
        reqCmd.characteristicUUID = CHARACTERISTIC_UUID;
        reqCmd.type = Command.CommandType.WRITE_NO_RESPONSE;
        reqCmd.tag = TAG_OTA_START_REQ;
        reqCmd.data = new byte[]{OTA_START_REQ & 0xFF, (byte) (OTA_START_REQ >> 8 & 0xFF)};
        sendCommand(mOtaCallback, reqCmd);
    }

    // OTA 开始时发送的命令
    private void sendOtaStartCommand() {
        Command startCmd = Command.newInstance();
        startCmd.serviceUUID = SERVICE_UUID;
        startCmd.characteristicUUID = CHARACTERISTIC_UUID;
        startCmd.type = Command.CommandType.WRITE_NO_RESPONSE;
        startCmd.tag = TAG_OTA_START;
        startCmd.data = new byte[]{OTA_START & 0xFF, (byte) (OTA_START >> 8 & 0xFF)};
        sendCommand(mOtaCallback, startCmd);
    }

    private void sendOtaEndCommand() {
        Command endCmd = Command.newInstance();
        endCmd.serviceUUID = SERVICE_UUID;
        endCmd.characteristicUUID = CHARACTERISTIC_UUID;
        endCmd.type = Command.CommandType.WRITE_NO_RESPONSE;
        endCmd.tag = TAG_OTA_END;
        endCmd.data = new byte[]{OTA_END & 0xFF, (byte) (OTA_END >> 8 & 0xFF)};

        sendCommand(mOtaCallback, endCmd);
    }

    private void sendLastReadCommand() {
        Command cmd = Command.newInstance();
        cmd.serviceUUID = SERVICE_UUID;
        cmd.characteristicUUID = CHARACTERISTIC_UUID;
        cmd.type = Command.CommandType.READ;
        cmd.tag = TAG_OTA_LAST_READ;
        this.sendCommand(mOtaCallback, cmd);
    }

    /*private boolean sendNextOtaPacketCommand(int delay) {
        boolean result = false;

        if (this.mOtaParser.hasNextPacket()) {
            Command cmd = Command.newInstance();
            cmd.serviceUUID = SERVICE_UUID;
            cmd.characteristicUUID = CHARACTERISTIC_UUID;
            cmd.type = Command.CommandType.WRITE_NO_RESPONSE;
            cmd.data = this.mOtaParser.getNextPacket();
            cmd.tag = TAG_OTA_WRITE;
            cmd.delay = delay;
            if (this.mOtaParser.isLast()) {
                ycBleLog.d("ota last packet");
                result = true;
                //cmd.tag = TAG_OTA_LAST;
                Command end = Command.newInstance();
                end.serviceUUID = SERVICE_UUID;
                end.characteristicUUID = CHARACTERISTIC_UUID;
                end.type = Command.CommandType.WRITE_NO_RESPONSE;
                end.tag = TAG_OTA_LAST;
                end.delay = 0;
                byte[] endPacket = new byte[6];
                endPacket[0] = 0x02;
                endPacket[1] = (byte) 0xFF;
                endPacket[2] = cmd.data[0];
                endPacket[3] = cmd.data[1];
                endPacket[4] = (byte) (0xFF - cmd.data[0]);
                endPacket[5] = (byte) (0xFF - cmd.data[1]);
                end.data = endPacket;
                this.sendCommand(this.mOtaCallback, cmd);
                this.sendCommand(this.mOtaCallback, end);
            } else {
                this.sendCommand(this.mOtaCallback, cmd);
            }
        }

        return result;
    }*/

    private void sendNextOtaPacketCommand(int delay) {
        if (this.mOtaParser.hasNextPacket()) {
            Command cmd = Command.newInstance();
            cmd.serviceUUID = SERVICE_UUID;
            cmd.characteristicUUID = CHARACTERISTIC_UUID;
            cmd.type = Command.CommandType.WRITE_NO_RESPONSE;
            cmd.data = this.mOtaParser.getNextPacket();
            cmd.tag = TAG_OTA_WRITE;
            cmd.delay = delay;
            this.sendCommand(this.mOtaCallback, cmd);
        } else {
//            sendOTALastCommand();
            sendLastReadCommand();
        }
    }

    private void sendOTALastCommand() {
        Command end = Command.newInstance();
        end.serviceUUID = SERVICE_UUID;
        end.characteristicUUID = CHARACTERISTIC_UUID;
        end.type = Command.CommandType.WRITE_NO_RESPONSE;
        end.tag = TAG_OTA_LAST;
        end.delay = 0;
        byte[] endPacket = new byte[6];
        endPacket[0] = 0x02;
        endPacket[1] = (byte) 0xFF;
        int index = mOtaParser.getCurIndex();
        endPacket[2] = (byte) (index & 0xFF);
        endPacket[3] = (byte) (index >> 8 & 0xFF);
        endPacket[4] = (byte) (0xFF - index & 0xFF);
        endPacket[5] = (byte) (0xFF - (index >> 8 & 0xFF));
        end.data = endPacket;
        this.sendCommand(this.mOtaCallback, end);
    }

    private void enableOtaNotification() {
        Command endCmd = Command.newInstance();
        endCmd.serviceUUID = SERVICE_UUID;
        endCmd.characteristicUUID = CHARACTERISTIC_UUID;
        endCmd.type = Command.CommandType.ENABLE_NOTIFY;
        endCmd.tag = TAG_OTA_ENABLE_NOTIFICATION;
        sendCommand(mOtaCallback, endCmd);
    }

    private boolean validateOta() {
        /**
         * 发送read指令
         */
        int sectionSize = 16 * 4;
        int sendTotal = this.mOtaParser.getNextPacketIndex() * 16;
//        ycBleLog.i("ota onCommandSampled byte length : " + sendTotal);
        if (sendTotal > 0 && sendTotal % sectionSize == 0) {

            if (!isReadSupport) {
                return true;
            }
//            ycBleLog.i("onCommandSampled ota read packet " + mOtaParser.getNextPacketIndex());
            Command cmd = Command.newInstance();
            cmd.serviceUUID = SERVICE_UUID;
            cmd.characteristicUUID = CHARACTERISTIC_UUID;
            cmd.type = Command.CommandType.READ;
            cmd.tag = TAG_OTA_READ;
            this.sendCommand(mOtaCallback, cmd);
            return true;
        }
        return false;
    }

    public interface Callback {
        void onConnected(Device device);

        void onDisconnected(Device device);

        void onServicesDiscovered(Device device);

        void onOtaStateChanged(Device device, int state);
    }

    private final class OtaCommandCallback implements Command.Callback {

        @Override
        public void success(Peripheral peripheral, Command command, Object obj) {
            if (command.tag.equals(TAG_OTA_PRE_READ)) {
                ycBleLog.d("read =========> " + BleUtil.byte2HexStr((byte[]) obj, "-"));
            } else if (command.tag.equals(TAG_OTA_START)) {
                sendNextOtaPacketCommand(0);
            } else if (command.tag.equals(TAG_OTA_END)) {
                // ota success
                resetOta();
                setOtaProgressChanged();
                onOtaSuccess();
            } else if (command.tag.equals(TAG_OTA_LAST)) {
//                sendLastReadCommand();

                sendOtaEndCommand();
                /*resetOta();
                setOtaProgressChanged();
                onOtaSuccess();*/

                // OTA测试时无需发后面两个指令
                /*resetOta();
                setOtaProgressChanged();
                onOtaSuccess();*/
            } else if (command.tag.equals(TAG_OTA_WRITE)) {
                //int delay = 0;
                //if (delay <= 0) {
                /*if (!validateOta()) {
                    sendNextOtaPacketCommand(0);
                } else {
                    sendNextOtaPacketCommand(20);
//                    mDelayHandler.postDelayed(mOtaTask, delay);
                }*/
                if (!validateOta()) {
                    sendNextOtaPacketCommand(0);
                } else {
                    if (!isReadSupport) {
                        sendNextOtaPacketCommand((int) delay);
                    }
                }
                setOtaProgressChanged();
            } else if (command.tag.equals(TAG_OTA_READ)) {
                sendNextOtaPacketCommand(0);
            } else if (command.tag.equals(TAG_OTA_LAST_READ)) {
                sendOTALastCommand();
//                sendLastReadCommand();
            }
        }

        @Override
        public void error(Peripheral peripheral, Command command, String errorMsg) {
            ycBleLog.d("error packet : " + command.tag + " errorMsg : " + errorMsg);
            if (command.tag.equals(TAG_OTA_END)) {
                // ota success
                resetOta();
                setOtaProgressChanged();
                onOtaSuccess();
            } else {
                resetOta();
                onOtaFailure();
            }
        }

        @Override
        public boolean timeout(Peripheral peripheral, Command command) {
            ycBleLog.d("timeout : " + BleUtil.byte2HexStr(command.data, ":"));
            if (command.tag.equals(TAG_OTA_END)) {
                // ota success
                resetOta();
                setOtaProgressChanged();
                onOtaSuccess();
            } else {
                resetOta();
                onOtaFailure();
            }
            return false;
        }
    }
}
