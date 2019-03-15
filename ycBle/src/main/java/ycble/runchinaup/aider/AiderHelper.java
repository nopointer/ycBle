package ycble.runchinaup.aider;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.ContactsContract;

import ycble.runchinaup.aider.callback.MsgCallback;
import ycble.runchinaup.aider.phone.NPContactsUtil;
import ycble.runchinaup.aider.phone.NPPhoneStateReceiver;
import ycble.runchinaup.aider.sms.NPSmsReciver;
import ycble.runchinaup.log.ycBleLog;

/**
 * Created by nopointer on 2018/7/26.
 * 辅助工具助手，在里面可以定义是否需要启用通知栏监听,来电监听，以及短信监听
 */

public final class AiderHelper {

    private MsgNotifyHelper notifyHelper = MsgNotifyHelper.getMsgNotifyHelper();

    private static Context mContext = null;

    public static Context getContext() {
        return mContext;
    }

    public static boolean isPrintContacts;

    private AiderHelper() {
    }


    private static AiderHelper aiderHelper = new AiderHelper();

    public static void init(final Context context) {
        mContext = context;
        new Thread(new Runnable() {
            @Override
            public void run() {
                NPContactsUtil.reLoadData(context);
            }
        }).start();

        try {
            //手机状态变化的监听
            context.getContentResolver().registerContentObserver(
                    ContactsContract.Contacts.CONTENT_URI, true, phoneObserver);

            //短信数据库变化的监听
//            context.getContentResolver().registerContentObserver(smsPath1, true, smsObserver);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    public static AiderHelper getAiderHelper() {
        return aiderHelper;
    }

    //是否需要启用app存活久一点的辅助，其实也没多大卵用
    public void enableAppLiveFunction(boolean enable) {
        if (enable) {
            NotificationMsgUtil.startNotifyService(mContext);
            registerReceiver();
        } else {
            NotificationMsgUtil.stopNotifyService(mContext);
            unregisterReceiver();
        }
    }

    //是否有通知栏监听消息的权限
    public boolean isNotifyEnable() {
        return NotificationMsgUtil.isEnabled(mContext);
    }

    //前往设置通知权限授权界面
    public void goToSettingNotificationAccess() {
        NotificationMsgUtil.goToSettingNotificationAccess(mContext);
    }

    //前往打开辅助功能
    public void goToSettingAccessibility() {
        NotificationMsgUtil.goToSettingAccessibility(mContext);
    }

    /**
     * 设置消息回调通知
     *
     * @param msgReceiveCakkback
     */
    public void setMsgReceiveCallback(MsgCallback msgReceiveCakkback) {
        notifyHelper.setMsgCallabck(msgReceiveCakkback);
    }

    //接收一些常用的广播接收器，用来激活通知栏，不让他挂掉
    private NPLiveReceiver npLiveReceiver = new NPLiveReceiver();
    //来电状态广播接收器
    private NPPhoneStateReceiver npPhoneStateReceiver = new NPPhoneStateReceiver();
    //短信接收广播接收器
    private NPSmsReciver npSmsReciver = new NPSmsReciver();

    //注册广播
    private void registerReceiver() {
        //常用的系统广播
        mContext.registerReceiver(npLiveReceiver, NPLiveReceiver.createIntentFilter());
        //注册来电状态的广播
        mContext.registerReceiver(npPhoneStateReceiver, NPPhoneStateReceiver.createIntentFilter());
        //注册短信接收的广播
        mContext.registerReceiver(npSmsReciver, NPSmsReciver.createIntentFilter());
    }


    private void unregisterReceiver() {
        try {
            mContext.unregisterReceiver(npLiveReceiver);
            mContext.unregisterReceiver(npPhoneStateReceiver);
            mContext.getContentResolver().unregisterContentObserver(phoneObserver);
//            mContext.unregisterReceiver(npSmsReciver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //手机来电状态观察者
    private static ContentObserver phoneObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            //这里就是联系人变化的相关操作，根据自己的 逻辑来处理
            ycBleLog.e("debug=phoneObserver=onChange==>");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NPContactsUtil.reLoadData(mContext);
                }
            }).start();
        }
    };


    //手机短信状态观察者，暂时不用，国内的贱商搞得收不到通知类的短信了，暂时没得玩
    private static ContentObserver smsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
//            ycBleLog.e("短信数据库有变化");
//            NPSmsUtil.reLoadSms(mContext,smsPath1);
        }
    };
}