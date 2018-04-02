package org.lynxz.zzvideoview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager

import org.lynxz.zzplayerlibrary.widget.VideoPlayer

class MainActivity : Activity(), View.OnClickListener {

    private val mVp: VideoPlayer? = null
    private val mVideoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)//隐藏标题
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)//设置全屏
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {
        findViewById(R.id.btn_custom).setOnClickListener(this)
        findViewById(R.id.btn_custom_raw).setOnClickListener(this)
        findViewById(R.id.btn_custom_local).setOnClickListener(this)
    }


    override fun onClick(v: View) {
        val id = v.id
        var intent: Intent? = Intent(this, ZZPlayerDemoActivity::class.java)
        when (id) {
            R.id.btn_custom -> intent!!.putExtra(ZZPlayerDemoActivity.KEY_VIDEO_TYPE, ZZPlayerDemoActivity.TYPE_URL)
            R.id.btn_custom_local -> intent!!.putExtra(ZZPlayerDemoActivity.KEY_VIDEO_TYPE, ZZPlayerDemoActivity.TYPE_LOCAL)
            R.id.btn_custom_raw -> intent!!.putExtra(ZZPlayerDemoActivity.KEY_VIDEO_TYPE, ZZPlayerDemoActivity.TYPE_RAW)
            else -> intent = null
        }

        if (intent != null) {
            startActivity(intent)
        }
    }
}