package com.example.rhythmproto;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.TextView;

public class ComboAnimationController {
    private Animator currentAnimator; // 현재 실행 중인 애니메이터 참조

    // 애니메이션 시작 메서드
    public void startAnimation(TextView comboText) {
        // 현재 진행 중인 애니메이션 취소
        if (currentAnimator != null) {
            currentAnimator.cancel();
            comboText.clearAnimation();
        }

        comboText.setVisibility(View.VISIBLE);  // 판정텍스트뷰를 활성화

        ObjectAnimator moveDown = ObjectAnimator.ofFloat(comboText, "translationY", 0f, 30f);
        moveDown.setDuration(50); // 0.5초 동안 페이드인

        ObjectAnimator moveUp = ObjectAnimator.ofFloat(comboText, "translationY", 30f, 0f);
        moveUp.setDuration(150); // 1초간 유지

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(moveDown, moveUp);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null; // 애니메이터 참조 제거
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null; // 애니메이터 참조 제거
            }
        });

        currentAnimator = animatorSet; // 현재 애니메이터 참조 갱신
        animatorSet.start(); // 애니메이션 시작
    }
}

