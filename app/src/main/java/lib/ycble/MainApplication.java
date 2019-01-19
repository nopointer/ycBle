package lib.ycble;

import android.app.Application;

import ycble.runchinaup.aider.AiderHelper;
import ycble.runchinaup.core.ycBleSDK;

public class MainApplication extends Application {


    public static  MainApplication mainApplication =null;
    @Override
    public void onCreate() {
        super.onCreate();
        ycBleSDK.initSDK(this);
        AiderHelper.getAiderHelper().enableAppLiveFunction(true);
        mainApplication =this;
    }

    public static MainApplication getMainApplication() {
        return mainApplication;
    }
}
