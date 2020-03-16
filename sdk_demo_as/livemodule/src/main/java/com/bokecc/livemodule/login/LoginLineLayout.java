package com.bokecc.livemodule.login;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bokecc.livemodule.R;

/**
 * 登录输入框控件
 */
public class LoginLineLayout extends LinearLayout implements View.OnClickListener {

    public LoginLineLayout(Context context) {
        super(context);
    }

    public LoginLineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    EditText editText;
    ImageView imageView;

    private void initView(Context context) {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(HORIZONTAL);

        editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        editText.setBackground(null);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.0f);
        editText.setSingleLine();
        editText.setHintTextColor(Color.rgb(187, 187, 187));
        ll.addView(editText);

        imageView = new ImageView(context);
        imageView.setPadding(15, 0, 15, 0);
        imageView.setImageDrawable(getResources().getDrawable(R.mipmap.login_line_close));
        imageView.setTag("delete");
        imageView.setVisibility(View.INVISIBLE);
        ll.addView(imageView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (!TextUtils.isEmpty(editText.getText())) {
                        imageView.setVisibility(View.VISIBLE);
                    }
                } else {
                    imageView.setVisibility(View.INVISIBLE);
                }
            }
        });
        imageView.setOnClickListener(this);

        addView(ll, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    // TextWatcher包装类
    class MyTextWatcher implements TextWatcher {
        TextWatcher mTextWatch;

        MyTextWatcher(TextWatcher textWatcher) {
            this.mTextWatch = textWatcher;
        }


        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mTextWatch.beforeTextChanged(charSequence, i, i1, i2);

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mTextWatch.onTextChanged(charSequence, i, i1, i2);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            mTextWatch.afterTextChanged(editable);

            if (maxEditTextLength > 0) {
                if (editable.toString().length() > maxEditTextLength) {
                    editText.setText(editable.subSequence(0, maxEditTextLength));
                    editText.setSelection(maxEditTextLength);
                }
            }


            if (!TextUtils.isEmpty(editText.getText()) && editText.hasFocus()) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.INVISIBLE);
            }
        }
    }


    // 设置每行显示的最大字数
    public int maxEditTextLength = 0;

    public LoginLineLayout addOnTextChangeListener(TextWatcher textWatcher) {
        editText.addTextChangedListener(new MyTextWatcher(textWatcher));
        return this;
    }


    private <T extends View> T findViewById(int id, View view) {
        return (T) view.findViewById(id);
    }

    /**
     * 获取editttext的内容
     *
     * @return
     */
    public String getText() {
        return editText.getText().toString();
    }

    /**
     * 设置edittext的内容
     *
     * @param text
     */
    public void setText(String text) {
        editText.setText(text);
    }

    /**
     * 设置hint
     *
     * @param hint
     * @return this
     */
    public LoginLineLayout setHint(String hint) {
        editText.setHint(hint);
        return this;
    }

    /**
     * 设置edittext的输入类型
     *
     * @param type
     * @return
     */
    public LoginLineLayout setInputType(int type) {
        editText.setInputType(type);
        return this;
    }

    @Override
    public void onClick(View view) {
        if ("delete".equals(view.getTag())) {
            editText.setText("");
            imageView.setVisibility(View.INVISIBLE);
        }
    }
}
