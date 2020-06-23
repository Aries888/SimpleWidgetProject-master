package com.dragon.devl.widget.simple;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class CountdownTextView extends AppCompatTextView {

    public static final int HANDLER_TAG_UPDATE_FRAME = 0x10001;
    public static final int HANDLER_TAG_UPDATE_FRAME_DELAY_TIME = 1000;

    private int mCountdown;

    private OnCountdownListener mOnCountdownListener;

    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (HANDLER_TAG_UPDATE_FRAME == msg.what) {
                if (mCountdown <= 0) {
                    if (mOnCountdownListener != null) {
                        mOnCountdownListener.onChange(mCountdown);
                        mOnCountdownListener.onFinish();
                    }
                } else {
                    mCountdown--;
                    setText(String.valueOf(mCountdown));
                    mUIHandler.removeMessages(HANDLER_TAG_UPDATE_FRAME);
                    mUIHandler.sendEmptyMessageDelayed(HANDLER_TAG_UPDATE_FRAME, HANDLER_TAG_UPDATE_FRAME_DELAY_TIME);
                }
            }
            return false;
        }
    });

    public CountdownTextView(Context context) {
        this(context, null);
    }

    public CountdownTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountdownTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*@Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState st = new SavedState(superState);
        st.countdown = mCountdown;
        return superState;
    }*/

    /*@Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState st = (SavedState) state;
        super.onRestoreInstanceState(st.getSuperState());
        if (st.countdown > 0) {//倒计时还没有结束的

        }
    }*/

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUIHandler.removeCallbacksAndMessages(null);
    }

    public void resumeCountdown() {
        setCountdown(mCountdown);
    }

    public void pauseCountdown() {
        mUIHandler.removeMessages(HANDLER_TAG_UPDATE_FRAME);
    }

    public void setCountdown(int countdown) {
        this.mCountdown = countdown;
        if (mOnCountdownListener != null) {
            mOnCountdownListener.onStart();
        }
        if (this.mCountdown > 0) {
            mUIHandler.removeMessages(HANDLER_TAG_UPDATE_FRAME);
            mUIHandler.sendEmptyMessageDelayed(HANDLER_TAG_UPDATE_FRAME, HANDLER_TAG_UPDATE_FRAME_DELAY_TIME);
        }
    }

    public void setOnCountdownListener(OnCountdownListener onCountdownListener) {
        this.mOnCountdownListener = onCountdownListener;
    }

    public interface OnCountdownListener {
        void onStart();
        void onChange(int countdown);
        void onFinish();
    }

    public static class SavedState extends BaseSavedState {

        public int countdown;

        public SavedState(Parcel source) {
            super(source);
            this.countdown = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(countdown);
        }


        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

    }

}
