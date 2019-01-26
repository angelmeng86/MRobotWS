package com.maple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maple.device.DeviceTool;
import com.maple.model.RequestModel;
import com.maple.model.ResponseModel;
import com.maple.rmi.LocalMessageInterface;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import lipermi.handler.CallHandler;
import lipermi.net.Client;

public class MRobotClient extends WebSocketClient {

	public boolean running = true;
	private boolean logined = false;

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
		String request = DataUtils.jsonRequest(DataUtils.getUUID32(), RequestModel.DeviceLoginOP, data);
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
	public void onMessage(String msg) {
		System.out.println("received message: " + msg);
		final String message = msg;
		new Thread() {
			public void run() {
				try {
					if (logined) {
						RequestModel request = JSON.parseObject(message, RequestModel.class);
						if (request == null) {
							System.out.println("parse request error.");
							return;
						}
						switch (request.getOp()) {
						case RequestModel.ShellExecOP: {
							JSONObject param = request.getParam();
							String cmd = param.getString("cmd");
							String result = DeviceTool.execCommand(cmd);

							JSONObject resultObj = new JSONObject();
							resultObj.put("result", result);
							String response = DataUtils.jsonResponse(request.getId(), 0, null, resultObj);
							send(response);
						}
							break;
						case RequestModel.AppUpgradeOP: {
							JSONObject param = request.getParam();
							int type = param.getIntValue("type");
							String url = param.getString("url");

							String result = "";
							if (type == 2) {
								result = DeviceTool.execCommand("wget -O /data/local/tmp/hello.sh -nv " + url);
								result += "\n";
								result += DeviceTool.execCommand("chmod 777 /data/local/tmp/hello.sh");
								result += "\n";
								result += DeviceTool.execCommand("/data/local/tmp/hello.sh");
							} else {
								result = DeviceTool.execCommand("wget -O /data/local/tmp/hello.apk -nv " + url);
								result += "\n";
								result += DeviceTool.execCommand("pm install -r /data/local/tmp/hello.apk");
							}

							JSONObject resultObj = new JSONObject();
							resultObj.put("result", result);
							String response = DataUtils.jsonResponse(request.getId(), 0, null, resultObj);
							send(response);
						}
							break;
						default: {
							String result = callRemoteMessage(message);
							if (result == null) {
								String response = DataUtils.jsonResponse(request.getId(), -1, "RPC通信失败");
								send(response);
							} else {
								//String response = DataUtils.jsonResponse(request.getId(), 0, null,
								//		JSON.parseObject(result));
								//send(response);
								send(result);
							}
						}
							break;
						}
					} else {
						//解析登录结果
						ResponseModel response = JSON.parseObject(message, ResponseModel.class);
						if (response != null && response.getCode() == 0) {
							logined = true;
							System.out.println("login success.");
						} else {
							running = false;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("parse request error.");
				}
			}
		}.start();

	}

	public static String callRemoteMessage(String json) {
		String remoteHost = "127.0.0.1";
		int port = 8099;
		Client client = null;
		try {
			CallHandler callHandler = new CallHandler();
			client = new Client(remoteHost, port, callHandler);
			LocalMessageInterface remoteObj = (LocalMessageInterface) client.getGlobal(LocalMessageInterface.class);
			return remoteObj.forwardMessage(json);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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
		System.out.println("DeviceId:" + DeviceTool.getDeviceId());
		System.out.println("DeviceType:" + DeviceTool.getDeviceType());

		MRobotClient client = new MRobotClient(new URI("ws://www.ucicloud.com:8085"));
		client.connect();
		while (client.running) {
			Thread.sleep(1000);
		}

		System.out.println("MRobotClient exit");
	}

}
