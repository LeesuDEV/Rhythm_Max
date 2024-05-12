package com.example.rhythmproto;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class QuitActivity extends AppCompatActivity {

    TextView loadingTV; // 게임오버,메인으로가기 텍스트뷰
    int i = 1;
    Runnable run = new Runnable() {
        @Override
        public void run() {`
            updateLoadingText(); // . ~ ...을 반복
            handler.postDelayed(this, 300); //0.3초 간격으로 반복
        }
    };
    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quit_loading);

        loadingTV = findViewById(R.id.loadingText);

        handler.post(run);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(QuitActivity.this, MainActivity.class);
                startActivity(intent);  // 인턴트로 메인으로 이동
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // 2초동안 페이드인아웃
            }
        }, 2500); //2초후 게임시작
    }

    private void updateLoadingText() {
        switch (i) {
            case 1:
                loadingTV.setText(".");
                i = 2;
                break;
            case 2:
                loadingTV.setText("..");
                i = 3;
                break;
            case 3:
                loadingTV.setText("...");
                i = 1;
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacksAndMessages(run);
    }
}
