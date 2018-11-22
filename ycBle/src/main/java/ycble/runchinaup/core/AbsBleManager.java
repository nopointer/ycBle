package ycble.runchinaup.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import ycble.runchinaup.core.callback.BleConnCallback;
import ycble.runchinaup.device.BleDevice;
import ycble.runchinaup.exception.BleUUIDNullException;
import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.util.BleUtil;

import static ycble.runchinaup.BleCfg.npBleTag;

/**
 * Created by nopointer on 2018/7/17.
 * 具体操作蓝牙交互的管理器
 */

public abstract class AbsBleManager implements ConnScanListener {

    /**
     * mac地址的正则匹配表达式
     */
    private static final String strMacRule = "^[A-F0-9]{2}(:[A-F0-9]{2}){5}$";

    private static Context mContext = null;
    //蓝牙抽象的管理器

    //蓝牙扫描器
    public BleScaner myBleScaner = BleScaner.getBleScaner();

    private AbsBleConnManger absBleConnManger = null;

    protected static void initSDK(Context context) {
        mContext = context;
        BleScaner.initSDK(context);
    }

    //初始化
    protected void init() {
        absBleConnManger = new AbsBleConnManger(mContext);
        absBleConnManger.setAbsBleConnAndStateCallback(absBleConnCallback, bleStateListener);
    }

    //初始化
    protected void init(UUID... mustUUIDs) {
        absBleConnManger = new AbsBleConnManger(mContext);
        if (mustUUIDs != null) {
            for (UUID uuid : mustUUIDs) {
                absBleConnManger.addMustUUID(uuid);
            }
        }
        absBleConnManger.setAbsBleConnAndStateCallback(absBleConnCallback, bleStateListener);
    }

    //重发次数
    protected int cfgResendCount = 3;
    //单包数据的响应超时时间 毫秒为单位，默认800毫秒
    protected int cfgTimeOutSinglePkgMilli = 800;
    //多包数据的响应超时时间 毫秒为单位，默认3000毫秒
    protected int cfgTimeOutMultiPkgMilli = 3000;

    //蓝牙开关是否是打开的
    public boolean isBLeEnabled() {
        return myBleScaner.isEnabled();
    }

    //任务是否已经结束了
    private static boolean isTaskFinish = false;
    //时序任务队列
    private List<BleUnitTask> bleUnitTaskList = new ArrayList<>();
    //当前时序的序号
    private static int bleUnitTaskIndex = -1;
    //任务队列的长度
    private static int bleTaskSize = 0;
    //蓝牙连接结果的回调
    private HashSet<BleConnCallback> bleBleConnCallbackHashSet = new HashSet();
    //是否调用了连接方法，避免多次调用连接方法
    private boolean isConnectIng = false;

    /**
     * 是否是ota模式，如果是手动断开的话，再执行ota流程是没有问题的，
     * 但是有的设备是写个指令让设备进入OTA模式，此时会自动断开连接，
     * 会被误认为是连接异常，由于有重连机制，会对ota有干扰，因此在ota之前告诉设备要进行ota,暂时不需要重连
     */
    private boolean isOTAMode = false;

    public void setOTAMode(boolean OTAMode) {
        isOTAMode = OTAMode;
    }

    public boolean isOTAMode() {
        return isOTAMode;
    }

    //当前的连接状态
    private BleConnState bleConnState = null;

    public BleConnState getBleConnState() {
        return bleConnState;
    }

    //注册连接回调
    public void registerConnCallback(BleConnCallback connCallback) {
        if (!bleBleConnCallbackHashSet.contains(connCallback)) {
            bleBleConnCallbackHashSet.add(connCallback);
        }
    }

    //注销连接回调
    public void unRegisterConnCallback(BleConnCallback connCallback) {
        if (bleBleConnCallbackHashSet.contains(connCallback)) {
            bleBleConnCallbackHashSet.remove(connCallback);
        }
    }

