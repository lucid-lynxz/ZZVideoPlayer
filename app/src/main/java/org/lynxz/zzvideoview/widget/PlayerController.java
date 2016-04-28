package org.lynxz.zzvideoview.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.lynxz.zzvideoview.R;
import org.lynxz.zzvideoview.controller.IControllerImpl;

/**
 * Created by zxz on 2016/4/28.
 */
public class PlayerController extends RelativeLayout {
    private IControllerImpl mContrllerImpl;

    public PlayerController(Context context) {
        super(context);
        initView(context);
    }

    public PlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PlayerController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.zz_video_player_controller, this);
    }

    public void setControllerImpl(IControllerImpl controllerImpl) {
        this.mContrllerImpl = controllerImpl;
    }
}
