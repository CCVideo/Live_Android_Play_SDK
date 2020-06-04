package com.bokecc.dwlivedemo.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bokecc.dwlivedemo.BuildConfig;
import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.utils.Permissions;
import com.bokecc.dwlivedemo.utils.StatusBarUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


/**
 * 观看直播 & 观看回放 & 离线回放 入口选择页
 */
public class PilotActivity extends BaseActivity {
    private static final String TAG = "PilotActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 全屏
//        requestFullScreenFeature();
        // 沉浸式
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.setLightStatusBar(this, true, false);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pilot);
        ((TextView) (findViewById(R.id.about_version_code))).setText(getString(R.string.pilot_version, BuildConfig.VERSION_NAME));

        /**************************************************************************
         *
         *   CC SDK 直播观看核心类：DWLive, 回放观看核心类 DWLiveReplay, 离线回放核心类：DWLiveLocalReplay
         *
         *   使用流程：登录 --> 观看
         *
         *   页面流程：登录页 --> 观看页
         *
         *   流程详解：登录操作的Activity和观看页Activity为两个Activity，监听到登录成功后再跳转到播放页进行观看。
         *
         *   离线回放流程：下载CCR文件 --> 解压文件 --> 调用DWLiveLocalReplay相关方法进行播放
         *
         **************************************************************************/


        // 跳转到直播登录页
        findViewById(R.id.btn_start_live).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PilotActivity.this, LiveLoginActivity.class);
                checkoutLivePermission(intent);
            }
        });

        // 跳转到回放登录页
        findViewById(R.id.btn_start_replay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PilotActivity.this, ReplayLoginActivity.class);
                checkoutPermission(intent);
            }
        });

        // 跳转到离线回放列表页
        findViewById(R.id.btn_start_local_replay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PilotActivity.this, DownloadListActivity.class);
                checkoutPermission(intent);
            }
        });
    }

    private void checkoutPermission(final Intent intent) {
        Permissions.request(this, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new Permissions.Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        if (integer == PERMISSION_GRANTED) {
                            startActivity(intent);
                        }

                    }
                });


    }
    private void checkoutLivePermission(final Intent intent) {
        Permissions.request(this, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE},
                new Permissions.Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        if (integer == PERMISSION_GRANTED) {
                            startActivity(intent);
                        }

                    }
                });


    }

}
