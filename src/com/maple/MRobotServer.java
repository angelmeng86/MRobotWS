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
import com.maple.model.ConnSession;
import com.maple.model.RequestModel;
import com.maple.model.ResponseModel;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class MRobotServer extends WebSocketServer {

	private Map<WebSocket, ConnSession> sessions = new HashMap<>();

	public String masterKey = "MRobot2019";

	public MRobotServer(InetSocketAddress address) {
		super(address);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		// conn.send("pm list packages -3");
		// conn.send("Welcome to the server!"); //This method sends a message to the new
		// client
		// broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This
		// method sends a message to all clients connected
		System.out.println(DataUtils.getDate() + "new connection to " + conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		sessions.remove(conn);
		System.out.println(DataUtils.getDate() + 
				"closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println(DataUtils.getDate() + "received message from " + conn.getRemoteSocketAddress() + ": " + message);
		try {
			ConnSession session = sessions.get(conn);
			if (session == null || session.getType() == ConnSession.AdminType) {// 尚未登录或者管理员才解析请求
				RequestModel request = JSON.parseObject(message, RequestModel.class);
				if (request == null) {
					System.out.println(DataUtils.getDate() + "parse request error.");
					return;
				}
				if (session == null) {
					switch (request.getOp()) {
					case RequestModel.DeviceLoginOP: {
						JSONObject param = request.getParam();
						String deviceType = param.getString("deviceType");
						String deviceId = param.getString("deviceId");

						if (deviceId != null && !deviceId.isEmpty() && deviceType != null && !deviceType.isEmpty()) {
							session = new ConnSession();
							session.setType(ConnSession.DeviceType);
							session.setDeviceId(deviceId);
							session.setDeviceType(deviceType);
							sessions.put(conn, session);

							String response = DataUtils.jsonResponse(request.getId(), 0, null);
							conn.send(response);
						} else {
							String response = DataUtils.jsonResponse(request.getId(), -2, "param无效");
							conn.send(response);
						}
					}
						break;
					case RequestModel.AdminLoginOP: {
						JSONObject param = request.getParam();
						String key = param.getString("masterKey");

						if (key != null && key.equals(masterKey)) {
							session = new ConnSession();
							session.setType(ConnSession.AdminType);
							session.setWaitQueue(new HashSet<String>());

							JSONObject data = new JSONObject();
							JSONArray online = new JSONArray();
							for (ConnSession s : sessions.values()) {
								if (s.getType() == ConnSession.DeviceType) {
									online.add(s.getDeviceId());
								}
							}
							data.put("online", online);

							sessions.put(conn, session);

							String response = DataUtils.jsonResponse(request.getId(), 0, null, data);
							conn.send(response);
						} else {
							String response = DataUtils.jsonResponse(request.getId(), -2, "param无效");
							conn.send(response);
						}
					}
						break;
					default: {
						String response = DataUtils.jsonResponse(request.getId(), -1, "op无效");
						conn.send(response);
					}
						break;
					}
				} else {
					if (request.getDestDevice() != null) {
						boolean finded = false;
						for (Entry<WebSocket, ConnSession> s : sessions.entrySet()) {
							if (s.getValue().getType() == ConnSession.DeviceType
									&& s.getValue().getDeviceId().equals(request.getDestDevice())) {
								Set<String> set = session.getWaitQueue();
								synchronized (set) {
									set.add(request.getId());
								}
								s.getKey().send(message);
								finded = true;
								break;
							}
						}

						if (!finded) {
							String response = DataUtils.jsonResponse(request.getId(), -3, "destDevice不存在");
							conn.send(response);
						}
					} else {
						// TODO 服务端请求处理
						System.out.println("服务端请求处理");
						String response = DataUtils.jsonResponse(request.getId(), -1, "服务端未实现请求响应");
						conn.send(response);
					}
				}

			} else {
				ResponseModel response = JSON.parseObject(message, ResponseModel.class);
				if (response != null && response.getId() != null) {
					boolean finded = false;
					for (Entry<WebSocket, ConnSession> s : sessions.entrySet()) {
						if (s.getValue().getType() == ConnSession.AdminType
								&& s.getValue().getWaitQueue().contains(response.getId())) {
							Set<String> set = s.getValue().getWaitQueue();
							synchronized (set) {
								if (set.contains(response.getId())) {
									set.remove(response.getId());
									finded = true;
									s.getKey().send(message);
									break;
								}
							}
						}
					}
					if (!finded) {
						// TODO 服务端响应处理
						System.out.println(DataUtils.getDate() + "服务端响应处理");
					}
				} else {
					System.out.println(DataUtils.getDate() + "parse request error.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(DataUtils.getDate() + "parse error.");
		}

	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message) {
		System.out.println(DataUtils.getDate() + "received ByteBuffer from " + conn.getRemoteSocketAddress());
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		System.err.println(DataUtils.getDate() + 
				"an error occured on connection " + conn == null ? "null" : conn.getRemoteSocketAddress() + ":" + ex);
	}

	@Override
	public void onStart() {
		System.out.println(DataUtils.getDate() + "server started successfully");
	}

	public static void main(String[] args) {
		String host = "0.0.0.0";
		int port = 8085;

		WebSocketServer server = new MRobotServer(new InetSocketAddress(host, port));
		server.run();
	}

}
