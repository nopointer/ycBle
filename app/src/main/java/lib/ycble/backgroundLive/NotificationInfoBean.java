package lib.ycble.backgroundLive;

import android.content.Context;
import android.text.TextUtils;

import java.io.Serializable;

public class NotificationInfoBean implements Serializable {

    private Context context;
    //通知栏标题
    private String title;
    //app图标
    private int smallIconResId;
    //通知消息
    private String message;
    //点击后跳转的activity
    private Class pendingActivity;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSmallIconResId() {
        return smallIconResId;
    }

    public void setSmallIconResId(int smallIconResId) {
        this.smallIconResId = smallIconResId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Class getPendingActivity() {
        return pendingActivity;
    }

    public void setPendingActivity(Class pendingActivity) {
        this.pendingActivity = pendingActivity;
    }

    public boolean isDataOk() {
        if (context == null) return false;
        if (TextUtils.isEmpty(title)) return false;
        if (TextUtils.isEmpty(message)) return false;
        if (smallIconResId == 0) return false;
        return true;
    }


}
