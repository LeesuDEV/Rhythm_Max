package com.example.rhythmproto;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

public class AnimationController {
    private Animator currentAnimator; // 현재 실행 중인 애니메이터 참조

    // 애니메이션 시작 메서드
    public void startAnimation(TextView judgmentTV, String judgment,String color) {
        // 현재 진행 중인 애니메이션 취소
        if (currentAnimator != null) {
            currentAnimator.cancel();
            judgmentTV.clearAnimation();
        }

        judgmentTV.setText(judgment);  // 판정텍스트뷰를 판정텍스트로 설정
        judgmentTV.setTextColor(Color.parseColor(color)); // 판정텍스트뷰를 컬러매개변수로 컬러변경
        judgmentTV.setVisibility(View.VISIBLE);  // 판정텍스트뷰를 활성화

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(judgmentTV, "alpha", 0f, 1f);
        fadeIn.setDuration(200); // 0.5초 동안 페이드인

        ObjectAnimator stay = ObjectAnimator.ofFloat(judgmentTV, "alpha", 1f, 1f);
        stay.setDuration(300); // 1초간 유지

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(judgmentTV, "alpha", 1f, 0f);
        fadeOut.setDuration(200); // 0.5초 동안 페이드아웃

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(fadeIn, stay, fadeOut);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                judgmentTV.setVisibility(View.INVISIBLE); // 애니메이션이 끝나면 TV를 숨김
                currentAnimator = null; // 애니메이터 참조 제거
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                judgmentTV.setVisibility(View.INVISIBLE); // 애니메이션 취소 시 텍스트뷰 숨김
                currentAnimator = null; // 애니메이터 참조 제거
            }
        });

        currentAnimator = animatorSet; // 현재 애니메이터 참조 갱신
        animatorSet.start(); // 애니메이션 시작
    }
}

