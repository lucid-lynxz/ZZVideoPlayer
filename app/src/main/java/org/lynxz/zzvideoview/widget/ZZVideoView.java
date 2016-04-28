package org.lynxz.zzvideoview.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by zxz on 2016/4/28.
 */
public class ZZVideoView extends VideoView {
    public ZZVideoView(Context context) {
        super(context);
    }

    public ZZVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZZVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ZZVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
