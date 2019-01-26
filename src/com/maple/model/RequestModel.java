package com.maple.model;

import com.alibaba.fastjson.JSONObject;

public class RequestModel {
    public static final int DeviceLoginOP = 2;
    public static final int AdminLoginOP = 1;
    
    public static final int ShellExecOP = 3;
    public static final int AppUpgradeOP = 4;
    
    
    private String id;
    private String destDevice;
    private int op;
    private JSONObject param;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getDestDevice() {
        return destDevice;
    }
    public void setDestDevice(String destDevice) {
        this.destDevice = destDevice;
    }
    public int getOp() {
        return op;
    }
    public void setOp(int op) {
        this.op = op;
    }
    public JSONObject getParam() {
        return param;
    }
    public void setParam(JSONObject param) {
        this.param = param;
    }
}
