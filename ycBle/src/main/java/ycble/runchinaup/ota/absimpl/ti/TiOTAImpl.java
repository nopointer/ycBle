package ycble.runchinaup.ota.absimpl.ti;

import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import ycble.runchinaup.core.AbsBleManager;
import ycble.runchinaup.exception.BleUUIDNullException;
import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.ota.callback.OTACallback;
import ycble.runchinaup.util.BleUtil;

import static ycble.runchinaup.ota.OTAErrCode.TI_FAILURE;


class TiOTAImpl extends AbsBleManager implements TIBleCfg {


    private static final int OAD_IMG_HDR_SIZE = 8;
    private static final int OAD_BLOCK_SIZE = 16;
    private static final int FILE_BUFFER_SIZE = 0x40000;
    private static final int OAD_BUFFER_SIZE = 2 + OAD_BLOCK_SIZE;
    private static final int HAL_FLASH_WORD_SIZE = 4;


    private String filePath = null;

    private OTACallback otaCallback;
    private ImgHdr mFileImgHdr = new ImgHdr();

    private final byte[] mFileBuffer = new byte[FILE_BUFFER_SIZE];

    private final byte[] mOadBuffer = new byte[OAD_BUFFER_SIZE];

    public TiOTAImpl() {
        init(UUID_OTA_SEND_DATA);
    }

    private boolean mProgramming = false;

    private ProgInfo mProgInfo = new ProgInfo();
    private Handler handler = new Handler();
    private int imageDataIndex = 0;
    private boolean isSuccess = false;


    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setOtaCallback(OTACallback otaCallback) {
        this.otaCallback = otaCallback;
    }

