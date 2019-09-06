package com.bokecc.livemodule.live.doc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.widget.DocView;

/**
 * 直播间文档展示控件
 */
public class LiveDocComponent extends LinearLayout implements LiveDocSizeChangeListener {

    private final int SCALE_CENTER_INSIDE = 0;
    private final int SCALE_FIT_XY = 1;
    private final int SCALE_CROP_CENTER = 2;
    private int mCurrentScaleType = SCALE_CENTER_INSIDE;

    private Context mContext;
    private DocView mDocView;


    public LiveDocComponent(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public LiveDocComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        mDocView = new DocView(mContext);
        // 设置true：响应文档内容上下滑动，不支持悬浮窗拖动  设置false：支持悬浮窗拖动，不响应文档内容上下滑动
        mDocView.setScrollable(false);
        mDocView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mDocView);
        DWLiveCoreHandler liveCoreHandler = DWLiveCoreHandler.getInstance();
        if (liveCoreHandler != null) {
            liveCoreHandler.setDocView(mDocView);
            liveCoreHandler.setDocSizeChangeListener(this);
        }
    }

    // 设置文档区域是否可滑动
    public void setDocScrollable(boolean scrollable) {
        if (mDocView != null) {
            mDocView.setScrollable(scrollable);
        }
    }

    public void setScaleType(int type) {
        mCurrentScaleType = type;
        if (SCALE_CENTER_INSIDE == mCurrentScaleType) {
            DWLive.getInstance().setDocScaleType(DocView.ScaleType.CENTER_INSIDE);
            //innerUpdateSize(mSrcWidth, mSrcHeight);

        } else if (SCALE_FIT_XY == mCurrentScaleType) {
            DWLive.getInstance().setDocScaleType(DocView.ScaleType.FIT_XY);
            //innerUpdateSize(mSrcWidth, mSrcHeight);
        } else if (SCALE_CROP_CENTER == mCurrentScaleType) {
            DWLive.getInstance().setDocScaleType(DocView.ScaleType.CROP_CENTER);
            //innerUpdateSize(mSrcWidth, mSrcHeight);
        }

    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
////        if (mSrcWidth != 0 || mSrcHeight != 0) {
////            innerUpdateSize(mSrcWidth, mSrcHeight);
////        }
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        if (SCALE_CROP_CENTER == mCurrentScaleType) {
//            int childCount = getChildCount();
//            if (childCount > 0) {
//                View docView = getChildAt(childCount - 1);
//                //这里不能使用getWidth()因为获取的是没有重新计算的结果
//                ViewGroup.LayoutParams lp = docView.getLayoutParams();
//                int w = lp.width;
//                int h = lp.height;
//                int offsetL = 0;
//                int offsetH = 0;
//                if (w != getWidth()) {
//                    offsetL = (w - getWidth()) / 2;
//                } else if (h != getHeight()) {
//                    offsetH = (h - getHeight()) / 2;
//                }
//                docView.layout(l - offsetL, t - offsetH, l - offsetL + w, t - offsetH + h);
//            }
//        }
        super.onLayout(changed, l, t, r, b);
    }


    /**
     * @param srcWidth  图片的原始宽度
     * @param srcHeight 图片的原始高度
     */
    public void innerUpdateSize(final int srcWidth, final int srcHeight) {
//        mSrcWidth = srcWidth;
//        mSrcHeight = srcHeight;
//        if (mDocView == null) {
//            return;
//        }
//        if (SCALE_CROP_CENTER == mCurrentScaleType) {
//            if (mSrcWidth == 0 && mSrcHeight == 0) {
//                return;
//            }
//            int disWidth = getWidth();
//            int disHeight = getHeight();
//            //宽度缩放的比率 --->1.5
//            float scaleW = disWidth * 1.0f / mSrcWidth;
//            float scaleH = disHeight * 1.0f / mSrcHeight;
//            float animWith = 0;
//            float animHeight = 0;
//            if (scaleW < scaleH) {
//                animWith = scaleH * mSrcWidth * 1.0f;
//                animHeight = disHeight;
//            } else {
//                animHeight = scaleW * mSrcHeight * 1.0f;
//                animWith = disWidth;
//            }
//            ViewGroup.LayoutParams docLp = mDocView.getLayoutParams();
//            if (docLp == null) return;
//            docLp.width = (int) animWith;
//            docLp.height = (int) animHeight;
//
//            mDocView.setLayoutParams(docLp);
//            requestLayout();
//        } else {
//            mDocView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            requestLayout();
//        }
    }


    public void updateSize(int srcWidth, int srcHeight) {
//        post(new Runnable() {
//            @Override
//            public void run() {
//                innerUpdateSize(srcWidth, srcHeight);
//            }
//        });

    }

}

