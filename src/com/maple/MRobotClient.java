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
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import lipermi.handler.CallHandler;
import lipermi.net.Client;

public class MRobotClient extends WebSocketClient implements WxHandler {

	public boolean running = true;
	private WxServer server;

	public MRobotClient(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public MRobotClient(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		Log.out("new connection opened");
		if(server != null) {
		    JSONObject broadcast = new JSONObject();
	        broadcast.put("op", "MBroadcast");
	        broadcast.put("value", "relogin");
		    server.broadcast(broadcast.toString()); 
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
	    Log.out("closed with exit code " + code + " additional info: " + reason);
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
        Log.out("received message: " + message);

        try {
            JSONObject request = JSON.parseObject(message);
            if (request == null) {
                Log.out("parse request error.");
                return;
            }
            String id = request.getString("id");
            String wxId = request.getString("wxId");
            String op = request.getString("op");
            if (op != null) {
                switch (op) {
                    case RequestModel.ShellExecOP: {
                        JSONObject param = request.getJSONObject("param");
                        String cmd = param.getString("cmd");
                        String result = DeviceTool.execCommand(cmd);

                        JSONObject resultObj = new JSONObject();
                        resultObj.put("result", result);
                        String response = DataUtils.jsonResponse(id, 0, null, resultObj);
                        send(response);
                    }
                        break;
                    case RequestModel.AppUpgradeOP: {
                        JSONObject param = request.getJSONObject("param");
                        int type = param.getIntValue("type");
                        String url = param.getString("url");

                        String result = "";
                        if (type == 2) {
                            result = DeviceTool
                                    .execCommand("wget -O /data/local/tmp/hello.sh -nv " + url);
                            result += "\n";
                            result += DeviceTool.execCommand("chmod 777 /data/local/tmp/hello.sh");
                            result += "\n";
                            result += DeviceTool.execCommand("/data/local/tmp/hello.sh");
                        } else {
                            result = DeviceTool
                                    .execCommand("wget -O /data/local/tmp/hello.apk -nv " + url);
                            result += "\n";
                            result += DeviceTool
                                    .execCommand("pm install -r /data/local/tmp/hello.apk");
                        }

                        JSONObject resultObj = new JSONObject();
                        resultObj.put("result", result);
                        String response = DataUtils.jsonResponse(id, 0, null, resultObj);
                        send(response);
                    }
                        break;
                    default: {
                        if (wxId != null) {
                            if (server == null || !server.forwardMessage(wxId, message)) {
                                String response = DataUtils.jsonResponse(id, -1, "RPC通信失败，未找到指定wxId");
                                send(response);
                            }
                        }
                    }
                        break;
                }
            }
            else {
                if (wxId != null) {
                    if (server == null || !server.forwardMessage(wxId, message)) {
                        Log.out("drop message");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.out("parse request error.");
        }

    }

	@Override
	public void onMessage(ByteBuffer message) {
	    Log.out("received ByteBuffer");
	}

	@Override
	public void onError(Exception ex) {
	    Log.out("an error occurred:" + ex);
	    ex.printStackTrace();
	}

	public static void main(String[] args) throws URISyntaxException, InterruptedException {
		Log.out("MRobotClient is run");
		Log.out("DeviceId:" + DeviceTool.getDeviceId());
		Log.out("DeviceType:" + DeviceTool.getDeviceType());

		MRobotClient client = new MRobotClient(new URI("ws://121.42.162.228:8089/robot/websocket"));
		client.connect();
		
        WebSocketServer server = new WxServer(new InetSocketAddress("127.0.0.1", 8085), client);
        server.run();

		Log.out("MRobotClient exit");
	}
	
	@Override
    public	void onServerStart(WxServer s) {
	    server = s;
	}

    @Override
    public boolean sendMessage(String msg) {
        try {
            if(this.isOpen()) {
                this.send(msg);
                return true;
            }
        }
        catch(Exception e) {
            Log.out("send err:" + e.toString());
        }
        return false;
    }

}