    @Override
    public void onFinishTaskAfterConn() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadFile();
            }
        }, 500);

    }

    @Override
    public void onDataReceive(byte[] data, UUID uuid) {
//        ycBleLog.e("接收到数据" + uuid.toString() + "//" + BleUtil.byte2HexStr(data));
//        if (uuid.equals(UUID_OTA_RECV_DATA)) {
//
//        } else if (uuid.equals(UUID_OTA_SEND_DATA)) {
//            if (mProgramming) {
////                postDta(data);
//            }
//        }
    }

    @Override
    public void onConnectSuccess() {

    }

    @Override
    public void onHandDisConn() {
        if (!isSuccess) {
            otaCallback.onFailure(TI_FAILURE, "connException");
        }
    }

    @Override
    public void loadCfg() {
//        addBleUnitTask(BleUnitTask.createEnableNotify(UUID_OTA_SERVICE, UUID_OTA_SEND_DATA, "打开通知"));
//        addBleUnitTask(BleUnitTask.createEnableNotify(UUID_OTA_SERVICE, UUID_OTA_SEND_DATA, "打开通知"));
//        addBleUnitTask(BleUnitTask.createEnableNotify(UUID_OTA_SERVICE, UUID_OTA_RECV_DATA, "打开通知"));
//        addBleUnitTask(BleUnitTask.createWriteWithOutResp(UUID_OTA_SERVICE, UUID_OTA_RECV_DATA, new byte[]{0}, "查询版本"));
//        addBleUnitTask(BleUnitTask.createWriteWithOutResp(UUID_OTA_SERVICE, UUID_OTA_RECV_DATA, new byte[]{1}, "查询版本"));
    }

    @Override
    public void onConnException() {
        if (otaCallback != null) {
            if (!isSuccess) {
                otaCallback.onFailure(TI_FAILURE, "connException");
            }
        }
    }

    @Override
    public void onDataWrite(byte[] data, boolean isChara, UUID... uuid) {
        hanWithTask();
        if (mProgramming) {
            postDta(imageDataIndex++);
        }
    }


    /**
     * 处理超时数据
     */
    private void hanWithTask() {
        taskSuccess(5);
    }

    public void startOTA(String mac) {
        connDevice(mac);
    }

    public void stopOTA() {
        mProgramming = false;
        isSuccess = false;
        disConn();
    }

    private void start() {
        byte[] buf = new byte[OAD_IMG_HDR_SIZE + 2 + 2];
        buf[0] = (byte) (mFileImgHdr.ver & 0xff);
        buf[1] = (byte) (mFileImgHdr.ver >> 8);
        buf[2] = (byte) (mFileImgHdr.len & 0xff);
        buf[3] = (byte) (mFileImgHdr.len >> 8);
        System.arraycopy(mFileImgHdr.uid, 0, buf, 4, 4);
        mProgramming = true;
        mProgInfo.reset();
        try {
            writeDataWithoutResp(UUID_OTA_SERVICE, UUID_OTA_RECV_DATA, buf);
        } catch (BleUUIDNullException e) {
            e.printStackTrace();
        }

    }


    private void postDta(int index) {
        programBlock(index);
    }

    private void programBlock(int block) {
        if (mProgInfo.iBlocks < mProgInfo.nBlocks) {
            mProgramming = true;
            String msg = new String();

            mProgInfo.iBlocks = (short) block;

            // Prepare block
            mOadBuffer[0] = (byte) (mProgInfo.iBlocks & 0xFF);
            mOadBuffer[1] = (byte) (mProgInfo.iBlocks >> 8);
            System.arraycopy(mFileBuffer, mProgInfo.iBytes, mOadBuffer, 2, OAD_BLOCK_SIZE);

            boolean success = writeImageData(mOadBuffer);
            // Send block
//            ycBleLog.e("FwUpdateActivity" + String.format("TX Block %02x%02x", mOadBuffer[1], mOadBuffer[0]));

            if (success) {
                // Update stats
                mProgInfo.iBlocks++;
                mProgInfo.iBytes += OAD_BLOCK_SIZE;
                float progress = (mProgInfo.iBlocks * 100) / mProgInfo.nBlocks;
                if (otaCallback != null) {
                    otaCallback.onProgress((int) progress);
                }
                ycBleLog.e("progress===>" + progress);
                if (mProgInfo.iBlocks == mProgInfo.nBlocks) {
                    ycBleLog.e("OTA 完成 Programming finished");
                    isSuccess = true;
                    if (otaCallback != null) {
                        otaCallback.onSuccess();
                    }
                }
            } else {
                mProgramming = false;
                msg = "GATT writeCharacteristic failed\n";
                if (otaCallback != null) {
                    otaCallback.onFailure(TI_FAILURE, "writeCharacteristic failed");
                }
            }
            if (!success) {
                ycBleLog.e(msg);
            }
        } else {
            mProgramming = false;
        }
    }

    private boolean writeImageData(byte data[]) {
        try {
            return writeDataWithoutResp(UUID_OTA_SERVICE, UUID_OTA_SEND_DATA, data);
        } catch (BleUUIDNullException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void loadFile() {
        // Load binary file
        try {
            // Read the file raw into a buffer
            InputStream stream;
            File f = new File(filePath);
            stream = new FileInputStream(f);
            stream.read(mFileBuffer, 0, mFileBuffer.length);
            stream.close();
        } catch (IOException e) {
            // Handle exceptions here
            ycBleLog.e("File open failed: " + filePath + "\n");
        }

        // Show image info
        mFileImgHdr.ver = BleUtil.byte2ShortLR(mFileBuffer[5], mFileBuffer[4]);
        mFileImgHdr.len = BleUtil.byte2ShortLR(mFileBuffer[7], mFileBuffer[6]);
        mFileImgHdr.imgType = ((mFileImgHdr.ver & 1) == 1) ? 'B' : 'A';

        start();
    }

    private class ImgHdr {
        short ver;
        short len;
        Character imgType;
        byte[] uid = new byte[4];
    }


    private class ProgInfo {
        int iBytes = 0; // Number of bytes programmed
        short iBlocks = 0; // Number of blocks programmed
        short nBlocks = 0; // Total number of blocks
        int iTimeElapsed = 0; // Time elapsed in milliseconds

        void reset() {
            iBytes = 0;
            iBlocks = 0;
            iTimeElapsed = 0;
            nBlocks = (short) (mFileImgHdr.len / (OAD_BLOCK_SIZE / HAL_FLASH_WORD_SIZE));
        }
    }


}
