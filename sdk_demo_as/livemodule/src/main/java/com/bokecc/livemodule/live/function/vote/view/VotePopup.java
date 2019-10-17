package com.bokecc.livemodule.live.function.vote.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.function.vote.adapter.VoteSummaryAdapter;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bokecc.sdk.mobile.live.DWLive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class VotePopup extends BasePopupWindow {

    public VotePopup(Context context) {
        super(context);
    }

    private ImageView qsClose;

    //-----------------答题界面-----------------------------------
    private LinearLayout selectLayout;
    private ImageView selectNav;

    private RelativeLayout rl0;
    private RelativeLayout rl1;
    private RelativeLayout rl2;
    private RelativeLayout rl3;
    private RelativeLayout rl4;

    private RelativeLayout mrl0;
    private RelativeLayout mrl1;
    private RelativeLayout mrl2;
    private RelativeLayout mrl3;
    private RelativeLayout mrl4;

    private RadioGroup radioGroup;
    private RadioButton radio0;
    private RadioButton radio1;
    private RadioButton radio2;
    private RadioButton radio3;
    private RadioButton radio4;

    private LinearLayout checkboxGroup;
    private CheckBox checkBox0;
    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private CheckBox checkBox4;

    private ImageView multiIv0;
    private ImageView multiIv1;
    private ImageView multiIv2;
    private ImageView multiIv3;
    private ImageView multiIv4;

    private ImageView cbIv0;
    private ImageView cbIv1;
    private ImageView cbIv2;
    private ImageView cbIv3;
    private ImageView cbIv4;

    private RadioGroup doubleGroup;
    private RadioButton double0;
    private RadioButton double1;

    private ImageView doubleIv0;
    private ImageView doubleIv1;

    private Button submit;

    //-----------------统计界面-----------------------------------
    private LinearLayout summaryLayout;
    private ImageView summaryNav;
    private TextView tvVoteCount;
    private TextView tvUserResult;
    private RecyclerView summaryList;

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

    VoteSummaryAdapter adapter;

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
        selectNav = findViewById(R.id.qs_select_nav);

        radioGroup = findViewById(R.id.rg_qs_multi);
        radio0 = findViewById(R.id.rb_multi_0);
        radio1 = findViewById(R.id.rb_multi_1);
        radio2 = findViewById(R.id.rb_multi_2);
        radio3 = findViewById(R.id.rb_multi_3);
        radio4 = findViewById(R.id.rb_multi_4);

        checkboxGroup = findViewById(R.id.ll_qs_checkboxs);
        checkBox0 = findViewById(R.id.cb_multi_0);
        checkBox1 = findViewById(R.id.cb_multi_1);
        checkBox2 = findViewById(R.id.cb_multi_2);
        checkBox3 = findViewById(R.id.cb_multi_3);
        checkBox4 = findViewById(R.id.cb_multi_4);

        rls = new ArrayList<>();
        rl0 = findViewById(R.id.rl_qs_single_select_0);
        rl1 = findViewById(R.id.rl_qs_single_select_1);
        rl2 = findViewById(R.id.rl_qs_single_select_2);
        rl3 = findViewById(R.id.rl_qs_single_select_3);
        rl4 = findViewById(R.id.rl_qs_single_select_4);

        mrls = new ArrayList<>();
        mrl0 = findViewById(R.id.rl_qs_mulit_select_0);
        mrl1 = findViewById(R.id.rl_qs_mulit_select_1);
        mrl2 = findViewById(R.id.rl_qs_mulit_select_2);
        mrl3 = findViewById(R.id.rl_qs_mulit_select_3);
        mrl4 = findViewById(R.id.rl_qs_mulit_select_4);

        rls.add(rl0);
        rls.add(rl1);
        rls.add(rl2);
        rls.add(rl3);
        rls.add(rl4);

        mrls.add(mrl0);
        mrls.add(mrl1);
        mrls.add(mrl2);
        mrls.add(mrl3);
        mrls.add(mrl4);

        rbs = new ArrayList<>();
        rbs.add(radio0);
        rbs.add(radio1);
        rbs.add(radio2);
        rbs.add(radio3);
        rbs.add(radio4);

        cbs = new ArrayList<>();
        cbs.add(checkBox0);
        cbs.add(checkBox1);
        cbs.add(checkBox2);
        cbs.add(checkBox3);
        cbs.add(checkBox4);

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


        multiIv0 = findViewById(R.id.iv_qs_single_select_sign_0);
        multiIv1 = findViewById(R.id.iv_qs_single_select_sign_1);
        multiIv2 = findViewById(R.id.iv_qs_single_select_sign_2);
        multiIv3 = findViewById(R.id.iv_qs_single_select_sign_3);
        multiIv4 = findViewById(R.id.iv_qs_single_select_sign_4);

        cbIv0 = findViewById(R.id.iv_qs_multi_select_sign_0);
        cbIv1 = findViewById(R.id.iv_qs_multi_select_sign_1);
        cbIv2 = findViewById(R.id.iv_qs_multi_select_sign_2);
        cbIv3 = findViewById(R.id.iv_qs_multi_select_sign_3);
        cbIv4 = findViewById(R.id.iv_qs_multi_select_sign_4);

        ivs = new ArrayList<>();
        ivs.add(multiIv0);
        ivs.add(multiIv1);
        ivs.add(multiIv2);
        ivs.add(multiIv3);
        ivs.add(multiIv4);

        mivs = new ArrayList<>();
        mivs.add(cbIv0);
        mivs.add(cbIv1);
        mivs.add(cbIv2);
        mivs.add(cbIv3);
        mivs.add(cbIv4);

        doubleGroup = findViewById(R.id.rg_qs_double);
        double0 = findViewById(R.id.rb_double_0);
        double1 = findViewById(R.id.rb_double_1);

        double0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initRadioButtonAndImageView();
                selectOption = 0;
                double0.setChecked(true);
                doubleIv0.setVisibility(View.VISIBLE);
            }
        });

        double1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initRadioButtonAndImageView();
                selectOption = 1;
                double1.setChecked(true);
                doubleIv1.setVisibility(View.VISIBLE);
            }
        });

        doubleIv0 = findViewById(R.id.iv_qs_double_select_sign_0);
        doubleIv1 = findViewById(R.id.iv_qs_double_select_sign_1);

        submit = findViewById(R.id.btn_qs_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (voteType == 0) {
                    // 判断是否作答
                    if (selectOption == -1) {
                        Toast.makeText(mContext, "请先选择答案", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 提交结果
                    DWLive.getInstance().sendVoteResult(selectOption);
                } else if (voteType == 1) {
                    // 判断是否作答
                    if (selectOptions.size() < 1) {
                        Toast.makeText(mContext, "请先选择答案", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 生成结果
                    ArrayList<Integer> selectResult = new ArrayList<>();
                    for (String option : selectOptions) {
                        selectResult.add(Integer.valueOf(option));
                    }
                    // 提交结果
                    DWLive.getInstance().sendVoteResult(selectResult);
                }
                dismiss();
            }
        });

        //统计界面
        summaryLayout = findViewById(R.id.qs_summary_layout);
        summaryNav = findViewById(R.id.qs_summary_nav);
        tvVoteCount = findViewById(R.id.qs_vote_people_number);
        tvUserResult = findViewById(R.id.tv_user_result);
        summaryList = findViewById(R.id.qs_summary_list);


        summaryList.setLayoutManager(new LinearLayoutManager(mContext));
        adapter = new VoteSummaryAdapter(mContext);
        summaryList.setAdapter(adapter);
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
    }

    /**
     * 单选时 --- 初始化单选按钮
     */
    private void initRadioButtonAndImageView() {
        submit.setEnabled(true);

        for (RadioButton rb: rbs) {
            rb.setChecked(false);
        }

        for (ImageView view: ivs) {
            view.setVisibility(View.GONE);
        }

        doubleIv0.setVisibility(View.GONE);
        doubleIv1.setVisibility(View.GONE);

        double0.setChecked(false);
        double1.setChecked(false);
    }

    /**
     * 多选时 --- 初始化按钮
     */
    private void initCheckBoxButtonAndImageView() {
        submit.setEnabled(true);

        for (CheckBox cb: cbs) {
            cb.setChecked(false);
        }

        for (ImageView view: mivs) {
            view.setVisibility(View.GONE);
        }
    }


    @Override
    protected int getContentView() {
        return R.layout.vote_layout;
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
    int voteType; // 0为单选，1为多选

    public void startVote(int voteCount, int type) {
        this.voteCount = voteCount;
        this.voteType = type;

        showSelectLayout();
    }

    private void showSelectLayout() {
        changeLayoutShow(true);

        if (voteType == 0) {
            // 单选
            selectOption = -1;
            initRadioButtonAndImageView();
            submit.setEnabled(false);
            if (voteCount == 2) {
                radioGroup.setVisibility(View.GONE);
                checkboxGroup.setVisibility(View.GONE);
                doubleGroup.setVisibility(View.VISIBLE);
            } else {
                radioGroup.setVisibility(View.VISIBLE);
                checkboxGroup.setVisibility(View.GONE);
                doubleGroup.setVisibility(View.GONE);

                for (int i=0; i<rls.size();i++) {
                    RelativeLayout rl = rls.get(i);
                    if (i < voteCount) {
                        rl.setVisibility(View.VISIBLE);
                    } else {
                        rl.setVisibility(View.GONE);
                    }
                }
            }
        } else if (voteType == 1) {
            // 多选
            selectOptions = new ArrayList<>();
            initCheckBoxButtonAndImageView();

            radioGroup.setVisibility(View.GONE);
            checkboxGroup.setVisibility(View.VISIBLE);
            doubleGroup.setVisibility(View.GONE);

            for (int i=0; i<mrls.size();i++) {
                RelativeLayout mrl = mrls.get(i);
                if (i < voteCount) {
                    mrl.setVisibility(View.VISIBLE);
                } else {
                    mrl.setVisibility(View.GONE);
                }
            }
        }
    }

    private void changeLayoutShow(boolean isSelectSummary) {
        if (isSelectSummary) {
            selectLayout.setVisibility(View.VISIBLE);
            selectNav.setVisibility(View.VISIBLE);

            summaryLayout.setVisibility(View.GONE);
            summaryNav.setVisibility(View.GONE);
        } else {
            selectLayout.setVisibility(View.GONE);
            selectNav.setVisibility(View.GONE);

            summaryLayout.setVisibility(View.VISIBLE);
            summaryNav.setVisibility(View.VISIBLE);
        }

    }

    String[] correntOptions;
    int correntOption;
    ArrayList<VoteSingleStatisics> voteStatisices = new ArrayList<>();
    public void onVoteResult(JSONObject jsonObject) {
        initVoteResult();
        showSummaryLayout();

        try {
            if (voteType == 1) {
                JSONArray jsonArray = jsonObject.getJSONArray("correctOption");
                String jsonString = jsonArray.toString();
                jsonString = jsonString.substring(1, jsonString.length() - 1);
                this.correntOptions = jsonString.split(",");
            } else {
                this.correntOption = jsonObject.getInt("correctOption");
            }

            JSONArray statisics = jsonObject.getJSONArray("statisics");
            for (int i=0; i < statisics.length(); i++) {
                voteStatisices.add(new VoteSingleStatisics(statisics.getJSONObject(i)));
            }

            adapter.add(voteStatisices);

            // 获取答题人数
            if (jsonObject.has("answerCount")) {
                tvVoteCount.setText("回答结束，共" + jsonObject.getInt("answerCount") + "人回答。");
            } else {
                int count = 0;
                for (VoteSingleStatisics voteSingleStatisics: voteStatisices) {
                    count += voteSingleStatisics.getCount();
                }
                tvVoteCount.setText("回答结束，共" + count + "人回答。");
            }

        } catch (JSONException e) {
            Log.e("demo", e.getLocalizedMessage());
        }

        // 判断做题结果是否正确
        if (voteType == 1) {
            if (selectOptions.size() != correntOptions.length) {
                isVoteRight = correntOptions.length == 1 && correntOptions[0].isEmpty() && selectOptions.size() == 0;
            } else {
                if (correntOptions.length == 1 && correntOptions[0].isEmpty()) {
                    isVoteRight = selectOptions.size() == 0;
                } else {
                    boolean flag = true;
                    for (int i = 0; i < correntOptions.length; i++) {
                        if (!selectOptions.contains(correntOptions[i])) {
                            flag = false;
                            break;
                        }
                    }
                    isVoteRight = flag;
                }
            }
        } else if (voteType == 0) {
            isVoteRight = correntOption == selectOption;
        }

        if (voteCount > 2) {
            setOtherResult();
        } else {
            setTwoResult();
        }

    }

    boolean isVoteRight;

    String wrongTextColor = "#fc512b";
    String rightTextColor = "#12b88f";

    int[] rightImage = new int[]{R.drawable.qs_pic_option_right_0, R.drawable.qs_pic_option_right_1};
    int[] wrongImage = new int[]{R.drawable.qs_pic_option_wrong_0, R.drawable.qs_pic_option_wrong_1};

    private void setTwoResult() {
        String myAnswer = "您的答案：" + getVoteOptionString(selectOption);

        String rightAnswer = "正确答案：" + getVoteOptionString(correntOption);

        String msg = myAnswer + "\u3000\u3000" + rightAnswer;

        SpannableString ss = new SpannableString(msg);

        ss.setSpan(new ForegroundColorSpan(Color.parseColor(getMyAnswerColor())),
                0,
                myAnswer.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(new ForegroundColorSpan(Color.parseColor(rightTextColor)),
                myAnswer.length() + 1,
                msg.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (selectOption > -1) {
            if (isVoteRight) {
                setImageSpan(ss, rightImage[selectOption], myAnswer.length() - 1);
            } else {
                setImageSpan(ss, wrongImage[selectOption], myAnswer.length() - 1);
            }
        }

        if (correntOption > -1 && rightImage.length > correntOption) {
            setImageSpan(ss, rightImage[correntOption], msg.length() - 1);
        }

        tvUserResult.setText(ss);
    }

    private void setImageSpan(SpannableString ss, int resId, int position) {
        Bitmap imgBitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
        imgBitmap = ThumbnailUtils.extractThumbnail(imgBitmap, imgBitmap.getWidth(), imgBitmap.getHeight());
        ImageSpan imgSpan = new ImageSpan(mContext, imgBitmap);
        ss.setSpan(imgSpan, position, position + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private String getMyAnswerColor() {
        if (isVoteRight) {
            return rightTextColor;
        } else {
            return wrongTextColor;
        }
    }

    private void setOtherResult() {
        String myAnswer = "您的答案：";
        if (voteType == 1) {
            if (selectOptions.size() > 0) {
                ArrayList<Integer> selectIndexs = new ArrayList<>();
                for (String option : selectOptions) {
                    selectIndexs.add(Integer.valueOf(option));
                }
                Collections.sort(selectIndexs, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 >= o2 ? 1 : -1;
                    }
                });
                for (Integer optionIndex : selectIndexs) {
                    myAnswer += getVoteOptionString(optionIndex);
                }
            }
        } else {
            myAnswer += getVoteOptionString(selectOption);
        }

        String rightAnswer = "正确答案：";

        if (voteType == 1) {
            if (correntOptions.length > 0) {
                ArrayList<Integer> correntIndexs = new ArrayList<>();
                for (int i=0; i < correntOptions.length; i++) {
                    if (!correntOptions[i].isEmpty()) {
                        correntIndexs.add(Integer.valueOf(correntOptions[i]));
                    }
                }
                Collections.sort(correntIndexs, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 >= o2 ? 1 : -1;
                    }
                });
                for (Integer optionIndex : correntIndexs) {
                    rightAnswer += getVoteOptionString(optionIndex);
                }
            }
        } else {
            rightAnswer += getVoteOptionString(correntOption);
        }

        String msg = myAnswer + "\u3000\u3000" + rightAnswer;

        SpannableString ss = new SpannableString(msg);

        ss.setSpan(new ForegroundColorSpan(Color.parseColor(getMyAnswerColor())),
                0,
                myAnswer.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(new ForegroundColorSpan(Color.parseColor(rightTextColor)),
                myAnswer.length() + 1,
                msg.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvUserResult.setText(ss);
    }

    String[] voteNumbers = new String[]{"A", "B", "C", "D", "E"};
    private String getVoteOptionString(int index) {
        if (index > -1 && index < 5) {
            return voteNumbers[index];
        } else {
            return " ";
        }
    }

    private void initVoteResult() {
        voteStatisices.clear();
    }

    private void showSummaryLayout() {
        changeLayoutShow(false);
    }

    public static final class VoteSingleStatisics {

        int count;
        int option;
        String percent;

        public int getCount() {
            return count;
        }

        public int getOption() {
            return option;
        }

        public String getPercent() {
            return percent;
        }

        // [{"count":0,"option":0,"percent":"0.0"},
        public VoteSingleStatisics(JSONObject jsonObject) {
            try {
                this.count = jsonObject.getInt("count");
                this.option = jsonObject.getInt("option");
                this.percent = jsonObject.getString("percent");
            } catch (JSONException e) {
                Log.e("demo", e.getLocalizedMessage());
            }

        }
    }
}