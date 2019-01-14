package org.lynxz.zzplayerlibrary.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by lynxz on 2016/5/3.
 */
public class NetworkUtil {
    /**
     * 网络是否可用
     *
     * @param cxt 上下文,若为空则直接返回true
     * @return true-有网络
     */
    public static boolean isNetworkAvailable(Context cxt) {
        if (cxt == null) {
            return true;
        }

        ConnectivityManager manager = (ConnectivityManager) cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean connected = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            connected = true;
        }
        return connected;
    }
}
