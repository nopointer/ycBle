package ycble.runchinaup.aider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ycble.runchinaup.log.ycBleLog;


/**
 * Created by nopointer on 2018/7/26.
 * 通知栏消息监听工具
 */

final class NotificationMsgUtil {

    //读取通知栏消息
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    //设置打开通知栏读取权限
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    /**
     * 判断消息栏通知权限是否授权
     *
     * @param context
     * @return
     */
    public static boolean isEnabled(Context context) {

        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        debug(packageNames);
        if (packageNames.contains(context.getPackageName())) {
            return true;
        }
        return false;
    }


    private static void debug(Set<String> packageNames) {
        List<String> list = new ArrayList<>();
        for (String string : packageNames) {
            list.add(string);
        }
        ycBleLog.e("获取了通知栏监听权限的应用包名:" + list.toString());
    }

    /**
     * 前往设置允许通知栏权限
     *
     * @param context
     */
    public static void goToSettingNotificationAccess(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void goToSettingAccessibility(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //确认NotificationMonitor是否开启
    public static void ensureCollectorRunning(Context context) {
        startNotifyService(context);
    }


    //重新绑定service
    public static void reBindService(Context context) {
        ycBleLog.e("reBindService==>NPNotificationService");
        ComponentName thisComponent = new ComponentName(context, NPNotificationService.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static void startNotifyService(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, NPNotificationService.class));
            context.startForegroundService(new Intent(context, NPAccessibilityService.class));
        }
        context.startService(new Intent(context, NPNotificationService.class));
        context.startService(new Intent(context, NPAccessibilityService.class));
    }

    public static void stopNotifyService(Context context) {
        context.stopService(new Intent(context, NPNotificationService.class));
//        context.stopService(new Intent(context, NPAccessibilityService.class));
    }

}
