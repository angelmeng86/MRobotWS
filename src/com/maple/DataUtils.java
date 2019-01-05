package com.maple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maple.model.RequestModel;
import com.maple.model.ResponseModel;

import java.util.UUID;

public class DataUtils {
    
    public static String getUUID32(){
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
    
    public static String jsonResponse(String id, int code, String errMsg) {
        return jsonResponse(id, code, errMsg, null);
    }
    
    public static String jsonResponse(String id, int code, String errMsg, JSONObject jsonData) {
        ResponseModel model = new ResponseModel();
        model.setCode(code);
        model.setErrorMsg(errMsg);
        model.setData(jsonData);
        model.setId(id);
        
        return JSON.toJSONString(model);
    }
    
    public static String jsonRequest(String id, String op, JSONObject jsonData, String destDevice) {
        RequestModel model = new RequestModel();
        model.setDestDevice(destDevice);
        model.setOp(op);
        model.setParam(jsonData);
        model.setId(id);
        
        return JSON.toJSONString(model);
    }
    
    public static String jsonRequest(String id, String op, JSONObject jsonData) {
        return jsonRequest(id, op, jsonData, null);
    }
}
