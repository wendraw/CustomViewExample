package com.wendraw.customviewexample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 从上到下的排列布局
 */
public class CustomViewLayout extends ViewGroup {

    public CustomViewLayout(Context context) {
        super(context);
    }

    public CustomViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //将所有的子View进行测量，这会触发每个子View的onMeasure函数
        //注意要与measureChild区分，measureChild是对单个view进行测量
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childCount = getChildCount();

        if (childCount == 0) {
            //如果没有子View,当前ViewGroup没有存在的意义，不用占用空间
            setMeasuredDimension(0, 0);
        } else {
            //如果高宽都是包裹内容
            if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
                //我们就将高度设为所有子 View 的高度相加，宽度设为子 View 最大的。
                int width = getMaxChildWidth();
                int height = getTotalHeight();
                setMeasuredDimension(width, height);

            } else if (widthMode == MeasureSpec.AT_MOST) {    //只有宽度是包裹内容
                //高度设置为 ViewGroup 的测量值，宽度为子 View 的最大宽度
                setMeasuredDimension(getMaxChildWidth(), heightSize);

            } else if (heightMode == MeasureSpec.AT_MOST) {    //只有高度是包裹内容
                //高度设置为 ViewGroup 的测量值，宽度为子 View 的最大宽度
                setMeasuredDimension(widthSize, getTotalHeight());
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        //记录当前的高度位置
        int curHeight = t;
        //将子 View 逐个拜访
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            //摆放子 View，参数分别是子 View 矩形区域的左、上、右、下边
            child.layout(l, curHeight, l + width, curHeight + height);
            curHeight += height;
        }
    }

    /**
     * 获取子 View 中宽度最大的值
     *
     * @return 子 View 中宽度最大的值
     */
    private int getMaxChildWidth() {
        int childCount = getChildCount();
        int maxWidth = 0;
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getMeasuredWidth() > maxWidth) {
                maxWidth = childView.getMeasuredWidth();
            }
        }
        return maxWidth;
    }

    /**
     * 将所有子 View 的高度相加
     *
     * @return 所有子 View 的高度的总和
     */
    private int getTotalHeight() {
        int childCount = getChildCount();
        int height = 0;
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            height += childView.getMeasuredHeight();
        }
        return height;
    }
}
