package org.lynxz.zzvideoview

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    private val rxPermission by lazy { RxPermissions(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)//隐藏标题
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)//设置全屏
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {
        btn_custom.setOnClickListener { playOnlineVideo() }
        btn_custom_raw.setOnClickListener { playRawVideo() }
        btn_custom_local.setOnClickListener { playLocalVideo() }
    }

    /**
     * 播放项目自带的raw视频文件
     * */
    private fun playRawVideo() {
        startActivity(Intent(this, ZZPlayerDemoActivity::class.java)
                .putExtra(ZZPlayerDemoActivity.KEY_VIDEO_TYPE, ZZPlayerDemoActivity.TYPE_RAW))
    }

    /**
     * 播放在线视频(url,需要Internet权限)
     * */
    private fun playOnlineVideo() {
        startActivity(Intent(this, ZZPlayerDemoActivity::class.java)
                .putExtra(ZZPlayerDemoActivity.KEY_VIDEO_TYPE, ZZPlayerDemoActivity.TYPE_URL))
    }

    /**
     * 播放本机视频,需要sd卡权限
     * */
    private fun playLocalVideo() {
        if (Build.VERSION.SDK_INT >= 23) {
            rxPermission.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe({
                        if (it) {
                            startActivity(Intent(this, ZZPlayerDemoActivity::class.java)
                                    .putExtra(ZZPlayerDemoActivity.KEY_VIDEO_TYPE, ZZPlayerDemoActivity.TYPE_LOCAL))
                        } else {
                            showToast("没有sd卡权限")
                        }

                    }, { it.printStackTrace() })
        } else {
            startActivity(Intent(this, ZZPlayerDemoActivity::class.java)
                    .putExtra(ZZPlayerDemoActivity.KEY_VIDEO_TYPE, ZZPlayerDemoActivity.TYPE_LOCAL))
        }
    }
}