package com.bokecc.dwlivemoduledemo.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bokecc.dwlivemoduledemo.R;
import com.bokecc.dwlivemoduledemo.activity.extra.ReplayMixPlayActivity;
import com.bokecc.dwlivemoduledemo.base.BaseActivity;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * 观看直播 & 观看回放 & 离线回放 入口选择页
 */
public class PilotActivity extends BaseActivity {

    private RxPermissions rxPermissions;
    private int grantNum = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestFullScreenFeature();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pilot);
        rxPermissions = new RxPermissions(this);

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
                checkoutPermission(intent);
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
                // Intent intent = new Intent(PilotActivity.this, DownloadListActivity.class);
                // startActivity(intent);

                // TODO Demo模版：跳转到在线和离线混合（支持列表切换）播放页 (ReplayMixPlayActivity)
                Intent intent = new Intent(PilotActivity.this, ReplayMixPlayActivity.class);
                checkoutPermission(intent);
            }
        });
    }




    private void checkoutPermission(final Intent intent){
        grantNum = 0;

        Observable<Permission> permissionObservable = rxPermissions.requestEach(Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );

        Disposable subscribe = permissionObservable.subscribe(new Consumer<Permission>() {
            @Override
            public void accept(Permission permission) {
                if (permission.granted) {
                    ELog.i(this, "permission granted:" + permission.name);
                    grantNum++;
                    if (grantNum == 3) {
                        ELog.i(this, "all permission is granted:");
                        startActivity(intent);
                    }
                }
            }
        });
    }

}
