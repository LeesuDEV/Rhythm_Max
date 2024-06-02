package com.example.rhythmproto;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MissionFail extends AppCompatActivity {

    AnimatorSet animatorSet;

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
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // 2초동안 페이드인아웃
            }
        });


    }

    public void gameOverAnimation(){
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(gotomainTV, "alpha", 0f, 1f);
        fadeIn.setDuration(500); // 0.5초 동안 페이드인

        ObjectAnimator stay = ObjectAnimator.ofFloat(gotomainTV, "alpha", 1f, 1f);
        stay.setDuration(500); // 1초간 유지

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(gotomainTV, "alpha", 1f, 0f);
        fadeOut.setDuration(500); // 0.5초 동안 페이드아웃

        animatorSet = new AnimatorSet();
        animatorSet.playSequentially(fadeIn, stay, fadeOut);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(animatorSet != null) {
                    animatorSet.start();
                }
            }
        });
        animatorSet.start();

        gotomainTV.setVisibility(View.VISIBLE);
    }
}
