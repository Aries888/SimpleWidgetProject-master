package com.aptdev.framework.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aptdev.framework.R;
import com.aptdev.framework.ui.activity.main.TestCountdownTextViewActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnOpenCountdown;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnOpenCountdown = findViewById(R.id.btn_open_countdown);

        btnOpenCountdown.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnOpenCountdown) {
            startActivity(new Intent(this, TestCountdownTextViewActivity.class));
        }
    }
}
