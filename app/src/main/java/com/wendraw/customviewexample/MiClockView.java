package com.wendraw.customviewexample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Calendar;

public class MiClockView extends View {

    private static final String TAG = "MiClockView";

    /* 全局画布 */
    private Canvas mCanvas;
    /* 12、3、6、9小时文本画笔 */
    private Paint mTextPaint;
    /* 小时文本字体大小 */
    private float mTextSize;
    /* 小时圆圈画笔 */
    private Paint mCirclePaint;
    /* 测量小时文本宽高的矩形 */
    private Rect mTextRect = new Rect();
    /* 小时文本圆圈的外接矩形 */
    private RectF mCircleRectF = new RectF();
    /* 小时文本圆圈线条宽度 */
    private float mCircleStrokeWidth = 4;


    /* 时钟半径，不包括padding值 */
    private float mRadius;
    /* 刻度圆弧画笔 */
    private Paint mScaleArcPaint;
    /* 刻度圆弧的外接矩形 */
    private RectF mScaleArcRectF = new RectF();
    /* 刻度线画笔 */
    private Paint mScaleLinePaint;
    /* 刻度线的长度 */
    private float mScaleLineLen;

    /* 梯度扫描渐变 */
    private SweepGradient mSweepGradient;
    /* 渐变矩阵，作用在SweepGradient */
    private Matrix mGradientMatrix;


    /* 暗色，圆弧、刻度线、时针、渐变起始色 */
    private int mDarkColor;
    /* 亮色，用于分针、秒针、渐变终止色 */
    private int mLightColor;
    /* 背景色 */
    private int mBackgroundColor;


    /* 时针角度 */
    private float mHourDegree;
    /* 分针角度 */
    private float mMinuteDegree;
    /* 秒针角度 */
    private float mSecondDegree;
    /* 时针画笔 */
    private Paint mHourHandPaint;
    /* 分针画笔 */
    private Paint mMinuteHandPaint;
    /* 秒针画笔 */
    private Paint mSecondHandPaint;
    /* 时针路径 */
    private Path mHourHandPath = new Path();
    /* 分针路径 */
    private Path mMinuteHandPath = new Path();
    /* 秒针路径 */
    private Path mSecondHandPath = new Path();


    /* 加一个默认的padding值，为了防止用camera旋转时钟时造成四周超出view大小 */
    private float mDefaultPadding;
    private float mPaddingLeft;
    private float mPaddingTop;
    private float mPaddingRight;
    private float mPaddingBottom;

    /* 指针的在x轴的位移
    private float mCanvasTranslateX;
     指针的在y轴的位移
    private float mCanvasTranslateY;
     指针的最大位移
    private float mMaxCanvasTranslate;*/

    public MiClockView(Context context) {
        super(context);
    }

