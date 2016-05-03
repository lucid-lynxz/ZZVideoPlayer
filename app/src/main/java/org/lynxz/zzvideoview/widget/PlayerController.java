package org.lynxz.zzvideoview.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.lynxz.zzvideoview.R;
import org.lynxz.zzvideoview.bean.PlayState;
import org.lynxz.zzvideoview.controller.IControllerImpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by zxz on 2016/4/28.
 */
public class PlayerController extends FrameLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private IControllerImpl mControllerImpl;
    private CustomSeekBar mCsb;
    private ImageView mIvPlayPause;
    private TextView mTvCurrentTime;
    private TextView mTvTotalTime;
    private ImageView mIvToggleExpandable;
    private int mDuration = 0;//视频长度(ms)
    private SimpleDateFormat mFormatter = null;
    private static final String ZERO_TIME = "00:00";

    public PlayerController(Context context) {
        super(context);
        initView(context);
    }

    public PlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PlayerController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.zz_video_player_controller, this);

        View rlPlayPause = findViewById(R.id.rl_play_pause);
        mIvPlayPause = (ImageView) findViewById(R.id.iv_play_pause);
        mTvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        mTvTotalTime = (TextView) findViewById(R.id.tv_total_time);

        mCsb = (CustomSeekBar) findViewById(R.id.csb);

        View rlToggleExpandable = findViewById(R.id.rl_toggle_expandable);
        mIvToggleExpandable = (ImageView) findViewById(R.id.iv_toggle_expandable);

        rlPlayPause.setOnClickListener(this);
        rlToggleExpandable.setOnClickListener(this);
        mIvPlayPause.setOnClickListener(this);
        mCsb.setOnSeekBarChangeListener(this);
    }

    /**
     * 设置控制条功能回调
     */
    public void setControllerImpl(IControllerImpl controllerImpl) {
        this.mControllerImpl = controllerImpl;
    }

    @Override
    public void onClick(View v) {
        if (mControllerImpl == null) {
            return;
        }

        int id = v.getId();
        switch (id) {
            case R.id.rl_play_pause:
            case R.id.iv_play_pause:
                mControllerImpl.onPlayTurn();
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 设置播放状态
     *
     * @param curPlayState 参考 {@link PlayState}
     */
    public void setPlayState(int curPlayState) {

        switch (curPlayState) {
            case PlayState.PLAY:
                mIvPlayPause.setImageResource(R.drawable.zz_player_pause);
                break;
            case PlayState.PAUSE:
            case PlayState.STOP:
            case PlayState.COMPLETE:
            case PlayState.ERROR:
                mIvPlayPause.setImageResource(R.drawable.zz_player_play);
                break;
        }
    }

    /**
     * 更新播放进度
     * 参考 {@link #updateProgress(int, int, int)}
     */
    public void updateProgress(int progress, int secondProgress) {
        updateProgress(progress, secondProgress, mDuration);
    }

    /**
     * 更新播放进度
     *
     * @param progress       当前进度
     * @param secondProgress 缓冲进度
     * @param maxValue       最大值
     */
    public void updateProgress(int progress, int secondProgress, int maxValue) {
        mDuration = maxValue;
        mCsb.setMax(maxValue);

        // 更新播放时间信息
        if (mFormatter == null) {
            if (maxValue >= (59 * 60 * 1000 + 59 * 1000)) {
                mFormatter = new SimpleDateFormat("HH:mm:ss");
            } else {
                mFormatter = new SimpleDateFormat("mm:ss");
            }
            mFormatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        }
        mTvTotalTime.setText(formatPlayTime(maxValue));

        mCsb.setProgress(progress);
        mCsb.setSecondaryProgress(secondProgress * maxValue / 100);
        mTvCurrentTime.setText(formatPlayTime(progress));
    }

    @SuppressLint("SimpleDateFormat")
    private String formatPlayTime(long time) {
        if (time <= 0) {
            return ZERO_TIME;
        }

        String timeStr = mFormatter.format(new Date(time));
        if (TextUtils.isEmpty(timeStr)) {
            timeStr = ZERO_TIME;
        }
        return timeStr;
    }
}
