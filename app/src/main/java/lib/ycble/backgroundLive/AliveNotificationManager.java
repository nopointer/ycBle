package lib.ycble.backgroundLive;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import lib.ycble.backgroundLive.utils.BackLog;

/**
 * 常驻通知栏管理器
 */
public class AliveNotificationManager {
    private static final AliveNotificationManager ourInstance = new AliveNotificationManager();

    //通知是否显示
    private boolean isNotifyShow = false;

    public boolean isNotifyShow() {
        return isNotifyShow;
    }

    public static AliveNotificationManager getInstance() {
        return ourInstance;
    }

    private AliveNotificationManager() {
    }


     private final int notificationId = 0x111;

    public int getNotificationId() {
        return notificationId;
    }

    /**
     * 关闭通知
     *
     * @param context
     */
    public void cancel(Context context) {
        isNotifyShow = false;
        NotificationManager notificationManager = getNotificationManager(context);
        notificationManager.cancel(notificationId);
    }

    /**
     * 关闭通知
     *
     * @param context
     * @param id
     */
    public void cancel(Context context, int id) {
        isNotifyShow = false;
        NotificationManager notificationManager = getNotificationManager(context);
        notificationManager.cancel(id);
    }


    /**
     * 获取通知栏管理器
     *
     * @param context
     * @return
     */
    public static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 发送通知
     *
     * @param context
     * @param notificationInfoBean
     */
    public void sendNotification(Context context, NotificationInfoBean notificationInfoBean) {
        isNotifyShow = true;
        Notification notification = createAppNotification(notificationInfoBean);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        getNotificationManager(context).notify(notificationId, notification);
    }

    /**
     * 发送通知
     *
     * @param context
     * @param notification
     */
    public void sendNotification(Context context, Notification notification) {
        isNotifyShow = true;
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        getNotificationManager(context).notify(notificationId, notification);
    }

    /**
     * 发送通知
     *
     * @param context
     * @param notificationInfoBean
     * @param id
     */
    public void sendNotification(Context context, NotificationInfoBean notificationInfoBean, int id) {
        isNotifyShow = true;
        Notification notification = createAppNotification(notificationInfoBean);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        getNotificationManager(context).notify(id, notification);
    }


    public static Notification createAppNotification(NotificationInfoBean notificationInfoBean) {
        if (notificationInfoBean == null || !notificationInfoBean.isDataOk()) {
            BackLog.e("notificationInfoBean 为空或者内容不完整，请填写各个参数");
        }
        PendingIntent pendingIntent = null;
        if (notificationInfoBean.getPendingActivity() != null) {
            pendingIntent = PendingIntent.getActivity(notificationInfoBean.getContext(), 0, new Intent(notificationInfoBean.getContext(), notificationInfoBean.getPendingActivity()), 0);
        }

        Notification.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_ID = "channel_id_" + notificationInfoBean.getTitle();
            final String CHANNEL_NAME = "channel_name_" + notificationInfoBean.getTitle();
            NotificationManager mManager = getNotificationManager(notificationInfoBean.getContext());
            /**
             * Oreo不用Priority了，用importance
             * IMPORTANCE_NONE 关闭通知
             * IMPORTANCE_MIN 开启通知，不会弹出，但没有提示音，状态栏中无显示
             * IMPORTANCE_LOW 开启通知，不会弹出，不发出提示音，状态栏中显示
             * IMPORTANCE_DEFAULT 开启通知，不会弹出，发出提示音，状态栏中显示
             * IMPORTANCE_HIGH 开启通知，会弹出，发出提示音，状态栏中显示
             */
            NotificationChannel notificationChannel = new
                    NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW); //如果这里用IMPORTANCE_NOENE就需要在系统的设置里面开启渠道， //通知才能正常弹出
            mManager.createNotificationChannel(notificationChannel);
            builder = new Notification.Builder(notificationInfoBean.getContext(), CHANNEL_ID);
        } else {
            builder = new Notification.Builder(notificationInfoBean.getContext());
        }
        builder.setShowWhen(true);
        builder.setContentText(notificationInfoBean.getMessage())
                .setContentTitle(notificationInfoBean.getTitle())
                .setSmallIcon(notificationInfoBean.getSmallIconResId());
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        return builder.build();
    }

}
