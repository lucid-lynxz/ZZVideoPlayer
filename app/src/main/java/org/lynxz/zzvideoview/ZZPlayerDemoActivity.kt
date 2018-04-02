package org.lynxz.zzvideoview

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
import android.widget.Toast

import org.lynxz.zzplayerlibrary.controller.IPlayerImpl
import org.lynxz.zzplayerlibrary.util.OrientationUtil
import org.lynxz.zzplayerlibrary.widget.VideoPlayer

import java.io.File

class ZZPlayerDemoActivity : Activity() {

    private var mVp: VideoPlayer? = null
    private var mVideoUrl = "http://hometime-img-service.b0.upaiyun.com/1703070530554567833.mp4"
    private var mVideoType: Int = 0

    private val playerImpl = object : IPlayerImpl() {
        override fun onNetWorkError() {
            showToast(null)
        }

        override fun onBack() {
            // 全屏播放时,单击左上角返回箭头,先回到竖屏状态,再关闭
            // 这里功能最好跟onBackPressed()操作一致
            val orientation = OrientationUtil.getOrientation(this@ZZPlayerDemoActivity)
            if (orientation == OrientationUtil.HORIZONTAL) {
                OrientationUtil.forceOrientation(this@ZZPlayerDemoActivity, OrientationUtil.VERTICAL)
            } else {
                finish()
            }
        }

        override fun onError() {
            showToast("播放器发生异常")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)//隐藏标题
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)//设置全屏
        setContentView(R.layout.activity_custom_player_demo)

        mVideoType = intent.getIntExtra(KEY_VIDEO_TYPE, TYPE_URL)
        initData()
        initView()
        initListener()
    }

    private fun initListener() {
        mVp!!.setPlayerController(playerImpl)
    }

    private fun initData() {
        when (mVideoType) {
            TYPE_URL -> mVideoUrl = "http://hometime-img-service.b0.upaiyun.com/1703070530554567833.mp4"
            TYPE_RAW -> mVideoUrl = "android.resource://" + packageName + "/" + R.raw.tu_ling
            TYPE_LOCAL -> {
                val externalCacheDir = Environment.getExternalStorageDirectory()
                if (externalCacheDir != null && externalCacheDir.exists()) {
                    val videoPath = externalCacheDir.absolutePath + "/shuai_dan_ge.mp4"
                    val video = File(videoPath)
                    if (video.exists()) {
                        mVideoUrl = "file://$videoPath"
                        return
                    }
                }
            }
        }
    }

    private fun initView() {
        mVp = findViewById(R.id.vp) as VideoPlayer
        mVp!!.setTitle("视频名称")
        mVp!!.loadAndStartVideo(this, mVideoUrl)
        //设置控制栏播放/暂停/全屏/退出全屏按钮图标
        mVp!!.setIconPlay(R.drawable.play)
        mVp!!.setIconPause(R.drawable.pause)
        mVp!!.setIconExpand(R.drawable.expand)
        mVp!!.setIconShrink(R.drawable.shrink)
        //隐藏/显示控制栏时间值信息
        // mVp.hideTimes();
        // mVp.showTimes();
        // 自定义加载框图标
        mVp!!.setIconLoading(R.drawable.loading_red)

        // 设置进度条样式
        mVp!!.setProgressThumbDrawable(R.drawable.progress_thumb)
        mVp!!.setProgressLayerDrawables(R.drawable.biz_video_progressbar)//自定义的layer-list
        // mVp.setProgressLayerDrawables(0, 0, R.drawable.shape_progress);//逐层设置,0的话表示保持默认

        // mVp.setControlFlag(VideoPlayer.FLAG_DISABLE_VOLUME_CHANGE);
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (mVp != null) {
            mVp!!.updateActivityOrientation()
        }
    }


    private fun showToast(msg: String?) {
        var msg = msg
        if (TextUtils.isEmpty(msg)) {
            msg = resources.getString(R.string.zz_player_network_invalid)
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        mVp!!.onHostResume()
    }

    override fun onPause() {
        super.onPause()
        mVp!!.onHostPause()
    }

    override fun onBackPressed() {
        mVp!!.onHostDestroy()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mVp!!.onHostDestroy()
    }

    companion object {
        val KEY_VIDEO_TYPE = "video_type"
        val TYPE_URL = 1
        val TYPE_RAW = 2
        val TYPE_LOCAL = 3
    }
}
