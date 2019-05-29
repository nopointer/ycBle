package lib.ycble.backgroundLive;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

import lib.ycble.MainActivity;
import lib.ycble.R;
import lib.ycble.backgroundLive.extra.AliveBroadcastReceiver;
import lib.ycble.backgroundLive.utils.BackLog;
import lib.ycble.backgroundLive.utils.ServiceUtils;

/**
 * 后台运行的主service
 */
public class MainBackLiveService extends Service implements AliveBroadcastReceiver.OnBroadcastReceiveCallback {

    private static String Tag = "MainBackLiveService";

    private ServiceConnection serviceConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        BackLog.e("onCreate 创建service" + MainBackLiveService.this);
        AliveBroadcastReceiver.getInstance().register(this, this);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MainBackLiveServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BackLog.e("onStartCommand 开始 service" + MainBackLiveService.this);
        startAndBindGuardService();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startNotification() {
        NotificationInfoBean notificationInfoBean = new NotificationInfoBean();
        notificationInfoBean.setContext(this);
        notificationInfoBean.setTitle("Title");
        notificationInfoBean.setSmallIconResId(R.mipmap.ic_launcher);
        notificationInfoBean.setMessage("Message");
        notificationInfoBean.setPendingActivity(MainActivity.class);
        Notification notification = AliveNotificationManager.createAppNotification(notificationInfoBean);
        AliveNotificationManager.getInstance().sendNotification(this, notification);
        startForeground(AliveNotificationManager.getInstance().getNotificationId(), notification);
    }


    private void startAndBindGuardService() {
        boolean isGuardRun = ServiceUtils.isServiceExisted(this, GuardLiveService.class);
        BackLog.e("守护进程运行的状态:" + isGuardRun);
        if (!isGuardRun) {
            BackLog.e("准备启动守护service");
            Intent intent = new Intent(new Intent(this, GuardLiveService.class));
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder iBinder) {
                    RunServiceInterface process = RunServiceInterface.Stub.asInterface(iBinder);
                    BackLog.e("绑定守护service成功了");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    BackLog.e("GuardLiveService 远程服务挂掉了,远程服务被杀死");
                }

                @Override
                public void onBindingDied(ComponentName name) {
                    BackLog.e("GuardLiveService 远程服务挂掉了,远程服务被杀死");
                }
            };
            startService(intent);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        BackLog.e("通知是否可见:" + AliveNotificationManager.getInstance().isNotifyShow());
        if (!AliveNotificationManager.getInstance().isNotifyShow()) {
            startNotification();
        } else {

        }
    }

    @Override
    public void onBroadcastReceive(String action) {
        startAndBindGuardService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (serviceConnection != null) {
                unbindService(serviceConnection);
            }
            AliveBroadcastReceiver.getInstance().unRegister(this, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class MainBackLiveServiceBinder extends RunServiceInterface.Stub {

        public MainBackLiveService getService() {
            return MainBackLiveService.this;
        }
    }
}
