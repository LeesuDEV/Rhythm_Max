package com.example.rhythmproto;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MissionFail extends AppCompatActivity {

    TextView gameoverTV,gotomainTV; // 게임오버,메인으로가기 텍스트뷰

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over);

        gameoverTV = findViewById(R.id.gameOverTV);
        gotomainTV = findViewById(R.id.gotoMainTV);

        gameOverAnimation(); // 게임오버 애니메이션 진행

        gotomainTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MissionFail.this,MainActivity.class);
                startActivity(intent);  // 인턴트로 메인으로 이동
                finish();
            }
        });
    }

    public void gameOverAnimation(){
        ObjectAnimator gameOver = ObjectAnimator.ofFloat(gameoverTV,"alpha",0f,1f); // 텍스트 애니메이션 설정
        gameOver.setDuration(1000); //1초동안 진행

        ObjectAnimator gotomain = ObjectAnimator.ofFloat(gotomainTV,"alpha",0f,1f);
        gotomain.setDuration(1500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(gameOver,gotomain); //AnimatorSet을 이용해 순차저긍로 애니메이션을 진행시킴
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                gameoverTV.setVisibility(View.VISIBLE);   // 애니메이터가 시작할때 게임오버가 1초동안 진행
                Log.d("check","check");
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                gotomainTV.setVisibility(View.VISIBLE);  // 게임오버 애니메이터가 끝나고나면 메인으로가 1초동안 진행
                Log.d("check","check");
            }
        });
        animatorSet.start();
    }
}
