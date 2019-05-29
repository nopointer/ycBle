//package lib.ycble.backgroundLive.extra;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//import lib.ycble.backgroundLive.GuardLiveService;
//import lib.ycble.backgroundLive.MainBackLiveService;
//import lib.ycble.backgroundLive.utils.ServiceUtils;
//
///**
// * 时间变化的监听器
// */
//public class TimeChangeStaticReceiver extends BroadcastReceiver {
//
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
//            boolean isRunMainBgService = ServiceUtils.isServiceExisted(context, MainBackLiveService.class);
//            boolean isRunGuardService = ServiceUtils.isServiceExisted(context, GuardLiveService.class);
//
//            if (!isRunMainBgService) {
//                context.startService(new Intent(context, MainBackLiveService.class));
//            }
//
//            if (!isRunGuardService) {
//                context.startService(new Intent(context, GuardLiveService.class));
//            }
//        }
//    }
//
//}
