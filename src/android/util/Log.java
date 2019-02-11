package android.util;

import com.maple.DataUtils;

public class Log {
    public static int d(String tag, String msg) {
        System.out.println(DataUtils.getDate() + msg);
        return 0;
    }
}
