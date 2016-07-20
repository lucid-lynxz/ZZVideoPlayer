package org.lynxz.zzplayerlibrary.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.lynxz.zzplayerlibrary.R;
import org.lynxz.zzplayerlibrary.constant.PlayState;
import org.lynxz.zzplayerlibrary.constant.SeekBarState;
import org.lynxz.zzplayerlibrary.constant.VideoUriProtocol;
import org.lynxz.zzplayerlibrary.controller.AnimationImpl;
import org.lynxz.zzplayerlibrary.controller.IControllerImpl;
import org.lynxz.zzplayerlibrary.controller.IPlayerImpl;
import org.lynxz.zzplayerlibrary.controller.ITitleBarImpl;
import org.lynxz.zzplayerlibrary.util.DensityUtil;
import org.lynxz.zzplayerlibrary.util.NetworkUtil;
import org.lynxz.zzplayerlibrary.util.OrientationUtil;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

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


    private int mDuration = 0;//视频长度
    private long mCurrentDownTime = 0;//当前action_down时的时间值
    private long mLastDownTime = 0;//上次action_down的时间值，防止快速触摸多次触发
    private int mCurrentPlayState = PlayState.IDLE;
    private int mNetworkState = -1;//0-无网络

    private static final int MIN_CLICK_INTERVAL = 400;//连续两次down事件最小时间间隔(ms)
    private static final int UPDATE_TIMER_INTERVAL = 1000;
    private static final int TIME_AUTO_HIDE_BARS_DELAY = 3800;

    private static final int MSG_UPDATE_PROGRESS_TIME = 1;//更新播放进度时间
    private static final int MSG_AUTO_HIDE_BARS = 2;//隐藏标题栏和控制条

    private Timer mUpdateTimer = null;

    private WeakReference<Activity> mHostActivity;
    private int mLastPlayingPos = -1;//onPause时的播放位置

    private BroadcastReceiver mNetworkReceiver;

    private boolean mOnPrepared;
    private boolean mHasSetPath2vv;//是否已经将路径设置给了VideoView
    /**
     * 断网时获取的已缓冲长度
     * 从-1开始,用于加载前就断网,此时通过方法getBufferLength()得到的是0,不便于判断
     */
    private int mLastBufferLength = -1;

    private ITitleBarImpl mTitleBarImpl = new ITitleBarImpl() {
        @Override
        public void onBackClick() {
            if (mIPlayerImpl != null) {
                mIPlayerImpl.onBack();
            } else {
                mHostActivity.get().finish();
            }
        }
    };

    /**
     * 更新播放器状态
     *
     * @param playState {@link PlayState}
     */
    private void updatePlayState(int playState) {
        mCurrentPlayState = playState;
        mController.setPlayState(playState);
    }

    private IControllerImpl mControllerImpl = new IControllerImpl() {
        @Override
        public void onPlayTurn() {
            //网络不正常时,不允许切换,本地视频则跳过这一步
            if (VideoUriProtocol.PROTOCOL_HTTP.equalsIgnoreCase(mVideoProtocol)
                    && !mNetworkAvailable) {
                mIPlayerImpl.onNetWorkError();
                return;
            }

            switch (mCurrentPlayState) {
                case PlayState.PLAY:
                    pausePlay();
                    break;
                case PlayState.IDLE:
                case PlayState.PREPARE:
                case PlayState.PAUSE:
                case PlayState.COMPLETE:
                case PlayState.STOP:
                    startOrRestartPlay();
                    break;
                case PlayState.ERROR:
                    break;
            }
            sendAutoHideBarsMsg();
        }

        @Override
        public void onProgressChange(int state, int progress) {
            switch (state) {
                case SeekBarState.START_TRACKING:
                    mHandler.removeMessages(MSG_AUTO_HIDE_BARS);
                    break;
                case SeekBarState.STOP_TRACKING:
                    if (mOnPrepared && isPlaying()) {
                        mVv.seekTo(progress);
                        sendAutoHideBarsMsg();
                    }
                    break;
            }
        }

        @Override
        public void onOrientationChange() {
            OrientationUtil.changeOrientation(mHostActivity.get());
        }
    };

    public int mLastUpdateTime = 0;//上一次updateTimer更新的播放时间值
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what == MSG_UPDATE_PROGRESS_TIME) {
                if (mNetworkAvailable) {
                    mLastBufferLength = -1;
                }
                mLastPlayingPos = getCurrentTime();
                if (mCurrentPlayState == PlayState.COMPLETE) {
                    mLastPlayingPos = 0;
                }
                mController.updateProgress(mLastPlayingPos, getBufferProgress());
                mVv.setBackgroundColor(Color.TRANSPARENT);
            } else if (what == MSG_AUTO_HIDE_BARS) {
                animateShowOrHideBars(false);
            }
        }
    };
    private boolean mNetworkAvailable;


    public boolean isPlaying() {
        try {
            return mVv.isPlaying();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            // 这里再设定zOrder就已经无效了
            //            mVv.setZOrderOnTop(false);

            // 在这里去掉背景的话,需要延时下,不然还是会有瞬间的透明色
            // 我放到更新进度条的时候再来去掉背景了
            //            mVv.setBackgroundColor(Color.TRANSPARENT);
            //            mVv.setBackgroundResource(0);
            mOnPrepared = true;
            updatePlayState(PlayState.PREPARE);
            mDuration = mp.getDuration();
            mController.updateProgress(mLastUpdateTime, 0, mDuration);
            sendAutoHideBarsMsg();
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e(TAG, "MediaPlayer.OnErrorListener what = " + what + " , extra = " + extra + " ,mNetworkAvailable:" + mNetworkAvailable + " ,mCurrentPlayState:" + mCurrentPlayState);
            if (mCurrentPlayState != PlayState.ERROR) {
                //  判断网络状态,如果有网络,则重新加载播放,如果没有则报错
                if ((mIsOnlineSource && mNetworkAvailable) || !mIsOnlineSource) {
                    startOrRestartPlay();
                } else {
                    if (mIPlayerImpl != null) {
                        mIPlayerImpl.onError();
                    }
                    mOnPrepared = false;
                    updatePlayState(PlayState.ERROR);
                }
            }
            return true;
        }
    };
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mLastPlayingPos = 0;
            mLastBufferLength = -1;
            mController.updateProgress(0, 100);
            stopUpdateTimer();
            updatePlayState(PlayState.COMPLETE);
            if (mIPlayerImpl != null) {
                mIPlayerImpl.onComplete();
            }
        }
    };
    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            sendAutoHideBarsMsg();
            return false;
        }
    };
    private boolean mIsOnlineSource;
    private ProgressBar mPbLoading;

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
        View rlPlayer = findViewById(R.id.rl_player);
        mVv = (ZZVideoView) findViewById(R.id.zzvv_main);
        mTitleBar = (PlayerTitleBar) findViewById(R.id.pt_title_bar);
        mController = (PlayerController) findViewById(R.id.pc_controller);
        mPbLoading = (ProgressBar) findViewById(R.id.pb_loading);

        initAnimation();

        mTitleBar.setTitleBarImpl(mTitleBarImpl);
        mController.setControllerImpl(mControllerImpl);
        //        mVv.setZOrderOnTop(true);
        //        mVv.setBackgroundColor(Color.RED);
        mVv.setOnTouchListener(this);
        rlPlayer.setOnTouchListener(this);
        mVv.setOnPreparedListener(mPreparedListener);
        //        mVv.setOnInfoListener(mInfoListener);
        mVv.setOnCompletionListener(mCompletionListener);
        mVv.setOnErrorListener(mErrorListener);
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

    /**
     * 加载视频
     * 在这里才真正把视频路径设置到VideoView中
     */
    private void load() {
        //        if (mHasSetPath2vv) {
        //            return;
        //        }
        mNetworkAvailable = NetworkUtil.isNetworkAvailable(mHostActivity.get());

        // 处理加载过程中,断网,再联网,如果重新设置video路径,videoView会去reset mediaPlayer,可能出错
        // TODO: 2016/7/15 郁闷了,不重新设置的话,断网播放到缓冲尽头又联网时,没法继续加载播放,矛盾啊,先备注下
        if (mIsOnlineSource) {
            if (!mNetworkAvailable) {
                Log.i(TAG, "load failed because network not available");
                return;
            }
            mVv.setVideoPath(mVideoUri.toString());
        } else if (VideoUriProtocol.PROTOCOL_ANDROID_RESOURCE.equalsIgnoreCase(mVideoProtocol)) {
            mVv.setVideoURI(mVideoUri);
        }
        mHasSetPath2vv = true;
    }

    /**
     * 开始播放或重新加载播放
     */
    public void startOrRestartPlay() {
        // 断过网
        if (mLastBufferLength >= 0 && mIsOnlineSource) {
            resumeFromError();
        } else {
            goOnPlay();
        }
    }

    public void resumeFromError() {
        load();
        mVv.start();
        mVv.seekTo(mLastPlayingPos);
        updatePlayState(PlayState.PLAY);
        resetUpdateTimer();
    }


    public void goOnPlay() {
        // 在线视频,网络异常时,不进行加载播放
        if (mIsOnlineSource && !mNetworkAvailable) {
            return;
        }
        mVv.start();
        if (mCurrentPlayState == PlayState.COMPLETE) {
            mVv.seekTo(0);
        }
        updatePlayState(PlayState.PLAY);
        resetUpdateTimer();
    }

    public void pausePlay() {
        updatePlayState(PlayState.PAUSE);
        if (canPause()) {
            mVv.pause();
        }

        //        stopUpdateTimer();
    }

    public void stopPlay() {
        if (canStop()) {
            mVv.stopPlayback();
        }
    }

    /**
     * 设置视频播放路径
     * 1. 设置当前项目中res/raw目录中的文件: "android.resource://" + getPackageName() + "/" + R.raw.yourName
     * 2. 设置网络视频文件: "http:\//****\/abc.mp4"
     *
     * @param path
     * @return 设置成功返回 true
     */
    public void setVideoUri(@NonNull Activity act, @NonNull String path) {
        mHostActivity = new WeakReference<Activity>(act);
        mVideoUri = Uri.parse(path);
        mVideoProtocol = mVideoUri.getScheme();
        if (VideoUriProtocol.PROTOCOL_HTTP.equalsIgnoreCase(mVideoProtocol)) {
            mIsOnlineSource = true;
        }

        initNetworkMonitor();
        registerNetworkReceiver();
    }

    public void loadAndStartVideo(@NonNull Activity act, @NonNull String path) {
        setVideoUri(act, path);
        load();
        startOrRestartPlay();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCurrentDownTime = Calendar.getInstance().getTimeInMillis();
                if (isTouchEventValid()) {
                    mHandler.removeMessages(MSG_AUTO_HIDE_BARS);
                    if (mController.getVisibility() == VISIBLE) {
                        showOrHideBars(false, true);
                    } else {
                        showOrHideBars(true, true);
                    }
                    mLastDownTime = mCurrentDownTime;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                sendAutoHideBarsMsg();
                break;
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
            if (mTitleBar.getVisibility() != VISIBLE) {
                mTitleBar.startAnimation(mEnterFromTop);
                mController.startAnimation(mEnterFromBottom);
            }
        } else {
            if (mTitleBar.getVisibility() != GONE) {
                mTitleBar.startAnimation(mExitFromTop);
                mController.startAnimation(mExitFromBottom);
            }
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

    private void resetUpdateTimer() {
        stopUpdateTimer();
        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 播放结束(onComplete)后,点击播放按钮,开始播放时初次读取到的时间值是视频结束位置
                int currentUpdateTime = getCurrentTime();
                if (currentUpdateTime >= 1000 && Math.abs(currentUpdateTime - mLastUpdateTime) >= 800) {
                    mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS_TIME);
                    mLastUpdateTime = currentUpdateTime;
                    mLastPlayingPos = 0;
                }

            }
        }, 0, UPDATE_TIMER_INTERVAL);
    }

    private void stopUpdateTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    private int getCurrentTime() {
        return mVv.getCurrentPosition();
    }

    /**
     * @return 缓冲百分比 0-100
     */
    private int getBufferProgress() {
        return mIsOnlineSource ? mVv.getBufferPercentage() : 100;
    }

    /**
     * 发送message给handler,自动隐藏标题栏
     */
    private void sendAutoHideBarsMsg() {
        //  初始自动隐藏标题栏和控制栏
        mHandler.removeMessages(MSG_AUTO_HIDE_BARS);
        mHandler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_BARS, TIME_AUTO_HIDE_BARS_DELAY);
    }

    /**
     * 屏幕方向改变时,回调该方法
     */
    public void updateActivityOrientation() {
        int orientation = OrientationUtil.getOrientation(mHostActivity.get());

        //更新播放器宽高
        float width = DensityUtil.getWidthInPx(mHostActivity.get());
        float height = DensityUtil.getHeightInPx(mHostActivity.get());
        if (orientation == OrientationUtil.HORIZONTAL) {
            getLayoutParams().height = (int) height;
            getLayoutParams().width = (int) width;
        } else {
            width = DensityUtil.getWidthInPx(mHostActivity.get());
            height = DensityUtil.dip2px(mHostActivity.get(), 200f);
        }
        getLayoutParams().height = (int) height;
        getLayoutParams().width = (int) width;

        //需要强制显示再隐藏控制条,不然若切换为横屏时控制条是隐藏的,首次触摸显示时,会显示在200dp的位置
        forceShowOrHideBars(true);
        sendAutoHideBarsMsg();

        //更新全屏图标
        mController.setOrientation(orientation);
    }

    /**
     * 宿主页面onResume的时候从上次播放位置继续播放
     */
    public void onHostResume() {
        Log.i(TAG, "onHostResume " + mLastPlayingPos);
        mNetworkAvailable = NetworkUtil.isNetworkAvailable(mHostActivity.get());
        if (mLastPlayingPos >= 0) {
            // 进度条更新为上次播放时间
            startOrRestartPlay();
        }

        //强制弹出标题栏和控制栏
        forceShowOrHideBars(true);
        sendAutoHideBarsMsg();
    }

    /**
     * 宿主页面onPause的时候记录播放位置，好在onResume的时候从中断点继续播放
     * 如果在宿主页面onStop的时候才来记录位置,则取到的都会是0
     */
    public void onHostPause() {
        mLastPlayingPos = getCurrentTime();
        getBufferLength();
        stopUpdateTimer();
        mHandler.removeMessages(MSG_UPDATE_PROGRESS_TIME);
        mHandler.removeMessages(MSG_AUTO_HIDE_BARS);
        // 在这里不进行stop或者pause播放的行为，因为特殊情况下会导致ANR出现
    }

    /**
     * 宿主页面destroy的时候页面恢复成竖直状态
     */
    public void onHostDestroy() {
        OrientationUtil.forceOrientation(mHostActivity.get(), OrientationUtil.VERTICAL);
        unRegisterNetworkReceiver();
    }

    /**
     * 初始化网络变化监听器
     */
    public void initNetworkMonitor() {
        unRegisterNetworkReceiver();
        // 网络变化
        mNetworkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 网络变化
                if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    mNetworkAvailable = NetworkUtil.isNetworkAvailable(mHostActivity.get());
                    mController.updateNetworkState(mNetworkAvailable || !mIsOnlineSource);
                    if (!mNetworkAvailable) {
                        getBufferLength();
                        mIPlayerImpl.onNetWorkError();
                    } else {
                        if (mCurrentPlayState == PlayState.ERROR) {
                            updatePlayState(PlayState.IDLE);
                        }
                    }
                }
            }
        };
    }

    /**
     * 获取已缓冲长度 毫秒
     */
    private int getBufferLength() {
        mLastBufferLength = getBufferProgress() * mDuration / 100;
        return mLastBufferLength;
    }

    private void registerNetworkReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mHostActivity.get().registerReceiver(mNetworkReceiver, filter);
    }

    public void unRegisterNetworkReceiver() {
        if (mNetworkReceiver != null) {
            mHostActivity.get().unregisterReceiver(mNetworkReceiver);
            mNetworkReceiver = null;
        }
    }

    private boolean canPause() {
        return ((mCurrentPlayState != PlayState.ERROR) && mOnPrepared && isPlaying() && mVv.canPause());
    }

    private boolean canStop() {
        return (mCurrentPlayState == PlayState.PREPARE)
                || (mCurrentPlayState == PlayState.PAUSE)
                || (mCurrentPlayState == PlayState.COMPLETE)
                || isPlaying();

    }


    /**
     * 设置进度条样式
     *
     * @param resId 进度条progressDrawable分层资源
     *              数组表示的进度资源分别为 background - secondaryProgress - progress
     *              若对应的数组元素值 <=0,表示该层素材保持不变;
     *              注意:progress和secondaryProgress的shape资源需要做成clip的,否则会直接完全显示
     */
    public void setProgressLayerDrawables(@DrawableRes int... resId) {
        mController.setProgressLayerDrawables(resId);
    }

    public void setProgressLayerDrawables(@DrawableRes int resId) {
        mController.setProgressLayerDrawables(resId);

    }

    /**
     * 设置进度条按钮图片
     *
     * @param thumbId
     */
    public void setProgressThumbDrawable(@DrawableRes int thumbId) {
        mController.setProgressThumbDrawable(thumbId);
    }

    /**
     * 设置暂停按钮图标
     *
     * @param iconPause
     */
    public void setIconPause(@DrawableRes int iconPause) {
        mController.setIconPause(iconPause);
    }

    /**
     * 设置播放按钮图标
     *
     * @param iconPlay
     */
    public void setIconPlay(@DrawableRes int iconPlay) {
        mController.setIconPlay(iconPlay);
    }

    /**
     * 设置退出全屏按钮
     *
     * @param iconShrink
     */
    public void setIconShrink(@DrawableRes int iconShrink) {
        mController.setIconShrink(iconShrink);
    }

    /**
     * 设置退出全屏按钮
     *
     * @param iconExpand
     */
    public void setIconExpand(@DrawableRes int iconExpand) {
        mController.setIconExpand(iconExpand);
    }

    /**
     * 隐藏时间进度和总时间信息
     */
    public void hideTimes() {
        mController.hideTimes();
    }

    /**
     * 显示时间进度和总时间信息
     */
    public void showTimes() {
        mController.showTimes();
    }
}
