package com.bokecc.livemodule.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bokecc.livemodule.R;
import com.bokecc.sdk.mobile.live.pojo.UserRedminAction;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2016/08/11
 * Create Time: 上午10:00
 */
public class AutoScrollTextView extends TextSwitcher implements
        ViewSwitcher.ViewFactory {

    private static final int FLAG_START_AUTO_SCROLL = 1001;
    private static final int FLAG_END_AUTO_SCROLL = 1002;
    private static final String REDMIN_LEFT = "【";
    private static final String REDMIN_RIGHT = "】";
    private static final String REDMIN_MORE = "...";
    /**
     * 轮播时间间隔
     */
    private int scrollDuration = 500;
    /**
     * 动画时间
     */
    private int animDuration = 1500;

    /**
     * 文字大小
     */
    private float mTextSize = 24;
    /**
     * 文字Padding
     */
    private int mPadding = 20;
    /**
     * 文字颜色
     */
    private int textColor = Color.BLACK;
    private Context mContext;
    private LinkedList<UserRedminAction> textList;
    private AutoScrollView.AutoScrollCallBack autoScrollCallBack;

    public AutoScrollTextView(Context context) {
        this(context, null);
        mContext = context;
    }

    public AutoScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoScrollHeight);
        mTextSize = a.getDimension(R.styleable.AutoScrollHeight_textSize, 24);
        mPadding = (int) a.getDimension(R.styleable.AutoScrollHeight_padding, 20);
        scrollDuration = a.getInteger(R.styleable.AutoScrollHeight_scrollDuration, 500);
        animDuration = a.getInteger(R.styleable.AutoScrollHeight_animDuration, 1500);
        textColor = a.getColor(R.styleable.AutoScrollHeight_textColor, Color.BLACK);
        a.recycle();
        init(dp2px(mPadding*2)+sp2px(mTextSize));
    }
    /**
     * dp转px
     *
     * @param dpVal
     * @return
     */
    public int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, mContext.getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @param spVal
     * @return
     */
    public  int sp2px(float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, mContext.getResources().getDisplayMetrics());
    }
    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLAG_START_AUTO_SCROLL://开始循环
                    if (textList.size() > 0) {
                        UserRedminAction userRedminAction = textList.pollFirst();
                        StringBuffer stringBuffer = new StringBuffer();
                        if (!TextUtils.isEmpty(userRedminAction.getPrefixContent())){
                            stringBuffer.append(userRedminAction.getPrefixContent());
                        }
                        stringBuffer.append(REDMIN_LEFT);
                        String name = null;
                        if (userRedminAction.getUserName().length()>4){
                            String substring = userRedminAction.getUserName().substring(0, 4);
                            name = substring+REDMIN_MORE;
                        }else{
                            name = userRedminAction.getUserName();
                        }
                        stringBuffer.append(name);
                        stringBuffer.append(REDMIN_RIGHT);
                        if (!TextUtils.isEmpty(userRedminAction.getSuffixContent())){
                            stringBuffer.append(userRedminAction.getSuffixContent());
                        }
                        setText(stringBuffer.toString());
                        handler.sendEmptyMessageDelayed(FLAG_START_AUTO_SCROLL, animDuration+scrollDuration);
                    }else{
                        handler.sendEmptyMessageDelayed(FLAG_END_AUTO_SCROLL,animDuration);
                    }
                    break;
                case  FLAG_END_AUTO_SCROLL:
                    setText("");
                    if (autoScrollCallBack!=null){
                        autoScrollCallBack.onEnd();
                    }
                    break;
            }
        }
    };
    private void init(float fontHeight) {
        textList = new LinkedList<>();
        setFactory(this);
        Animation in = new TranslateAnimation(0, 0, fontHeight, 0);
        in.setDuration(scrollDuration);
        in.setInterpolator(new AccelerateInterpolator());
        Animation out = new TranslateAnimation(0, 0, 0, -fontHeight);
        out.setDuration(scrollDuration);
        out.setInterpolator(new AccelerateInterpolator());
        setInAnimation(in);
        setOutAnimation(out);
    }

    /**
     * 添加要展示的数据
     */
    public void addDate(UserRedminAction userJoinExitAction) {
        boolean flag = false;
        //判断是否有该数据  如果有 将原先的数据进行替换
        if (textList!=null){
            Iterator<UserRedminAction> iterator = textList.iterator();
            while (iterator.hasNext()){
                UserRedminAction next = iterator.next();
                if (next.getUserId().equals(userJoinExitAction.getUserId())){
                    next.setType(userJoinExitAction.getType());
                    next.setPrefixContent(userJoinExitAction.getPrefixContent());
                    next.setSuffixContent(userJoinExitAction.getSuffixContent());
                    flag = true;
                    break;
                }
            }
        }
        if (!flag){
            handler.removeMessages(FLAG_END_AUTO_SCROLL);
            if (textList == null){
                textList = new LinkedList<UserRedminAction>();
            }
            if (textList.size()>=10){
                textList.removeFirst();
            }
            textList.addLast(userJoinExitAction);
            if (getVisibility() == VISIBLE&&handler.hasMessages(FLAG_START_AUTO_SCROLL)){
                //如果是显示 证明是正在运行 不需要处理
            }else{
                //如果不是显示状态  那么就需要手动去运行了
                if (autoScrollCallBack!=null){
                    autoScrollCallBack.onStart();
                }
                handler.sendEmptyMessage(FLAG_START_AUTO_SCROLL);
            }
        }
    }

    @Override
    public View makeView() {
        TextView t = new TextView(mContext);
        t.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        t.setMaxLines(1);
        t.setPadding(mPadding, mPadding, mPadding, mPadding);
        t.setTextColor(textColor);
        t.setTextSize(mTextSize);
        return t;
    }

    public void setListener(AutoScrollView.AutoScrollCallBack autoScrollCallBack) {
        this.autoScrollCallBack=autoScrollCallBack;
    }
}