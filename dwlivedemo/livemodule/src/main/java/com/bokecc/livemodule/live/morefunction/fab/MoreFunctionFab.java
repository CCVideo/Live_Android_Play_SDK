package com.bokecc.livemodule.live.morefunction.fab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bokecc.livemodule.R;

/**
 * 更多功能悬浮按钮
 */
public class MoreFunctionFab extends RelativeLayout implements View.OnClickListener {

    /**
     * 输出log标签
     */
    private static final String TAG = "SuspensionFab";
    /**
     * 默认按钮的tag标识
     */
    private Object defaultTag = 0;
    /**
     * 上下文
     */
    private Context context;

    /**
     * 两个按钮间的间距
     */
    private int fabSpacing;
    /**
     * 按钮当前是否是展开还是关闭状态   true为展开
     */
    private boolean currentState;
    /**
     * 动画的执行时长 ms
     */
    private int animateDuration = 200;
    /**
     * 默认展开在按钮的上方
     */
    private int orientation;
    /**
     * 动画管理者
     */
    private AnimationManager animationManager;
    /**
     * 按钮点击事件
     */
    private OnFabClickListener fabClickListener;

    public MoreFunctionFab(Context context) {
        super(context);
    }

    public MoreFunctionFab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        loadAttrs(context, attrs);
    }

    public MoreFunctionFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttrs(context, attrs);
    }

    /**
     * 加载自定义的属性
     */
    @SuppressLint("CustomViewStyleable")
    private void loadAttrs(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.a_zhon);
        fabSpacing = array.getDimensionPixelSize(R.styleable.a_zhon_fab_spacing, dip2px(10));
        orientation = array.getInt(R.styleable.a_zhon_fab_orientation, ExpandOrientation.FAB_TOP.getValue());
        //TypedArray需要被回收
        array.recycle();
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    private void init(Context context) {
        this.context = context;
        setBackground(null);
        //添加默认显示的一个按钮
        FabAttributes build = new FabAttributes.Builder()
                .setSrc(context.getResources().getDrawable(R.drawable.more_function_add))
                .setFabSize(FloatingActionButton.SIZE_MINI)
                .setPressedTranslationZ(10)
                .setElevation(15)
                .setTag(defaultTag)
                .build();
        FloatingActionButton fab = new FloatingActionButton(context);
        fab.setId(R.id.default_fab_id);
        fab.setOnClickListener(this);
        setAttributes(fab, build.getBuilder());
        addView(fab);
    }


    /**
     * 添加按钮
     *
     * @param attrs 按钮的参数
     *              添加的默认按钮显示在容器的最上方，后面添加的依次叠加在默认按钮的下方
     */
    public void addFab(FabAttributes... attrs) {
        if (attrs != null && attrs.length != 0) {
            for (FabAttributes attr : attrs) {
                FabAttributes.Builder b = attr.getBuilder();
                FloatingActionButton fab = new FloatingActionButton(context);
                fab.setOnClickListener(this);
                setVisible(fab, false);
                setAttributes(fab, b);
                //后面添加的按钮一直放在第一个位置
                addView(fab, 0);
            }
        }
    }

    @Override
    public void addView(View child, int index) {
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (orientation == ExpandOrientation.FAB_TOP.getValue()) {
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        } else if (orientation == ExpandOrientation.FAB_BOTTOM.getValue()) {
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            lp.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.default_fab_id);
        } else if (orientation == ExpandOrientation.FAB_LEFT.getValue()) {
            lp.addRule(RelativeLayout.CENTER_VERTICAL, TRUE);
        } else if (orientation == ExpandOrientation.FAB_RIGHT.getValue()) {
            lp.addRule(RelativeLayout.CENTER_VERTICAL, TRUE);
            lp.addRule(RelativeLayout.ALIGN_RIGHT, R.id.default_fab_id);
        }
        super.addView(child, index, lp);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag().equals(defaultTag)) {
            if (!currentState) {
                //展开
                openAnimate();
            } else {
                //关闭
                closeAnimate();
            }
        } else {
            if (fabClickListener != null) {
                fabClickListener.onFabClick((FloatingActionButton) v, v.getTag());
            }
        }
    }

    /**
     * 按钮的展开
     */
    private void openAnimate() {
        currentState = true;
        setLayoutHeightOrWidth();
        if (animationManager != null) {
            animationManager.defaultFabAnimation(getFabFromTag(defaultTag), ExpandOrientation.getEnum(orientation), currentState);
        }
        //按照添加的顺序一次展开按钮，去除默认的第一个按钮
        int displacement = 0;
        for (int i = getChildCount() - 1; i > 0; i--) {
            FloatingActionButton view = (FloatingActionButton) getChildAt(i - 1);
            //fab位移的距离
            displacement += view.getHeight() + fabSpacing;
            setVisible(view, true);
            if (animationManager != null)
                animationManager.openAnimation(view, ExpandOrientation.getEnum(orientation));
            if (orientation == ExpandOrientation.FAB_TOP.getValue()) {
                //向上展开
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0f, -displacement);
                viewAnimate(view, animator);
            } else if (orientation == ExpandOrientation.FAB_BOTTOM.getValue()) {
                //向下展开
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0f, displacement);
                viewAnimate(view, animator);
            } else if (orientation == ExpandOrientation.FAB_LEFT.getValue()) {
                //向左展开
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0f, -displacement);
                viewAnimate(view, animator);
            } else if (orientation == ExpandOrientation.FAB_RIGHT.getValue()) {
                //向右展开
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0f, displacement);
                viewAnimate(view, animator);
            }
        }
    }

    /**
     * 按钮的关闭
     */
    private void closeAnimate() {
        currentState = false;
        if (animationManager != null) {
            animationManager.defaultFabAnimation(getFabFromTag(defaultTag), ExpandOrientation.getEnum(orientation), currentState);
        }
        //按照添加的顺序一次折叠按钮，去除默认的第一个按钮
        int j = getChildCount() - 1;
        for (int i = 0; i < getChildCount() - 1; i++) {
            j--;
            FloatingActionButton view = (FloatingActionButton) getChildAt(i);
            //位移的高度要加上按钮之间的间距
            int displacement = view.getHeight() * j + fabSpacing * j;
            if (animationManager != null)
                animationManager.closeAnimation(view, ExpandOrientation.getEnum(orientation));
            if (orientation == ExpandOrientation.FAB_TOP.getValue()) {
                //向上折叠
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -displacement, 0f);
                viewAnimate(view, animator);
            } else if (orientation == ExpandOrientation.FAB_BOTTOM.getValue()) {
                //向下折叠
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", displacement, 0f);
                viewAnimate(view, animator);
            } else if (orientation == ExpandOrientation.FAB_LEFT.getValue()) {
                //向左折叠
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", -displacement, 0f);
                viewAnimate(view, animator);
            } else if (orientation == ExpandOrientation.FAB_RIGHT.getValue()) {
                //向右折叠
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", displacement, 0f);
                viewAnimate(view, animator);
            }
        }
    }

    /**
     * view的位移动画
     */
    @SuppressLint("ObjectAnimatorBinding")
    private void viewAnimate(final Object target, ObjectAnimator animator) {
        animator.setDuration(animateDuration);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //当为折叠状态的时候
                if (!currentState) {
                    setVisible(((View) target), false);
                }
            }
        });
    }

    /**
     * 设置展开的viewGroup大小
     */
    private void setLayoutHeightOrWidth() {
        View view = getFabFromTag(defaultTag);
        if (currentState) {
            int height = view.getHeight() * getChildCount() + fabSpacing * getChildCount();
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (orientation == ExpandOrientation.FAB_TOP.getValue() ||
                    orientation == ExpandOrientation.FAB_BOTTOM.getValue()) {
                layoutParams.height = height;
            } else if (orientation == ExpandOrientation.FAB_LEFT.getValue() ||
                    orientation == ExpandOrientation.FAB_RIGHT.getValue()) {
                layoutParams.width = height;
            }
            setLayoutParams(layoutParams);
        }
        if (orientation == ExpandOrientation.FAB_TOP.getValue()) {
            setGravity(Gravity.BOTTOM);
        } else if (orientation == ExpandOrientation.FAB_LEFT.getValue()) {
            setGravity(Gravity.RIGHT);
        }
    }

    /**
     * 设置默认按钮的tag
     *
     * @param defaultTag 标记
     */
    public void setDefaultTag(Object defaultTag) {
        try {
            FloatingActionButton fab = getFabFromTag(this.defaultTag);
            this.defaultTag = defaultTag;
            fab.setTag(defaultTag);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "请检查是否设置过这个tag-->" + defaultTag);
        }
    }

    /**
     * 设置默认的一个按钮的属性
     *
     * @param attributes 属性
     */
    public void setDefaultFab(FabAttributes attributes) {
        try {
            FloatingActionButton fab = getFabFromTag(defaultTag);
            setAttributes(fab, attributes.getBuilder());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "请检查是否设置过这个tag-->" + defaultTag);
        }
    }

    /**
     * 设置按钮属性
     * @param fab 按鈕
     * @param b 按鈕的参数
     */
    private void setAttributes(FloatingActionButton fab, FabAttributes.Builder b) {
        fab.setTag(b.tag);
        fab.setSize(b.fabSize);
        fab.setImageDrawable(b.src);
        fab.setRippleColor(b.rippleColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(b.backgroundTint));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setElevation(dip2px(b.elevation));
            fab.setTranslationZ(dip2px(b.pressedTranslationZ));
        }
    }

    /**
     * 设置view的显示隐藏
     *
     * @param view 按钮
     * @param isVisible 是否显示
     */
    private void setVisible(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 设置展开的方向
     *
     * @param orientation 方向
     * @see ExpandOrientation
     */
    public void setOrientation(ExpandOrientation orientation) {
        this.orientation = orientation.getValue();
    }

    /**
     * 设置按钮的间距
     *
     * @param fabSpacing 大小
     */
    public void setFabSpacing(int fabSpacing) {
        this.fabSpacing = dip2px(fabSpacing);
    }

    /**
     * 获取默认的动画时长
     *
     * @return 动画时长
     */
    public int getAnimateDuration() {
        return animateDuration;
    }

    /**
     * 设置默认的动画时长
     *
     * @param animateDuration ms
     */
    public void setAnimateDuration(int animateDuration) {
        this.animateDuration = animateDuration;
    }

    /**
     * 设置按钮的动画 必须继承自
     *
     * @param animationManager 管理者
     * @see AnimationManager
     */
    public void setAnimationManager(AnimationManager animationManager) {
        this.animationManager = animationManager;
    }

    /**
     * 设置按钮点击事件
     *
     * @param fabClickListener 监听
     */
    public void setFabClickListener(OnFabClickListener fabClickListener) {
        this.fabClickListener = fabClickListener;
    }

    /***
     * 获取指定位置的fab
     *
     * @param tag 每个按钮的标记
     * @return FloatingActionButton
     */
    public FloatingActionButton getFabFromTag(Object tag) {
        return (FloatingActionButton) findViewWithTag(tag);
    }

    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    private int dip2px(float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
