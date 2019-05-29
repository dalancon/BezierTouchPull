package com.jie.demo_01;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by huangjie on 2017/12/18.
 * 类名：
 * 说明：
 */

public class TouchPullView extends View {
    private Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float mCircleRadius = 50;
    private float mCirclePointX, mCirclePointY; //圆心坐标
    private int mDargHeight = 400;  //最大可下拉的高度
    private float mProgress;  //下拉进度值
    private int mTargetWidth = 400; //目标宽度
    private Path mPath = new Path(); //贝塞尔路径
    private Paint mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //贝塞尔画笔
    private int mTargetGravityHeight = 10; //重心点最终高度，决定控制点的Y坐标
    private int mTargetAngle = 105; //角度变换 0~135
    private Interpolator mProgessInterpolator = new DecelerateInterpolator(); //一个由快到慢的插值器
    private Interpolator mTanentAngleInterpolator;
    private Drawable content = null;
    private int mContentDrawableMargin = 0;

    public TouchPullView(Context context) {
        this(context, null);
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        //获取xml属性值
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TouchPullView);
        int color = a.getColor(R.styleable.TouchPullView_pColor, Color.RED);
        mCircleRadius = a.getFloat(R.styleable.TouchPullView_pRadus, 50);
        mDargHeight = a.getDimensionPixelOffset(R.styleable.TouchPullView_pDragHeight, 400);
        mTargetAngle = a.getInt(R.styleable.TouchPullView_pTangentAngle, 105);
        mTargetWidth = a.getDimensionPixelOffset(R.styleable.TouchPullView_pTargeWidth, 400);
        mTargetGravityHeight = a.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetGravityHeight, 10);
        content = a.getDrawable(R.styleable.TouchPullView_pContentDrawable);
        mContentDrawableMargin = a.getDimensionPixelOffset(R.styleable.TouchPullView_pContentDrawableMargin, 0);
        a.recycle();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setAntiAlias(true); //抗锯齿
        paint.setDither(true); //防抖动
        paint.setStyle(Paint.Style.FILL);
        mCirclePaint = paint;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setAntiAlias(true); //抗锯齿
        paint.setDither(true); //防抖动
        paint.setStyle(Paint.Style.FILL);
        mPathPaint = paint;
        //初始化路径插值器
        mTanentAngleInterpolator = PathInterpolatorCompat.create((mCircleRadius * 2) / mDargHeight, 90.0f / mTargetAngle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //基础坐标系改变
        int count = canvas.save();
        //获取平移画布的X的值,随着下滑起始点的坐标移动
        final float transX = (getWidth() - getValueByLine(getWidth(), mTargetWidth, mProgress)) / 2;
        canvas.translate(transX, 0);
        //绘制贝塞尔
        canvas.drawPath(mPath, mPathPaint);
        //画圆
        canvas.drawCircle(mCirclePointX, mCirclePointY, mCircleRadius, mCirclePaint);
        //绘制Drawable
        Drawable drawable = content;
        if (drawable != null) {
            canvas.save();
            canvas.clipRect(drawable.getBounds());
            drawable.draw(canvas);
            canvas.restore();
        }
        canvas.restoreToCount(count);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int MIN_W = (int) (mCircleRadius * 2 + getPaddingLeft() + getPaddingRight()); //需要的最小宽度
        int MIN_H = (int) ((mDargHeight * mProgress + 0.5f)  //mDargHeight * mProgress = moveSize(即actionMove.getY - actionDown.getY),+0.5f为四舍五入
                + getPaddingBottom() + getPaddingTop());
        int widthMeasure = getMeasureSize(widthMeasureSpec, MIN_W);
        int heightMeasure = getMeasureSize(heightMeasureSpec, MIN_H);
        setMeasuredDimension(widthMeasure, heightMeasure);
    }

    /**
     * 获取所需要的宽/高的测量结果
     *
     * @param Spec     测量模式
     * @param minValue 规定的最小值
     * @return 测量结果
     */
    private int getMeasureSize(int Spec, int minValue) {
        int result;
        int mode = MeasureSpec.getMode(Spec);
        int size = MeasureSpec.getSize(Spec);
        switch (mode) {
            case MeasureSpec.AT_MOST: //wrap_content
                result = Math.min(size, minValue); //取测量值和规定的最小宽度中的最小值
                break;
            case MeasureSpec.EXACTLY: //match_parent or exactly num
                result = size;
                break;
            default: //其余情况取最小值
                result = minValue;
                break;
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updatePathLayout();
    }

    /**
     * 设置当前进度值
     *
     * @param mProgress
     */
    public void setProgress(float mProgress) {
        this.mProgress = mProgress;
        requestLayout(); //重新测量
    }

    /**
     * 更新路径
     */
    private void updatePathLayout() {
        final float progress = mProgessInterpolator.getInterpolation(mProgress);

        //获取所有的可绘制的宽/高  此值会根据progress不断的变化
        final float w = getValueByLine(getWidth(), mTargetWidth, mProgress);
        final float h = getValueByLine(0, mDargHeight, mProgress);

        //圆心X坐标
        final float cPointX = w / 2;
        //半径
        final float cRadius = mCircleRadius;
        //圆心Y坐标
        final float cPaintY = h - cRadius;
        //控制点结束Y的值
        final float endPointY = mTargetGravityHeight;
        //更新圆心坐标
        mCirclePointX = cPointX;
        mCirclePointY = cPaintY;

        final Path path = mPath;
        path.reset(); //重置
        path.moveTo(0, 0);

        //坐标系是以最左边的起始点为原点
        float lEndPointX, lEndPointY; //结束点的X,Y坐标
        float lControlPointX, lControlPointY; //控制点的X，Y坐标
        //获取当前切线的弧度
        double angle = mTanentAngleInterpolator.getInterpolation(progress) * mTargetAngle;//获取当前的角度
        double radian = Math.toRadians(angle); //获取当前弧度
        float x = (float) (Math.sin(radian) * cRadius);  //求出“股”的长度（长的那条直角边）
        float y = (float) (Math.cos(radian) * cRadius);  //求出“勾”的长度（短的那条直角边）
        lEndPointX = cPointX - x; //以起始点为原点，x坐标就等于圆的X坐标减去股的长度
        lEndPointY = cPaintY + y; //以起始点为原点，y坐标就等于圆的y坐标加上勾的长度
        lControlPointY = getValueByLine(0, endPointY, progress);//获取控制点的Y坐标
        float tHeight = lEndPointY - lControlPointY; //结束点与控制点的Y坐标差值
        float tWidth = (float) (tHeight / Math.tan(radian));  //通过计算两个角度是相等的，因此弧度依旧适用
        lControlPointX = lEndPointX - tWidth; //结束点的x - ‘勾’ 的长度求出了控制点的X坐标

        path.quadTo(lControlPointX, lControlPointY, lEndPointX, lEndPointY); //画左边贝塞尔曲线
        path.lineTo(cPointX + (cPointX - lEndPointX), lEndPointY); //左右两个结束点相连
        path.quadTo(cPointX + (cPointX - lControlPointX), lControlPointY, w, 0); //画右边贝塞尔曲线

        updateContentLayout(cPointX, cPaintY, cRadius);
    }

    /**
     * 测量并设置中心Drawable
     *
     * @param cx
     * @param cy
     * @param radius
     */
    private void updateContentLayout(float cx, float cy, float radius) {
        Drawable drawable = content;
        if (drawable != null) {
            int margin = mContentDrawableMargin;
            int l = (int) (cx - radius + margin);
            int r = (int) (cx + radius - margin);
            int t = (int) (cy - radius + margin);
            int b = (int) (cy + radius - margin);
            drawable.setBounds(l, t, r, b);
        }
    }

    /**
     * 获取某一时刻的值
     *
     * @param star      起始点
     * @param end       结束点
     * @param mProgress 当前进度值
     * @return
     */
    private float getValueByLine(float star, float end, float mProgress) {
        return star + (end - star) * mProgress;
    }

    private ValueAnimator valueAnimator;

    public void release() {
        if (valueAnimator == null) {
            ValueAnimator animator = ValueAnimator.ofFloat(mProgress, 0);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(400);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object val = animation.getAnimatedValue();
                    if (val instanceof Float) {
                        setProgress((Float) val);
                    }
                }
            });
            valueAnimator = animator;
        } else {
            valueAnimator.cancel();
            valueAnimator.setFloatValues(mProgress, 0);
        }
        valueAnimator.start();
    }
}
