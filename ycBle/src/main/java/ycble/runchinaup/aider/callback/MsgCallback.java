package ycble.runchinaup.aider.callback;

import ycble.runchinaup.aider.MsgType;

/**
 * Created by nopointer on 2018/7/26.
 * 消息回调处理
 */

public interface MsgCallback {

    /**
     * app通知消息来了,里面内置了常见的社交app类型，如果不能满足用户的需求，开发者可以根据 packName 自定义推送
     *
     * @param packName   应用包名
     * @param msgType    常见的社交app
     * @param from       消息发送方
     * @param msgContent 消息内容
     */
    void onAppMsgReceive(String packName, MsgType msgType, String from, String msgContent);

    /**
     * 用户处理结果，一般用于手环通知提醒，也有某些手环要求，挂断了或者接受了电话，就要清除手环的界面提醒
     *
     * @param phoneNumber
     * @param contactName
     * @param userHandResult 0，还在响铃阶段，用户未处理，1接听了，2挂断了
     */
    void onPhoneInComing(String phoneNumber, String contactName, int userHandResult);

    /**
     * 短信来了
     *
     * @param phoneNumber    短信发送方的号码
     * @param contactName    短信发送方的姓名，如果没有的话 就是号码
     * @param messageContent 短信内容
     */
    void onMessageReceive(String phoneNumber, String contactName, String messageContent);

}
