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
import com.bokecc.dwlivemoduledemo.activity.extra.LivePlayClassicActivity;
import com.bokecc.dwlivemoduledemo.activity.extra.LivePlayDocActivity;
import com.bokecc.dwlivemoduledemo.base.BaseActivity;
import com.bokecc.dwlivemoduledemo.popup.LoginPopupWindow;
import com.bokecc.dwlivemoduledemo.scan.qr_codescan.MipcaActivityCapture;
import com.bokecc.livemodule.login.LoginLineLayout;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.DWLiveLoginListener;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.LoginInfo;
import com.bokecc.sdk.mobile.live.pojo.PublishInfo;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;
import com.bokecc.sdk.mobile.live.pojo.TemplateInfo;
import com.bokecc.sdk.mobile.live.pojo.Viewer;

import java.util.HashMap;
import java.util.Map;

/***
 * 直播观看登录页面
 */
public class LiveLoginActivity extends BaseActivity implements View.OnClickListener {

    static final int MAX_NAME = 20;  // 用户昵称最多20字符

    View mRoot;

    LoginPopupWindow loginPopupWindow;   // 登录Loading控件

    LoginLineLayout lllLoginLiveUid;        // CC 账号ID
    LoginLineLayout lllLoginLiveRoomid;     // 直播间ID
    LoginLineLayout lllLoginLiveName;       // 用户昵称
    LoginLineLayout lllLoginLivePassword;   // 用户密码

    Button btnLoginLive; // 登录按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideActionBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_login);
        initViews();

        preferences = getSharedPreferences("live_login_info", Activity.MODE_PRIVATE);
        getSharePrefernce();
        if (map != null) {
            initEditTextInfo();
        }
    }

    private void initViews() {
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_scan).setOnClickListener(this);

        btnLoginLive = findViewById(com.bokecc.livemodule.R.id.btn_login_live);
        lllLoginLiveUid = findViewById(com.bokecc.livemodule.R.id.lll_login_live_uid);
        lllLoginLiveRoomid = findViewById(com.bokecc.livemodule.R.id.lll_login_live_roomid);
        lllLoginLiveName = findViewById(com.bokecc.livemodule.R.id.lll_login_live_name);
        lllLoginLivePassword = findViewById(com.bokecc.livemodule.R.id.lll_login_live_password);

        lllLoginLiveUid.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_uid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginLiveRoomid.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_roomid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginLiveName.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_name_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginLiveName.maxEditTextLength = MAX_NAME;
        lllLoginLivePassword.setHint(getResources().getString(com.bokecc.livemodule.R.string.login_s_password_hint)).addOnTextChangeListener(myTextWatcher)
                .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        btnLoginLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLiveLogin();
            }
        });

        loginPopupWindow = new LoginPopupWindow(this);
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
     * 执行直播登录操作
     */
    private void doLiveLogin() {

        loginPopupWindow.show(mRoot);

        // 创建登录信息
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setRoomId(lllLoginLiveRoomid.getText());
        loginInfo.setUserId(lllLoginLiveUid.getText());
        loginInfo.setViewerName(lllLoginLiveName.getText());
        loginInfo.setViewerToken(lllLoginLivePassword.getText());

        // 设置登录参数
        DWLive.getInstance().setDWLiveLoginParams(new DWLiveLoginListener() {
            @Override
            public void onLogin(TemplateInfo templateInfo, Viewer viewer, final RoomInfo roomInfo, PublishInfo publishInfo) {
                toastOnUiThread("登录成功");
                // 缓存登陆的参数
                writeSharePreference();
                dismissPopupWindow();
                go(LivePlayActivity.class); // 直播默认Demo页
                //go(LivePlayDocActivity.class); // 直播'文档大屏/视频小屏'的Demo页
                //go(LivePlayClassicActivity.class);  // 直播经典播放页
            }

            @Override
            public void onException(final DWLiveException e) {
                toastOnUiThread("登录失败" + e.getLocalizedMessage());
                dismissPopupWindow();
            }
        }, loginInfo);

        // 执行登录操作
        DWLive.getInstance().startLogin();
    }

    //------------------------------- 缓存数据相关方法-----------------------------------------

    SharedPreferences preferences;

    private void writeSharePreference() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("liveuid", lllLoginLiveUid.getText());
        editor.putString("liveroomid", lllLoginLiveRoomid.getText());
        editor.putString("liveusername", lllLoginLiveName.getText());
        editor.putString("livepassword", lllLoginLivePassword.getText());
        editor.commit();
    }

    private void getSharePrefernce() {
        lllLoginLiveUid.setText(preferences.getString("liveuid", ""));
        lllLoginLiveRoomid.setText(preferences.getString("liveroomid", ""));
        lllLoginLiveName.setText(preferences.getString("liveusername", ""));
        lllLoginLivePassword.setText(preferences.getString("livepassword", ""));
    }

    //—————————————————————————————————— 扫码相关逻辑 ——————————————————————————————————————

    private static final int QR_REQUEST_CODE = 111;

    String userIdStr = "userid";  // 用户id
    String roomIdStr = "roomid";  // 房间id

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
                    if (lllLoginLiveUid != null) {
                        initEditTextInfo();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void initEditTextInfo() {
        if (map.containsKey(roomIdStr)) {
            lllLoginLiveRoomid.setText(map.get(roomIdStr));
        }

        if (map.containsKey(userIdStr)) {
            lllLoginLiveUid.setText(map.get(userIdStr));
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
            boolean isLoginEnabled = isNewLoginButtonEnabled(lllLoginLiveName, lllLoginLiveRoomid, lllLoginLiveUid);
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
