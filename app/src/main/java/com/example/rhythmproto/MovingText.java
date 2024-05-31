package com.example.rhythmproto;

import android.text.TextUtils;
import android.widget.TextView;

public class MovingText {
    public MovingText() {
    }

    public static void moveText(TextView textView) {
        textView.setSingleLine(true); //옆으로 넘기기위해 텍스트뷰를 다음줄로 넘기지않도록 설정
        textView.setMarqueeRepeatLimit(-1); // 넘어가는 텍스트 반복횟수 설정 (-1은 무한)
        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE); // (텍스트 크기를 초과할때 설정할 텍스트 애니메이션)
        textView.setSelected(true); // 텍스트를 선택상태로 설정(흐르기위해)
    }

    //옆으로 흐르는 텍스트뷰
}
