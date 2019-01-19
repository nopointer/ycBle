package lib.ycble.bleModule;

import android.graphics.BitmapFactory;
import android.os.Handler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lib.ycble.MainApplication;
import lib.ycble.R;
import lib.ycble.bleModule.imageTransport.DevImageUtils;
import lib.ycble.bleModule.imageTransport.ReceiveImageDataCallback;
import lib.ycble.bleModule.utils.DevDataUtils;
import ycble.runchinaup.core.AbsBleManager;
import ycble.runchinaup.core.BleUnitTask;
import ycble.runchinaup.exception.BleUUIDNullException;
import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.util.BleUtil;

/**
 * Created by nopointer on 2017/11/18.
 */

public class BleManager extends AbsBleManager implements BleCfg {

    DevImageUtils devImageUtils = DevImageUtils.getInstance();

    //线程池
    private ExecutorService cachedThreadPool = Executors.newFixedThreadPool(20);

    private static final SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");

    private BleManager() {
        init(imageDataNotifyUUID);
        //初始化ble 日志文件夹
        ycBleLog.initLogDirName("魔力宝");
        //配置单包数据的响应超时时间
        cfgTimeOutSinglePkgMilli = 1200;
        //配置多包数据的响应超时
        cfgTimeOutMultiPkgMilli = 6000;
        image();
    }

    private static BleManager bleManager = new BleManager();

    public static BleManager getBleManager() {
        return bleManager;
    }

//    //步数解析对象
//    private DevStepUtil devStepUtil = DevStepUtil.gteInstance();
//
//    //睡眠解析对象
//    private DevSleepUtil devSleepUtil = DevSleepUtil.getInstance();
//
//    //检测数据解析对象
//    private DevMeasureUtil devMeasureUtil = DevMeasureUtil.gteInstance();

    //设备电量
    private int batteryInt = -1;

    //获取设备电量
    public int getBatteryInt() {
        return batteryInt;
    }

    //设备功能列表
//    private DeviceFunction deviceFunction = new DeviceFunction();

    //获取设备功能列表
//    public DeviceFunction getDeviceFunction() {
//        return deviceFunction;
//    }

    //是否是获取了电量，因为电量上报会干扰数据的同步，所以在获取电量的时候，才认为是同步时序的任务，其他时候不作为任务结束的判断
    private boolean isGetBattery = false;

    //是否是连接后的第一个动作，获取必要的数据和同步历史数据
    private boolean isAfterConnFirstTask = false;

    private boolean boolIsSyncData = false;


    private Handler handler = new Handler();


    long[] longs = new long[]{3000, 800, 2000, 500, 2000, 300};

    //最多连接异常几次后，提示
    private static final int intMaxLostCount = 1;
    //当前属于第几次异常
    private int intLostCount = 0;

    //设备信息
//    private DeviceInfoEntity deviceInfoEntity = null;

//    public DeviceInfoEntity getDeviceInfoEntity() {
//        return deviceInfoEntity;
//    }

    //是否是在同步历史数据
    private boolean isSyncHistoryData = false;

    @Override
    public void loadCfg() {

//        devStepUtil.clearReceiveBuffer();
        //添加任务
        addBleUnitTask(BleUnitTask.createEnableNotify(dataServiceUUID, dataNotifyUUID, "打开通知"));
        addBleUnitTask(BleUnitTask.createEnableNotify(dataServiceUUID, imageDataNotifyUUID, "打开通知"));
        addBleUnitTask(BleUnitTask.createWrite(dataServiceUUID, dataWriteUUID, DevDataUtils.choiceDevUIType(2), "获取电量"));
//        addBleUnitTask(BleUnitTask.createWrite(U_SER, U_write, DevDataUtil.currentTime(), "同步时间"));
//        addBleUnitTask(BleUnitTask.createWrite(U_SER, U_write, DevDataUtil.createFirmware(), "获取固件版本"));
//        addBleUnitTask(BleUnitTask.createWrite(U_SER, U_write, DevDataUtil.arrBytePackData(SharedPrefereceSleepRemindLock.read())));
//        addBleUnitTask(BleUnitTask.createWrite(U_SER, U_write, DevDataUtil.devFunction(), "获取手环功能"));
    }

