package com.dragon.devl.widget.video;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.dragon.devl.widget.R;
import com.dragon.devl.widget.utils.NiceUtil;
import com.dragondevl.utils.AudioUtil;
import com.dragondevl.utils.StringUtils;

import java.io.File;


/**
 * Created by lb on 2018/7/8.
 */

public class NativeVideoPlayer extends FrameLayout implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnVideoSizeChangedListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {

    private static final int UPDATE_SEEK_BAR = 0x01;

    private static final int HANDLER_HIDE_TOP_BOTTOM = 0x02;
    private static final int HANDLER_HIDE_TOP_BOTTOM_DELAY = 5 * 1000;
    private static final int THRESHOLD = 80;
    /**
     * 普通模式
     **/
    public static final int MODE_NORMAL = 10;
    /**
     * 全屏模式
     **/
    public static final int MODE_FULL_SCREEN = 11;

    public static final int PLAY_MODE_LIST = 0;//列表播放模式
    public static final int PLAY_MODE_ONE = 1;//单曲循环播放模式


    private int mCurrentMode = MODE_NORMAL;

    private FrameLayout mContainer;

    private VideoView videoView;
    private LinearLayout bottom;
    private ImageView playLast;
    private ImageView restartOrPause;
    private ImageView playNext;
    private ImageView ivPlayMode;
    private TextView position;
    private TextView duration;
    private SeekBar seek;
    private TextView exit;
    private LinearLayout changeVolume;
    private ProgressBar changeVolumeProgress;
    private LinearLayout top;
    private TextView title;
    private ImageView fullScreen;
    private ImageView ivVdoList;


    private boolean remove;
    private boolean firstHideBottom;

    private int skipToPosition;
    private int mGestureDownVolume;

    private int playMode;

