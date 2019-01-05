package com.maple.model;

public class ConnSession {
    public static final int DeviceType = 0;
    public static final int AdminType = 1;
    
    private int type;
    private String deviceId;
    private String deviceType;
    
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public String getDeviceType() {
        return deviceType;
    }
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
