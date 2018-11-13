package ycble.runchinaup.aider.phone;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ycble.runchinaup.aider.entity.NPContactEntity;
import ycble.runchinaup.log.ycBleLog;

/**
 * 通讯录工具
 */
public final class NPContactsUtil {

    private NPContactsUtil() {
    }

    //联系人的信息集合
    private static List<NPContactEntity> npContactInfoList = new ArrayList<>();
    //做缓存用的
    private static List<NPContactEntity> tmpList = new ArrayList<>();
    //纯数字正则匹配
    private static final String NUMBER_RULE = "/^\\d+$/";
    //存在通讯录数据的mimetype
    private static final String phoneDataMimeType = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;

    /**
     * 获取手机系统联系人的列表
     * 这个系统的数据库，不知道怎么存放数据的，数据比较杂乱，不同的手机，也有不同的字段，取出来 操蛋得很，再次呼吁安卓大统
     * 不逼逼了，说正事:
     * 目前就我所调试的几部手机来看，下面这个mimetype存放了比较完整的联系人的信息，先用着吧
     * ：CONTENT_ITEM_TYPE="vnd.android.cursor.item/phone_v2",
     * 这里有个他娘的恶心点，data1/data4 能拿到联系人数据（号码），但是你不确定他是号吗还是其他啥的，号码有可能有区号，也有可能中间有空格,
     * display_name_alt/display_name 能拿到联系人数据（名称），还能怎么办，不能原谅他呗
     *
     * @param context
     * @throws JSONException
     */
    public static synchronized void reLoadData(Context context) {
        try {
            tmpList.clear();
            Uri uri = Data.CONTENT_URI; // 联系人Uri；
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, Data.RAW_CONTACT_ID);
            while (cursor.moveToNext()) {
                String mimeType = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
                if (!mimeType.equals(phoneDataMimeType)) {
                    continue;
                }
//            debugCursor(cursor);
                try {
                    String display_name_alt = cursor.getString(cursor.getColumnIndex("display_name_alt"));
                    String display_name = cursor.getString(cursor.getColumnIndex("display_name"));
                    String data1 = cursor.getString(cursor.getColumnIndex("data1"));
                    String data4 = cursor.getString(cursor.getColumnIndex("data4"));
                    tmpList.add(new NPContactEntity(display_name_alt, display_name, data1, data4));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            npContactInfoList.clear();
            npContactInfoList.addAll(tmpList);
            tmpList.clear();
        } catch (SecurityException e) {

        }
    }

    /**
     * 获取联系人姓名
     *
     * @param phoneNumber
     * @return
     */
    public synchronized static String getContactName(String phoneNumber) {
        phoneNumber = phoneNumber.replace("-", "").replace("+", "").replace(" ", "");
        if (npContactInfoList == null || npContactInfoList.size() < 1) return phoneNumber;
        NPContactEntity tmpContact = null;
        for (NPContactEntity contactInfo : npContactInfoList) {
            if (TextUtils.isEmpty(contactInfo.getData1()) && TextUtils.isEmpty(contactInfo.getData4())) {
                return phoneNumber;
            }
            String phone = contactInfo.getData4();
            if (TextUtils.isEmpty(phone)) {
                phone = contactInfo.getData1();
            }
            phone = phone.replace(" ", "").replace("+", "").replace("-", "");
            if (phone.indexOf(phoneNumber) != -1) {
                tmpContact = contactInfo;
                break;
            }
        }
        if (tmpContact != null) {
            if (tmpContact.getDisplay_name().matches(NUMBER_RULE)) {
                if (tmpContact.getDisplay_name_alt().matches(NUMBER_RULE)) {
                    return phoneNumber;
                } else {
                    return tmpContact.getDisplay_name_alt();
                }
            } else {
                return tmpContact.getDisplay_name();
            }
        }
        return phoneNumber;
    }


    public static List<NPContactEntity> getNpContactInfoList() {
        return npContactInfoList;
    }


    private static void debugCursor(Cursor cursor) {
        if (cursor == null) return;
        HashMap<String, String> jsonObject = new HashMap<>();
        int len = cursor.getColumnCount();
        for (int i = 0; i < len; i++) {
            try {
                String name = cursor.getColumnName(i);
                jsonObject.put(name, cursor.getString(cursor.getColumnIndex(name)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ycBleLog.e("json:" + jsonObject.toString());
    }
}