    //连接异常
    @Override
    public void onConnException() {
//        devStepUtil.clearReceiveBuffer();
        batteryInt = -1;
//        deviceInfoEntity = null;
//        setSyncFinish(true);
        if (isOTAMode()) {
            ycBleLog.e(":OTA 模式 不需要扫描设备");
            return;
        } else {
            ycBleLog.e(" 蓝牙此时还打开着，判定为异常断开");
            //收集异常次数
            intLostCount++;
//            if (intLostCount >= intMaxLostCount) {
//                AlostLTO alostLTO = SharePreferenceAlost.read();
//                if (alostLTO.isEnable()) {
//                    long[] longs = new long[]{3000, 800, 2000, 500, 2000, 300};
//                    VibratorUtils.getInstance(MainApplication.getApp()).vibrator(longs, -1);
//                    lostPlayUtil.play();
//                } else {
//                    lostPlayUtil.stop();
//                }
//                intLostCount = 0;
//            }

//            BleDevice bleDevice = SharedPrefereceDevice.read();
//            if (bleDevice != null && !TextUtils.isEmpty(bleDevice.getMac())) {
//                scanAndConn(bleDevice.getMac(), 0);
//            }
        }
    }


    /**
     * 蓝牙打开的回调
     */
    @Override
    public synchronized void onBleOpen() {
        intLostCount = 0;
//        deviceInfoEntity = null;
        isSyncHistoryData = false;
//        BleDevice bleDevice = SharedPrefereceDevice.read();
//        if (bleDevice != null && !TextUtils.isEmpty(bleDevice.getMac())) {
//            scanAndConn(bleDevice.getMac(), 0);
//        }
    }


    //连接成功
    @Override
    public void onConnectSuccess() {
//        devStepUtil.clearReceiveBuffer();
        intLostCount = 0;
        isAfterConnFirstTask = true;
        isGetBattery = false;
//        deviceInfoEntity = null;
    }

    //手动断开
    @Override
    public void onHandDisConn() {
//        devStepUtil.clearReceiveBuffer();
        batteryInt = -1;
//        deviceInfoEntity = null;
        isSyncHistoryData = false;
        ycBleLog.e("手动断开的设备");
    }

    //步数
    private static final int TYPE_STEP = 1;
    //睡眠
    private static final int TYPE_SLEEP = 2;
    //检测
    private static final int TYPE_MEASURE = 3;

    //同步数据的类型 1步数，2睡眠，3测量
    private int dataType = TYPE_STEP;
    //同步天数
    private int syncIndex = -1;

    /**
     * 某个数据需要请求多少次,也就是需要同步多久的数据 最多七天 根据app保存的最后一条数据来看的
     */
    private int needQueryDayCount = 0;

    //同步任务完成后
    @Override
    public void onFinishTaskAfterConn() {
        isAfterConnFirstTask = false;
        isSyncHistoryData = true;
        ycBleLog.e("同步时序任务结束");

        ycBleLog.e("基本指令同步完成....");
        ycBleLog.e("先拿到今天的步数....");
//
        try {
            writeData(dataServiceUUID, dataWriteUUID, DevDataUtils.controlDevUI(1));
        } catch (BleUUIDNullException e) {
            e.printStackTrace();
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                devImageUtils.start();
            }
        }, 1000 * 21);

//        ycBleLog.e("先罗列一下设备功能列表:" + deviceFunction.toString());

