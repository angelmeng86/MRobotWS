package com.maple.model;

import com.alibaba.fastjson.JSONObject;

public class RequestModel {
    public static final String DeviceLoginOP = "DeviceLogin";
    public static final String AdminLoginOP = "AdminLogin";
    public static final String ShellExecOP = "ShellExec";
    
    private String id;
    private String destDevice;
    private String op;
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
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
    public JSONObject getParam() {
        return param;
    }
    public void setParam(JSONObject param) {
        this.param = param;
    }
}
