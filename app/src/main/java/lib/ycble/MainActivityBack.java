//package lib.ycble;
//
//import android.Manifest;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Environment;
//import android.support.annotation.RequiresApi;
//import android.support.v7.app.AppCompatActivity;
//
//import ycble.runchinaup.aider.PushAiderHelper;
//import ycble.runchinaup.core.BleScaner;
//
//
//public class MainActivityBack extends AppCompatActivity {
//
////    BleManager bleManager = BleManager.getBleManager();
////    String mac = "C9:6F:C2:DA:2C:A1";
////    String mac = "D2:BE:1A:8D:55:C8";
//
//
//    private BleScaner bleScaner = BleScaner.getBleScaner();
//
//    //    String appFileStringPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/htx/A7.bin";
//    String appFileStringPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/htx/A7_2.bin";
//
//    //    String mDeviceAddress = "D2:7A:23:98:2F:09";
//    String mDeviceAddress = "C0:08:22:C7:91:4D";
////    String mDeviceAddress = "E7:E6:A2:82:3A:5F";
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        if (!PushAiderHelper.getAiderHelper().isNotifyEnable()) {
//            PushAiderHelper.getAiderHelper().goToSettingNotificationAccess();
//        }
//
//        requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE}, 100);
////        bleManager.scanAndConn(mac, 0);
////
////        bleScaner.setBleDeviceFilter(MyDeviceFilter.getInstance());
////        bleScaner.registerScanListener(new ScanListener() {
////            @Override
////            public void onScan(BleDevice bleDevice) {
////                ycBleLog.e("bleDevice==>"+bleDevice.toString());
////            }
////        });
////        bleScaner.startScan();
//
////        if (!AiderHelper.getAiderHelper().isNotifyEnable()) {
////            AiderHelper.getAiderHelper().enableAppLiveFunction(true);
////        }
////
////        OTAHelper.getInstance().startOTA(this, appFileStringPath, mDeviceAddress, "", FirmType.HTX, new OTACallback() {
////            @Override
////            public void onFailure(String message) {
////                ycBleLog.e(message + "ota失败了");
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        Toast.makeText(MainActivity.this, "ota失败！！！！！", 0).show();
////                    }
////                });
////            }
////
////            @Override
////            public void onSuccess() {
////                ycBleLog.e(  "ota成功了");
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        Toast.makeText(MainActivity.this, "ota成功", 0).show();
////                    }
////                });
////            }
////
////            @Override
////            public void onProgress(int progress) {
////                ycBleLog.e("onProgress====>" + progress);
////            }
////        });
//    }
//
//
//}
