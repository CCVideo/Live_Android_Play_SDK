package com.bokecc.livemodule.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.util.SparseArray;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bumptech.glide.Glide;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图文混排展示控件(基于TextView和ImageView)
 *
 * @author RenHui
 */
public class MixedTextView extends LinearLayout {

    String IMAGE_SRC_REGEX = "<img[^<>]*?\\ssrc=['\"]?(.*?)['\"].*?>";

    private Context mContext;
    private String mContent;
    private int mColorRes;
    private SparseArray<String> mImageArray;
    private boolean mCenterStyle; // 控制图文混排的显示样式
    private boolean mNoImage; // 记录内容里面没有图片

    public MixedTextView(Context context) {
        super(context);
    }

    /**
     * 构造函数(非居中，用于便签集展示)
     *
     * @param context
     * @param content
     */
    public MixedTextView(Context context, String content) {
        this(context);
        mContext = context;
        mContent = content;
        mImageArray = new SparseArray<String>();
        mCenterStyle = false;
        mColorRes = -1;
        this.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        // params.topMargin = 24;
        params.topMargin = 11;
        params.bottomMargin = 11;
        params.gravity = Gravity.CENTER_VERTICAL;
        this.setLayoutParams(params);
        this.removeAllViews();
        createShowView();
    }

    public void createShowView() {
        Matcher m = Pattern.compile(IMAGE_SRC_REGEX).matcher(mContent);
        while (m.find()) {
            mImageArray.append(mContent.indexOf("<img"), m.group(1));
            mContent = mContent.replaceFirst("<img[^>]*>", "");
        }

        if (mImageArray.size() == 0) {
            mNoImage = true;
            appendTextView(mContent);
        } else {
            mNoImage = false;
            for (int i = 0; i < mImageArray.size(); i++) {
                String s;
                if (i == 0 && (mImageArray.size() - 1 == 0)) {
                    s = mContent.substring(0, mImageArray.keyAt(i));
                    appendTextView(s);
                    appendImageView(mImageArray.valueAt(i));
                    s = mContent.substring(mImageArray.keyAt(i), mContent.length());
                    appendTextView(s);
                } else if (i == 0) {
                    s = mContent.substring(0, mImageArray.keyAt(i));
                    appendTextView(s);
                    appendImageView(mImageArray.valueAt(i));
                } else if (i == mImageArray.size() - 1) {
                    s = mContent.substring(mImageArray.keyAt(i - 1), mImageArray.keyAt(i));
                    appendTextView(s);
                    s = mContent.substring(mImageArray.keyAt(i), mContent.length());
                    appendImageView(mImageArray.valueAt(i));
                    appendTextView(s);
                } else {
                    s = mContent.substring(mImageArray.keyAt(i - 1), mImageArray.keyAt(i));
                    appendTextView(s);
                    appendImageView(mImageArray.valueAt(i));
                }
            }
        }
    }

    // 添加图片
    @SuppressLint("RtlHardcoded")
    private void appendImageView(final String uri) {
        if (uri == null || uri.isEmpty()) {
            return;
        }

        // 去掉URI里面的空格
        final String uriStr = uri.replaceAll(" ", "");

        LinearLayout imageLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams param = new LayoutParams(LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageLayout.setLayoutParams(param);

        final ImageView image = new ImageView(mContext);
        image.setLayoutParams(param);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(mContext).load(uriStr).crossFade().into(image);
        imageLayout.addView(image);
        this.addView(imageLayout);
    }

    // 添加文本内容
    @SuppressLint("RtlHardcoded")
    private void appendTextView(String content) {
        // 判断内容是否为空
        if (content == null || content.isEmpty()) {
            content=getResources().getString(R.string.no_introduction);
        }

        // 如果内容仅仅是回车,不进行显示
        if (content.length() == 1 && content.charAt(0) == '\n') {
            content=getResources().getString(R.string.no_introduction);
        }

        // 逻辑： <br /> 长度为6 加一个回车字符是7
        if (content.startsWith("<br>") || content.startsWith("<br />")) {
            if (content.length() <= 7) {
                content=getResources().getString(R.string.no_introduction);
            }
        }

        TextView textView = new TextView(mContext);
        LinearLayout.LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (mCenterStyle) {
            if (mNoImage) {
                if (content.length() < 25) {
                    textView.setGravity(Gravity.CENTER);
                } else {
                    textView.setGravity(Gravity.CENTER | Gravity.LEFT);
                }
            } else {
                textView.setGravity(Gravity.CENTER | Gravity.LEFT);
            }

            textView.setTextSize(18);
        } else {
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextSize(16);
        }

        if (mColorRes != -1) {
            textView.setTextColor(getContext().getResources().getColor(mColorRes));
        }

        textView.setPadding(0, 3, 0, 3);
        textView.setLayoutParams(params);
        textView.setText(Html.fromHtml(content));
        this.addView(textView);
    }
}