    public MiClockView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes
                (attrs, R.styleable.MiClockView, 0, 0);
        mDarkColor = typedArray.getColor(R.styleable.MiClockView_clock_darkColor,
                Color.parseColor("#80ffffff"));
        mLightColor = typedArray.getColor(R.styleable.MiClockView_clock_lightColor,
                Color.parseColor("#ffffff"));
        mBackgroundColor = typedArray.getColor(R.styleable.MiClockView_clock_backgroundColor,
                Color.parseColor("#237EAD"));
        mTextSize = typedArray.getDimension(R.styleable.MiClockView_clock_textSize,
                DensityUtils.sp2px(context, 14));
        typedArray.recycle();

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);  //设置抗锯齿标志
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(mDarkColor);
        //居中绘制文字
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mCircleStrokeWidth);
        mCirclePaint.setColor(mDarkColor);

        mScaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleLinePaint.setStyle(Paint.Style.STROKE);
        mScaleLinePaint.setColor(mBackgroundColor);

        mScaleArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleArcPaint.setStyle(Paint.Style.STROKE);

        mGradientMatrix = new Matrix();

        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setStyle(Paint.Style.FILL);
        mSecondHandPaint.setColor(mLightColor);

        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setStyle(Paint.Style.FILL);
        mMinuteHandPaint.setColor(mLightColor);

        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mHourHandPaint.setColor(mDarkColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureDimension(widthMeasureSpec), measureDimension(heightMeasureSpec));
        Log.i(TAG, "onMeasure");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        getCurrentTime();
        drawOutSideTextAndArc();
        drawScaleLine();
        drawHourHand();
        drawMinuteHand();
        drawSecondHand();
        //重绘当前的 View，最后会循环调用 onDraw 方法，从而达到时钟转动的效果
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRadius = Math.min(w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingTop() - getPaddingBottom()) / 2f;
        mDefaultPadding = 0.12f * mRadius;
        mPaddingLeft = mDefaultPadding + w / 2f - mRadius + getPaddingLeft();
        mPaddingRight = mDefaultPadding + w / 2f - mRadius + getPaddingRight();
        mPaddingTop = mDefaultPadding + h / 2f - mRadius + getPaddingTop();
        mPaddingBottom = mDefaultPadding + h / 2f - mRadius + getPaddingBottom();

        mScaleLineLen = 0.12f * mRadius;//根据比例确定刻度线长度
        mScaleLinePaint.setStrokeWidth(0.012f * mRadius);

        mScaleArcPaint.setStrokeWidth(mScaleLineLen);

        //梯度扫描渐变，以(w/2,h/2)为中心点，两种起止颜色梯度渐变
        //float数组表示，[0,0.75)为起始颜色所占比例，[0.75,1}为起止颜色渐变所占比例
        mSweepGradient = new SweepGradient(w / 2f, h / 2f,
                new int[]{mDarkColor, mLightColor}, new float[]{0.75f, 1});
    }

    private int measureDimension(int measureSpec) {
        int defaultSize = 800;
        int model = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch (model) {
            case MeasureSpec.EXACTLY:
                return size;
            case MeasureSpec.AT_MOST:
                return Math.min(size, defaultSize);
            case MeasureSpec.UNSPECIFIED:
                return defaultSize;
            default:
                return defaultSize;
        }
    }

    /**
     * 画最外圈的文本和数字
     */
    private void drawOutSideTextAndArc() {
        String[] timeTexts = new String[]{"12", "3", "6", "9"};
        //计算数字的高度
        mTextPaint.getTextBounds(timeTexts[1], 0, timeTexts[1].length(), mTextRect);

        //设置最外围的圆圈的外接矩形
        mCircleRectF.set(
                mPaddingLeft + mTextRect.width() / 2f + mCircleStrokeWidth / 2,
                mPaddingTop + mTextRect.height() / 2f + mCircleStrokeWidth / 2,
                getWidth() - mPaddingRight - mTextRect.width() / 2f - mCircleStrokeWidth / 2,
                getHeight() - mPaddingBottom - mTextRect.height() / 2f - mCircleStrokeWidth / 2);

        //用 drawText 方法画 12，3，6，9 四个数字
        //其中 x 参数表示所绘文本的中间坐标
        //其中 y 参数表示所绘文本的底部坐标
        mCanvas.drawText(timeTexts[0], getWidth() / 2f, mCircleRectF.top + mTextRect.height() / 2f, mTextPaint);
        mCanvas.drawText(timeTexts[1], mCircleRectF.right, getHeight() / 2f + mTextRect.height() / 2f, mTextPaint);
        mCanvas.drawText(timeTexts[2], getWidth() / 2f, mCircleRectF.bottom + mTextRect.height() / 2f, mTextPaint);
        mCanvas.drawText(timeTexts[3], mCircleRectF.left, getHeight() / 2f + mTextRect.height() / 2f, mTextPaint);
        //画连接数字的四段弧线
        for (int i = 0; i < 4; i++) {
            mCanvas.drawArc(mCircleRectF, 5 + 90 * i, 80, false, mCirclePaint);
        }
    }

    /**
     * 画外圈的秒针刻度线
     * 画一圈梯度渲染的亮暗色渐变圆弧，重绘时不断旋转，上面盖一圈背景色的刻度线
     */
    private void drawScaleLine() {
        mCanvas.save();
//        mCanvas.translate(mCanvasTranslateX, mCanvasTranslateY);
        mScaleArcRectF.set(
                mPaddingLeft + mTextRect.height() / 2f + mScaleLineLen * 1.5f,
                mPaddingTop + mTextRect.height() / 2f + mScaleLineLen * 1.5f,
                getWidth() - mPaddingRight - mTextRect.height() / 2f - mScaleLineLen * 1.5f,
                getHeight() - mPaddingBottom - mTextRect.height() / 2f - mScaleLineLen * 1.5f);

        //matrix默认会在三点钟方向开始颜色的渐变，为了吻合钟表十二点钟顺时针旋转的方向，把秒针旋转的角度减去90度
        mGradientMatrix.setRotate(mSecondDegree - 90, getWidth() / 2f, getHeight() / 2f);
        mSweepGradient.setLocalMatrix(mGradientMatrix);
        //画笔设置着色器，类似雷达的扫描效果
        mScaleArcPaint.setShader(mSweepGradient);
        //根据外接矩形绘制圆环
        mCanvas.drawArc(mScaleArcRectF, 0, 360, false, mScaleArcPaint);

        //画背刻度线，颜色与背景一样，会与圆环重合
        for (int i = 0; i < 200; i++) {
            mCanvas.drawLine(getWidth() / 2f, mPaddingTop + mScaleLineLen + mTextRect.height() / 2f,
                    getWidth() / 2f, mPaddingTop + mScaleLineLen * 2 + mTextRect.height() / 2f,
                    mScaleLinePaint);
            //旋转获得线段，其中 degrees 为旋转角度，px 为旋转的中心点的 x 坐标，py 为旋转的中心点的 y 坐标。
            mCanvas.rotate(1.8f, getWidth() / 2f, getHeight() / 2f);
        }
        mCanvas.restore();
    }

    /**
     * 获取当前时间
     */
    private void getCurrentTime() {
        Calendar calendar = Calendar.getInstance();

        //计算出当前的时、分、秒，并且精确到小数点
        float milliSecond = calendar.get(Calendar.MILLISECOND);
        float second = calendar.get(Calendar.SECOND) + milliSecond / 1000;
        float minute = calendar.get(Calendar.MINUTE) + second / 60;
        float hour = calendar.get(Calendar.HOUR) + minute / 60;

        //根据时、分、秒计算出当前指针的位置
        mSecondDegree = second / 60 * 360;
        mMinuteDegree = minute / 60 * 360;
        mHourDegree = hour / 12 * 360;
    }

    /**
     * 绘制时针
     */
    private void drawHourHand() {
        mCanvas.save();
//        mCanvas.translate(mCanvasTranslateX * 1.2f, mCanvasTranslateY * 1.2f);
        //利用 Canvas 的 rotate 方法移动指针位置
        mCanvas.rotate(mHourDegree, getWidth() / 2f, getHeight() / 2f);

        //用 Path 绘制时针指针图标
        mHourHandPath.reset();
        float offset = mPaddingLeft + mTextRect.height() / 2f;
        mHourHandPath.moveTo(getWidth() / 2f - 0.018f * mRadius, getHeight() / 2f - 0.03f * mRadius);
        mHourHandPath.lineTo(getWidth() / 2f - 0.009f * mRadius, offset + 0.48f * mRadius);
        mHourHandPath.quadTo(getWidth() / 2f, offset + 0.46f * mRadius,
                getWidth() / 2f + 0.009f * mRadius, offset + 0.48f * mRadius);
        mHourHandPath.lineTo(getWidth() / 2f + 0.018f * mRadius, getHeight() / 2f - 0.03f * mRadius);
        mHourHandPath.close();
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mHourHandPath, mHourHandPaint);

        //指针环
        mCircleRectF.set(getWidth() / 2f - 0.03f * mRadius, getHeight() / 2f - 0.03f * mRadius,
                getWidth() / 2f + 0.03f * mRadius, getHeight() / 2f + 0.03f * mRadius);
        mHourHandPaint.setStyle(Paint.Style.STROKE);
        mHourHandPaint.setStrokeWidth(0.02f * mRadius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, mHourHandPaint);

        mCanvas.restore();
    }

    /**
     * 绘制分针
     */
    private void drawMinuteHand() {
        mCanvas.save();
//        mCanvas.translate(mCanvasTranslateX * 2f, mCanvasTranslateY * 2f);
        //利用 Canvas 的 rotate 方法移动指针位置
        mCanvas.rotate(mMinuteDegree, getWidth() / 2f, getHeight() / 2f);

        //用 Path 绘制分针指针图标
        mMinuteHandPath.reset();
        float offset = mPaddingTop + mTextRect.height() / 2f;
        mMinuteHandPath.moveTo(getWidth() / 2f - 0.01f * mRadius, getHeight() / 2f - 0.03f * mRadius);
        mMinuteHandPath.lineTo(getWidth() / 2f - 0.008f * mRadius, offset + 0.365f * mRadius);
        mMinuteHandPath.quadTo(getWidth() / 2f, offset + 0.345f * mRadius,
                getWidth() / 2f + 0.008f * mRadius, offset + 0.365f * mRadius);
        mMinuteHandPath.lineTo(getWidth() / 2f + 0.01f * mRadius, getHeight() / 2f - 0.03f * mRadius);
        mMinuteHandPath.close();
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mMinuteHandPath, mMinuteHandPaint);

        //指针环
        mCircleRectF.set(getWidth() / 2f - 0.03f * mRadius, getHeight() / 2f - 0.03f * mRadius,
                getWidth() / 2f + 0.03f * mRadius, getHeight() / 2f + 0.03f * mRadius);
        mMinuteHandPaint.setStyle(Paint.Style.STROKE);
        mMinuteHandPaint.setStrokeWidth(0.02f * mRadius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, mMinuteHandPaint);

        mCanvas.restore();
    }

    /**
     * 绘制秒针
     */
    private void drawSecondHand() {
        mCanvas.save();
        //利用 Canvas 的 rotate 方法移动指针位置
        mCanvas.rotate(mSecondDegree, getWidth() / 2f, getHeight() / 2f);

        //用 Path 绘制秒针指针图标
        mSecondHandPath.reset();
        float offset = mPaddingTop + mTextRect.height() / 2f;
        mSecondHandPath.moveTo(getWidth() / 2f, offset + 0.26f * mRadius);
        mSecondHandPath.lineTo(getWidth() / 2f - 0.05f * mRadius, offset + 0.34f * mRadius);
        mSecondHandPath.lineTo(getWidth() / 2f + 0.05f * mRadius, offset + 0.34f * mRadius);
        mSecondHandPath.close();
        mCanvas.drawPath(mSecondHandPath, mSecondHandPaint);

        mCanvas.restore();
    }
}
