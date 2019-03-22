package com.bokecc.livemodule.live.function.practice.view;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bokecc.sdk.mobile.live.pojo.PracticeInfo;
import com.bokecc.sdk.mobile.live.util.NetworkUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 随堂测答题弹出界面
 */
public class PracticePopup extends BasePopupWindow {

    public PracticePopup(Context context) {
        super(context);
    }

    private ImageView qsClose;

    //-----------------答题界面-----------------------------------
    private LinearLayout selectLayout;

    private TextView chooseTypeDesc;

    private TextView timerText;

    private TextView networkError;

    private RelativeLayout rl0;
    private RelativeLayout rl1;
    private RelativeLayout rl2;
    private RelativeLayout rl3;
    private RelativeLayout rl4;
    private RelativeLayout rl5;

    private RelativeLayout mrl0;
    private RelativeLayout mrl1;
    private RelativeLayout mrl2;
    private RelativeLayout mrl3;
    private RelativeLayout mrl4;
    private RelativeLayout mrl5;

    private RadioGroup radioGroup;
    private RadioButton radio0;
    private RadioButton radio1;
    private RadioButton radio2;
    private RadioButton radio3;
    private RadioButton radio4;
    private RadioButton radio5;

    private LinearLayout checkboxGroup;
    private CheckBox checkBox0;
    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private CheckBox checkBox4;
    private CheckBox checkBox5;

    private ImageView multiIv0;
    private ImageView multiIv1;
    private ImageView multiIv2;
    private ImageView multiIv3;
    private ImageView multiIv4;
    private ImageView multiIv5;

    private ImageView cbIv0;
    private ImageView cbIv1;
    private ImageView cbIv2;
    private ImageView cbIv3;
    private ImageView cbIv4;
    private ImageView cbIv5;

    private Button submit;

    private int selectOption = -1;  // 单选结果
    private ArrayList<String> selectOptions = new ArrayList<>(); // 多选结果

    // 单选
    private ArrayList<RadioButton> rbs;  // 单选框集合
    private ArrayList<ImageView> ivs;
    private ArrayList<RelativeLayout> rls;

    // 多选
    private ArrayList<CheckBox> cbs; // 多选框集合
    private ArrayList<ImageView> mivs;
    private ArrayList<RelativeLayout> mrls;

    PracticeInfo practiceInfo;

