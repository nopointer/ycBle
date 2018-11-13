package ycble.runchinaup.aider.entity;

/**
 * 通讯录联系人信息，不保证所有手机里面这些字段是可以取出数据，绝大部分手机
 */
public class NPContactEntity {
    private String display_name_alt;
    private String display_name;
    private String data1;
    private String data4;

    public NPContactEntity(String display_name_alt, String display_name, String data1, String data4) {
        this.display_name_alt = display_name_alt;
        this.display_name = display_name;
        this.data1 = data1;
        this.data4 = data4;
    }

    public String getDisplay_name_alt() {
        return display_name_alt;
    }

    public void setDisplay_name_alt(String display_name_alt) {
        this.display_name_alt = display_name_alt;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getData1() {
        return data1;
    }

    public void setData1(String data1) {
        this.data1 = data1;
    }

    public String getData4() {
        return data4;
    }

    public void setData4(String data4) {
        this.data4 = data4;
    }

    @Override
    public String toString() {
        return "NPContactInfo{" +
                "display_name_alt='" + display_name_alt + '\'' +
                ", display_name='" + display_name + '\'' +
                ", data1='" + data1 + '\'' +
                ", data4='" + data4 + '\'' +
                '}';
    }
}
