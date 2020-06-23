package com.aptdev.framework.ui.activity.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aptdev.framework.R;
import com.dragon.devl.widget.simple.CountdownTextView;

public class TestCountdownTextViewActivity extends AppCompatActivity {

    private CountdownTextView tvCdt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_countdown_textview_layout);
        tvCdt = findViewById(R.id.tv_cdt);
        tvCdt.setOnCountdownListener(new CountdownTextView.OnCountdownListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onChange(int countdown) {

            }

            @Override
            public void onFinish() {

            }
        });
        tvCdt.setCountdown(100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvCdt.resumeCountdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        tvCdt.pauseCountdown();
    }
}
