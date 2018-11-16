package ycble.runchinaup.ota.absimpl.nordic;

import android.app.Activity;

import no.nordicsemi.android.dfu.DfuBaseService;

public abstract class DfuServiceAbs extends DfuBaseService {


    @Override
    protected abstract Class<? extends Activity> getNotificationTarget();

    @Override
    protected abstract boolean isDebug();
}