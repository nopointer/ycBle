package lib.ycble;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import lib.ycble.bleModule.BleManager;
import ycble.runchinaup.core.BleConnState;
import ycble.runchinaup.core.BleScaner;
import ycble.runchinaup.core.callback.BleConnCallback;


public class MainActivity extends Activity {


    String mac = "45:90:78:56:34:75";

    String file1Path = "/storage/emulated/0/Download/Bluetooth/826x_ble_W28-1.6.1-1.bin";
    String file2Path = "/storage/emulated/0/Download/Bluetooth/826x_ble_W28-1.7.0-2.bin";


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        }, 100);
//        if (!PushAiderHelper.getAiderHelper().isNotifyEnable(this)) {
//            PushAiderHelper.getAiderHelper().goToSettingNotificationAccess(this);
//        } else {
//            PushAiderHelper.getAiderHelper().start(this);
//        }
//        BleScaner.getBleScaner().startScan(this);

        mac = "D2:A6:3E:E0:B6:1E";
        BleManager.getBleManager().scanAndConn(mac, 0);
        BleManager.getBleManager().registerConnCallback(new BleConnCallback() {
            @Override
            public void onConnState(BleConnState bleConnState) {
//                if (!bleConnState.equals(BleConnState.CONNECTED)) {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            BleManager.getBleManager().scanAndConn(mac, 0);
//                        }
//                    }, 3000);
//                }
            }
        });
//        startService(new Intent(this, BgService.class));


//        BleDevice bleDevice = new BleDevice("W28", mac);
//
//        OTAHelper.getInstance().startOTA(this, file1Path, bleDevice, FirmType.TELINK, new OTACallback() {
//            @Override
//            public void onFailure(String message) {
//
//            }
//
//            @Override
//            public void onSuccess() {
//
//            }
//
//            @Override
//            public void onProgress(int progress) {
//                ycBleLog.e("progress===>" + progress);
//            }
//        });

//        startService(new Intent(this, MainBackLiveService.class));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleScaner.getBleScaner().stopScan();
    }
}
