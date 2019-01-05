package com.maple;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.maple.device.DeviceTool;
import com.maple.model.ConnSession;
import com.maple.model.RequestModel;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class MRobotServer extends WebSocketServer {
    
    private Map<WebSocket, ConnSession> sessions = new HashMap <>(); 
    public String masterKey = "MRobot2019";

	public MRobotServer(InetSocketAddress address) {
		super(address);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		//conn.send("pm list packages -3");
		//conn.send("Welcome to the server!"); //This method sends a message to the new client
		//broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This method sends a message to all clients connected
		System.out.println("new connection to " + conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
	    sessions.remove(conn);
		System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("received message from "	+ conn.getRemoteSocketAddress() + ": " + message);
		try {
		    ConnSession session = sessions.get(conn);
		    if(session == null || session.getType() == ConnSession.AdminType) {//尚未登录或者管理员才解析请求
		        RequestModel request = JSON.parseObject(message, RequestModel.class);
	            if(request != null && request.getOp() != null) {
	               if(session == null) {
	                    if(request.getOp().equals(RequestModel.DeviceLoginOP)) {
	                        JSONObject param = JSON.parseObject(request.getParam());
	                        String deviceType = param.getString("deviceType");
	                        String deviceId = param.getString("deviceId");
	                        
	                        if(deviceId != null && !deviceId.isEmpty() &&
	                                deviceType != null && !deviceType.isEmpty())
	                        {
	                            session = new ConnSession();
	                            session.setType(ConnSession.DeviceType);
	                            session.setDeviceId(deviceId);
	                            session.setDeviceType(deviceType);
	                            sessions.put(conn, session);
	                            
	                            String response = DataUtils.jsonResponse(request.getId(), 0, null);
	                            conn.send(response);
	                        }
	                        else {
	                            String response = DataUtils.jsonResponse(request.getId(), -2, "param无效");
	                            conn.send(response);
	                        }
	                    }
	                    else if(request.getOp().equals(RequestModel.AdminLoginOP)) {
	                        JSONObject param = JSON.parseObject(request.getParam());
                            String key = param.getString("masterKey");
                            
                            if(key != null && !key.equals(masterKey))
                            {
                                session = new ConnSession();
                                session.setType(ConnSession.AdminType);
                                
                                JSONObject data = new JSONObject();
                                JSONArray online = new JSONArray();
                                for(ConnSession s : sessions.values()) {
                                    if(s.getType() == ConnSession.DeviceType) {
                                        online.add(s.getDeviceId());
                                    }
                                }
                                data.put("online", online);
                                
                                sessions.put(conn, session);
                                
                                String response = DataUtils.jsonResponse(request.getId(), 0, null, data.toString());
                                conn.send(response);
                            }
                            else {
                                String response = DataUtils.jsonResponse(request.getId(), -2, "param无效");
                                conn.send(response);
                            }
                        }
	                    else {
	                        String response = DataUtils.jsonResponse(request.getId(), -1, "op无效");
	                        conn.send(response);
	                    }
	                }
	                else {
	                    //TODO
	                    if(request.getOp().equals(RequestModel.ShellExecOP)) {
	                        JSONObject param = JSON.parseObject(request.getParam());
	                        String cmd = param.getString("cmd");
	                        String result = DeviceTool.execCommand(cmd);
	                        
	                        JSONObject resultObj = new JSONObject();
	                        resultObj.put("result", result);
	                        String response = DataUtils.jsonResponse(request.getId(), 0, null, resultObj.toString());
	                        conn.send(response);
	                    }
	                    else {
	                        String response = DataUtils.jsonResponse(request.getId(), -1, "op无效");
	                        conn.send(response);
	                    }
	                }
	                
	            }
	            else {
	                System.out.println("parse request error.");
	            }
		    }
            RequestModel request = JSON.parseObject(message, RequestModel.class);
            if(request != null && request.getOp() != null) {
                if(request.getOp().equals(RequestModel.ShellExecOP)) {
                    JSONObject param = JSON.parseObject(request.getParam());
                    String cmd = param.getString("cmd");
                    String result = DeviceTool.execCommand(cmd);
                    
                    JSONObject resultObj = new JSONObject();
                    resultObj.put("result", result);
                    String response = DataUtils.jsonResponse(request.getId(), 0, null, resultObj.toString());
                    conn.send(response);
                }
                else {
                    String response = DataUtils.jsonResponse(request.getId(), -1, "op无效");
                    conn.send(response);
                }
            }
            else {
                System.out.println("parse request error.");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("parse error.");
        }
		
	}

	@Override
	public void onMessage( WebSocket conn, ByteBuffer message ) {
		System.out.println("received ByteBuffer from "	+ conn.getRemoteSocketAddress());
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		System.err.println("an error occured on connection " +  conn == null ? "null" : conn.getRemoteSocketAddress()  + ":" + ex);
	}
	
	@Override
	public void onStart() {
		System.out.println("server started successfully");
	}


	public static void main(String[] args) {
		String host = "0.0.0.0";
		int port = 8085;

		WebSocketServer server = new MRobotServer(new InetSocketAddress(host, port));
		server.run();
	}

}
