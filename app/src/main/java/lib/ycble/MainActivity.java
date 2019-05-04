package lib.ycble;

import android.app.Activity;
import android.os.Bundle;

import ycble.runchinaup.device.BleDevice;
import ycble.runchinaup.log.ycBleLog;
import ycble.runchinaup.ota.FirmType;
import ycble.runchinaup.ota.OTAHelper;
import ycble.runchinaup.ota.callback.OTACallback;


public class MainActivity extends Activity {


    String mac = "45:90:78:56:34:75";

    String file1Path = "/storage/emulated/0/Download/Bluetooth/826x_ble_W28-1.6.1-1.bin";
    String file2Path = "/storage/emulated/0/Download/Bluetooth/826x_ble_W28-1.7.0-2.bin";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BleDevice bleDevice = new BleDevice("W28", mac);

        OTAHelper.getInstance().startOTA(this, file1Path, bleDevice, FirmType.TELINK, new OTACallback() {
            @Override
            public void onFailure(String message) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onProgress(int progress) {
                ycBleLog.e("progress===>" + progress);
            }
        });


    }


}
