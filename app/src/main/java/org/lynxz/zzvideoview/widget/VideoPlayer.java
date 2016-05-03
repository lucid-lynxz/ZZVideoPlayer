package org.lynxz.zzvideoview.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import org.lynxz.zzvideoview.R;
import org.lynxz.zzvideoview.constant.VideoUriProtocol;
import org.lynxz.zzvideoview.controller.AnimationImpl;
import org.lynxz.zzvideoview.controller.IControllerImpl;
import org.lynxz.zzvideoview.controller.IPlayerImpl;
import org.lynxz.zzvideoview.controller.ITitleBarImpl;
import org.lynxz.zzvideoview.util.DebugLog;

import java.util.Calendar;

/**
 * Created by zxz on 2016/4/28.
 */
public class VideoPlayer extends RelativeLayout implements View.OnTouchListener {

    private Context mContent;
    private PlayerTitleBar mTitleBar;
    private ZZVideoView mVv;
    private PlayerController mController;
    private static final String TAG = "zzVideoPlayer";
    private boolean barsIfShow = true;//标题栏控制栏是否显示

    private Uri mVideoUri;
    private String mVideoProtocol;//视频地址所用协议

    private Animation mEnterFromTop;
    private Animation mEnterFromBottom;
    private Animation mExitFromTop;
    private Animation mExitFromBottom;


    private long mCurrentDownTime = 0;
    private long mLastDownTime = 0;
    private static final int MIN_CLICK_INTERVAL = 500;//连续两次down事件最小时间间隔(ms)

    private ITitleBarImpl mTitleBarImpl = new ITitleBarImpl() {
        @Override
        public void onBackClick() {
            if (mIPlayerImpl != null) {
                mIPlayerImpl.onBack();
            }
        }
    };

    private IControllerImpl mControllerImpl = new IControllerImpl() {
        @Override
        public void onPlayTurn() {

        }
    };

    /**
     * 播放器控制功能对外开放接口,包括返回按钮,播放等...
     */
    public void setPlayerController(IPlayerImpl IPlayerImpl) {
        mIPlayerImpl = IPlayerImpl;
    }

    private IPlayerImpl mIPlayerImpl = null;

    public VideoPlayer(Context context) {
        super(context);
        initView(context);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoPlayer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        mContent = context;
        inflate(context, R.layout.zz_video_player, this);
        mVv = (ZZVideoView) findViewById(R.id.zzvv_main);
        mTitleBar = (PlayerTitleBar) findViewById(R.id.pt_title_bar);
        mController = (PlayerController) findViewById(R.id.pc_controller);

        mVv.setOnTouchListener(this);

        mTitleBar.setTitleBarImpl(mTitleBarImpl);
        mController.setControllerImpl(mControllerImpl);

        initAnimation();
    }

    /**
     * 初始化标题栏/控制栏显隐动画效果
     */
    private void initAnimation() {
        mEnterFromTop = AnimationUtils.loadAnimation(mContent, R.anim.enter_from_top);
        mEnterFromBottom = AnimationUtils.loadAnimation(mContent, R.anim.enter_from_bottom);
        mExitFromTop = AnimationUtils.loadAnimation(mContent, R.anim.exit_from_top);
        mExitFromBottom = AnimationUtils.loadAnimation(mContent, R.anim.exit_from_bottom);

        mEnterFromTop.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mTitleBar.setVisibility(VISIBLE);
            }
        });
        mEnterFromBottom.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mController.setVisibility(VISIBLE);
            }
        });
        mExitFromTop.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mTitleBar.setVisibility(GONE);
            }
        });
        mExitFromBottom.setAnimationListener(new AnimationImpl() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mController.setVisibility(GONE);
            }
        });
    }

    /**
     * 设置视频标题
     */

    public void setTitle(String title) {
        mTitleBar.setTitle(title);
    }

    private void load() {
        if (VideoUriProtocol.PROTOCOL_HTTP.equalsIgnoreCase(mVideoProtocol)) {
            mVv.setVideoPath(mVideoUri.toString());
        } else if (VideoUriProtocol.PROTOCOL_ANDROID_RESOURCE.equalsIgnoreCase(mVideoProtocol)) {
            mVv.setVideoURI(mVideoUri);
        }
    }

    public void startPlay() {
        mVv.start();
    }

    public void pausePlay() {
        mVv.pause();
    }

    public void stopPlay() {
        mVv.stopPlayback();
    }

    /**
     * 设置视频播放路径
     * 1. 设置当前项目中res/raw目录中的文件: "android.resource://" + getPackageName() + "/" + R.raw.yourName
     * 2. 设置网络视频文件: "http:\//****\/abc.mp4"
     *
     * @param path
     * @return 设置成功返回 true
     */
    public void setVideoUri(@NonNull String path) {
        mVideoUri = Uri.parse(path);
        mVideoProtocol = mVideoUri.getScheme();
        DebugLog.i(TAG, "setVideoUri path = " + path + " mVideoProtocol = " + mVideoProtocol);
    }

    public void loadAndStartVideo(@NonNull String path) {
        setVideoUri(path);
        load();
        startPlay();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mCurrentDownTime = Calendar.getInstance().getTimeInMillis();
            if (isTouchEventValid()) {
                if (mController.getVisibility() == VISIBLE) {
                    showOrHideBars(false, true);
                } else {
                    showOrHideBars(true, true);
                }
                mLastDownTime = mCurrentDownTime;
                return true;
            }
        }
        return false;
    }


    /**
     * 显隐标题栏和控制条
     *
     * @param show          是否显示
     * @param animateEffect 是否需要动画效果
     */
    private void showOrHideBars(boolean show, boolean animateEffect) {
        if (animateEffect) {
            animateShowOrHideBars(show);
        } else {
            forceShowOrHideBars(show);
        }
    }

    /**
     * 直接显隐标题栏和控制栏
     */
    private void forceShowOrHideBars(boolean show) {
        mTitleBar.clearAnimation();
        mController.clearAnimation();

        if (show) {
            mController.setVisibility(VISIBLE);
            mTitleBar.setVisibility(VISIBLE);
        } else {
            mController.setVisibility(GONE);
            mTitleBar.setVisibility(GONE);
        }
    }

    /**
     * 带动画效果的显隐标题栏和控制栏
     */
    private void animateShowOrHideBars(boolean show) {
        mTitleBar.clearAnimation();
        mController.clearAnimation();

        if (show) {
            mTitleBar.startAnimation(mEnterFromTop);
            mController.startAnimation(mEnterFromBottom);
        } else {
            mTitleBar.startAnimation(mExitFromTop);
            mController.startAnimation(mExitFromBottom);
        }
    }

    /**
     * 判断连续两次触摸事件间隔是否符合要求,避免快速点击等问题
     *
     * @return
     */
    private boolean isTouchEventValid() {
        if (mCurrentDownTime - mLastDownTime >= MIN_CLICK_INTERVAL) {
            return true;
        }
        return false;
    }

    //    @Override
    //    public void onClick(View v) {
    //        if (!isTouchEventValid()) {
    //            return;
    //        }
    //
    //        if (mController.getVisibility() == VISIBLE) {
    //            showOrHideBars(false, true);
    //        } else {
    //            showOrHideBars(true, true);
    //        }
    //    }
}
