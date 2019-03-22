package com.bokecc.dwlivemoduledemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bokecc.dwlivemoduledemo.R;
import com.bokecc.dwlivemoduledemo.base.BaseActivity;

/**
 * 观看直播 & 观看回放 入口选择页
 */
public class PilotActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestFullScreenFeature();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pilot);


        /**************************************************************************
         *
         *   CC SDK 直播观看核心类：DWLive, 回放观看核心类 DWLiveReplay
         *
         *   使用流程：登录 --> 观看
         *
         *   页面流程：登录页 --> 观看页
         *
         *   流程详解：登录操作的Activity和观看页Activity为两个Activity，监听到登录成功后
         *   再跳转到播放页进行观看。
         *
         **************************************************************************/


        // 跳转到直播登录页
        findViewById(R.id.btn_start_live).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PilotActivity.this, LiveLoginActivity.class);
                startActivity(intent);
            }
        });

        // 跳转到回放登录页
        findViewById(R.id.btn_start_replay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PilotActivity.this, ReplayLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