    //是否是处于连接中
    protected boolean isConn = false;
    //连接之前 先扫描设备，指定时间内没有扫描到设备的话，这个设备不在附近，提示不连接
    /**
     * 连接的设备mac地址
     */
    private String connMac = null;
    /**
     * 是否有扫描到这个设备
     */
    private boolean hasScanDevice = false;

    public String getMac() {
        if (absBleConnManger == null || absBleConnManger.getBluetoothGatt() == null) {
            return "";
        }
        return absBleConnManger.getBluetoothGatt().getDevice().getAddress();
    }

    /**
     * 先扫描，扫描到设备以后再连接设备，如果在指定的时间没有扫描到设备，就不再扫描了，认为设备不在附近
     *
     * @param mac           过滤的mac地址
     * @param timeOutSecond 扫描超时时间 单位秒，0 表示一直扫描
     */
    public void scanAndConn(final String mac, int timeOutSecond) {
        if (!isBLeEnabled()) {
            ycBleLog.e("蓝牙没有打开呢！");
            return;
        }
        if (isConn) {
            ycBleLog.e("已经是连接的，，不需要花里胡哨的了");
            myBleScaner.stopScanForConn();
            return;
        }
        if (TextUtils.isEmpty(mac) || !mac.matches(strMacRule)) {
            ycBleLog.e("mac地址都不对,地址要注意大写,且不能为空！！！！！");
            return;
        }
        if (!TextUtils.isEmpty(connMac) && !mac.equals(connMac)) {
            ycBleLog.e("连接新的设备之前，需先调用断开指令");
            return;
        }

        if (isOTAMode) {
            ycBleLog.e("醒醒吧 现在是在OTA模式下");
            return;
        }

        connMac = mac;
        ycBleLog.reCreateLogFile(mac);
        BleStateReceiver.getStateReceiver().setListenerMac(connMac);
        BluetoothDevice bluetoothDevice = AbsBleConnManger.isInConnList(mac, mContext);
        ycBleLog.e("debug===先判断 当前蓝牙设备是不是在其他的app中连接了");
        if (bluetoothDevice != null) {
            ycBleLog.e("debug===还真被其他应用连接了,那就简单了,直接去拿连接过来就是了");
            connWithSysConn(bluetoothDevice);
            return;
        }
        ycBleLog.e("debug===扫描然后再连接我的设备===>超时时间:" + timeOutSecond);
        hasScanDevice = false;
        myBleScaner.setScanForMyDeviceMac(mac);
        myBleScaner.setConnScanListener(this);
        withBleConnState(BleConnState.SEARCH_ING);
        myBleScaner.startScanForConn();
        if (timeOutSecond == 0) return;
        handler.sendEmptyMessageDelayed(MSG_AFTER_SCAN_TIMEOUT, timeOutSecond * 1000);
    }

    //连接设备
    public void connDevice(final String mac) {
        privateConnnect(mac);
    }

    //连接系统连接中的设备
    private void connWithSysConn(BluetoothDevice bluetoothDevice) {
        if (absBleConnManger != null) {
            absBleConnManger.connect(bluetoothDevice);
        }
    }

    //断开连接
    public void disConn() {
        clearSomeFlag();
        connMac = null;
        myBleScaner.setScanForMyDeviceMac(null);
        myBleScaner.stopScanForConn();
        isConnectIng = false;
        if (absBleConnManger != null) {
            absBleConnManger.disConnect();
        } else {
            ycBleLog.e("==absBleConnManger 为 null");
        }
    }

    /**
     * 在操作数据之前，把数据提前拿到，就在此处做超时管理
     * 特殊的指令处理,这里用来标识响应包数据是多包还是单包数据
     *
     * @param unitTask
     */
    private final void insertBeforeWrite(BleUnitTask unitTask) {
        collectData(specialCommand(unitTask), unitTask);
    }

