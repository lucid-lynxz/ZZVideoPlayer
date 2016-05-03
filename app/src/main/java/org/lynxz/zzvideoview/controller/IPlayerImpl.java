package org.lynxz.zzvideoview.controller;

/**
 * Created by zxz on 2016/5/3.
 */
public interface IPlayerImpl {
    /**
     * 标题栏返回按钮功能
     */
    void onBack();

    /**
     * 播放结束后
     */
    void onComplete();

    /**
     * 播放发生异常
     */
    void onError();
}
