package com.assolutions.altaf.cdlitablet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;


/**
 * Created by HAWONG on 09-Sep-17.
 */

public class CircularTextView extends android.support.v7.widget.AppCompatTextView {

    private float strokeWidth;
    int strokeColor,solidColor;

    public CircularTextView(Context context) {
        super(context);
    }

    public CircularTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint bgPaint=new Paint();
        bgPaint.setColor(solidColor);
        bgPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        Paint outlinePaint=new Paint();
        outlinePaint.setColor(strokeColor);
        outlinePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        int h=this.getHeight();
        int w=this.getWidth();

        int diameter= (h>w)?h:w;
        int radius=diameter/2;


        this.setHeight(diameter);
        this.setWidth(diameter);

        canvas.drawCircle(diameter / 2 , diameter / 2, radius, outlinePaint);

        canvas.drawCircle(diameter / 2, diameter / 2, radius-strokeWidth, bgPaint);

    }

    public void setStrokeWidth(int dp)
    {
        float scale = getContext().getResources().getDisplayMetrics().density;
        strokeWidth = dp*scale;

    }

    public void setStrokeColor(String color)
    {
        strokeColor = Color.parseColor(color);
    }

    public void setSolidColor(String color)
    {
        solidColor = Color.parseColor(color);

    }
}