    /**
     * 对于特殊的数据，要特殊处理，防止打乱任务队列
     *
     * @param bleUnitTask
     * @return 此处的返回值，是用来告知设备，他的响应数据是单条 还是多条
     */
    protected boolean specialCommand(BleUnitTask bleUnitTask) {
        return false;
    }


    /**
     * 超时的指令
     *
     * @param command
     */
    public void onResponseTimeOut(byte[] command) {

    }

    //读取数据
    public final boolean readData(UUID U_ser, UUID U_chara) throws BleUUIDNullException {
        if (absBleConnManger != null) {
            return absBleConnManger.readData(U_ser, U_chara);
        }
        return false;
    }

    //读取描述符数据
    public final boolean readData(UUID U_ser, UUID U_chara, UUID U_descr) throws BleUUIDNullException {
        if (absBleConnManger != null) {
            return absBleConnManger.readData(U_ser, U_chara, U_descr);
        }
        return false;
    }

    //写数据
    public final boolean writeData(UUID U_ser, UUID U_chara, byte[] data) throws BleUUIDNullException {
        if (absBleConnManger != null) {
            insertBeforeWrite(BleUnitTask.createWrite(U_ser, U_chara, data, "写数据"));
            return absBleConnManger.writeData(U_ser, U_chara, data);
        }
        return false;
    }

    //写描述符数据
    public final boolean writeData(UUID U_ser, UUID U_chara, UUID U_descr, byte[] data) throws BleUUIDNullException {
        if (absBleConnManger != null) {
            insertBeforeWrite(BleUnitTask.createWrite(U_ser, U_chara, data, "写数据"));
            return absBleConnManger.writeData(U_ser, U_chara, U_descr, data);
        }
        return false;
    }

    //无响应数据
    protected final boolean writeDataWithoutResp(UUID U_ser, UUID U_chara, byte[] data) throws BleUUIDNullException {
        if (absBleConnManger != null) {
            insertBeforeWrite(BleUnitTask.createWriteWithOutResp(U_ser, U_chara, data, "无响应数据"));
            return absBleConnManger.writeDataWithOutResponse(U_ser, U_chara, data);
        }
        return false;
    }

    //使能通知
    protected final boolean enableNotity(UUID U_ser, UUID U_chara) throws BleUUIDNullException {
        if (absBleConnManger != null) {
            insertBeforeWrite(BleUnitTask.createEnableNotify(U_ser, U_chara, "使能通知"));
            return absBleConnManger.enableNotity(U_ser, U_chara);
        }
        return false;
    }

    //使能指示
    protected final boolean enableIndication(UUID U_ser, UUID U_chara) throws BleUUIDNullException {
        if (absBleConnManger != null) {
            insertBeforeWrite(BleUnitTask.createEnableIndicate(U_ser, U_chara, "使能指示"));
            return absBleConnManger.enableIndication(U_ser, U_chara);
        }
        return false;
    }

    //使不能通知/指示
    public final boolean disAbleNotityOrIndication(UUID U_ser, UUID U_chara) throws BleUUIDNullException {
        if (absBleConnManger != null) {
            insertBeforeWrite(BleUnitTask.createDisEnableNotifyOrIndicate(U_ser, U_chara, "使不能通知/指示"));
            return absBleConnManger.disAbleNotityOrIndication(U_ser, U_chara);
        }
        return false;
    }

    public final boolean isConn() {
        return isConn;
    }

    /**
     * 清除某些状态，比如任务队列,和上次的消息队列
     */
    private final void clearSomeFlag() {
        bleUnitTaskList.clear();
        bleUnitTaskIndex = -1;
        bleTaskSize = 0;
        handler.removeCallbacksAndMessages(null);
    }


    private void withOnConnException() {
        clearSomeFlag();
        withBleConnState(BleConnState.CONNEXCEPTION);
    }


    /**
     * 系统的蓝牙打开
     */
    public void onBleOpen() {
    }

    //注册连接成功回调事件
    private final void withOnConnectSuccess() {
        clearSomeFlag();
        withBleConnState(BleConnState.CONNECTED);
    }


