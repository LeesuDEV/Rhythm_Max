package com.example.rhythmproto;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.ImageView;

public class AnimationController {
    private Animator currentAnimator; // 현재 실행 중인 애니메이터 참조

    // 애니메이션 시작 메서드
    public void startAnimation(ImageView judgmentImage, String judgment) {
        // 현재 진행 중인 애니메이션 취소
        if (currentAnimator != null) {
            currentAnimator.cancel();
            judgmentImage.clearAnimation();
        }

        switch (judgment){
            case "PERFECT" :
                judgmentImage.setImageResource(R.drawable.perfect_judgment);
                break;
            case "GREAT" :
                judgmentImage.setImageResource(R.drawable.great_judgment);
                break;
            case "GOOD" :
                judgmentImage.setImageResource(R.drawable.good_judgment);
                break;
            case "BAD" :
                judgmentImage.setImageResource(R.drawable.bad_judgment);
                break;
            case "MISS" :
                judgmentImage.setImageResource(R.drawable.miss_judgment);
                break;
        }
        judgmentImage.setVisibility(View.VISIBLE);  // 판정텍스트뷰를 활성화

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(judgmentImage, "alpha", 0f, 1f);
        fadeIn.setDuration(200); // 0.5초 동안 페이드인

        ObjectAnimator stay = ObjectAnimator.ofFloat(judgmentImage, "alpha", 1f, 1f);
        stay.setDuration(300); // 1초간 유지

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(judgmentImage, "alpha", 1f, 0f);
        fadeOut.setDuration(200); // 0.5초 동안 페이드아웃

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(fadeIn, stay, fadeOut);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                judgmentImage.setVisibility(View.INVISIBLE); // 애니메이션이 끝나면 TV를 숨김
                currentAnimator = null; // 애니메이터 참조 제거
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                judgmentImage.setVisibility(View.INVISIBLE); // 애니메이션 취소 시 텍스트뷰 숨김
                currentAnimator = null; // 애니메이터 참조 제거
            }
        });

        currentAnimator = animatorSet; // 현재 애니메이터 참조 갱신
        animatorSet.start(); // 애니메이션 시작
    }
}

