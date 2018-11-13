package ycble.runchinaup.core;

import java.util.UUID;

//蓝牙指令的最小任务单元，适用于在连接后每次必做的流程
public class BleUnitTask {

    //读取数据
    public static final int TYPE_READ = 1;
    //默认写数据
    public static final int TYPE_WRITE = 2;
    //没有响应的写数据
    public static final int TYPE_WRITE_WITHOUT_RESP = 3;
    //打开通知
    public static final int TYPE_ENABLE_NOTIFY = 4;
    //打开指示
    public static final int TYPE_ENABLE_INDICATE = 5;
    //关闭通知或者指示
    public static final int TYPE_DISABLE_NOTIFY_OR_INDICATE = 6;

    //
    public String msg;
    private UUID U_service;
    private UUID U_chara;
    private int optionType;
    private byte[] data;

    public int getOptionType() {
        return optionType;
    }

    public UUID getU_service() {
        return U_service;
    }

    public UUID getU_chara() {
        return U_chara;
    }

    public byte[] getData() {
        if (data == null) {
            if (getOptionType() == TYPE_ENABLE_NOTIFY) {
                return new byte[]{01, 00};
            } else if (getOptionType() == TYPE_ENABLE_INDICATE) {
                return new byte[]{02, 00};
            } else if (getOptionType() == TYPE_DISABLE_NOTIFY_OR_INDICATE) {
                return new byte[]{00, 00};
            } else {
                data = new byte[1];
            }
        }
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    private BleUnitTask(UUID u_service, UUID u_chara) {
        U_service = u_service;
        U_chara = u_chara;
    }

    //创建一个读的任务
    public static BleUnitTask createRead(UUID u_service, UUID u_chara, String... msg) {
        BleUnitTask task = new BleUnitTask(u_service, u_chara);
        task.optionType = TYPE_READ;
        if (msg != null && msg.length > 0) {
            task.msg = msg[0];
        } else {
            task.msg = String.format("read {%s>>>>%s", u_service.toString(), u_chara.toString());
        }
        return task;
    }

    //创建一个写的任务
    public static BleUnitTask createWrite(UUID u_service, UUID u_chara, byte[] data, String... msg) {
        BleUnitTask task = new BleUnitTask(u_service, u_chara);
        task.optionType = TYPE_WRITE;
        if (msg != null && msg.length > 0) {
            task.msg = msg[0];
        } else {
            task.msg = String.format("write { %s>>>>%s", u_service.toString(), u_chara.toString());
        }
        task.data = data;
        return task;
    }

    //创建一个无响应的写的任务,
    public static BleUnitTask createWriteWithOutResp(UUID u_service, UUID u_chara, byte[] data, String... msg) {
        BleUnitTask task = new BleUnitTask(u_service, u_chara);
        task.optionType = TYPE_WRITE_WITHOUT_RESP;
        if (msg != null && msg.length > 0) {
            task.msg = msg[0];
        } else {
            task.msg = String.format("write withoutResponse{ %s>>>>%s", u_service.toString(), u_chara.toString());
        }
        task.data = data;
        return task;
    }

    //创建一个通知使能操作的任务
    public static BleUnitTask createEnableNotify(UUID u_service, UUID u_chara, String... msg) {
        BleUnitTask task = new BleUnitTask(u_service, u_chara);
        task.optionType = TYPE_ENABLE_NOTIFY;
        if (msg != null && msg.length > 0) {
            task.msg = msg[0];
        } else {
            String msgType = "notify";
            task.msg = String.format("%s{ %s>>>>%s", msgType, u_service.toString(), u_chara.toString());
        }
        return task;
    }

    //创建一个指示使能操作的任务
    public static BleUnitTask createEnableIndicate(UUID u_service, UUID u_chara, String... msg) {
        BleUnitTask task = new BleUnitTask(u_service, u_chara);
        task.optionType = TYPE_ENABLE_INDICATE;
        if (msg != null && msg.length > 0) {
            task.msg = msg[0];
        } else {
            String msgType = "notify";
            task.msg = String.format("%s{ %s>>>>%s", msgType, u_service.toString(), u_chara.toString());
        }
        return task;
    }

    //创建一个通知关闭使能操作的任务
    public static BleUnitTask createDisEnableNotifyOrIndicate(UUID u_service, UUID u_chara, String... msg) {
        BleUnitTask task = new BleUnitTask(u_service, u_chara);
        task.optionType = TYPE_DISABLE_NOTIFY_OR_INDICATE;
        if (msg != null && msg.length > 0) {
            task.msg = msg[0];
        } else {
            String msgType = "notify";
            task.msg = String.format("%s{ %s>>>>%s", msgType, u_service.toString(), u_chara.toString());
        }
        return task;
    }

}
