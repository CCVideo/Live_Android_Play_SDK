package com.bokecc.dwlivemoduledemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bokecc.dwlivemoduledemo.R;
import com.bokecc.dwlivemoduledemo.base.BaseActivity;
import com.bokecc.dwlivemoduledemo.popup.LoginPopupWindow;
import com.bokecc.dwlivemoduledemo.scan.qr_codescan.MipcaActivityCapture;
import com.bokecc.livemodule.login.LoginLineLayout;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.TemplateInfo;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplayLoginListener;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLoginInfo;

import java.util.HashMap;
import java.util.Map;

/***
 * 在线回放登录页面
 */
public class ReplayLoginActivity extends BaseActivity implements View.OnClickListener {

    static final int MAX_NAME = 20;  // 用户昵称最多20字符

    LoginLineLayout lllLoginReplayUid;      // CC 账号ID
    LoginLineLayout lllLoginReplayRoomid;    // 直播间ID
    LoginLineLayout lllLoginReplayLiveid;    // 直播ID
    LoginLineLayout lllLoginReplayRecordid;  // 回放ID
    LoginLineLayout lllLoginReplayName;      // 用户昵称
    LoginLineLayout lllLoginReplayPassword;  // 用户密码

    Button btnLoginLive;

    LoginPopupWindow loginPopupWindow;   // 登录Loading控件
    private View mRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideActionBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_login);
        initViews();

        preferences = getSharedPreferences("live_login_info", Activity.MODE_PRIVATE);
        getSharePrefernce();
        if (map != null) {
            initEditTextInfo();
        }

    }

    private void initViews() {
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_scan).setOnClickListener(this);
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        btnLoginLive = findViewById(com.bokecc.livemodule.R.id.btn_login_replay);
        lllLoginReplayUid = findViewById(com.bokecc.livemodule.R.id.lll_login_replay_uid);
        lllLoginReplayRoomid = findViewById(com.bokecc.livemodule.R.id.lll_login_replay_roomid);
        lllLoginReplayLiveid = findViewById(com.bokecc.livemodule.R.id.lll_login_replay_liveid);
        lllLoginReplayRecordid = findViewById(com.bokecc.livemodule.R.id.lll_login_replay_recordid);
        lllLoginReplayName = findViewById(com.bokecc.livemodule.R.id.lll_login_replay_name);
        lllLoginReplayPassword = findViewById(com.bokecc.livemodule.R.id.lll_login_replay_password);

        lllLoginReplayUid.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_uid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayRoomid.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_roomid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayLiveid.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_liveid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayRecordid.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_recordid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayName.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_name_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayName.maxEditTextLength = MAX_NAME;
        lllLoginReplayPassword.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_s_password_hint)).addOnTextChangeListener(myTextWatcher)
                .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        preferences = getSharedPreferences("live_login_info", Activity.MODE_PRIVATE);

        loginPopupWindow = new LoginPopupWindow(this);

        btnLoginLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLiveLogin();
            }
        });
    }

    /**
     * 隐藏弹窗
     */
    private void dismissPopupWindow() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loginPopupWindow != null && loginPopupWindow.isShowing()) {
                    loginPopupWindow.dismiss();
                }
            }
        });
    }

    //———————————————————————————————————— 登录相关方法（核心方法）  —————————————————————————————————————————

    /**
     * 执行回放登录操作
     */
    private void doLiveLogin() {

        // 创建登录信息
        ReplayLoginInfo replayLoginInfo = new ReplayLoginInfo();
        replayLoginInfo.setUserId(lllLoginReplayUid.getText());
        replayLoginInfo.setRoomId(lllLoginReplayRoomid.getText());
        replayLoginInfo.setLiveId(lllLoginReplayLiveid.getText());
        replayLoginInfo.setRecordId(lllLoginReplayRecordid.getText());
        replayLoginInfo.setViewerName(lllLoginReplayName.getText());
        replayLoginInfo.setViewerToken(lllLoginReplayPassword.getText());

        // 设置登录参数
        DWLiveReplay.getInstance().setLoginParams(new DWLiveReplayLoginListener() {

            @Override
            public void onException(final DWLiveException exception) {
                dismissPopupWindow();
                toastOnUiThread("登录失败");
            }

            @Override
            public void onLogin(TemplateInfo templateInfo) {
                dismissPopupWindow();
                writeSharePreference();
                toastOnUiThread("登录成功");
                go(ReplayPlayActivity.class); // 回放默认Demo页
                // go(ReplayPlayDocActivity.class);  // 回放'文档大屏/视频小屏'的Demo页
                dismissPopupWindow();
            }
        }, replayLoginInfo);

        loginPopupWindow.show(mRoot);
        // 执行登录操作
        DWLiveReplay.getInstance().startLogin();
    }

    //------------------------------- 缓存数据相关方法-----------------------------------------

    SharedPreferences preferences;

    private void writeSharePreference() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("replayuid", lllLoginReplayUid.getText());
        editor.putString("replayroomid", lllLoginReplayRoomid.getText());
        editor.putString("replayliveid", lllLoginReplayLiveid.getText());
        editor.putString("replayrecordid", lllLoginReplayRecordid.getText());
        editor.putString("replayusername", lllLoginReplayName.getText());
        editor.putString("replaypassword", lllLoginReplayPassword.getText());
        editor.commit();
    }

    private void getSharePrefernce() {
        lllLoginReplayUid.setText(preferences.getString("replayuid", ""));
        lllLoginReplayRoomid.setText(preferences.getString("replayroomid", ""));
        lllLoginReplayLiveid.setText(preferences.getString("replayliveid", ""));
        lllLoginReplayRecordid.setText(preferences.getString("replayrecordid", ""));
        lllLoginReplayName.setText(preferences.getString("replayusername", ""));
        lllLoginReplayPassword.setText(preferences.getString("replaypassword", ""));
    }

    //—————————————————————————————————— 扫码相关逻辑 ——————————————————————————————————————

    private static final int QR_REQUEST_CODE = 222;

    String userIdStr = "userid";  // 用户id
    String roomIdStr = "roomid";  // 房间id
    String liveIdStr = "liveid";  // 直播id
    String recordIdStr = "recordid";  // 回放id

    Map<String, String> map;

    // 跳转到扫码页面
    private void showScan() {
        Intent intent = new Intent(this, MipcaActivityCapture.class);
        startActivityForResult(intent, QR_REQUEST_CODE);
    }

    // 接收并处理扫码页返回的数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case QR_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");

                    if (!result.contains("userid=")) {
                        Toast.makeText(getApplicationContext(), "扫描失败，请扫描正确的播放二维码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    map = parseUrl(result);
                    initEditTextInfo();
                }
                break;
            default:
                break;
        }
    }

    private void initEditTextInfo() {
        if (map.containsKey(roomIdStr)) {
            lllLoginReplayRoomid.setText(map.get(roomIdStr));
        }

        if (map.containsKey(userIdStr)) {
            lllLoginReplayUid.setText(map.get(userIdStr));
        }

        if (map.containsKey(liveIdStr)) {
            lllLoginReplayLiveid.setText(map.get(liveIdStr));
        }

        if (map.containsKey(recordIdStr)) {
            lllLoginReplayRecordid.setText(map.get(recordIdStr));
        }
    }

    //------------------------------------- 工具方法 -------------------------------------

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_scan:
                showScan();
                break;
        }
    }

    private TextWatcher myTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean isLoginEnabled = isNewLoginButtonEnabled(lllLoginReplayUid, lllLoginReplayRoomid, lllLoginReplayLiveid, lllLoginReplayName);
            btnLoginLive.setEnabled(isLoginEnabled);
            btnLoginLive.setTextColor(isLoginEnabled ? Color.parseColor("#ffffff") : Color.parseColor("#f7d8c8"));
        }
    };


    // 检测登录按钮是否应该可用
    public static boolean isNewLoginButtonEnabled(LoginLineLayout... views) {
        for (int i = 0; i < views.length; i++) {
            if ("".equals(views[i].getText().trim())) {
                return false;
            }
        }
        return true;
    }

    // 解析扫码获取到的URL
    private Map<String, String> parseUrl(String url) {
        Map<String, String> map = new HashMap<String, String>();
        String param = url.substring(url.indexOf("?") + 1, url.length());
        String[] params = param.split("&");

        if (params.length < 2) {
            return null;
        }
        for (String p : params) {
            String[] en = p.split("=");
            map.put(en[0], en[1]);
        }
        return map;
    }
}
