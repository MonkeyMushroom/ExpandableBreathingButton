package com.monkey.expandablebreathingbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 可展开的，会呼吸的按钮
 */
public class ExpandableBreathingButton extends View {

    private String mButtonStr;
    private String[] mItemTextArr = {"文  字", "照  片", "视  频"};
    private float mButtonTextSize;
    private int mButtonTextColor;
    private float mItemTextSize;
    private int mItemTextColor;
    private int mInnerCircleColor;
    private int mBackgroundColor;
    private int mOuterRadius;//外圆半径
    private int mInnerRadius;//内圆半径
    private Paint mInnerCirclePaint;
    private Paint mmBackgroundRectPaint;
    private Paint mButtonTextPaint;
    private Paint mItemTextPaint;
    private Paint mBreathePaint;
    private RectF mBackgroundRectF;
    private int mInnerCircleCenterX;
    private int mInnerCircleCenterY;
    private int mItemWidth;
    private int mBackgroundRectFLeft;
    private int mArcWidth = 12;
    private float mBreatheRadius;
    private int mItemTextAlpha = 255;
    private boolean isOpen;//按钮是否打开
    private boolean isAniming;//动画正在进行时，阻止点击事件，防止动画混乱
    private ValueAnimator mBreatheAnim;
    private OnButtonItemClickListener mOnButtonItemClickListener;

    public ExpandableBreathingButton(Context context) {
        this(context, null);
    }

