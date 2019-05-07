/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ycble.runchinaup.log;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

import ycble.runchinaup.util.PhoneDeviceUtil;

/**
 * ble日志输出管理工具
 * 最后修改日期np 2018-11-7 11：52
 */
public class ycBleLog {

    public static final String npBleTag = "ycBleTag";

    static String logMac = "";


    //app的名字
    static String appName = "ycBleLog";

    public static void initLogDirName(String appBleLogDirName) {
        Log.e("initBleLogDir", "初始化文件夹名称" + appBleLogDirName);
        appName = appBleLogDirName;
    }


    public static File getBleLogFileDir() {
        File appDir = new File(Environment.getExternalStorageDirectory(), "ycBleLogs/" + appName);
        return appDir;
    }


    private ycBleLog() {
    }

    public static boolean allowD = true;
    public static boolean allowE = true;
    public static boolean allowI = true;
    public static boolean allowV = true;
    public static boolean allowW = true;
    public static boolean allowWtf = true;
    //是否允许把日志文件写在本地文件里面
    public static boolean allowWriteLogToLocalFile = true;

    private static SimpleDateFormat smp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static void e(String content) {
        if (allowE) {
            Log.e(npBleTag, content);
        }
        if (TextUtils.isEmpty(logMac)) return;
        if (allowWriteLogToLocalFile) {
            String dateTime = smp.format(new Date());
            writeFile(dateTime + "  " + content);
        }
    }

    public static void w(String content) {
        if (!allowW) return;
        Log.w(npBleTag, content);
    }

    public static void i(String content) {
        if (!allowW) return;
        Log.i(npBleTag, content);
    }

    public static void d(String content) {
        if (!allowD) return;
        Log.d(npBleTag, content);
    }

    //记录日志
    public synchronized static void writeFile(String strLine) {
        // 首先创建文件夹
        File appDir = new File(Environment.getExternalStorageDirectory(), "ycBleLogs/" + appName + "/");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        String name = new SimpleDateFormat("yyyy-MM-dd______").format(new Date());
        String fileName = name + logMac + ".txt";
        File file = new File(appDir, fileName);
        if (!file.exists()) {
            //文件第一次创建的时候,追加一些额外信息，比如app版本和手机型号等等
            BufferedWriter fileOutputStream = null;
            try {
                fileOutputStream = new BufferedWriter(new FileWriter(file, true));
                fileOutputStream.write(gteAppInfo());
                fileOutputStream.newLine();
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //追加文件写的内容
        try {
            BufferedWriter fileOutputStream = new BufferedWriter(new FileWriter(file, true));
            fileOutputStream.write(strLine);
            fileOutputStream.newLine();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除并重新创建日志文件
     */
    public synchronized static void reCreateLogFile(String deviceMac) {
        logMac = deviceMac;
        File appDir = new File(Environment.getExternalStorageDirectory(), "ycBleLogs/" + appName);
        if (null == appDir) return;

        Log.e("===dir", appDir.getAbsolutePath());
        if (appDir.isDirectory()) {
            File file[] = appDir.listFiles();
            if (null == file) return;
            for (File f : file) {
                if (null == f) return;
                Log.e("===file", f.getAbsolutePath());
                f.delete();
            }
        }
    }

    /**
     * 删除日志文件
     */
    public synchronized static void clearLogFile() {
        File appDir = new File(Environment.getExternalStorageDirectory(), "ycBleLogs/" + appName);
        if (null == appDir) return;
        Log.e("===dir", appDir.getAbsolutePath());
        if (appDir.isDirectory()) {
            File file[] = appDir.listFiles();
            if (null == file) return;
            for (File f : file) {
                if (null == f) return;
                Log.e("===file", f.getAbsolutePath());
                f.delete();
            }
        }
    }

    /**
     * 向src文件添加header
     *
     * @param content
     * @param srcPath
     * @throws Exception
     */
    private static void appendFileHeader(String content, String srcPath) throws Exception {
        RandomAccessFile src = new RandomAccessFile(srcPath, "rw");
        int srcLength = (int) src.length();
        byte[] buff = new byte[srcLength];
        src.read(buff, 0, srcLength);
        src.seek(0);
        byte[] header = content.getBytes("utf-8");
        src.write(header);
        src.seek(header.length);
        src.write(buff);
        src.close();
    }


    //获取app的版本 和手机信息
    private static String gteAppInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("手机品牌:" + PhoneDeviceUtil.getDeviceBrand()).append("\n");
        stringBuilder.append("手机型号:" + PhoneDeviceUtil.getSystemModel()).append("\n");
        stringBuilder.append("安卓版本:" + PhoneDeviceUtil.getSystemVersion()).append("\n");
        stringBuilder.append("语言环境:" + PhoneDeviceUtil.getSystemLanguage()).append("\n");
//        stringBuilder.append("手机IMEI:" + PhoneDeviceUtil.getIMEI(AiderHelper.getContext())).append("\n");
        return stringBuilder.toString();
    }

}