//        if (deviceFunction.isSupportStep()) {
//            needQueryDayCount = devStepUtil.needQueryDayDataCount();
//            dataType = TYPE_STEP;
//        } else {
//            if (deviceFunction.isSupportSleep()) {
//                needQueryDayCount = devSleepUtil.needQueryDayDataCount();
//                dataType = TYPE_SLEEP;
//            } else {
//                if (deviceFunction.isSupportHr() || deviceFunction.isSupportOx() || deviceFunction.isSupportBlood()) {
//                    dataType = TYPE_MEASURE;
//                }
//            }
//        }
        syncIndex = -1;
        syncDataWithFunction();
    }

    /**
     * 根据设备功能列表来同步数据
     */
    private void syncDataWithFunction() {
        //先自增是为了防止 数据日期发生偏移
        syncIndex++;
//        if (deviceFunction.isSupportStep() && dataType == TYPE_STEP) {
//            //如果是支持步数的话
//            LogUtil.e("debug===继续请求步数数据===>" + syncIndex);
//            if (syncIndex < needQueryDayCount) {
//                //写完数据后 在收到回调以后会自行调用syncDataWithFunction() 方法
//                privateWriteData(DevDataUtil.createHistorySportData(-syncIndex));
//            } else {
//                dataType = TYPE_SLEEP;
//                syncIndex = -1;
//                needQueryDayCount = devSleepUtil.needQueryDayDataCount();
//                if (isSyncHistoryData) {
//                    //如果是在同步历史数据的时候，才能自动循环同步数据
//                    syncDataWithFunction();
//                }
//            }
//            return;
//        }
//        if (deviceFunction.isSupportSleep() && dataType == TYPE_SLEEP) {
//            //如果支持睡眠数据的话
//            LogUtil.e("debug===继续请求睡眠数据===>" + syncIndex);
//            if (syncIndex < needQueryDayCount) {
//                //写完数据后 在收到回调以后会自行调用syncDataWithFunction() 方法
//                privateWriteData(DevDataUtil.createHistorySleepData(-syncIndex));
//            } else {
//                dataType = TYPE_MEASURE;
//                syncIndex = -1;
//                needQueryDayCount = devMeasureUtil.needQueryDayDataCount(deviceFunction);
//                if (isSyncHistoryData) {
//                    //如果是在同步历史数据的时候，才能自动循环同步数据
//                    syncDataWithFunction();
//                }
//            }
//            return;
//        }
//        if ((deviceFunction.isSupportHr() || deviceFunction.isSupportOx() || deviceFunction.isSupportBlood()) && dataType == TYPE_MEASURE) {
//            //如果支持检测数据的话
//            LogUtil.e("debug===继续请求测量数据===>");
//            if (syncIndex < needQueryDayCount) {
//                //写完数据后 在收到回调以后会自行调用syncDataWithFunction() 方法
//                privateWriteData(DevDataUtil.createHistoryHealthData(-syncIndex));
//            } else {
//                dataType = -1;
//                syncIndex = -1;
//                if (isSyncHistoryData) {
//                    //如果是在同步历史数据的时候，才能自动循环同步数据
//                    syncDataWithFunction();
//                }
//            }
//            return;
//        }
//        isSyncHistoryData = false;
//        LogUtil.e("debug===数据同步完成了============>");
//        LogUtil.e("debug===数据同步完成了============>");
//        LogUtil.e("debug===数据同步完成了============>");
//        bleManager.writeData(DevDataUtil.createAutoReportStep(true));
    }


    @Override
    public void onDataWrite(byte[] data, boolean isChara, UUID... uuid) {
        super.onDataWrite(data, isChara, uuid);
        if (isChara) {
            if (uuid[0].equals(imageDataWriteUUID)) {
                devImageUtils.next();
            }
            taskSuccess();
//            handWithWriteDataFlag(data);
        } else {
            //打开通知成功
            if (BleUtil.byte2HexStr(data).equalsIgnoreCase("0100")) {
                //通知打开了，移除这个指令
                taskSuccess();
            }
        }
    }


    @Override
    protected boolean specialCommand(BleUnitTask bleUnitTask) {
        byte[] data = bleUnitTask.getData();
        if (bleUnitTask.getOptionType() != BleUnitTask.TYPE_READ) {
            int flag = BleUtil.byte2IntLR(data[0]);
            if (flag == 0x13 || flag == 0x15 || flag == 0x16) {
                //这几个数据可能存在多包响应
                return true;
            } else if (flag == 0x14) {
                isGetBattery = true;
            }
        }
        return false;
    }

    @Override
    public void onDataReceive(final byte[] data, final UUID uuid) {
        ycBleLog.e("接收到数据>>>:" + BleUtil.byte2HexStr(data));

//        if (uuid.equals(imageDataNotifyUUID)) {
//            int index = BleUtil.byte2IntLR(data[1], data[2]);
//            devImageUtils.withNext(index);
//        }

//        onHandData(data);
    }

    public void writeData(final byte[] data) {
        ycBleLog.e("准备写指令<<<" + BleUtil.byte2HexStr(data));
        //如果没有连接 或者正在同步数据，结束刷新
        if (!isConn() || isAfterConnFirstTask) {
            ycBleLog.e(">没有连接,或者正在数据同步历史");
            return;
        } else if (isConn() && !isAfterConnFirstTask) {
            privateWriteData(data);
        } else if (isAfterConnFirstTask) {
            ycBleLog.e(">数据正在同步,不能交互");
        }
    }

    //多包数据
    private List<byte[]> listMultiPckData = new ArrayList<>();
    //多包数据的索引
    private int intMultiPckDataIndex = -1;
    //是否正在写多包数据
    private boolean boolIsMultiWriteDataIng = false;

    private Handler handlerMulti = new Handler();

    //写多包数据，多用于消息推送，最长消息只能是11包
    public synchronized void writeMuliteData(final ArrayList<byte[]> data) {
        if (boolCanRWN()) {
            if (!boolIsMultiWriteDataIng) {
                clearMultiFlag();
                listMultiPckData.addAll(data);

                nextPckDataForMultiData();
                handlerMulti.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        clearMultiFlag();
                    }
                }, cfgTimeOutMultiPkgMilli);

            } else {
                ycBleLog.e("消息队列里面还有消息没有推送完成");
            }
        }

    }

    //清除写多包数据的标志位
    private synchronized void clearMultiFlag() {
        intMultiPckDataIndex = -1;
        listMultiPckData.clear();
        boolIsMultiWriteDataIng = false;
    }

    //写多包数据的下一包数据
    private synchronized void nextPckDataForMultiData() {
        intMultiPckDataIndex++;
        if (intMultiPckDataIndex < listMultiPckData.size()) {
            writeData(listMultiPckData.get(intMultiPckDataIndex));
        } else {
            handlerMulti.removeCallbacksAndMessages(null);
            clearMultiFlag();
        }
    }

    //修改某些数据的状态
    private void reUpdateSomeStataAndFlag(byte[] data) {
        int flag = data[0] & 0xff;
        if (flag == 0x13) {
            //同步步数的时候，要清空之前的数据
//            devStepUtil.clearReceiveBuffer();
        } else if (flag == 0x15) {
            //同步睡眠的时候，要清空之前的数据
        } else if (flag == 0x16) {
//            allHistoryMeasureData.clear();
        } else if (flag == 0x31) {
            //实时上报数据，打开
        } else if (BleUtil.byte2HexStr(data).equalsIgnoreCase("1d0155aa")) {
            ycBleLog.e("OTA 指令" + BleUtil.byte2HexStr(data));
        }
    }

    //处理通知上报的数据
    private void onHandData(final byte[] data) {
        int flag = data[0] & 0xff;

        switch (flag) {
            case 0x81://同步时间回应
            {
                ycBleLog.e("时间同步成功了");
                taskSuccess();
            }
            break;
            case 0x82: //闹钟或者久坐提醒 消息推送开关 回应
            {
                ycBleLog.e("闹钟或者久坐提醒同步成功了");
                taskSuccess();
            }
            break;
            case 0x83:  //设备功能列表,单包数据
            {
//                deviceFunction = DevDataUtil.getDevFunctionList(data);
//                for (DeviceFunction.DeviceFunctionCallback deviceFunctionCallback : deviceFunctionCallbackHashSet) {
//                    deviceFunctionCallback.onGetFunction(deviceFunction);
//                }
//                ycBleLog.e("获取到设备功能列表了+" + deviceFunction.toString());
                taskSuccess();
            }
            break;
            case 0x85: //天气回应
            {
                ycBleLog.e("天气数据写成功了");
                taskSuccess();
            }
            break;
            case 0x93: //步数数据解析,多包数据
            {
                cachedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
//                        devStepUtil.receiveData(data);
//                        //处理结束数据的标识，回收超时处理
//                        if ((data[4] & 0xff) == 0xff) {
//                            taskSuccess();
//                            if (isSyncHistoryData) {
//                                //如果是在同步历史数据的时候，才能自动循环同步数据
//                                syncDataWithFunction();
//                            }
//                        }
                    }
                });
            }
            break;
            case 0x94:  //电量，单包数据
            {
                batteryInt = BleUtil.byte2IntLR(data[1]);
//                BatteryHelper.getBatteryHelper().setBattery(batteryInt);
                if (isGetBattery) {
                    taskSuccess();
                }
            }
            break;
            case 0x95:  //睡眠数据解析，多包数据
            {
                cachedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
//                        devSleepUtil.receiveData(data, -syncIndex);
//                        //处理结束数据的标识，回收超时处理
//                        if (BleUtil.byte2IntLR(data[1], data[2]) == 0xFFFF) {
//                            taskSuccess();
//                            if (isSyncHistoryData) {
//                                //如果是在同步历史数据的时候，才能自动循环同步数据
//                                syncDataWithFunction();
//                            }
//                        }
                    }
                });
            }
            break;
            case 0x96: //心率。血压、血氧的测量历史，多包数据
            {
//                devMeasureUtil.receiveData(data, syncIndex);
//                if ((data[1] & 0xff) == 0xff && (data[2] & 0xff) == 0xff) {
//                    taskSuccess();
//                    if (isSyncHistoryData) {
//                        //如果是在同步历史数据的时候，才能自动循环同步数据
//                        syncDataWithFunction();
//                    } else if (syncIndex == 0) {
//                        HrDataSyncHelper.getInstance().notifySyncHr();
//                        BloodDataSyncHelper.getInstance().notifySyncBlood();
//                    }
//                }
            }
            break;
            case 0xB2:
            case 0xB3: //实时步数上报，总的数据
            {
//                devStepUtil.getTotalStepEntity(data);
                taskSuccess();
            }
            break;
            case 0x53: //查找/停止查找 手机
            {
                handFindMusic(BleUtil.byte2IntLR(data[1]) == 1);
                taskSuccess();
            }
            break;
            case 0x9F: //固件信息，版本
            {
//                deviceInfoEntity = DevDataUtil.getDeviceInfo(data);
//                ycBleLog.e("固件版本:" + deviceInfoEntity.toString());
                taskSuccess();
            }
            break;
            case 0xF3: //来电拒绝
            {
//                MainApplication.getApp().sendBroadcast(new Intent(BleReceiver.actionEndCall));
            }
            break;
            case 0xA2: {
                ycBleLog.e("拍照指令>>>");
//                sendAction(ActionCfg.takePhotoAction);
                taskSuccess();
            }
            break;
            case 0xD2: //拍照或者取消拍照
            {
                switch (BleUtil.byte2IntLR(data[1])) {
                    case 0://APP退出拍照界面（disable）
//                        sendAction(ActionCfg.exitTakePhotoForApp);
                        break;
                    case 1://APP进入拍照界面（enable）
                        break;
                }
                taskSuccess();
            }
            break;
            case 0xE0: //检测状态 ，开始，停止
            {
//                devMeasureUtil.getMeasureState(data);
                taskSuccess();
            }
            break;
            case 0xE1: //实时检测数据
            {
//                devMeasureUtil.getRealMeasureEntity(data);
                taskSuccess();
            }
            break;
            case 0xEE: //数据响应错误了
            {
                if (BleUtil.byte2HexStr(data).equalsIgnoreCase("EEEEEEEE")) {
                    ycBleLog.e("数据响应错误了，不知道是个什么鬼情况");
                }
            }
            break;
            default:
                ycBleLog.e("默认task成功的标志");
                taskSuccess();
                break;
        }
    }

    //寻找手机
    private void handFindMusic(boolean isFindEnable) {
//        VibratorUtils.getInstance(MainApplication.getMainApplication()).vibrator(longs, -1);
//        //如果在后台运行的话 就发送通知
//        if (!AppBaseUtils.isForeground(MainApplication.getMainApplication())) {
//            NotifyUtils.sendFindNotify(MainApplication.getMainApplication());
//        } else {
//            //如果在后台运行的话 就toast 震动
//            ToastHelper.getToastHelper().show(R.string.device_is_fond_phone);
//        }

    }

