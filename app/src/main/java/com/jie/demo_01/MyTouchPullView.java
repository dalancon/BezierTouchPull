package com.jie.demo_01;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by dalancon on 2019/5/27.
 */

public class MyTouchPullView extends View {

    private Paint mCirclePaint = null;

    private int MAX_HEIGHT = 1000;

    private int mRadius = 100;

    private float mProgress;

    public MyTouchPullView(Context context) {
        this(context, null);
    }

    public MyTouchPullView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyTouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setDither(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(Color.parseColor("#ff0000"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //bezier曲线
        Path bezierPath = new Path();

        //圆心
        float cx = getMeasuredWidth() / 2;
        float cy = getMeasuredHeight() / 2 - mRadius;

        //控制点
        float lControlX = getMeasuredWidth() / 2 * mProgress;
        float lControlY = 0;

        //获取角1和角2
        float angle1 = (float) Math.asin(mRadius / Math.sqrt((lControlX - cx) * (lControlX - cx) + (lControlY - cy) * (lControlY - cy)));
        float angle2 = (float) Math.asin(cy / Math.sqrt((lControlX - cx) * (lControlX - cx) + (lControlY - cy) * (lControlY - cy)));

        float temp = (float) (Math.cos(angle1) * Math.sqrt((lControlX - cx) * (lControlX - cx) + (lControlY - cy) * (lControlY - cy)));

        //计算结束点
        float lEndX = (float) (temp * Math.cos(angle1 + angle2) + lControlX);
        float lEndY = (float) (temp * Math.sin(angle1 + angle2));

        //绘制bezier曲线
        bezierPath.quadTo(lControlX, lControlY, lEndX, lEndY);
        //对称映射
        bezierPath.lineTo(cx + cx - lEndX, lEndY);
        bezierPath.quadTo(cx + cx - lControlX, lControlY, getMeasuredWidth(), 0);
        canvas.drawPath(bezierPath, mCirclePaint);


        //画圆
        canvas.drawCircle(cx, cy, mRadius, mCirclePaint);

        //画图片
        Drawable contentDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_content, null);
        float left = cx - 50;
        float top = cy - 50;
        float right = cx + 50;
        float bottom = cy + 50;
        canvas.save();//保存画布
        canvas.clipRect(left, top, right, bottom);

        Log.e("TAG", "l -> " + left + "  t -> " + top + "  r -> " + right + "  b -> " + bottom);

        contentDrawable.draw(canvas);
        canvas.restore();//恢复画布
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMeasure = MeasureSpec.getSize(widthMeasureSpec);
        int heightMeasure = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthMeasure, Math.min(heightMeasure, (int) (MAX_HEIGHT * mProgress + 0.5f)));
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        requestLayout();
//        invalidate();
    }

    public void release() {
        ValueAnimator animator = ValueAnimator.ofFloat(mProgress, 0);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();
                requestLayout();
            }
        });

        animator.start();

    }
}
