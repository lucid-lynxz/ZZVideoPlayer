# 简易视频播放器
封装了系统VideoView,主要进行了网络异常处理以及进度条拖动/横竖屏切换功能,
并且当用户切换到桌面或其他app后再返回,会从中断的位置继续播放


![播放器](https://github.com/lucid-lynxz/markdownPhotos/blob/master/zz_video_player/zz_video_player_01.gif?raw=true)

## 使用

### 播放网络视频添加权限
```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

### 设置播放页属性
```xml
<activity
    android:name=".ZZPlayerDemoActivity"
    android:configChanges="keyboard|orientation|screenSize"
    android:launchMode="singleTask"
    android:screenOrientation="portrait"/>
```

### 视频路径
```java
//工程文件内的视频文件以及网络视频地址
//mVideoUrl = "android.resource://" + getPackageName() + "/" + R.raw.shuai_dan_ge;
mVideoUrl = "http://7xt0mj.com2.z0.glb.clouddn.com/lianaidaren.v.640.480.mp4";
```

### 设置
```java
mVp = (VideoPlayer) findViewById(R.id.vp);
mVp.setTitle("视频名称"); //设置视频名称

mVp.setVideoUri(this,mVideoUrl);//设置视频路径
mVp.startPlay();//开始播放

//也可以直接设置路径并播放
mVp.loadAndStartVideo(this, mVideoUrl); // 设置视频播放路径并开始播放
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