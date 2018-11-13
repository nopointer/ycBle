package ycble.runchinaup.aider.sms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashMap;

import ycble.runchinaup.log.ycBleLog;


public class NPSmsUtil {
    private NPSmsUtil() {
    }

    //内容提供者路径1，所有的短信
    public static final Uri smsPath1 = Uri.parse("content://sms");

    //内容提供者路径2，收件箱
    public static final Uri smsPathInBox = Uri.parse("content://sms/inbox");

    //内容提供者路径3，所有的短信，4.4以后的吧
    public static final Uri smsPath3 = Uri.parse("content://mms");


    //最后一条短信的内容
    private static String strLastMessage;
    //最后一条短信的发件人
    private static String strLastPerson;


    public static synchronized void reLoadSms(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();

        ycBleLog.e("debug===uri==>" + uri);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

            Cursor cursor = contentResolver.query(uri, null, null, null, "date desc limit 0,2");
            if (cursor == null) return;
            while (cursor.moveToNext()) {
                int count = cursor.getColumnCount();
                HashMap<String, String> map = new HashMap<>();
                for (int i = 0; i < count; i++) {
                    try {
                        String name = cursor.getColumnName(i);
                        String value = cursor.getString(cursor.getColumnIndex(name));
                        map.put(name, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ycBleLog.e("json:" + map.toString());
            }
        }
    }

}
