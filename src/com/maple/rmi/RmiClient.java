package com.maple.rmi;

import java.io.IOException;

import lipermi.handler.CallHandler;
import lipermi.net.Client;

public class RmiClient {
	public static void main(String[] args) {
        // 建立连接
        CallHandler callHandler = new CallHandler();
        String remoteHost = "127.0.0.1";
        int port = 8099;
        Client client = null;
        try {
            client = new Client(remoteHost, port, callHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 获取远程实例，测试方法
        assert client != null;
        LocalMessageInterface remoteObj = (LocalMessageInterface) client.getGlobal(LocalMessageInterface.class);
        System.out.println(remoteObj.sayHello("Tomcat"));
        System.out.println(remoteObj.getDate());
    }
}