    @Override
    protected void onViewCreated() {
        qsClose = findViewById(R.id.qs_close);
        qsClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        // 答题界面
        selectLayout = findViewById(R.id.qs_select_layout);

        chooseTypeDesc = findViewById(R.id.choose_type_desc);

        timerText = findViewById(R.id.timer);

        networkError = findViewById(R.id.network_error);

        radioGroup = findViewById(R.id.rg_qs_multi);
        radio0 = findViewById(R.id.rb_multi_0);
        radio1 = findViewById(R.id.rb_multi_1);
        radio2 = findViewById(R.id.rb_multi_2);
        radio3 = findViewById(R.id.rb_multi_3);
        radio4 = findViewById(R.id.rb_multi_4);
        radio5 = findViewById(R.id.rb_multi_5);

        checkboxGroup = findViewById(R.id.ll_qs_checkboxs);
        checkBox0 = findViewById(R.id.cb_multi_0);
        checkBox1 = findViewById(R.id.cb_multi_1);
        checkBox2 = findViewById(R.id.cb_multi_2);
        checkBox3 = findViewById(R.id.cb_multi_3);
        checkBox4 = findViewById(R.id.cb_multi_4);
        checkBox5 = findViewById(R.id.cb_multi_5);

        rls = new ArrayList<>();
        rl0 = findViewById(R.id.rl_qs_single_select_0);
        rl1 = findViewById(R.id.rl_qs_single_select_1);
        rl2 = findViewById(R.id.rl_qs_single_select_2);
        rl3 = findViewById(R.id.rl_qs_single_select_3);
        rl4 = findViewById(R.id.rl_qs_single_select_4);
        rl5 = findViewById(R.id.rl_qs_single_select_5);


        mrls = new ArrayList<>();
        mrl0 = findViewById(R.id.rl_qs_mulit_select_0);
        mrl1 = findViewById(R.id.rl_qs_mulit_select_1);
        mrl2 = findViewById(R.id.rl_qs_mulit_select_2);
        mrl3 = findViewById(R.id.rl_qs_mulit_select_3);
        mrl4 = findViewById(R.id.rl_qs_mulit_select_4);
        mrl5 = findViewById(R.id.rl_qs_mulit_select_5);

        rls.add(rl0);
        rls.add(rl1);
        rls.add(rl2);
        rls.add(rl3);
        rls.add(rl4);
        rls.add(rl5);

        mrls.add(mrl0);
        mrls.add(mrl1);
        mrls.add(mrl2);
        mrls.add(mrl3);
        mrls.add(mrl4);
        mrls.add(mrl5);

        rbs = new ArrayList<>();
        rbs.add(radio0);
        rbs.add(radio1);
        rbs.add(radio2);
        rbs.add(radio3);
        rbs.add(radio4);
        rbs.add(radio5);

        cbs = new ArrayList<>();
        cbs.add(checkBox0);
        cbs.add(checkBox1);
        cbs.add(checkBox2);
        cbs.add(checkBox3);
        cbs.add(checkBox4);
        cbs.add(checkBox5);

        radio0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelect(0);
            }
        });

        radio1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelect(1);
            }
        });

        radio2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelect(2);
            }
        });

        radio3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelect(3);
            }
        });

        radio4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelect(4);
            }
        });

        radio5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelect(5);
            }
        });

        checkBox0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheck(0, isChecked);
            }
        });

        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheck(1, isChecked);
            }
        });

        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheck(2, isChecked);
            }
        });

        checkBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheck(3, isChecked);
            }
        });

        checkBox4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheck(4, isChecked);
            }
        });

        checkBox5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheck(5, isChecked);
            }
        });

        multiIv0 = findViewById(R.id.iv_qs_single_select_sign_0);
        multiIv1 = findViewById(R.id.iv_qs_single_select_sign_1);
        multiIv2 = findViewById(R.id.iv_qs_single_select_sign_2);
        multiIv3 = findViewById(R.id.iv_qs_single_select_sign_3);
        multiIv4 = findViewById(R.id.iv_qs_single_select_sign_4);
        multiIv5 = findViewById(R.id.iv_qs_single_select_sign_5);

        cbIv0 = findViewById(R.id.iv_qs_multi_select_sign_0);
        cbIv1 = findViewById(R.id.iv_qs_multi_select_sign_1);
        cbIv2 = findViewById(R.id.iv_qs_multi_select_sign_2);
        cbIv3 = findViewById(R.id.iv_qs_multi_select_sign_3);
        cbIv4 = findViewById(R.id.iv_qs_multi_select_sign_4);
        cbIv5 = findViewById(R.id.iv_qs_multi_select_sign_5);

        ivs = new ArrayList<>();
        ivs.add(multiIv0);
        ivs.add(multiIv1);
        ivs.add(multiIv2);
        ivs.add(multiIv3);
        ivs.add(multiIv4);
        ivs.add(multiIv5);

        mivs = new ArrayList<>();
        mivs.add(cbIv0);
        mivs.add(cbIv1);
        mivs.add(cbIv2);
        mivs.add(cbIv3);
        mivs.add(cbIv4);
        mivs.add(cbIv5);

        submit = findViewById(R.id.btn_qs_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkUtils.isNetworkAvailable(mContext)) {
                    networkError.setVisibility(View.VISIBLE);
                    return;
                } else {
                    networkError.setVisibility(View.GONE);
                }
                submit.setEnabled(false);
                dismiss();
                if (voteType == 0 || voteType == 1) {
                    // 提交结果
                    ArrayList<Integer> indexs = new ArrayList<>();
                    ArrayList<String> ids = new ArrayList<>();

                    for (int i = 0; i < practiceInfo.getOptions().size(); i++) {
                        if (selectOption == i) {
                            ids.add(practiceInfo.getOptions().get(i).getId());
                        }
                    }

                    indexs.add(selectOption);

                    DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
                    if (dwLiveCoreHandler != null) {
                        dwLiveCoreHandler.cachePracticeResult(practiceInfo.getId(), indexs);
                        dwLiveCoreHandler.sendPracticeAnswer(practiceInfo.getId(), ids);
                    }
                } else if (voteType == 2) {
                    // 判断是否作答
                    if (selectOptions.size() < 1) {
                        Toast.makeText(mContext, "请先选择答案", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ArrayList<String> ids = new ArrayList<>();
                    ArrayList<Integer> indexs = new ArrayList<>();
                    for (int i = 0; i < practiceInfo.getOptions().size(); i++) {
                        if (selectOptions.contains(String.valueOf(i))) {
                            ids.add(practiceInfo.getOptions().get(i).getId());
                            indexs.add(i);
                        }
                    }
                    DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
                    if (dwLiveCoreHandler != null) {
                        dwLiveCoreHandler.cachePracticeResult(practiceInfo.getId(), indexs);
                        dwLiveCoreHandler.sendPracticeAnswer(practiceInfo.getId(), ids);
                    }
                }
            }
        });
    }

    /**
     * 多选 -- 选中某个选项
     */
    private void setCheck(int index, boolean isChecked) {
        if (isChecked) {
            if (!selectOptions.contains(String.valueOf(index))) {
                selectOptions.add(String.valueOf(index));
            }
            mivs.get(index).setVisibility(View.VISIBLE);
        } else {
            if (selectOptions.contains(String.valueOf(index))) {
                selectOptions.remove(String.valueOf(index));
            }
            mivs.get(index).setVisibility(View.GONE);
        }

        if (selectOptions.size() > 0) {
            submit.setEnabled(true);
        } else {
            submit.setEnabled(false);
        }
    }

    /***
     * 单选 --- 选中某个选项
     * @param index 选项编号
     */
    private void setSelect(int index) {
        // 清除之前的选择
        initRadioButtonAndImageView();
        // 设定当前选择的选项
        selectOption = index;
        rbs.get(index).setChecked(true);
        ivs.get(index).setVisibility(View.VISIBLE);
        submit.setEnabled(true);
    }

    /**
     * 单选时 --- 初始化单选按钮
     */
    private void initRadioButtonAndImageView() {
        for (RadioButton rb : rbs) {
            rb.setChecked(false);
        }

        for (ImageView view : ivs) {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * 多选时 --- 初始化按钮
     */
    private void initCheckBoxButtonAndImageView() {
        for (CheckBox cb : cbs) {
            cb.setChecked(false);
        }

        for (ImageView view : mivs) {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.practice_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }


    int voteCount;
    int voteType; // 0为判断，1为单选，2为多选

    public void startPractice(final PracticeInfo practiceInfo) {
        this.practiceInfo = practiceInfo;
        this.voteCount = practiceInfo.getOptions().size();
        this.voteType = practiceInfo.getType();
        this.submit.setEnabled(false);
        this.networkError.setVisibility(View.GONE);
        showSelectLayout();

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    final long now = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    final Date date = sdf.parse(practiceInfo.getPublishTime());
                    if (timerText != null) {
                        timerText.post(new Runnable() {
                            @Override
                            public void run() {
                                timerText.setText(TimeUtil.getFormatTime(now - date.getTime()));
                            }
                        });
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        timer.schedule(timerTask, 0, 1000);

        this.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if(timerTask != null) {
                    timerTask.cancel();
                }
                if (timer != null) {
                    timer.cancel();
                }
            }
        });
    }

    Timer timer ;
    TimerTask timerTask;

    private void showSelectLayout() {

        selectLayout.setVisibility(View.VISIBLE);

        if (voteType == 0 || voteType == 1) {
            chooseTypeDesc.setText("单选题");
            // 单选
            selectOption = -1;
            initRadioButtonAndImageView();
            radioGroup.setVisibility(View.VISIBLE);
            checkboxGroup.setVisibility(View.GONE);
            for (int i = 0; i < rls.size(); i++) {
                RelativeLayout rl = rls.get(i);
                if (i < voteCount) {
                    rl.setVisibility(View.VISIBLE);
                } else {
                    rl.setVisibility(View.GONE);
                }
            }
        } else if (voteType == 2) {
            chooseTypeDesc.setText("多选题");
            // 多选
            selectOptions = new ArrayList<>();
            initCheckBoxButtonAndImageView();
            radioGroup.setVisibility(View.GONE);
            checkboxGroup.setVisibility(View.VISIBLE);
            for (int i = 0; i < mrls.size(); i++) {
                RelativeLayout mrl = mrls.get(i);
                if (i < voteCount) {
                    mrl.setVisibility(View.VISIBLE);
                } else {
                    mrl.setVisibility(View.GONE);
                }
            }
        }
    }
}