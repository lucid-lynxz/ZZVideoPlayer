package org.lynxz.zzvideoview.util;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by zxz on 2016/5/4.
 * 屏幕方向
 */
public class OrientationUtil {
    public static final int HORIZONTAL = 0x00;
    public static final int VERTICAL = 0x01;

    /**
     * 获取屏幕方向
     *
     * @param cxt
     * @return {@link #HORIZONTAL}  {@link #HORIZONTAL}
     */
    public static int getOrientation(Context cxt) {
        int orientation = VERTICAL;
        if (cxt.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = HORIZONTAL;
        }
        return orientation;
    }
}
