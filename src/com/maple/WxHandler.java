package com.maple;

public interface WxHandler {
    void onServerStart(WxServer server);
    boolean sendMessage(String msg);
    boolean isOpen();
}
