package com.maple.model;

import java.util.Set;

public class ConnSession {
    public static final int DeviceType = 0;
    public static final int AdminType = 1;
    
    private int type;
    
    private String deviceId;
    private String deviceType;
    
    private Set<String> waitQueue;
    
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
	public Set<String> getWaitQueue() {
		return waitQueue;
	}
	public void setWaitQueue(Set<String> waitQueue) {
		this.waitQueue = waitQueue;
	}
}
