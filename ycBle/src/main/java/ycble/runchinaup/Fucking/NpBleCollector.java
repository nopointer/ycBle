package ycble.runchinaup.Fucking;

import java.util.ArrayList;
import java.util.List;

public class NpBleCollector {


    /**
     * 能进来里面的都是垃圾ble 手机
     */
    private List<String> laJiPhone = new ArrayList<>();

    private NpBleCollector() {
        laJiPhone.add("");
    }


    public void add(String phone) {

    }

    public void remove(String phone) {

    }

    public void clear() {

    }

    public List<String> getDefault(){
        List<String> resultList =new ArrayList<>();
        resultList.add("LDN-AL00");
        resultList.add("HRY-AL00Ta");
        resultList.add("LLD-AL00");
        return resultList;
    }


}
