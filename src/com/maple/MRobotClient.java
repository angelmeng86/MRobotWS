package com.maple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maple.device.DeviceTool;
import com.maple.model.RequestModel;
import com.maple.model.ResponseModel;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class MRobotClient extends WebSocketClient {

    public MRobotClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public MRobotClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        JSONObject data = new JSONObject();
        data.put("deviceType", DeviceTool.getDeviceType());
        data.put("deviceId", DeviceTool.getDeviceId());
        String request = DataUtils.jsonRequest(DataUtils.getUUID32(), RequestModel.DeviceLoginOP, data.toString());
        send(request);
        System.out.println("new connection opened");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
        new Thread() {
            public void run() {
            	try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                MRobotClient.this.reconnect();
            }
        }.start(); 
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received message: " + message);
        try {
            RequestModel request = JSON.parseObject(message, RequestModel.class);
            if(request != null && request.getOp() != null) {
                if(request.getOp().equals(RequestModel.ShellExecOP)) {
                    JSONObject param = JSON.parseObject(request.getParam());
                    String cmd = param.getString("cmd");
                    String result = DeviceTool.execCommand(cmd);
                    
                    JSONObject resultObj = new JSONObject();
                    resultObj.put("result", result);
                    String response = DataUtils.jsonResponse(request.getId(), 0, null, resultObj.toString());
                    send(response);
                }
                else {
                    String response = DataUtils.jsonResponse(request.getId(), -1, "op无效");
                    send(response);
                }
            }
            else {
                System.out.println("parse request error.");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("parse request error.");
        }
        
    }

    @Override
    public void onMessage(ByteBuffer message) {
        System.out.println("received ByteBuffer");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }
    
    

    public static void main(String[] args) throws URISyntaxException, InterruptedException {      
        System.out.println("MRobotClient is run");
        System.out.println("DeviceId:" +  DeviceTool.getDeviceId());
        System.out.println("DeviceType:" +  DeviceTool.getDeviceType());
        
        WebSocketClient client = new MRobotClient(new URI("ws://www.ucicloud.com:8085"));
        client.connect();
        while(true) {
        	Thread.sleep(1000);
        }
    }

}
