package org.lynxz.zzvideoview;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import org.lynxz.zzvideoview.controller.IPlayerImpl;
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
            public void onBack() {
                finish();
            }
        };

        mVp.setPlayerController(playerImpl);
    }

    private void initData() {
        mVideoUrl = "android.resource://" + getPackageName() + "/" + R.raw.shuai_dan_ge;
        //        mVideoUrl = "http://7xt0mj.com2.z0.glb.clouddn.com/lianaidaren.v.640.480.mp4";
    }

    private void initView() {

        mVp = (VideoPlayer) findViewById(R.id.vp);
        mVp.setTitle("这是测试视频");
        mVp.loadAndStartVideo(mVideoUrl);
    }
}
