package lib.ycble;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import ycble.runchinaup.core.BleScaner;
import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.ota.FirmType;
import ycble.runchinaup.ota.OTAHelper;
import ycble.runchinaup.ota.callback.OTACallback;


public class MainActivity extends AppCompatActivity {

//    BleManager bleManager = BleManager.getBleManager();
//    String mac = "C9:6F:C2:DA:2C:A1";
//    String mac = "D2:BE:1A:8D:55:C8";


    private BleScaner bleScaner = BleScaner.getBleScaner();

    //    String appFileStringPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/htx/A7.bin";
    String appFileStringPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/htx/A7_2.bin";

    String mDeviceAddress = "D2:7A:23:98:2F:09";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        bleManager.scanAndConn(mac, 0);
//
//        bleScaner.setBleDeviceFilter(MyDeviceFilter.getInstance());
//        bleScaner.registerScanListener(new ScanListener() {
//            @Override
//            public void onScan(BleDevice bleDevice) {
//                ycBleLog.e("bleDevice==>"+bleDevice.toString());
//            }
//        });
//        bleScaner.startScan();

//        if (!AiderHelper.getAiderHelper().isNotifyEnable()) {
//            AiderHelper.getAiderHelper().enableAppLiveFunction(true);
//        }

        OTAHelper.getInstance().startOTA(this, appFileStringPath, mDeviceAddress, "", FirmType.HTX, new OTACallback() {
            @Override
            public void onFailure(String message) {

            }

            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "ota成功", 0).show();
                    }
                });
            }

            @Override
            public void onProgress(int progress) {
                ycBleLog.e("onProgress====>" + progress);
            }
        });
    }


}
