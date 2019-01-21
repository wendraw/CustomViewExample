package com.wendraw.customviewexample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomView extends View {

    private int mDefaultSize;

    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //第二个参数就是我们在styles.xml文件中的<declare-styleable>标签
        //即属性集合的标签，在R文件中名称为R.styleable+name
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomView);

        //第一个参数为属性集合里面的属性，R文件名称：R.styleable+属性集合名称+下划线+属性名称
        //第二个参数为，如果没有设置这个属性，则设置的默认的值
        mDefaultSize = typedArray.getDimensionPixelSize(R.styleable.CustomView_default_size, 100);

        //最后将 TypedArray 回收
        typedArray.recycle();
    }

    private int getMySize(int defaultSize, int measureSpec) {
        int mySize = defaultSize;

        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.UNSPECIFIED: {  //如果没有指定大小，就设置为默认值
                mySize = defaultSize;
                break;
            }
            case MeasureSpec.AT_MOST: {  //如果测量模式是最大值，就设置为 size
                //我们将大小取最大值,你也可以取其他值
                mySize = size;
                break;
            }
            case MeasureSpec.EXACTLY: {  //如果是固定的大小，那就不要去改变它
                mySize = size;
                break;
            }
            default:
                break;
        }
        return mySize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMySize(100, widthMeasureSpec);
        int height = getMySize(100, heightMeasureSpec);

        if (width < height) {
            height = width;
        } else {
            width = height;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //调用父View的onDraw函数，因为View这个类帮我们实现了一些
        // 基本的而绘制功能，比如绘制背景颜色、背景图片等
        super.onDraw(canvas);
        int r = getMeasuredHeight() / 2;
        //圆心的从横坐标
        int centerX = r;
        //圆心的从纵坐标
        int centerY = r;

        Paint p = new Paint();
        p.setColor(Color.GREEN);
        //开始绘制
        canvas.drawCircle(centerX, centerY, r, p);
    }
}