    public ExpandableBreathingButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableBreathingButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ExpandableBreathingButton, defStyleAttr, 0);
        mButtonStr = ta.getString(R.styleable.ExpandableBreathingButton_ebbButtonText);
        mButtonTextSize = ta.getDimension(R.styleable.ExpandableBreathingButton_ebbButtonTextSize,
                getResources().getDimensionPixelSize(R.dimen.medium_text_size));
        mButtonTextColor = ta.getColor(R.styleable.ExpandableBreathingButton_ebbButtonTextColor, Color.WHITE);
        mItemTextSize = ta.getDimension(R.styleable.ExpandableBreathingButton_ebbItemTextSize,
                getResources().getDimensionPixelSize(R.dimen.large_text_size));
        mItemTextColor = ta.getColor(R.styleable.ExpandableBreathingButton_ebbItemTextColor, Color.WHITE);
        mInnerCircleColor = ta.getColor(R.styleable.ExpandableBreathingButton_ebbInnerCircleColor, Color.parseColor("#0877f4"));
        mBackgroundColor = ta.getColor(R.styleable.ExpandableBreathingButton_ebbRectBackgroundColor, Color.parseColor("#5db8ff"));
        ta.recycle();
        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setStyle(Paint.Style.FILL);
        mInnerCirclePaint.setColor(mInnerCircleColor);
        mmBackgroundRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mmBackgroundRectPaint.setStyle(Paint.Style.FILL);
        mmBackgroundRectPaint.setColor(mBackgroundColor);
        mButtonTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButtonTextPaint.setColor(mButtonTextColor);
        mButtonTextPaint.setTextSize(mButtonTextSize);
        mItemTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mItemTextPaint.setColor(mItemTextColor);
        mItemTextPaint.setTextSize(mItemTextSize);
        mBreathePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBreathePaint.setStyle(Paint.Style.STROKE);
        mBreathePaint.setColor(mBackgroundColor);
        mBreathePaint.setStrokeWidth(mArcWidth / 2);
        mBackgroundRectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int width = 0;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            width = size;
        } else {
            if (mode == MeasureSpec.AT_MOST) {
                int maxWidth = 912;
                width = Math.min(maxWidth, size);
            }
        }
        return width;
    }

    private int measureHeight(int heightMeasureSpec) {
        int height = 0;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            height = size;
        } else {
            if (mode == MeasureSpec.AT_MOST) {
                int maxHeight = 140;
                height = Math.min(maxHeight, size);
            }
        }
        return height;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mOuterRadius = getHeight() / 2 - mArcWidth / 2;
        mInnerRadius = mOuterRadius - mArcWidth / 2;
        mBackgroundRectFLeft = getWidth() - mOuterRadius * 2 - mArcWidth / 2;
        mInnerCircleCenterX = getWidth() - mOuterRadius - mArcWidth / 2;
        mInnerCircleCenterY = getHeight() / 2;
        mItemWidth = (getWidth() - 2 * mOuterRadius - mArcWidth - mArcWidth / 2) / mItemTextArr.length;

        mBreatheRadius = getHeight() / 2 - mArcWidth / 4;
        mBreatheAnim = ValueAnimator.ofFloat(mBreatheRadius, mBreatheRadius - mArcWidth / 2);
        mBreatheAnim.setDuration(1000);
        mBreatheAnim.setRepeatMode(ValueAnimator.REVERSE);
        mBreatheAnim.setRepeatCount(Integer.MAX_VALUE);
        mBreatheAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBreatheRadius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mBreatheAnim.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mBackgroundRectF.set(mBackgroundRectFLeft, mArcWidth / 2, getWidth() - mArcWidth / 2, getHeight() - mArcWidth / 2);
        canvas.drawRoundRect(mBackgroundRectF, mOuterRadius, mOuterRadius, mmBackgroundRectPaint);//圆角背景矩形
        canvas.drawCircle(mInnerCircleCenterX, mInnerCircleCenterY, mInnerRadius, mInnerCirclePaint);//内圆
        canvas.drawCircle(mInnerCircleCenterX, mInnerCircleCenterY, mBreatheRadius, mBreathePaint);//呼吸圈
        float buttonTextWidth = mButtonTextPaint.measureText(mButtonStr, 0, mButtonStr.length());
        Paint.FontMetrics publishFontMetrics = mButtonTextPaint.getFontMetrics();
        canvas.drawText(mButtonStr, 0, mButtonStr.length(), getWidth() - mOuterRadius - mArcWidth / 2 - buttonTextWidth / 2,
                mOuterRadius + mArcWidth / 2 + -(publishFontMetrics.ascent + publishFontMetrics.descent) / 2, mButtonTextPaint);
        if (mBackgroundRectFLeft == mArcWidth / 2) {
            mItemTextPaint.setAlpha(mItemTextAlpha);
            for (int i = 0; i < mItemTextArr.length; i++) {
                float itemTextWidth = mButtonTextPaint.measureText(mItemTextArr[i], 0, mItemTextArr[i].length());
                Paint.FontMetrics itemFontMetrics = mButtonTextPaint.getFontMetrics();
                canvas.drawText(mItemTextArr[i], 0, mItemTextArr[i].length(),
                        mItemWidth * i + mItemWidth / 2 - itemTextWidth / 2, mOuterRadius + mArcWidth / 2 -
                                (itemFontMetrics.ascent + itemFontMetrics.descent) / 2, mItemTextPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isAniming) {//在动画的时候什么都不做
            return true;
        }
        int x;
        int y;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = (int) event.getX();
                y = (int) event.getY();
                if (!isOpen && x < getWidth() - 2 * mOuterRadius && y > 0 && y < getHeight()) {
                    //未展开状态下，点击发布圆左侧的位置，不处理事件
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                x = (int) event.getX();
                y = (int) event.getY();
                int d = (int) Math.sqrt(Math.pow(x - mInnerCircleCenterX, 2) + Math.pow(y - mInnerCircleCenterY, 2));
                if (d < mInnerRadius) {//点击发布按钮
                    if (isOpen) {
                        closeButton();
                    } else {
                        openButton();
                    }
                } else {
                    if (isOpen && y > 0 && y < getHeight()) {
                        for (int i = 0; i < mItemTextArr.length; i++) {//计算点击了哪个item
                            if (x < mItemWidth * (i + 1)) {
                                if (mOnButtonItemClickListener != null) {
                                    mOnButtonItemClickListener.onButtonItemClick(i);
                                }
                                break;
                            }
                        }
                        closeButton();
                    }
                }
                break;
        }
        return true;
    }

    private void openButton() {
        isAniming = true;
        isOpen = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mBreatheAnim.pause();
        } else {
            mBreatheAnim.cancel();
        }
        AnimatorSet openAnimSet = new AnimatorSet();
        ValueAnimator rectLeftAnim = ValueAnimator.ofInt(mBackgroundRectFLeft, mArcWidth / 2);
        rectLeftAnim.setDuration(250);
        ValueAnimator textAlphaAnim = ValueAnimator.ofInt(0, mItemTextAlpha);
        textAlphaAnim.setDuration(120);
        rectLeftAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBackgroundRectFLeft = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        textAlphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mItemTextAlpha = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        openAnimSet.playSequentially(rectLeftAnim, textAlphaAnim);
        openAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAniming = false;
            }
        });
        openAnimSet.start();
    }

    private void closeButton() {
        isAniming = true;
        isOpen = false;
        mItemTextAlpha = 255;
        ValueAnimator closeAnimSet = ValueAnimator.ofInt(mBackgroundRectFLeft, getWidth() - mOuterRadius * 2 - mArcWidth / 2);
        closeAnimSet.setDuration(250);
        closeAnimSet.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBackgroundRectFLeft = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        closeAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAniming = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mBreatheAnim.resume();
                } else {
                    mBreatheAnim.start();
                }
            }
        });
        closeAnimSet.start();
    }

    public void setOnButtonItemClickListener(OnButtonItemClickListener onButtonItemClickListener) {
        mOnButtonItemClickListener = onButtonItemClickListener;
    }

    public interface OnButtonItemClickListener {
        void onButtonItemClick(int position);
    }
}
