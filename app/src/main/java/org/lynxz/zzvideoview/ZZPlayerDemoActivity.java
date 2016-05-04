package org.lynxz.zzvideoview;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.lynxz.zzvideoview.util.OrientationUtil;
import org.lynxz.zzvideoview.controller.IPlayerImpl;
import org.lynxz.zzvideoview.util.DensityUtil;
import org.lynxz.zzvideoview.util.NetworkUtil;
import org.lynxz.zzvideoview.widget.VideoPlayer;

public class ZZPlayerDemoActivity extends Activity {

    private VideoPlayer mVp;
    private String mVideoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_custom_player_demo);

        initData();
        initView();
        initListener();

    }

    private void initListener() {
        IPlayerImpl playerImpl = new IPlayerImpl() {
            @Override
            public boolean isNetworkAvailable() {
                return NetworkUtil.isNetworkAvailable(ZZPlayerDemoActivity.this);
            }

            @Override
            public void onNetWorkError() {
                showToast(null);
            }

            @Override
            public void onBack() {
                finish();
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError() {

            }

        };

        mVp.setPlayerController(playerImpl);
    }

    private void initData() {
        //        mVideoUrl = "android.resource://" + getPackageName() + "/" + R.raw.shuai_dan_ge;
        mVideoUrl = "http://7xt0mj.com2.z0.glb.clouddn.com/lianaidaren.v.640.480.mp4";
    }

    private void initView() {
        mVp = (VideoPlayer) findViewById(R.id.vp);
        mVp.setTitle("这是测试视频");
        mVp.loadAndStartVideo(mVideoUrl);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //TODO: 全屏模式
        if (mVp == null) {
            return;
        }

        //根据屏幕方向重新设置播放器的大小
        int orientation = OrientationUtil.getOrientation(this);
        if (orientation == OrientationUtil.HORIZONTAL) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().invalidate();
            float height = DensityUtil.getWidthInPx(this);
            float width = DensityUtil.getHeightInPx(this);
            mVp.getLayoutParams().height = (int) width;
            mVp.getLayoutParams().width = (int) height;
        } else {
            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            float width = DensityUtil.getWidthInPx(this);
            float height = DensityUtil.dip2px(this, 200f);
            mVp.getLayoutParams().height = (int) height;
            mVp.getLayoutParams().width = (int) width;
        }
        mVp.updateActivityOrientation(orientation);
    }


    private void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            msg = getResources().getString(R.string.network_invalid);
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