//    private void uploadMeasure(List<MeasureHistoryData> measureHistoryDatas) {
//
//        for (MeasureHistoryData measureHistoryData : measureHistoryDatas) {
//            if (measureHistoryData.getHr() != 0) {
//                dataHelper.uploadHr(measureHistoryData);
//            }
//            if (measureHistoryData.getOx() != 0) {
//                dataHelper.uploadOx(measureHistoryData);
//            }
//            if (measureHistoryData.getBdH() != 0 && measureHistoryData.getBdL() != 0) {
//                dataHelper.uploadBd(measureHistoryData);
//            }
//        }
//
//    }

    //回调写处理标志位，在写回调里面（某些写操作不会有数据上报作为响应）
    private final void handWithWriteDataFlag(byte[] data) {
        int flag = data[0] & 0xff;
        if (flag == 0x73) {
            taskSuccess();
            nextPckDataForMultiData();
        } else if (flag == 0x11) {
            taskSuccess();
        } else if (flag == 0x1D) {
            taskSuccess();
        } else if (flag == 0x13) {
//            devStepUtil.clearReceiveBuffer();
        } else if (flag == 0x60) {
            ycBleLog.e("开启或者停止了测量");
            taskSuccess();
        }
    }

    //内部写数据
    private void privateWriteData(byte[] data) {
        if (isConn) {
            reUpdateSomeStataAndFlag(data);
            try {
                writeData(dataServiceUUID, dataWriteUUID, data);
            } catch (BleUUIDNullException e) {
                e.printStackTrace();
            }
        }
    }