    private OnMediaPlayStateListener onMediaPlayStateListener;

    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == UPDATE_SEEK_BAR) {
                seek.setProgress(videoView.getCurrentPosition());
                myHandler.sendEmptyMessageDelayed(UPDATE_SEEK_BAR, 300);
                position.setText(NiceUtil.formatTime(videoView.getCurrentPosition()));
            } else if (HANDLER_HIDE_TOP_BOTTOM == msg.what) {
                hideTopBottom();
            }
            return false;
        }
    });
    private float mDownX;
    private float mDownY;
    private boolean mNeedChangeVolume;

    public NativeVideoPlayer(@NonNull Context context) {
        this(context, null);
    }

    public NativeVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NativeVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        //enterFullScreen();
    }

    private void init() {
        playMode = getVideoMode();
        LayoutInflater.from(getContext()).inflate(R.layout.native_video_player_layout, this, true);
        mContainer = (FrameLayout) findViewById(R.id.root_view);
        videoView = (VideoView) findViewById(R.id.videoView);
        bottom = (LinearLayout) findViewById(R.id.bottom);
        playLast = (ImageView) findViewById(R.id.play_last);
        restartOrPause = (ImageView) findViewById(R.id.restart_or_pause);
        playNext = (ImageView) findViewById(R.id.play_next);
        ivPlayMode = (ImageView) findViewById(R.id.iv_play_mode);
        position = (TextView) findViewById(R.id.position);
        duration = (TextView) findViewById(R.id.duration);
        seek = (SeekBar) findViewById(R.id.seek);
        exit = (TextView) findViewById(R.id.exit);
        changeVolume = (LinearLayout) findViewById(R.id.change_volume);
        changeVolumeProgress = (ProgressBar) findViewById(R.id.change_volume_progress);
        top = (LinearLayout) findViewById(R.id.top);
        title = (TextView) findViewById(R.id.title);
        fullScreen = (ImageView) findViewById(R.id.full_screen);
        ivVdoList = (ImageView) findViewById(R.id.iv_vdo_list);

        mContainer.setOnTouchListener(this);
        mContainer.setOnClickListener(this);

        ivPlayMode.setOnClickListener(this);
        playLast.setOnClickListener(this);
        restartOrPause.setOnClickListener(this);
        playNext.setOnClickListener(this);
        fullScreen.setOnClickListener(this);
        exit.setOnClickListener(this);
        ivVdoList.setOnClickListener(this);
        videoView.setOnPreparedListener(this);
        videoView.setOnCompletionListener(this);
        videoView.setOnErrorListener(this);
        seek.setOnSeekBarChangeListener(this);
        seek.setOnSeekBarChangeListener(this);
        this.setOnClickListener(this);

        myHandler.removeMessages(UPDATE_SEEK_BAR);
        myHandler.sendEmptyMessageDelayed(UPDATE_SEEK_BAR, 300);
        if (!firstHideBottom) {
            showTopBottom();
        }


        // 屏幕常量
        mContainer.setKeepScreenOn(true);
        if (playMode == PLAY_MODE_LIST) {
            ivPlayMode.setImageResource(R.mipmap.ic_video_play_list);
        } else {
            ivPlayMode.setImageResource(R.mipmap.ic_video_play_one);
        }
    }

    public void setVideoSource(String path) {
        videoView.setVideoPath(path);
        File file = new File(path);
        title.setText(file.getName());
    }

    public void setVideoName(String name) {
        title.setText(name);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (playMode == PLAY_MODE_ONE) {
            videoView.seekTo(0);
            videoView.start();
        } else {
            if (onMediaPlayStateListener != null) {
                onMediaPlayStateListener.onNativeVideoComplete();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (onMediaPlayStateListener != null) {
            onMediaPlayStateListener.onNativeVideoError("what = " + what);
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e("TAG", "videoView.getDuration() = " + videoView.getDuration());
        if (remove) return;
        onVideoStart();
        int duration = videoView.getDuration();
        int currentPosition = videoView.getCurrentPosition();
        NativeVideoPlayer.this.position.setText(NiceUtil.formatTime(currentPosition));
        NativeVideoPlayer.this.duration.setText(NiceUtil.formatTime(duration));
        seek.setMax(duration);
        // 跳到指定位置播放
        if (skipToPosition != 0) {
            videoView.seekTo(skipToPosition);
            skipToPosition = 0;
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }


    public OnMediaPlayStateListener getOnMediaPlayStateListener() {
        return onMediaPlayStateListener;
    }

    public void setOnMediaPlayStateListener(OnMediaPlayStateListener onMediaPlayStateListener) {
        this.onMediaPlayStateListener = onMediaPlayStateListener;
    }

    @Override
    public void onClick(View v) {
        if (v == playLast) {
            if (onMediaPlayStateListener != null) {
                onMediaPlayStateListener.onNativeVideoPre();
            }
        } else if (v == restartOrPause) {
            if (videoView.isPlaying()) {
                if (onMediaPlayStateListener != null) {
                    onMediaPlayStateListener.onNativeVideoStop();
                }
                onVideoPause();
            } else {
                if (onMediaPlayStateListener != null) {
                    onMediaPlayStateListener.onNativeVideoStart();
                }
                onVideoStart();
            }
        } else if (v == playNext) {
            if (onMediaPlayStateListener != null) {
                onMediaPlayStateListener.onNativeVideoNext();
            }
        } else if (v == fullScreen) {
            if (mCurrentMode == MODE_FULL_SCREEN) {
                exitFullScreen();
            } else if (mCurrentMode == MODE_NORMAL) {
                //
                enterFullScreen();
            }
        } else if (v == exit) {
            if (onMediaPlayStateListener != null) {
                remove = true;
                exitFullScreen();
                onMediaPlayStateListener.onNativeVideoExit();
            }
        } else if (v == ivPlayMode) {
            if (playMode == PLAY_MODE_LIST) {
                if (setVideoMode(PLAY_MODE_ONE)) {
                    playMode = PLAY_MODE_ONE;
                    ivPlayMode.setImageResource(R.mipmap.ic_video_play_one);
                }
            } else {
                if (setVideoMode(PLAY_MODE_LIST)) {
                    playMode = PLAY_MODE_LIST;
                    ivPlayMode.setImageResource(R.mipmap.ic_video_play_list);
                }
            }
        } else if (v == ivVdoList) {
            if (onMediaPlayStateListener != null) {
                onMediaPlayStateListener.showNativeVdoList();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        myHandler.removeMessages(UPDATE_SEEK_BAR);
        //onVideoPause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        myHandler.sendEmptyMessage(UPDATE_SEEK_BAR);
        videoView.seekTo(seek.getProgress());
        onVideoStart();
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {

        return true;
    }*/

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onVideoViewDestroy();
    }

    private void onVideoViewDestroy() {
        videoView.stopPlayback();
        myHandler.removeCallbacksAndMessages(null);
    }

    private void onVideoPause() {
        videoView.pause();
        restartOrPause.setImageResource(R.mipmap.ic_player_start);
    }

    private void onVideoStart() {
        if (!videoView.isPlaying()) {
            videoView.setFocusable(true);
            videoView.requestFocus();
            videoView.start();
            restartOrPause.setImageResource(R.mipmap.ic_player_pause);
        }
    }

    protected void showChangeVolume(int newVolumeProgress) {
        changeVolume.setVisibility(View.VISIBLE);
        changeVolumeProgress.setProgress(newVolumeProgress);
    }

    protected void hideChangeVolume() {
        changeVolume.setVisibility(View.GONE);
    }

    private void showTopBottom() {
        //top.setVisibility(VISIBLE);
        bottom.setVisibility(VISIBLE);
        myHandler.removeMessages(HANDLER_HIDE_TOP_BOTTOM);
        myHandler.sendEmptyMessageDelayed(HANDLER_HIDE_TOP_BOTTOM, HANDLER_HIDE_TOP_BOTTOM_DELAY);
    }

    private void hideTopBottom() {
        //top.setVisibility(GONE);
        bottom.setVisibility(GONE);
        myHandler.removeMessages(HANDLER_HIDE_TOP_BOTTOM);
    }

    public void enterFullScreen() {
        if (mCurrentMode == MODE_FULL_SCREEN) return;
        skipToPosition = videoView.getCurrentPosition();
        fullScreen.setImageResource(R.mipmap.ic_player_shrink);
        // 隐藏ActionBar、状态栏，并横屏
        NiceUtil.hideActionBar(getContext());
        NiceUtil.scanForActivity(getContext())
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ViewGroup contentView = (ViewGroup) NiceUtil.scanForActivity(getContext())
                .findViewById(android.R.id.content);
        this.removeView(mContainer);

        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(mContainer, params);

        mCurrentMode = MODE_FULL_SCREEN;
    }

    public void exitFullScreen() {
        if (mCurrentMode == MODE_FULL_SCREEN) {
            skipToPosition = videoView.getCurrentPosition();
            fullScreen.setImageResource(R.mipmap.ic_player_enlarge);
            NiceUtil.showActionBar(getContext());
            NiceUtil.scanForActivity(getContext())
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            ViewGroup contentView = (ViewGroup) NiceUtil.scanForActivity(getContext())
                    .findViewById(android.R.id.content);
            contentView.removeView(mContainer);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mContainer.setBackgroundColor(Color.BLACK);
            this.addView(mContainer, params);

            mCurrentMode = MODE_NORMAL;
        }
    }

    public void addVideoListView(View view) {
        if (view.getParent() == null) {
            LayoutParams lp = new LayoutParams(360, LayoutParams.MATCH_PARENT, Gravity.RIGHT);
            mContainer.addView(view, lp);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                mNeedChangeVolume = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = y - mDownY;
                float absDeltaY = Math.abs(deltaY);
                if (!mNeedChangeVolume) {
                    if (mDownX < getWidth() * 0.5f) {

                    } else {
                        // 右侧改变声音
                        if (absDeltaY >= THRESHOLD) {
                            mNeedChangeVolume = true;
                            mGestureDownVolume = AudioUtil.getInstance(getContext()).getMediaVolume();
                        }
                    }
                }

                if (mNeedChangeVolume) {
                    deltaY = -deltaY;
                    int maxVolume = AudioUtil.getInstance(getContext()).getMediaMaxVolume();
                    int deltaVolume = (int) (maxVolume * deltaY * 3 / getHeight());
                    int newVolume = mGestureDownVolume + deltaVolume;
                    newVolume = Math.max(0, Math.min(maxVolume, newVolume));
                    AudioUtil.getInstance(getContext()).setMediaVolume(newVolume);
                    int newVolumeProgress = (int) (100f * newVolume / maxVolume);
                    showChangeVolume(newVolumeProgress);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (bottom.getVisibility() == GONE) {
                    showTopBottom();
                } else {
                    hideTopBottom();
                }
                if (mNeedChangeVolume) {
                    hideChangeVolume();
                    return true;
                }
                break;
        }
        return false;
    }

    public interface OnMediaPlayStateListener {
        void onNativeVideoStart();

        void onNativeVideoStop();

        void onNativeVideoComplete();

        void onNativeVideoNext();

        void onNativeVideoPre();

        void onNativeVideoExit();

        void onNativeVideoError(String errorMsg);

        void showNativeVdoList();
    }

    public boolean isFirstHideBottom() {
        return firstHideBottom;
    }

    public void setFirstHideBottom(boolean firstHideBottom) {
        this.firstHideBottom = firstHideBottom;
        if (firstHideBottom) {
            hideTopBottom();
        }
    }

    private int getVideoMode() {
        return getShared().getInt("video_mode", PLAY_MODE_LIST);
    }

    private boolean setVideoMode(int mode) {
        return getSharedEdit().putInt("video_mode", mode).commit();
    }

    private SharedPreferences getShared() {
        return getContext().getSharedPreferences("config", Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getSharedEdit() {
        return getShared().edit();
    }

}
