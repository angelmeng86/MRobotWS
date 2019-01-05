package com.maple.device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DeviceTool {
    public static String execCommand(String command) {
        String line = "";
        StringBuilder sb = new StringBuilder(line);
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(command);
            InputStream inputstream = proc.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            while ((line = bufferedreader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }

            // 使用exec执行不会等执行成功以后才返回,它会立即返回
            // 所以在某些情况下是很要命的(比如复制文件的时候)
            // 使用wairFor()可以等待命令执行完成以后才返回
            try {
                if (proc.waitFor() != 0) {
                    System.out.println("exit value = " + proc.exitValue());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
    public static String getDeviceId() {
        return execCommand("getprop ro.boot.serialno");
    }
    
    public static String getDeviceType() {
        return execCommand("getprop ro.product.model");
    }
    
    
}
