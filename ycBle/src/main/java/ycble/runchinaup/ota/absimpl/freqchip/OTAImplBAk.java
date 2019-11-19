//package ycble.runchinaup.ota.absimpl.fuwokun;
//
//import android.util.Log;
//
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.UUID;
//
//import ycble.runchinaup.core.AbsBleManager;
//import ycble.runchinaup.core.BleUnitTask;
//import ycble.runchinaup.exception.BleUUIDNullException;
//import ycble.runchinaup.log.ycBleLog;
//import ycble.runchinaup.ota.OTAErrCode;
//import ycble.runchinaup.ota.callback.OTACallback;
//import ycble.runchinaup.util.BleUtil;
//
//import static ycble.runchinaup.ota.absimpl.fuwokun.FRKUtils.OTA_CMD_WRITE_DATA;
//
//class OTAImplBAk extends AbsBleManager implements FreqBleCfg {
//
//    private String filePath = "/storage/emulated/0/Download/Bluetooth/OTA_TEST1.bin";
//
//    private OTACallback otaCallback;
//
//
//    private long leng;
//
//    private FileInputStream isfile = null;
//    private InputStream input;
//
//    private int firstaddr = 0;
//    private byte[] baseaddr = null;
//    private int sencondaddr = 0x14000;
//    private int recv_data;
//    private int writePrecent;
//    private boolean writeStatus = false;
//
//
//    private Object object = new Object();
//
//
//    public OTAImplBAk() {
//        init(UUID_OTA_SEND_DATA);
//    }
//
//    public void setFilePath(String filePath) {
//        this.filePath = filePath;
//    }
//
//    public void setOtaCallback(OTACallback otaCallback) {
//        this.otaCallback = otaCallback;
//    }
//
//    @Override
//    public void onFinishTaskAfterConn() {
//        if (verifyFile()) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        doSendFileByBluetooth(filePath);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (BleUUIDNullException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }
//    }
//
//    @Override
//    public void onDataReceive(byte[] data, UUID uuid) {
//        ycBleLog.e("接收到数据" + BleUtil.byte2HexStr(data));
//        baseaddr = data;
//        setRecv_data(1);
//    }
//
//    @Override
//    public void onConnectSuccess() {
//
//    }
//
//    @Override
//    public void onHandDisConn() {
//
//    }
//
//    @Override
//    public void loadCfg() {
//        addBleUnitTask(BleUnitTask.createEnableNotify(UUID_OTA_SERVICE, UUID_OTA_RECV_DATA, "打开通知"));
//    }
//
//    @Override
//    public void onConnException() {
//
//    }
//
//    @Override
//    public void onDataWrite(byte[] data, boolean isChara, UUID... uuid) {
//        super.onDataWrite(data, isChara, uuid);
//        if (isChara) {
//            writeStatus = true;
//        }
//        hanWithTask();
////        if (!isChara) {
////            打开通知成功
////            if (BleUtil.byte2HexStr(data).equalsIgnoreCase("0100")) {
////                通知打开了，移除这个指令
////                hanWithTask();
////            }
////        }
//    }
//
//
//    /**
//     * 处理超时数据
//     */
//    private void hanWithTask() {
//        taskSuccess(5);
//    }
//
//
//    /**
//     * 验证OTA文件
//     *
//     * @return
//     */
//    private boolean verifyFile() {
//        InputStream input;
//        byte[] Buffer = new byte[4];
//        File file = new File(filePath);
//        try {
//            FileInputStream infile = new FileInputStream(file);
//            try {
//                infile.skip(0x167);
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            input = new BufferedInputStream(infile);
//            try {
//                input.read(Buffer, 0, 4);
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        } catch (FileNotFoundException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
//        System.out.println("buffer: " + Buffer[0] + " " + Buffer[1]);
//        if ((Buffer[0] != 0x52) || (Buffer[1] != 0x51) || (Buffer[2] != 0x51) || (Buffer[3] != 0x52)) {
//            if (otaCallback != null) {
//                otaCallback.onFailure(OTAErrCode.FWK_FILE_INVALIDE, "文件校验不通过");
//            }
//            return false;
//        }
//        if (file.length() < 100) {
//            if (otaCallback != null) {
//                otaCallback.onFailure(OTAErrCode.FWK_FILE_INVALIDE, "文件校验不通过");
//            }
//            return false;
//        }
//        return true;
//    }
//
//
//    public void doSendFileByBluetooth(String filePath)
//            throws FileNotFoundException, BleUUIDNullException {
//        long startTime = System.currentTimeMillis();
//        if (!filePath.equals(null)) {
//            int read_count;
//            int i = 0;
//            int addr;
//            int delay_num;
//            int lastReadCount = 0;
//            int packageSize = 235;
//            long send_times;
//            int send_offset;
//            int send_each_count = 300;
//            int send_data_count = 0;
//            byte[] inputBuffer = new byte[packageSize];
//            File file = new File(filePath);// 成文件路径中获取文件
//            isfile = new FileInputStream(file);
//            leng = file.length();
//            send_times = leng / send_each_count;
//            send_offset = (int) (leng % send_each_count);
//            input = new BufferedInputStream(isfile);
//            setRecv_data(0);
//            ycBleLog.e("========1");
//            send_data(FRKUtils.OTA_CMD_GET_STR_BASE, 0, null, 0);
//
//            while (getRecv_data() != 1) {
//                if (checkDisconnect()) {
//                    return;
//                }
//            }
//
//            if (FRKUtils.bytetoint(baseaddr) == firstaddr) {
//                addr = sencondaddr;
//            } else {
//                addr = firstaddr;
//            }
//            setRecv_data(0);
//            ycBleLog.e("========2");
//
//            page_erase(addr, leng);
//            try {
//                while (((read_count = input.read(inputBuffer, 0, packageSize)) != -1)) {
//                    ycBleLog.e("========while");
//                    send_data(OTA_CMD_WRITE_DATA, addr, inputBuffer, read_count);
//                    //for(delay_num = 0;delay_num < 10000;delay_num++);
//                    addr += read_count;
//                    lastReadCount = read_count;
//                    send_data_count += read_count;
//                    //System.out.println("times" + i + " " + read_count);
//                    i++;
//                    writePrecent = (int) (((float) send_data_count / leng) * 100);
//                    //进度
////                    mHandler.sendEmptyMessage(1);
//                    while (!writeStatus) ;
//                    writeStatus = false;
//                    while (getRecv_data() != 1) {
//                        if (checkDisconnect()) {
//                            return;
//                        }
//                    }
//                    setRecv_data(0);
//                }
//                ycBleLog.e("========3");
//                while (FRKUtils.bytetoint(baseaddr) != (addr - lastReadCount)) {
//                    if (checkDisconnect()) {
//                        return;
//                    }
//                }
//                ycBleLog.e("========4");
//                send_data(FRKUtils.OTA_CMD_REBOOT, 0, null, 0);
////                mHandler.sendEmptyMessage(0);
//                Log.e("花费时间:", (System.currentTimeMillis() - startTime) + "");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        } else {
//            ycBleLog.e("请选择要发送的文件");
//        }
//    }
//
//    private boolean send_data(int type, int addr, byte[] buffer, int length) throws BleUUIDNullException {
//        byte[] cmd_write = null;
//        byte[] result_cmd = null;
//        byte[] cmd = new byte[1];
//        cmd_write = FRKUtils.cmd_operation(type, length, addr);
//        if ((type == FRKUtils.OTA_CMD_GET_STR_BASE) || ((type == FRKUtils.OTA_CMD_PAGE_ERASE))) {
//            result_cmd = cmd_write;
//        } else if (type == FRKUtils.OTA_CMD_REBOOT) {
//            cmd[0] = (byte) (type & 0xff);
//            result_cmd = cmd;
//        } else {
//            result_cmd = FRKUtils.byteMerger(cmd_write, buffer);
//        }
//        return writeDataWithoutResp(UUID_OTA_SERVICE, UUID_OTA_SEND_DATA, result_cmd);
//
//    }
//
//
//    public int getRecv_data() {
//        return recv_data;
//    }
//
//    public void setRecv_data(int recv_data) {
//        this.recv_data = recv_data;
//    }
//
//    private int page_erase(int addr, long length) throws BleUUIDNullException {
//
//        long count = length / 0x1000;
//        if ((length % 0x1000) != 0) {
//            count++;
//        }
//        for (int i = 0; i < count; i++) {
//            send_data(FRKUtils.OTA_CMD_PAGE_ERASE, addr, null, 0);
//            while (getRecv_data() != 1) ;
//            setRecv_data(0);
//            addr += 0x1000;
//        }
//        return 0;
//    }
//
//    boolean checkDisconnect() {
//        if (!isConn()) {
//            return true;
//        }
//        return false;
//    }
//
//    private void doWork() throws BleUUIDNullException {
//        //开始ota指令
//        startOTACmd();
//
//        int addr;
//
//        if (FRKUtils.bytetoint(baseaddr) == firstaddr) {
//            addr = sencondaddr;
//        } else {
//            addr = firstaddr;
//        }
//
//        //抹去原来的数据的地址
//        page_erase(addr, leng);
//
//        try {
//            while (((read_count = input.read(inputBuffer, 0, packageSize)) != -1)) {
//                ycBleLog.e("========while");
//                send_data(OTA_CMD_WRITE_DATA, addr, inputBuffer, read_count);
//                //for(delay_num = 0;delay_num < 10000;delay_num++);
//                addr += read_count;
//                lastReadCount = read_count;
//                send_data_count += read_count;
//                //System.out.println("times" + i + " " + read_count);
//                i++;
//                writePrecent = (int) (((float) send_data_count / leng) * 100);
//                //进度
////                    mHandler.sendEmptyMessage(1);
//                while (!writeStatus) ;
//                writeStatus = false;
//                while (getRecv_data() != 1) {
//                    if (checkDisconnect()) {
//                        return;
//                    }
//                }
//                setRecv_data(0);
//            }
//            ycBleLog.e("========3");
//            while (FRKUtils.bytetoint(baseaddr) != (addr - lastReadCount)) {
//                if (checkDisconnect()) {
//                    return;
//                }
//            }
//            ycBleLog.e("========4");
//            send_data(FRKUtils.OTA_CMD_REBOOT, 0, null, 0);
////                mHandler.sendEmptyMessage(0);
//            Log.e("花费时间:", (System.currentTimeMillis() - startTime) + "");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//    /**
//     * 开始OTA 指令
//     */
//    private void startOTACmd() throws BleUUIDNullException {
//        send_data(FRKUtils.OTA_CMD_GET_STR_BASE, 0, null, 0);
//    }
//
//    /**
//     * 发送OTA 数据
//     */
//    private void sendOTAData() {
//
//
//    }
//
//    /**
//     * 结束OTA 重启设备
//     */
//    private void endOTACmd() {
//
//    }
//
//    /**
//     * 等待
//     */
//    void waitObj() {
//        synchronized (object) {
//            try {
//                object.wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 等待
//     *
//     * @param time
//     */
//    void waitObj(int time) {
//        synchronized (object) {
//            try {
//                object.wait(time);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 唤醒
//     */
//    void notifyObj() {
//        synchronized (object) {
//            object.notify();
//        }
//    }
//
//
//}