    //注册手动断开回调事件
    public void withOnHandDisConn() {
        clearSomeFlag();
        withBleConnState(BleConnState.HANDDISCONN);
    }

    private final void withBleConnState(BleConnState connState) {
        this.bleConnState = connState;
        for (BleConnCallback connCallback : bleBleConnCallbackHashSet) {
            connCallback.onConnState(connState);
        }
    }

    //系统蓝牙异常
    public void onPhoneBleStateException() {
        clearSomeFlag();
    }

    //数据交互异常
    protected void onException(ErrCode errCode, String msg, UUID... uuids) {

    }


    protected void addBleUnitTask(BleUnitTask bleUnitTask) {
        if (bleUnitTaskList == null) {
            bleUnitTaskList = new ArrayList<>();
        }
        bleUnitTaskList.add(bleUnitTask);
    }


    public void onDataRead(byte[] data, boolean isChara, UUID... uuid) {
    }

    public void onDataWrite(byte[] data, boolean isChara, UUID... uuid) {
    }

    public void onRssi(int rssi) {

    }

    /**
     * 处理连接后的时序任务
     */
    private final synchronized void handWithCfg() {
        bleUnitTaskIndex = -1;
        isTaskFinish = false;
        bleTaskSize = bleUnitTaskList.size();
        //如果有任务队列，就执行任务队列
        if (bleUnitTaskIndex < bleTaskSize) {
            toNextTask();
        } else {
            //如果没有任务队列，直接直接结束任务
            setTaskFinish();
        }
    }


