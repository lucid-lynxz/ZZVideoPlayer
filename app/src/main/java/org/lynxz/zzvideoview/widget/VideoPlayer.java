package org.lynxz.zzvideoview.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.lynxz.zzvideoview.R;
import org.lynxz.zzvideoview.constant.VideoUriProtocol;
import org.lynxz.zzvideoview.controller.IControllerImpl;
import org.lynxz.zzvideoview.controller.IPlayerImpl;
import org.lynxz.zzvideoview.controller.ITitleBarImpl;
import org.lynxz.zzvideoview.util.DebugLog;

/**
 * Created by zxz on 2016/4/28.
 */
public class VideoPlayer extends RelativeLayout {

    private PlayerTitleBar mTitleBar;
    private ZZVideoView mVv;
    private PlayerController mController;
    private static final String TAG = "zzVideoPlayer";


    private Uri mVideoUri;
    private String mVideoProtocol;//视频地址所用协议

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
        inflate(context, R.layout.zz_video_player, this);
        mVv = (ZZVideoView) findViewById(R.id.zzvv_main);
        mTitleBar = (PlayerTitleBar) findViewById(R.id.pt_title_bar);
        mController = (PlayerController) findViewById(R.id.pc_controller);

        mTitleBar.setTitleBarImpl(mTitleBarImpl);
        mController.setControllerImpl(mControllerImpl);
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
}
