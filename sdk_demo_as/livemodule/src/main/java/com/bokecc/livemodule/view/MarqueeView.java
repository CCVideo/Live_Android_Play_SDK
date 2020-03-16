package com.bokecc.livemodule.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.livemodule.bean.MarqueeAction;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MarqueeView extends RelativeLayout {

    private Context mContext;
    private TextView textView;
    private ImageView imageView;
    private List<MarqueeAction> marqueeActions;
    private List<AnimationSet> animationSets;
    private int animationSetOrder = 0;
    private int repeatCount = 0;
    public static final int TEXT = 1;
    public static final int IMAGE = 2;
    private int type = 1;
    private String textContent = "";
    private int textFontSize = 20;
    private String textColor = "#ffffff";
    private Bitmap bitmap;
    private String imageUrl;
    private int imageWidth;
    private int imageHeight;
    private int loop = -1;
    private OnMarqueeImgFailListener onMarqueeImgFailListener;

    public MarqueeView(Context context) {
        this(context, null);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        textView = new TextView(mContext);
        textView.setText(textContent);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textFontSize);
        textView.setTextColor(Color.parseColor(textColor));
        this.addView(textView);

        imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.addView(imageView);

        animationSets = new ArrayList<>();
    }

    /**
     * @param marqueeActions 跑马灯的动作节点
     */
    public void setMarqueeActions(List<MarqueeAction> marqueeActions) {
        this.marqueeActions = marqueeActions;
        if (marqueeActions != null && marqueeActions.size() > 0) {
            MarqueeAction marqueeAction = new MarqueeAction();
            marqueeAction.setIndex(-1);
            marqueeAction.setDuration(1);
            marqueeAction.setStartXpos(0);
            marqueeAction.setStartYpos(0);
            marqueeAction.setStartAlpha(0);
            marqueeAction.setEndXpos(0);
            marqueeAction.setEndYpos(0);
            marqueeAction.setEndAlpha(0);
            marqueeActions.add(0, marqueeAction);
        }
    }

    public void start() {
        if (marqueeActions != null && marqueeActions.size() > 0) {
            for (int i = 0; i < marqueeActions.size(); i++) {
                MarqueeAction MarqueeAction = marqueeActions.get(i);
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.setRepeatCount(Animation.INFINITE);

                TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, MarqueeAction.getStartXpos(), Animation.RELATIVE_TO_PARENT, MarqueeAction.getEndXpos(), Animation.RELATIVE_TO_PARENT, MarqueeAction.getStartYpos(), Animation.RELATIVE_TO_PARENT, MarqueeAction.getEndYpos());
                translateAnimation.setDuration(MarqueeAction.getDuration());
                translateAnimation.setInterpolator(new LinearInterpolator());

                AlphaAnimation alphaAnimation = new AlphaAnimation(MarqueeAction.getStartAlpha(), MarqueeAction.getEndAlpha());
                alphaAnimation.setDuration(MarqueeAction.getDuration());
                alphaAnimation.setInterpolator(new LinearInterpolator());

                animationSet.addAnimation(translateAnimation);
                animationSet.addAnimation(alphaAnimation);
                animationSets.add(animationSet);

                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        animationSetOrder++;
                        if (animationSetOrder < marqueeActions.size()) {

                            if (type == 1) {
                                textView.startAnimation(animationSets.get(animationSetOrder));
                            } else if (type == 2) {
                                imageView.startAnimation(animationSets.get(animationSetOrder));
                            }
                        } else {
                            repeatCount++;
                            animationSetOrder = 0;
                            if (loop == -1 || loop > repeatCount) {
                                if (type == 1) {
                                    textView.startAnimation(animationSets.get(animationSetOrder));
                                } else if (type == 2) {
                                    imageView.startAnimation(animationSets.get(animationSetOrder));
                                }
                            }

                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                if (i == 0 && loop != 0) {
                    if (type == 1) {
                        textView.startAnimation(animationSet);
                    } else if (type == 2) {
                        imageView.startAnimation(animationSet);
                    }
                }

            }
        }

    }


    /**
     * @param type 跑马灯类型 1：文字 2：图片
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @param textContent 跑马灯文字内容
     */
    public void setTextContent(String textContent) {
        this.textContent = textContent;
        textView.setText(textContent);
    }

    /**
     * @param textFontSize 文字字体大小（单位：px）
     */
    public void setTextFontSize(int textFontSize) {
        this.textFontSize = textFontSize;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textFontSize);
    }

    /**
     * @param textColor 文字颜色 如"#ffffff"
     */
    public void setTextColor(String textColor) {
        this.textColor = textColor;
        textView.setTextColor(Color.parseColor("#ff0000"));
    }


    /**
     * @param bitmap      图片bitmap
     * @param imageWidth  图片宽度（单位px）
     * @param imageHeight 图片高度（单位px）
     */
    public void setMarqueeBitmap(Bitmap bitmap, int imageWidth, int imageHeight) {
        this.bitmap = bitmap;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        imageView.setImageBitmap(bitmap);
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = imageWidth;
        layoutParams.height = imageHeight;
        imageView.setLayoutParams(layoutParams);
    }

    /**
     * @param activity    上下文
     * @param imageUrl    图片url
     * @param imageWidth  图片宽度（单位px）
     * @param imageHeight 图片高度（单位px）
     */
    public void setMarqueeImage(final Activity activity, final String imageUrl, final int imageWidth, final int imageHeight) {
        this.imageUrl = imageUrl;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap imageBitmap = getBitmap(imageUrl);
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (imageBitmap != null) {
                                imageView.setImageBitmap(imageBitmap);
                                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                                layoutParams.width = imageWidth;
                                layoutParams.height = imageHeight;
                                imageView.setLayoutParams(layoutParams);
                            } else {
                                if (onMarqueeImgFailListener != null) {
                                    onMarqueeImgFailListener.onLoadMarqueeImgFail();
                                }
                            }

                        }
                    });
                }

            }
        }).start();
    }

    /**
     * @param loop 循环次数 -1表示无限循环
     */
    public void setLoop(int loop) {
        this.loop = loop;
    }

    private Bitmap getBitmap(String url) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imgUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.setConnectTimeout(8000);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void setOnMarqueeImgFailListener(OnMarqueeImgFailListener onMarqueeImgFailListener) {
        this.onMarqueeImgFailListener = onMarqueeImgFailListener;
    }
}
