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

public class GuardLiveService extends Service implements TimeChangeReceiver.OnTimeChangeCallback {


    private static String Tag = "GuardLiveService";

    private ServiceConnection serviceConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        TimeChangeReceiver.getInstance().register(this, this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new GuardLiveServiceBinder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logE("onStartCommand 开始 service");
        startAndBindMainBgService();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startAndBindMainBgService() {
        boolean isGuardRun = ServiceUtils.isServiceExisted(this, MainBackLiveService.class);
        logE("主进程运行的状态:" + isGuardRun);
        if (!isGuardRun) {
            logE("准备启动守护service");
            Intent intent = new Intent(new Intent(this, MainBackLiveService.class));
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder iBinder) {
                    RunServiceInterface process = RunServiceInterface.Stub.asInterface(iBinder);
                    logE("绑定主后台service成功了");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    logE("MainBackLiveService 远程服务挂掉了,远程服务被杀死");
                }

                @Override
                public void onBindingDied(ComponentName name) {
                    logE("MainBackLiveService 远程服务挂掉了,远程服务被杀死");
                }
            };
            startService(intent);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onTimeChange() {
        startAndBindMainBgService();
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


    public void debug(String message) {
        logE(message);
    }


    static void logE(String message) {
        Log.e(Tag, message);
    }



    public class GuardLiveServiceBinder extends RunServiceInterface.Stub {

        public GuardLiveService getService() {
            return GuardLiveService.this;
        }
    }




}
