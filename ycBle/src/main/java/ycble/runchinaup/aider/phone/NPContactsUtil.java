package ycble.runchinaup.aider.phone;

import android.Manifest;
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
import ycble.runchinaup.util.PhoneDeviceUtil;


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
    static synchronized void reLoadData(Context context) {
        try {
            tmpList.clear();
            Uri uri = Data.CONTENT_URI; // 联系人Uri；
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, Data.RAW_CONTACT_ID);
            if (cursor == null) {
                ycBleLog.e("联系人列表为空");
                return;
            } else {
                ycBleLog.e("联系人列表不为空");
            }
            while (cursor.moveToNext()) {
                try {
                    String mimeType = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
                    if (!mimeType.equals(phoneDataMimeType)) {
                        continue;
                    }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取联系人姓名
     *
     * @param phoneNumber
     * @return
     */
    @Deprecated
    private synchronized static String getContactName(String phoneNumber) {
        ycBleLog.e("联系人的号码是(带rom自定义的前缀):" + phoneNumber);
        //这里要先去除带了特殊格式的号码，把他变成一个连续的数字 即常规的手机号码
        if (phoneNumber.startsWith("86")) {
            phoneNumber = phoneNumber.substring(2);
        } else if (phoneNumber.startsWith("+86")) {
            phoneNumber = phoneNumber.substring(3);
        }
        phoneNumber = phoneNumber.replace("+", "").replace("-", "").replace(" ", "");

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
            //拿到通讯录的手机号码 也去除一下特殊的符号
            if (phone.startsWith("86")) {
                phone = phone.substring(2);
            } else if (phone.startsWith("+86")) {
                phone = phone.substring(3);
            }
            phone = phone.replace(" ", "").replace("+", "").replace("-", "");
            if (phone.equalsIgnoreCase(phoneNumber)) {
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
        ycBleLog.e("联系人信息json:" + jsonObject.toString());
    }


    /**
     * 查询联系人
     *
     * @param number 手机号码 先把这个手机号码过滤一下，变成普通的手机号码，然后再变成不同厂商喜欢的号码格式
     */
    public static String queryContact(Context context, String number) {

        boolean hasPermission = PhoneDeviceUtil.hasPermissions(context, new String[]{Manifest.permission.READ_CONTACTS});
        if (!hasPermission) {
            ycBleLog.e("没有 Manifest.permission.READ_CONTACTS 权限！！！，返回原始号码");
            return number;
        }
        number = number.replace(" ", "").replace("-", "");
        if (number.startsWith("+86")) {
            number = number.substring(3);
        }
        if (number.startsWith("86")) {
            number = number.substring(2);
        }

        String result = new String(number);
        ycBleLog.e("处理过后的号码格式是:" + result);

        String numberFormat0 = number;
        String numberFormat1 = "86" + number;
        String numberFormat2 = "+86" + number;
        String numberFormat3 = number;
        String numberFormat4 = number;
        String numberFormat5 = number;
        String numberFormat6 = number;
        String numberFormat7 = number;
        String numberFormat8 = number;
        String numberFormat9 = number;
        if (number.length() == 11) {
            numberFormat3 = number.substring(0, 3) + " " + number.substring(3, 7) + " " + number.substring(7);
            numberFormat4 = "86" + numberFormat3;
            numberFormat5 = "+86" + numberFormat3;
            numberFormat6 = "86 " + numberFormat3;
            numberFormat7 = "+86 " + numberFormat3;
            numberFormat8 = " 86 " + numberFormat3;
            numberFormat9 = " +86 " + numberFormat3;
        }

        Uri uri = Data.CONTENT_URI; // 联系人Uri；

        String selectionSql = new StringBuilder()
                .append(Data.DATA1).append("=? or ")
                .append(Data.DATA1).append("=? or ")
                .append(Data.DATA1).append("=? or ")
                .append(Data.DATA1).append("=? or ")
                .append(Data.DATA1).append("=? or ")
                .append(Data.DATA1).append("=? or ")
                .append(Data.DATA1).append("=? or ")
                .append(Data.DATA1).append("=? or ")
                .append(Data.DATA1).append("=? or ")
                .append(Data.DATA1).append("=? or ")
                .append(Data.DATA4).append("=? or ")
                .append(Data.DATA4).append("=? or ")
                .append(Data.DATA4).append("=? or ")
                .append(Data.DATA4).append("=? or ")
                .append(Data.DATA4).append("=? or ")
                .append(Data.DATA4).append("=? or ")
                .append(Data.DATA4).append("=? or ")
                .append(Data.DATA4).append("=? or ")
                .append(Data.DATA4).append("=? or ")
                .append(Data.DATA4).append("=?")
                .toString();

        String[] selectionArgs = new String[]{
                numberFormat0, numberFormat1, numberFormat2, numberFormat3, numberFormat4, numberFormat5, numberFormat6, numberFormat7, numberFormat8, numberFormat9,
                numberFormat0, numberFormat1, numberFormat2, numberFormat3, numberFormat4, numberFormat5, numberFormat6, numberFormat7, numberFormat8, numberFormat9};

//        selectionSql = null;
//        selectionArgs = null;


        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{Data.DISPLAY_NAME, Data.DISPLAY_NAME_ALTERNATIVE, Data.DATA1, Data.DATA4},
                selectionSql, selectionArgs, Data.RAW_CONTACT_ID);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String display_name_alt = cursor.getString(cursor.getColumnIndex("display_name_alt"));
                String display_name = cursor.getString(cursor.getColumnIndex("display_name"));
                if (!TextUtils.isEmpty(display_name)) {
                    result = display_name;
                } else {
                    if (!TextUtils.isEmpty(display_name_alt)) {
                        result = display_name_alt;
                    }
                }
                break;
            }
            cursor.close();
        }
        ycBleLog.e("联系人姓名查询结果:" + result);
        return result;
    }


}
