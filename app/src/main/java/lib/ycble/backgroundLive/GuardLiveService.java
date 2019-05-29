package lib.ycble.backgroundLive;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

import lib.ycble.backgroundLive.extra.AliveBroadcastReceiver;
import lib.ycble.backgroundLive.utils.BackLog;
import lib.ycble.backgroundLive.utils.ServiceUtils;

/**
 * 守护进程，用来启动主后台进程
 */
public class GuardLiveService extends Service implements AliveBroadcastReceiver.OnBroadcastReceiveCallback {


    private ServiceConnection serviceConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        //注册时间变化的广播监听
        AliveBroadcastReceiver.getInstance().register(this,this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new GuardLiveServiceBinder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BackLog.e("onStartCommand 开始 service"+GuardLiveService.this);
        startAndBindMainBgService();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 开始或者绑定主后台进程
     */
    private void startAndBindMainBgService() {
        boolean isGuardRun = ServiceUtils.isServiceExisted(this, MainBackLiveService.class);
        BackLog.e("主进程运行的状态:" + isGuardRun);
        if (!isGuardRun) {
            BackLog.e("准备启动守护service");
            Intent intent = new Intent(new Intent(this, MainBackLiveService.class));
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder iBinder) {
                    RunServiceInterface process = RunServiceInterface.Stub.asInterface(iBinder);
                    BackLog.e("绑定主后台service成功了");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    BackLog.e("MainBackLiveService 远程服务挂掉了,远程服务被杀死");
                }

                @Override
                public void onBindingDied(ComponentName name) {
                    BackLog.e("MainBackLiveService 远程服务挂掉了,远程服务被杀死");
                }
            };
            startService(intent);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onBroadcastReceive(String action) {
        startAndBindMainBgService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (serviceConnection != null) {
                unbindService(serviceConnection);
            }
            AliveBroadcastReceiver.getInstance().unRegister(this,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    



    public class GuardLiveServiceBinder extends RunServiceInterface.Stub {

        public GuardLiveService getService() {
            return GuardLiveService.this;
        }
    }


}
