package com.example.rhythmproto;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class JudgmentLineView extends View {
    private Paint paint;
    private float linePositionY;

    public JudgmentLineView(Context context, AttributeSet attrs) {
        super(context,attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
    }

    public void setLinePosition(float positionY) {
        this.linePositionY = positionY;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, linePositionY,getWidth(),linePositionY,paint);
    }
}
