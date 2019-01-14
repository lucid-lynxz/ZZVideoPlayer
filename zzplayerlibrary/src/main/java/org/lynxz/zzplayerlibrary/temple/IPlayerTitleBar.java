package org.lynxz.zzplayerlibrary.temple;

import android.support.annotation.Nullable;

import org.lynxz.zzplayerlibrary.controller.ITitleBarImpl;

/**
 * Created by lynxz on 2019/1/14
 * E-mail: lynxz8866@gmail.com
 * <p>
 * Description: 标题栏需要实现的功能
 */
public interface IPlayerTitleBar {
    /**
     * 设置视频标题
     */
    void setTitle(@Nullable CharSequence title);

    /**
     * 另外实现返回键功能
     */
    void setTitleBarImpl(@Nullable ITitleBarImpl titleBarImpl);
}
