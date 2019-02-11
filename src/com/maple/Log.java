package com.maple;

public class Log {
    public static void out(String msg) {
        android.util.Log.d("MRobotClient", msg);
        //System.out.println(DataUtils.getDate() + str);
    }
}
