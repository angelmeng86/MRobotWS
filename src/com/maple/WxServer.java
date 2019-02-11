package com.maple;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.maple.device.DeviceTool;
import com.maple.model.ConnSession;
import com.maple.model.RequestModel;
import com.maple.model.ResponseModel;
import com.maple.model.WxSession;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WxServer extends WebSocketServer {

    private WxHandler sender;
	public Map<WebSocket, WxSession> sessions = new HashMap<>();

	public WxServer(InetSocketAddress address, WxHandler s) {
	    super(address);
	    sender = s;
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		Log.out("new connection to " + conn.getRemoteSocketAddress());
		if(sender.isOpen()) {
		    JSONObject broadcast = new JSONObject();
	        broadcast.put("op", "MBroadcast");
	        broadcast.put("value", "relogin");
	        conn.send(broadcast.toString());
		}
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		sessions.remove(conn);
		Log.out("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
	    Log.out("received message from " + conn.getRemoteSocketAddress() + ": " + message);
		
		try {
		    JSONObject request = JSON.parseObject(message);
            if (request == null) {
                String response = DataUtils.jsonResponse(null, -800, "解析错误");
                conn.send(response);
                return;
            }
            String id = request.getString("id");
            String op = request.getString("op");
		    WxSession session = sessions.get(conn);
			if (session == null) {// 尚未登录才解析请求
				String wxId = request.getString("wxId");
				if(op != null && wxId != null && op.equals("login")) {
				    session = new WxSession();
                    session.setWxId(wxId);
                    sessions.put(conn, session);
                    
                    //添加设备序列号
                    String deviceId = DeviceTool.getDeviceId();
                    if(!deviceId.isEmpty()) {
                        request.put("param", DeviceTool.getDeviceId());
                    }
                    message = request.toString();
				}
				else {
					String response = DataUtils.jsonResponse(id, -800, "首次连接尚未登录");
					conn.send(response);
					return; 
				}

			}
			
			//TODO 转发至服务器
			if(sender.sendMessage(message)) {
			    
			}
			else {
			    String response = DataUtils.jsonResponse(id, -801, "与服务器尚未建立连接");
                conn.send(response);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.out("parse error.");
		}

	}
	
	public boolean forwardMessage(String wxId, String message) {
	    for (Entry<WebSocket, WxSession> s : sessions.entrySet()) {
            if (wxId.equals(s.getValue().getWxId())) {
                s.getKey().send(message);
                return true;
            }
        }
        return false;
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message) {
	    Log.out("received ByteBuffer from " + conn.getRemoteSocketAddress());
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
	    Log.out("an error occured on connection " + conn == null ? "null" : conn.getRemoteSocketAddress() + ":" + ex);
	}

	@Override
	public void onStart() {
	    Log.out("server started successfully");
	    sender.onServerStart(this);
	}

}
