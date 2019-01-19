package lib.ycble.bleModule.imageTransport;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.nio.ByteBuffer;

import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.util.BleUtil;

public class DevImageUtils {

    private static DevImageUtils instance = new DevImageUtils();

    private DevImageUtils() {
    }

    public static DevImageUtils getInstance() {
        return instance;
    }

    //图片宽 默认240
    private int imageWidth = 240;
    //图片高 默认240
    private int imageHeight = 240;
    //总共的字节数
    private int totalByte = imageWidth * imageHeight * 2;
    //单包数的长度
    private int singlePckDataLen = 18;
    //要传输的图片的包数
    private int totalBytePckCount = totalByte / singlePckDataLen;
    //当前传输的索引
    private int transportIndex = 0;
    //当前是否在传输
    private boolean isTransportIng = false;

    private byte imageByteArray[] = null;


    //色彩配置 默认RGB_565
    private ColorCfg colorCfg = ColorCfg.RGB_556;

    public void initImageCfg(Bitmap bitmap) {
        initImageCfg(bitmap, 240, 240, 18, ColorCfg.RGB_556);
    }

    public void initImageCfg(Bitmap bitmap, int width, int height, int singlePckDataLen, ColorCfg colorCfg) {
        this.imageWidth = width;
        this.imageHeight = height;
        this.singlePckDataLen = singlePckDataLen;
        this.colorCfg = colorCfg;
        calculationImageSizeAndSomeDataLen();
        Bitmap tmp = resizeBitmap(bitmap, width, height);
        bitmap2RGB(tmp);
    }

    //计算图表 或者某些数据长度
    private void calculationImageSizeAndSomeDataLen() {
        switch (colorCfg) {
            //2个byte表示一个像素点，所以长度是
            case RGB_556:
            default:
                totalByte = imageHeight * imageWidth * 2;
                break;
            case ARGB_8888:
                totalByte = imageHeight * imageWidth * 4;
                break;
        }
        totalBytePckCount = totalByte / singlePckDataLen;
        //如果长度不能被整除 那就多一包数据咯
        if (totalByte % singlePckDataLen != 0) {
            totalBytePckCount += 1;
        }
    }


    private void transport() {
        if (isTransportIng) {
            ycBleLog.e("当前正在传输图片的嘛");
            return;
        } else {
            isTransportIng = true;
            next();
        }
    }


    public void start() {
        transport();
    }

    public void next() {
        if (transportIndex < totalBytePckCount) {
            int tmpIndex = transportIndex;
            loadData(tmpIndex);
            transportIndex++;
        } else {
            ycBleLog.e("加载完成了");
            if (receiveImageDataCallback != null) {
                receiveImageDataCallback.onFinish();
            }
        }
    }

    public void withNext(int index) {
        transportIndex = index;
        if (transportIndex < totalBytePckCount) {
            int tmpIndex = transportIndex;
            loadData(tmpIndex);
            transportIndex++;
        } else {
            ycBleLog.e("加载完成了");
            if (receiveImageDataCallback != null) {
                receiveImageDataCallback.onFinish();
            }
        }
    }


    private void loadData(int index) {
//        ycBleLog.e("加载===" + index);
        byte data[] = new byte[18];
        System.arraycopy(imageByteArray, index * singlePckDataLen, data, 0, data.length);
        if (receiveImageDataCallback != null) {
            receiveImageDataCallback.onImageDataReceive(index, data);
        }

    }


    public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap != null) {
            int bmpWidth = bitmap.getWidth();
            int bmpHeight = bitmap.getHeight();

            float scaleW = ((float) width * 1.0f) / bmpWidth;
            float scaleH = ((float) height * 1.0f) / bmpHeight;
            Matrix matrix = new Matrix();
//            matrix.setScale(1, 1);//水平翻转
            matrix.postScale(scaleW, scaleH);
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
            return resizedBitmap;
        } else {
            return null;
        }
    }


    public void bitmap2RGB(Bitmap bitmap) {
        //实际的bitmap的大小 ARGB_8888
        int[] bitmap_data = new int[bitmap.getWidth() * bitmap.getHeight()];

        bitmap.getPixels(bitmap_data, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        Bitmap resultBmp = null;
        switch (colorCfg) {
            case RGB_556:
            default:
                resultBmp = Bitmap.createBitmap(bitmap_data, imageWidth, imageHeight, Bitmap.Config.RGB_565);
                break;
        }

        int bytes = resultBmp.getByteCount();
        ycBleLog.e("debug===分配的数据长度是:" + bytes);

        ByteBuffer buf = ByteBuffer.allocate(bytes);
        resultBmp.copyPixelsToBuffer(buf);
        imageByteArray = buf.array();

        ycBleLog.e("=======================");
        ycBleLog.e("imageByteArray:" + BleUtil.byte2HexStr(imageByteArray));
        ycBleLog.e("=======================");
        ycBleLog.e("debug===转换后的是:" + imageByteArray.length);


    }


    private ReceiveImageDataCallback receiveImageDataCallback = null;

    public void setReceiveImageDataCallback(ReceiveImageDataCallback receiveImageDataCallback) {
        this.receiveImageDataCallback = receiveImageDataCallback;
    }
}
