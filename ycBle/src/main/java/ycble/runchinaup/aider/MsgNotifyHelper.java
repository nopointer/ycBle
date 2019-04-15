package ycble.runchinaup.aider;

import ycble.runchinaup.aider.callback.MsgCallback;

/**
 * Created by nopointer on 2018/7/26.
 * 消息推送的管理者，并不是具体实现，只是起桥接作用，方便管理
 */

public final class MsgNotifyHelper {

    private MsgNotifyHelper() {
    }

    private static MsgNotifyHelper msgNotifyHelper = new MsgNotifyHelper();

    public static MsgNotifyHelper getMsgNotifyHelper() {
        return msgNotifyHelper;
    }

    //设置消息回调
    private MsgCallback msgCallback;

    public void setMsgCallback(MsgCallback msgCallback) {
        this.msgCallback = msgCallback;
    }

    public void onAppMsgReceiver(String pkhName, MsgType msgType, String from, String msgContent) {
        if (msgCallback != null) {
            msgCallback.onAppMsgReceive(pkhName, msgType, from, msgContent);
        }
    }

    public void onPhoneCallIng(String phoneNumber, String contactName, int userHandResult) {
        if (msgCallback != null) {
            msgCallback.onPhoneInComing(phoneNumber, contactName, userHandResult);
        }
    }

    public void onMessageReceive(String phoneNumber, String contactName, String messageContent) {
        if (msgCallback != null) {
            msgCallback.onMessageReceive(phoneNumber, contactName, messageContent);
        }
    }


}