//    private HashSet<DeviceFunction.DeviceFunctionCallback> deviceFunctionCallbackHashSet = new HashSet<>();
//
//    public void registerDeviceFunctionCallback(DeviceFunction.DeviceFunctionCallback deviceFunctionCallback) {
//        if (!deviceFunctionCallbackHashSet.contains(deviceFunctionCallback)) {
//            deviceFunctionCallbackHashSet.add(deviceFunctionCallback);
//        }
//    }
//
//    public void unRegisterDeviceFunctionCallback(DeviceFunction.DeviceFunctionCallback deviceFunctionCallback) {
//        if (deviceFunctionCallbackHashSet.contains(deviceFunctionCallback)) {
//            deviceFunctionCallbackHashSet.remove(deviceFunctionCallback);
//        }
//    }

    /***
     * 超时的指令
     */
    @Override
    public void onResponseTimeOut(byte[] command) {
        super.onResponseTimeOut(command);
        if (BleUtil.byte2HexStr(command).equalsIgnoreCase("0300")) {
//            deviceFunction = new DeviceFunction();
//            for (DeviceFunction.DeviceFunctionCallback deviceFunctionCallback : deviceFunctionCallbackHashSet) {
//                deviceFunctionCallback.onGetFunction(deviceFunction);
//            }
        }
        ycBleLog.e("--超时的指令是:" + BleUtil.byte2HexStr(command));
    }


    //是否是可以交互数据
    private boolean boolCanRWN() {
        if (!isConn || isAfterConnFirstTask || boolIsSyncData) {
            if (!isConn) {
                ycBleLog.e("没有连接");
            } else if (isAfterConnFirstTask) {
                ycBleLog.e("在同步任务时序");
            }
            return false;
        }
        return true;
    }

    //发送广播
    private void sendAction(String action) {
//        MainApplication.getMainApplication().sendBroadcast(new Intent(action));
    }


    private void image() {

        final long startTime = System.currentTimeMillis();

        devImageUtils.initImageCfg(BitmapFactory.decodeResource(MainApplication.getMainApplication().getResources(), R.drawable.demo_666));


        devImageUtils.setReceiveImageDataCallback(new ReceiveImageDataCallback() {
            @Override
            public void onImageDataReceive(int transportIndex, byte[] imageData) {
//                if (transportIndex < 1000) {
                    byte[] imageDataByte = new byte[20];
                    imageDataByte[0] = (byte) ((transportIndex & 0xff00) >> 8);
                    imageDataByte[1] = (byte) (transportIndex & 0xff);
                    System.arraycopy(imageData, 0, imageDataByte, 2, imageData.length);
//                        ycBleLog.e("第" + (transportIndex + 1) + "包数据是:" + BleUtil.byte2HexStr(imageDataByte));
                    try {
                        writeData(dataServiceUUID, imageDataWriteUUID, imageDataByte);
                    } catch (BleUUIDNullException e) {
                        e.printStackTrace();
                    }
//                } else {
//                    onFinish();
//                }
            }

            @Override
            public void onFinish() {
                try {
                    writeData(dataServiceUUID, dataWriteUUID, DevDataUtils.controlDevUI(0));

                    long endTime = System.currentTimeMillis();
                    ycBleLog.e("time:" + (endTime - startTime) / 1000);
                } catch (BleUUIDNullException e) {
                    e.printStackTrace();
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            writeData(dataServiceUUID, dataWriteUUID, DevDataUtils.setDevUICfg());
                        } catch (BleUUIDNullException e) {
                            e.printStackTrace();
                        }
                    }
                }, 1000);
            }
        });
    }

}
