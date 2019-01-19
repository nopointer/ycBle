package lib.ycble.bleModule.imageTransport;

/**
 * 抽象的原始数据对象，用于获取图片的数据，作为抽象的对象,具体包头要添加的数据，在其实现类里面完成
 */
public interface AbsReceiveImageDataCallback {

    void onImageDataReceive(int transportIndex, byte[] imageData);

    void onFinish();
}
