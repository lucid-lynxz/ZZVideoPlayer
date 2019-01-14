[博客地址](https://juejin.im/post/5a433b236fb9a045293708be)
#[![Build Status](https://travis-ci.org/tyzlmjj/Codec.svg?branch=master)](https://travis-ci.org/tyzlmjj/Codec)	
#[![Download](https://api.bintray.com/packages/tyzlmjj/maven/codec/images/download.svg) ](https://bintray.com/tyzlmjj/maven/codec/view)


### 注意: 若要测试播放本地视频,请先将项目中的 `app/src/main/res/raw/shuai_dan_ge.mp4` 拷贝到手机sd卡目录下

# 简易视频播放器
封装了系统VideoView,主要进行了网络异常处理以及进度条拖动/横竖屏切换功能,
并且当用户切换到桌面或其他app后再返回,会从中断的位置继续播放


![播放器](https://github.com/lucid-lynxz/markdownPhotos/blob/master/zz_video_player/zz_video_player_01.gif?raw=true)

## 使用
```maven
<dependency>
  <groupId>org.lynxz.zzplayerlibrarys</groupId>
  <artifactId>zzplayer</artifactId>
  <version>1.0.9</version>
  <type>pom</type>
</dependency>
```

```gradle
dependencies {
    ......
    implementation 'org.lynxz.zzplayerlibrarys:zzplayer:1.0.9'
}
```

### 播放网络视频添加权限
目前所需权限(`INTERNET`, `WRITE_SETTINGS`, `READ_EXTERNAL_STORAGE`, `ACCESS_NETWORK_STATE` 等) 已在 `zzplayerlibrary/src/main/AndroidManifest.xml` 中申明了;
app层请自行按需动态申请  `READ_EXTERNAL_STORAGE` 等权限即可;

### 设置播放页属性
```xml
<activity
    android:name=".ZZPlayerDemoActivity"
    android:configChanges="keyboard|orientation|screenSize"
    android:launchMode="singleTask"
    android:screenOrientation="portrait"/>
```

### 导入播放器
这里设置一个默认高度，在竖屏时的高度，横屏时默认为屏幕宽度
```xml
<org.lynxz.zzplayerlibrary.widget.VideoPlayer
    android:id="@+id/vp"
    android:layout_width="match_parent"
    android:layout_height="200dp"/>
```

### 视频路径
```java
//工程文件内的视频文件以及网络视频地址
// mVideoUrl = "android.resource://" + getPackageName() + "/" + R.raw.shuai_dan_ge; // raw视频
// mVideoUrl = "file:///storage/emulated/0/shuai_dan_ge.mp4"; // 本机sd卡视频
mVideoUrl = "http://hometime-img-service.b0.upaiyun.com/1703070530554567833.mp4";
```

### 设置
```java
mVp = (VideoPlayer) findViewById(R.id.vp);
mVp.setTitle("视频名称"); //设置视频名称
mVp.setToggleExpandable(boolean); //true-启用横竖屏切换按钮  false-隐藏横竖屏切换韩流

mVp.setVideoUri(this,mVideoUrl);//设置视频路径
mVp.startPlay();//开始播放

//也可以直接设置路径并播放
mVp.loadAndStartVideo(this, mVideoUrl); // 设置视频播放路径并开始播放

mVp.setFullScreenImmediately(); // 直接使用横屏显示
```

### 控制栏等自定义设置
```java
// 自定义加载框图标
mVp.setIconLoading(R.drawable.loading_red);

//设置控制栏播放/暂停/全屏/退出全屏按钮图标
mVp.setIconPlay(R.drawable.play);
mVp.setIconPause(R.drawable.pause);
mVp.setIconExpand(R.drawable.expand);
mVp.setIconShrink(R.drawable.shrink);
//隐藏/显示控制栏时间值信息
// mVp.hideTimes();
// mVp.showTimes();

// 设置进度条样式
mVp.setProgressThumbDrawable(R.drawable.progress_thumb);
mVp.setProgressLayerDrawables(R.drawable.biz_video_progressbar);//自定义的layer-list
// mVp.setProgressLayerDrawables(0, 0, R.drawable.shape_progress);//逐层设置,0的话表示保持默认,具体请参考方法注释

// 是否允许滑动调整音量,默认enable
mVp.setControlFlag(VideoPlayer.FLAG_ENABLE_VOLUME_CHANGE);
mVp.setControlFlag(VideoPlayer.FLAG_DISABLE_VOLUME_CHANGE);

// 是否允许滑动调整亮度,默认enable
mVp.setControlFlag(VideoPlayer.FLAG_ENABLE_BRIGHTNESS_CHANGE);
mVp.setControlFlag(VideoPlayer.FLAG_DISABLE_BRIGHTNESS_CHANGE);
```

### 实现IPlayerImpl控制类
```java
private IPlayerImpl playerImpl = new IPlayerImpl() {

    /**
     * 网络异常时处理
     */
    public void onNetWorkError() {
    }

    /**
     * 标题栏返回按钮功能
     */
    public void onBack() {
    }

    /**
     * 播放结束后
     */
    public void onComplete() {
    }

    /**
     * 播放发生异常
     */
    public void onError() {
    }
};

mVp.setPlayerController(playerImpl);
```

### 页面方向变化时,通知播放器更新
```java
@Override
public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (mVp != null) {
        mVp.updateActivityOrientation();
    }
}
```

### 设置页面在onResume时重新从上次中断位置继续播放,关闭页面时,解除网络监听等
```java

@Override
protected void onResume() {
    super.onResume();
    mVp.onHostResume();
}

@Override
protected void onPause() {
    super.onPause();
    mVp.onHostPause();
}

@Override
protected void onDestroy() {
    super.onDestroy();
    mVp.onHostDestroy();
}
```