package com.bokecc.livemodule.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.DensityUtil;

/**
 * 头像控件
 */
public class HeadView extends android.support.v7.widget.AppCompatImageView {

    public static final int DEFAULT_COLOR = Color.TRANSPARENT;
    public static final int DEFAULT_WIDTH = 0;

    private int mSize, mRadius, mRadiusP;
    private int mBorderColor, mShadowColor;
    private int mBorderWidth, mShadowWidth;

    private Matrix mMatrix;
    private Paint mBitmapPaint;
    private Paint mFillPaint;
    private Paint mBorderPaint;

    private Paint mCirPaint;
    private boolean isNewFlag = false;
    private int mRadiusCir;

    public HeadView(Context context) {
        this(context, null);
    }

    public HeadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);
        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mCirPaint = new Paint();
        mCirPaint.setAntiAlias(true);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HeadView, defStyleAttr, 0);

        mBorderColor = DEFAULT_COLOR;
        mShadowColor = DEFAULT_COLOR;

        mBorderWidth = DensityUtil.dp2px(context, DEFAULT_WIDTH);
        mShadowWidth = DensityUtil.dp2px(context, DEFAULT_WIDTH);

        if (array != null) {

            mBorderColor = array.getColor(R.styleable.HeadView_hv_border_color, mBorderColor);
            mBorderWidth = array.getDimensionPixelOffset(R.styleable.HeadView_hv_border_width, mBorderWidth);
            mShadowColor = array.getColor(R.styleable.HeadView_hv_shadow_color, mBorderColor);
            mShadowWidth = array.getDimensionPixelSize(R.styleable.HeadView_hv_shadow_width, mShadowWidth);

            array.recycle();
        }

        mRadiusCir = DensityUtil.dp2px(context, 4.5f);
        setScaleType(ScaleType.CENTER_CROP);

    }

    public void updateNew() {
        isNewFlag = true;
        postInvalidate();
    }

    public void clearNew() {
        isNewFlag = false;
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        mRadius = mSize / 2;
        mRadiusP = (mSize + (mBorderWidth + mShadowWidth) * 2) / 2;
        setMeasuredDimension(mSize + (mBorderWidth + mShadowWidth) * 2,
                mSize + (mBorderWidth + mShadowWidth) * 2);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }

        setUpShader();

        if (mShadowWidth > 0 && mShadowColor != Color.TRANSPARENT) {
            setLayerType(LAYER_TYPE_SOFTWARE, mBorderPaint);
            mBorderPaint.setShadowLayer(mShadowWidth, 0.0f, mShadowWidth / 2, mShadowColor);
        }

        // 绘制边框
        if (mBorderWidth > 0 && mBorderColor != Color.TRANSPARENT) {
            mBorderPaint.setColor(mBorderColor);
            canvas.drawCircle(mRadiusP, mRadiusP, mRadius + mBorderWidth, mBorderPaint);
        }

        // 绘制背景
        mFillPaint.setColor(Color.WHITE);
        canvas.drawCircle(mRadiusP, mRadiusP, mRadius, mFillPaint);

        // 绘制圆形图片
        canvas.drawCircle(mRadiusP, mRadiusP, mRadius, mBitmapPaint);

        if (isNewFlag) {
            mCirPaint.setColor(Color.parseColor("#f9504d"));
            canvas.drawCircle(getWidth() - mShadowWidth - mBorderWidth - mRadiusCir,
                    mRadiusCir + mBorderWidth + mShadowWidth, mRadiusCir, mCirPaint);
        }
    }

    private void setUpShader() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        Bitmap bitmap = drawable2Bitmap(drawable);

        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        float scale;
        int bSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        scale = mSize * 1.0f / bSize;
        // shader的变换矩阵，我们这里主要用于放大或者缩小
        mMatrix.setScale(scale, scale);
        // 移动图片进行居中显示
        mMatrix.postTranslate(mRadiusP - mRadius, mRadiusP - mRadius);
        // 设置变换矩阵
        bitmapShader.setLocalMatrix(mMatrix);
        // 设置shader
        mBitmapPaint.setShader(bitmapShader);

    }

    private Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable)
        {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

}