    protected synchronized void taskSuccess(int time) {
        clearTimeOutHandler(false);
        //如果没有结束任务
        if (isConn()) {
            //如果是连接中的
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toNextTask();
                }
            }, time);
        }
    }

    protected synchronized void taskSuccess() {
        taskSuccess(0);
    }

    //执行下一次任务
    private synchronized void toNextTask() {
        if (absBleConnManger == null) return;
        bleTaskSize = bleUnitTaskList.size();
        bleUnitTaskIndex++;
        if (bleUnitTaskIndex < bleTaskSize) {
            ycBleLog.e("ble===task====>" + (bleUnitTaskIndex + 1) + "/" + bleTaskSize);
            BleUnitTask unitTask = bleUnitTaskList.get(bleUnitTaskIndex);
            if (unitTask == null) return;
            try {
                switch (unitTask.getOptionType()) {
                    case BleUnitTask.TYPE_READ: {
                        ycBleLog.d(npBleTag + "读数据<<<< " + unitTask.msg);
                        readData(unitTask.getU_service(), unitTask.getU_chara());
                    }
                    break;
                    case BleUnitTask.TYPE_WRITE: {
                        ycBleLog.d(npBleTag + "写数据<<<< " + unitTask.msg);
                        writeData(unitTask.getU_service(), unitTask.getU_chara(), unitTask.getData());
                    }
                    break;
                    case BleUnitTask.TYPE_WRITE_WITHOUT_RESP: {
                        ycBleLog.d(npBleTag + "无响应写数据<<<< " + unitTask.msg);
                        writeDataWithoutResp(unitTask.getU_service(), unitTask.getU_chara(), unitTask.getData());
                    }
                    break;
                    case BleUnitTask.TYPE_ENABLE_NOTIFY: {
                        ycBleLog.d(npBleTag + "打开通知<<<< " + unitTask.msg);
                        enableNotity(unitTask.getU_service(), unitTask.getU_chara());
                    }
                    break;
                    case BleUnitTask.TYPE_ENABLE_INDICATE: {
                        ycBleLog.d(npBleTag + "打开指示<<<< " + unitTask.msg);
                        unitTask.setData(new byte[]{0x02, 0x00});
                        enableIndication(unitTask.getU_service(), unitTask.getU_chara());
                    }
                    break;
                    case BleUnitTask.TYPE_DISABLE_NOTIFY_OR_INDICATE: {
                        ycBleLog.d(npBleTag + " 关闭通知或者指示<<<< " + unitTask.msg);
                        disAbleNotityOrIndication(unitTask.getU_service(), unitTask.getU_chara());
                    }
                    break;
                }

            } catch (BleUUIDNullException e) {
                e.printStackTrace();
                ycBleLog.e("debug===如果没有找到设备的相关通道,继续执行，不能中断在这里");
                toNextTask();
            }
        } else {
            //连接时任务结束
            setTaskFinish();
        }

    }

    /**
     * 设置任务已经结束
     */
    private final void setTaskFinish() {
        if (!isTaskFinish) {
            clearSomeFlag();
            ycBleLog.e("连接后的时序指令下发完成，可以自由交互数据了");
            onFinishTaskAfterConn();
            isTaskFinish = true;
        }
    }

    //内部连接
    private void privateConnnect(String mac) {
        if (isConnectIng) {
            ycBleLog.e("ble-当前已经发出了连接请求，还没响应，不需要再发送这次请求");
            return;
        }
        withBleConnState(BleConnState.CONNECTING);
        absBleConnManger.connect(mac);
        isConnectIng = true;
    }


    private AbsBleConnManger.AbsBleConnCallback absBleConnCallback = new AbsBleConnManger.AbsBleConnCallback() {

        @Override
        protected void connResult(BleConnState connResult) {
            isConnectIng = false;
            isConn = connResult == BleConnState.CONNECTED;
            ycBleLog.e("连接结果==>connResult==>" + connResult + "=isConn=>" + isConn);

            //连接上的情况
            if (connResult == BleConnState.CONNECTED) {
                //连接成功状态
                withOnConnectSuccess();
                onConnectSuccess();
            } else {
                //非连接的情况就比较多了，可能是连接失败，可能是连接异常，可能是手动断开

                //这是个比较另类的问题，系统的蓝牙挂壁了，为小概率事件，且此处的手机型号收集并不完整
                if (connResult == BleConnState.PHONEBLEANR) {
                    onPhoneBleStateException();
                } else if (connResult == BleConnState.CONNEXCEPTION) {
                    withOnConnException();
                    onConnException();
                } else if (connResult == BleConnState.HANDDISCONN) {
                    withOnHandDisConn();
                    onHandDisConn();
                }
            }
        }


        /**
         * 在检测到service后，可以执行的操作
         * @param gatt
         */
        @Override
        protected void onLoadCharas(BluetoothGatt gatt) {
            //先写数据下去 (写的一些配置信息)
            clearSomeFlag();
            loadCfg();
            handWithCfg();
        }

        @Override
        protected void onException(ErrCode errCode, String msg, UUID... uuids) {
            super.onException(errCode, msg, uuids);
            AbsBleManager.this.onException(errCode, msg, uuids);
        }

        @Override
        protected void onDataRead(boolean isCharacteristic, byte[] data, UUID... uuid) {
            super.onDataRead(isCharacteristic, data, uuid);
            AbsBleManager.this.onDataRead(data, isCharacteristic, uuid);
        }

        @Override
        protected void onDataChange(UUID uuid, byte[] data) {
            super.onDataChange(uuid, data);
            AbsBleManager.this.onDataReceive(data, uuid);
        }

        @Override
        protected void onDataWrite(boolean isCharacteristic, byte[] data, UUID... uuid) {
            super.onDataWrite(isCharacteristic, data, uuid);
            AbsBleManager.this.onDataWrite(data, isCharacteristic, uuid);
        }

        @Override
        protected void onRssi(int rssi) {
            super.onRssi(rssi);
            AbsBleManager.this.onRssi(rssi);
        }

    };

    //=======================================================================
    //=======================================================================
    //=====状态接收器=========================================================
    //=======================================================================
    //=======================================================================
    private BleStateReceiver.BleStateListener bleStateListener = new BleStateReceiver.BleStateListener() {
        @Override
        public void onBleState(BleStateReceiver.SystemBluetoothState systemBluetoothState, BluetoothDevice bluetoothDevice) {

            if (isOTAMode()) {
                ycBleLog.e("当前是OTA状态,打印一下状态就好了" + systemBluetoothState + "-" + bluetoothDevice.getAddress());

            }

            if (systemBluetoothState == BleStateReceiver.SystemBluetoothState.StateOffBle) {
                ycBleLog.e("debug===蓝牙关闭，也算手动断开===>");
                isConnectIng = false;
                clearSomeFlag();
                timeOutHandler.removeCallbacksAndMessages(null);
                timeOutHelperHashMap.clear();
                withOnHandDisConn();
                onHandDisConn();
            } else if (systemBluetoothState == BleStateReceiver.SystemBluetoothState.StateDisConn) {
                isConn = false;
                isConnectIng = false;
                ycBleLog.e("debug==>蓝牙断开了，可能是手动的也有可能是异常断开");
                clearSomeFlag();
                timeOutHandler.removeCallbacksAndMessages(null);
                timeOutHelperHashMap.clear();
                if (absBleConnManger.isHandDisConn()) {
                    ycBleLog.e("debug===手动断开连接");
                    withOnHandDisConn();
                    onHandDisConn();
                } else {
                    ycBleLog.e("debug===设备异常断开");
                    withOnConnException();
                    onConnException();
                }
                //已经断开了，回调已经给了，修改手动断开的标志位为false
                absBleConnManger.setHandDisConn(false);
            } else if (systemBluetoothState == BleStateReceiver.SystemBluetoothState.StateOnBle) {
                isConnectIng = false;
                ycBleLog.e("debug===系统蓝牙打开了");
                timeOutHandler.removeCallbacksAndMessages(null);
                timeOutHelperHashMap.clear();
                onBleOpen();
            }
        }
    };

    //=======================================================================
    //=======================================================================
    //=====超时处理机制=======================================================
    //=======================================================================
    //=======================================================================


    private Handler timeOutHandler = new Handler();
    //超时处理器
    private HashMap<String, TimeOutHelper> timeOutHelperHashMap = new HashMap<>();


    /**
     * 延时处理函数,在写数据的时候，防止某条指令没有响应，可以重复写几次，如果几次后还是没有响应 ，那就跳过这个数据
     *
     * @param isMultiPkgResponse 是否会有多个响应包数据 true 代表有多包响应，false,表示只有一包数据响应
     * @param unitTask
     */
    private synchronized void collectData(boolean isMultiPkgResponse, final BleUnitTask unitTask) {
        final byte[] writeData = unitTask.getData();
        ycBleLog.e("debug===准备超时的数据>" + BleUtil.byte2HexStr(writeData) + "===>isMultiPgResponse:" + isMultiPkgResponse);
        final String key = BleUtil.byte2HexStr(writeData);
        if (!timeOutHelperHashMap.containsKey(key)) {
            TimeOutHelper timeOutHelper = new TimeOutHelper(key, isMultiPkgResponse ? cfgTimeOutMultiPkgMilli : cfgTimeOutSinglePkgMilli);
            timeOutHelperHashMap.put(key, timeOutHelper);
        }
        TimeOutHelper timeOutHelper = timeOutHelperHashMap.get(key);
        timeOutHelper.reSendCount++;
        if (timeOutHelper.reSendCount < cfgResendCount) {
            timeOutHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ycBleLog.e("debug====" + BleUtil.byte2HexStr(writeData) + "指令失败，重新下发");
                    try {
                        if (unitTask.getOptionType() == BleUnitTask.TYPE_ENABLE_NOTIFY) {
                            enableNotity(unitTask.getU_service(), unitTask.getU_chara());
                        } else if (unitTask.getOptionType() == BleUnitTask.TYPE_WRITE) {
                            writeData(unitTask.getU_service(), unitTask.getU_chara(), writeData);
                        }
                    } catch (BleUUIDNullException e) {
                        e.printStackTrace();
                    }
                }
            }, timeOutHelper.getMilliSecond());
        } else {
            timeOutHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    timeOutHelperHashMap.clear();
                    clearTimeOutHandler(true);
                    onResponseTimeOut(writeData);
                    if (!isTaskFinish) {
                        bleTaskSize = bleUnitTaskList.size();
                        if (bleUnitTaskIndex < bleTaskSize) {
                            toNextTask();
                        }
                    }
                }
            }, timeOutHelper.getMilliSecond());
        }
    }


    //清除超时机制的处理
    private void clearTimeOutHandler(boolean isTimeOut) {
        if (!isTimeOut) {
            ycBleLog.e("收到了数据，移除超时延时处理==>");
        } else {
            ycBleLog.e("没有收到响应指令,说明已经超时了==>");
        }
        timeOutHandler.removeCallbacksAndMessages(null);
    }


    @Override
    public void scanMyDevice(final BleDevice bleDevice) {
        if (bleDevice == null) return;
        if (TextUtils.isEmpty(connMac)) return;
        ycBleLog.e("扫描到了设备,handler里面发送消息==>" + bleDevice.toString());
        handler.sendMessage(handler.obtainMessage(MSG_SCAN_DEVICE, bleDevice));
    }


    private synchronized void handScanDevice(final BleDevice bleDevice) {
        if (bleDevice.getMac().equals(connMac)) {
            if (!hasScanDevice) {
                hasScanDevice = true;
                myBleScaner.stopScanForConn();
                handler.removeMessages(MSG_SCAN_DEVICE);
                myBleScaner.setConnScanListener(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connDevice(connMac);
                    }
                }, 1200);
            }
        }
    }


    /**
     * task任务完成
     */
    static final int MSG_TASK_SUCCESS = 0x01;

    /**
     * 扫描到设备了
     */
    static final int MSG_SCAN_DEVICE = 0x02;

    /**
     * 扫描的超时时间到了，处理超时，如果超时时间为0 就不会回调此处
     */
    static final int MSG_AFTER_SCAN_TIMEOUT = 0x03;

    /**
     * 延时处理机制，或者
     */
    private final Handler handler = new Handler() {
        @Override
        public synchronized void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //扫描到设备了，处理是不是我的设备
                case MSG_SCAN_DEVICE: {
                    handScanDevice((BleDevice) msg.obj);
                }
                break;
                //执行扫描超时后的时序
                case MSG_AFTER_SCAN_TIMEOUT: {
                    //担心会有在结束扫描的时候，同时又扫描到设备了，移除扫描到的消息
                    handler.removeMessages(MSG_SCAN_DEVICE);
                    if (!hasScanDevice) {
                        myBleScaner.setConnScanListener(null);
                        myBleScaner.stopScanForConn();
                        ycBleLog.e("设备都不在附近 你连接个锤子，连接失败");
                        withBleConnState(BleConnState.CONNFAILURE);
                    }
                }
                break;
            }
        }
    };

    //=======================================================================
    //=======================================================================
    //=====不用看了,下面全是回调===============================================
    //=======================================================================
    //=======================================================================

    /**
     * 执行完连接后的时序任务回调
     */
    public abstract void onFinishTaskAfterConn();

    /**
     * 数据接收回调
     *
     * @param data
     * @param uuid
     */
    public abstract void onDataReceive(byte[] data, UUID uuid);

    /**
     * 连接成功的回调
     */
    public abstract void onConnectSuccess();

    /**
     * 手动断开的回调
     */
    public abstract void onHandDisConn();

    /**
     * 处理连接后的时序，用户根据需求添加一系列的同步指令，比如同步时间，打开通知，读取数据等等,不需要的话就不管
     */
    public abstract void loadCfg();

    /**
     * 连接异常回调
     */
    public abstract void onConnException();

}
