package com.example.rhythmproto;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {
ImageView loadingIMG;
    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        loadingIMG = findViewById(R.id.loadingImg);
        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setRepeatCount(Animation.INFINITE);
        loadingIMG.startAnimation(rotate);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoadingActivity.this, GameActivity.class);
                startActivity(intent);  // 인턴트로 메인으로 이동
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // 2초동안 페이드인아웃
            }
        }, 3000); //2초후 게임시작
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
