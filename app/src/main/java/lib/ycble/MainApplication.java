package lib.ycble;

import android.app.Application;

import ycble.runchinaup.core.ycBleSDK;

public class MainApplication extends Application {


    public static MainApplication mainApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();
        ycBleSDK.initSDK(this);
        mainApplication = this;
//        PushAiderHelper.getAiderHelper().setMsgReceiveCallback(new MsgCallback() {
//            @Override
//            public void onAppMsgReceive(String packName, MsgType msgType, String from, String msgContent) {
//
//            }
//
//            @Override
//            public void onPhoneInComing(String phoneNumber, String contactName, int userHandResult) {
//
//            }
//
//            @Override
//            public void onMessageReceive(String phoneNumber, String contactName, String messageContent) {
//
//            }
//        });

//        NPContactsUtil.queryContact(this,"136 3169 7178");
    }

    public static MainApplication getMainApplication() {
        return mainApplication;
    }


}
