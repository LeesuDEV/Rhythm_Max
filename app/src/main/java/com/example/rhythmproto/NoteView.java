package com.example.rhythmproto;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

public class NoteView extends View {
    private float x, y; // 노트의 위치

    float value; // 노트의 현재 y위치 값
    private int screenHeight; //화면의 높이
    private int width, height; // 노트의 크기
    private ViewGroup noteLayout;
    private String judgment = "BAD"; // 판정 문자
    private Bitmap noteWhite,noteBlue;

    int index; // 노트블럭의 인덱스값
    boolean judged; // 노트가 판정을 받았는지의 여부 /추가지식/ java의 boolean은 기본값이 false이기떄문에 초기화를 안해줘도 된다.

    public NoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void loadNoteImage() {
        noteWhite = BitmapFactory.decodeResource(getResources(),R.drawable.white_note);
        noteBlue = BitmapFactory.decodeResource(getResources(),R.drawable.blue_note);
    }

    public NoteView(Context context, float x, float y, int color,int screen_Height, ViewGroup note_Layout,int index) {
        super(context);
        this.noteLayout = note_Layout;  // 부모 뷰 그룹 레이아웃 참조 - 뷰에서 노트블럭을 삭제하는등 관리할때 사용함.(부모레이아웃에 접근이 가능해야하기 떄문)
        this.screenHeight = screen_Height;
        this.index = index;
        this.x = x; // 초기 x 위치
        this.y = y; // 초기 y 위치
        setX(x);
        setY(y);
        this.width = 230; // 적절한 크기 설정
        this.height = 50; // 적절한 크기 설정
        loadNoteImage();
        setBackgroundColor(Color.parseColor("#000000"));
        setLayoutParams(new ViewGroup.LayoutParams(width, height));
    } //노트 뷰 생성자

    /*private void initPaint(int color) {
        paint = new Paint();
        paint.setColor(color);
    }  // 페인트 메소드 색깔을 설정함.*/

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (index==0 || index==2 || index==4) {
            if (noteWhite != null) {
                canvas.drawBitmap(noteWhite, null, new Rect(0, 0, width, height), null);
            }
        } else if (index==1 || index==3){
            if (noteBlue != null) {
                canvas.drawBitmap(noteBlue, null, new Rect(0, 0, width, height), null);
            }
        }
    }  // 그리기 메소드 165,50크기의 노트블럭을 canvas에 그림

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 크기가 변경될 때 이미지 재조정
        noteBlue = Bitmap.createScaledBitmap(noteBlue, w, h, true);
        noteWhite = Bitmap.createScaledBitmap(noteWhite, w, h, true);
    }  // 크기가 변경될 때 노트블럭 이미지 재조정

    public void startFalling(NoteView noteView, int endY, int duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(noteView.getY(), endY);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator()); // 일정한 속도로 애니메이션
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                value = (float) animation.getAnimatedValue();
                noteView.setY(value); // 실시간으로 NoteView의 Y 위치를 업데이트
                noteView.invalidate();

                // 화면 범위를 벗어나면 노트 제거
                if (value > screenHeight || value < -noteView.getHeight()) {
                    animation.cancel(); // 애니메이션 종료
                    noteLayout.removeView(noteView); // 뷰에서 노트 제거
                }
            }
        });
        animator.start();
    } //노트의 실시간 떨어지는 애니메이션 메소드

    public void setJudgment(String judgment){
        this.judgment = judgment;
        updateVisualsBasedOnJudgment();  // 판정이 작동하면 노트블럭을 뷰에서 지우는 메소드
        invalidate(); // 뷰를 다시 그리도록 요청
    }  // 판정 처리 메소드

    public String getJudgment() {
        return judgment;
    }  // 판정 문자열 반환 메소드

    private void updateVisualsBasedOnJudgment() {
        switch (judgment) {
            case "Perfect":
                removeNoteFromLayout();
                setJudged(true);
                break;
            case "Great":
                removeNoteFromLayout();
                setJudged(true);
                break;
            case "Good":
                removeNoteFromLayout();
                setJudged(true);
                break;
            case "BAD":
                removeNoteFromLayout();
                setJudged(true);
                break;
        }
    }  // 판정결과에 따라 노트객체를 레이아웃에서 삭제하고 + "판정됨" 을 표시하는 judged 값을 true로 변경

    private void removeNoteFromLayout() {
        if (noteLayout != null) {
            noteLayout.post(new Runnable() {
                @Override
                public void run() {
                    noteLayout.removeView(NoteView.this);
                }
            });
        }
    } // 판정 처리후, 노트블럭을 삭제하는 메소드

    public boolean isOffScreen() {
        return y > screenHeight;
    }  // 화면 아래로 완전히 벗어낫는지 확인하는 boolean 메소드 true/false 반환

    public boolean isJudged() {
        return judged;
    }  // 노트가 판정받았는지 확인하는 메소드

    public void setJudged(boolean judged){
        this.judged = judged;
    }  // 판정상태 설정 메소드
}

