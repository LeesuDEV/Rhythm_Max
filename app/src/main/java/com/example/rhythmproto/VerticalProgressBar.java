package com.example.rhythmproto;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ProgressBar;


public class VerticalProgressBar extends ProgressBar {
    // Progress는 가로형밖에 없으므로 세로형 클래스를 따로 만들어 정의해줘야함. 이것을 xml에서 사용할것
    public VerticalProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
    }

    public VerticalProgressBar(Context context,AttributeSet attrs){
        super(context,attrs);
    }

    public VerticalProgressBar(Context context) {
        super(context);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.rotate(-90);
        canvas.translate(-getHeight(),0);

        super.onDraw(canvas);
    }   //캔버스 90도 회전시키기

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }  //가로와 세로길이를 바꿔서 측정

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        invalidate();
    }
}
