package lib.ycble.backgroundLive;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import lib.ycble.backgroundLive.extra.TimeChangeReceiver;
import lib.ycble.backgroundLive.utils.ServiceUtils;

/**
 * 后台运行的主service
 */
public class MainBackLiveService extends Service implements TimeChangeReceiver.OnTimeChangeCallback {

    private static String Tag = "MainBackLiveService";

    private ServiceConnection serviceConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        logE("onCreate 创建service");
        TimeChangeReceiver.getInstance().register(this, this);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MainBackLiveServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logE("onStartCommand 开始 service");
        startAndBindGuardService();
        return super.onStartCommand(intent, flags, startId);
    }


    public void debug(String message) {
        logE(message);
    }


    static void logE(String message) {
        Log.e(Tag, message);
    }

    private void startAndBindGuardService() {
        boolean isGuardRun = ServiceUtils.isServiceExisted(this, GuardLiveService.class);
        logE("守护进程运行的状态:" + isGuardRun);
        if (!isGuardRun) {
            logE("准备启动守护service");
            Intent intent = new Intent(new Intent(this, GuardLiveService.class));
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder iBinder) {
                    RunServiceInterface process = RunServiceInterface.Stub.asInterface(iBinder);
                    logE("绑定守护service成功了");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    logE("GuardLiveService 远程服务挂掉了,远程服务被杀死");
                }

                @Override
                public void onBindingDied(ComponentName name) {
                    logE("GuardLiveService 远程服务挂掉了,远程服务被杀死");
                }
            };
            startService(intent);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onTimeChange() {
        startAndBindGuardService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (serviceConnection != null) {
                unbindService(serviceConnection);
            }
            TimeChangeReceiver.getInstance().unRegister(this);
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
