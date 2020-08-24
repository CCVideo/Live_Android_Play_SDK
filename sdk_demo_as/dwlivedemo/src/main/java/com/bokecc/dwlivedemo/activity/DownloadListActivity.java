package com.bokecc.dwlivedemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.download.DownLoadBean;
import com.bokecc.dwlivedemo.download.DownLoadManager;
import com.bokecc.dwlivedemo.download.DownLoadStatus;
import com.bokecc.dwlivedemo.download.DownLoadTaskListener;
import com.bokecc.dwlivedemo.download.DownloadItemClickListener;
import com.bokecc.dwlivedemo.download.DownloadListAdapter;
import com.bokecc.dwlivedemo.download.FileUtil;
import com.bokecc.dwlivedemo.popup.DownloadInfoDeletePopup;
import com.bokecc.dwlivedemo.popup.DownloadUrlInputDialog;
import com.bokecc.dwlivedemo.scan.qr_codescan.MipcaActivityCapture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 离线回放下载列表页
 */
public class DownloadListActivity extends BaseActivity implements View.OnClickListener, DownLoadTaskListener {

    public static String DOWNLOAD_DIR;
    /**
     * activity里最底层的父布局容器，用于弹出PopupWindow使用
     */
    private View mRoot;

    /**
     * 下载列表recyclerView
     */
    private RecyclerView mDownloadListView;

    /**
     * recyclerView 的adapter
     */
    private DownloadListAdapter adapter;

    /**
     * 下载删除弹出框
     */
    private DownloadInfoDeletePopup mDeletePopup;

    /**
     * 下载链接输入弹出框
     */
    private DownloadUrlInputDialog mUrlInputDialog;


    /**
     * 二维码扫描按钮
     */
    private ImageView mScanBtn;

    /**
     * 添加地址按钮
     */
    private TextView mAddNewAddressBtn;

    /**
     * 跳转二维码扫面界面请求码
     */
    private final int qrRequestCode = 111;
    private DownLoadManager downloadManager;
    private int checkCallPhonePermission;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideActionBar();
        setContentView(R.layout.activity_down_load_list);
        DOWNLOAD_DIR = FileUtil.getCCDownLoadPath(this);
        //初始化控件
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        mDownloadListView = findViewById(R.id.id_download_list);
        mScanBtn = findViewById(R.id.id_code_add);
        mAddNewAddressBtn = findViewById(R.id.id_new_add);
        mScanBtn.setOnClickListener(this);
        mAddNewAddressBtn.setOnClickListener(this);
        mUrlInputDialog = new DownloadUrlInputDialog();
        mDeletePopup = new DownloadInfoDeletePopup(this);
        mDeletePopup.setOutsideCancel(true);
        mDeletePopup.setBackPressedCancel(true);
        //文件下载管理器
        downloadManager = new DownLoadManager();
        downloadManager.setDownLoadTaskListener(this);
        //检测权限
        checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (checkCallPhonePermission == PackageManager.PERMISSION_GRANTED){
            //初始化下载列表
            initDownloadList();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (checkCallPhonePermission == PackageManager.PERMISSION_GRANTED){
            //初始化下载列表
            initDownloadList();
        }else{
            toastOnUiThread("请开启文件存储权限");
        }
    }
    private long crrentTime = 0;
    private void initDownloadList() {
        mDownloadListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadListAdapter(this);
        mDownloadListView.setAdapter(adapter);
        mDownloadListView.addItemDecoration(new DividerItemDecoration(DownloadListActivity.this, LinearLayout.VERTICAL));

        adapter.setItemClickListener(new DownloadItemClickListener() {
            @Override
            public void onFinishTaskClick(String path) {
                if (System.currentTimeMillis() - crrentTime<2000){
                    toastOnUiThread("点击太频繁了");
                    return;
                }
                crrentTime = System.currentTimeMillis();
                Intent intent = new Intent(DownloadListActivity.this, LocalReplayPlayActivity.class);
                intent.putExtra("fileName", FileUtil.getUnzipFileName(path));
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(final DownLoadBean downLoadBean) {
                mDeletePopup.setListener(new DownloadInfoDeletePopup.ConfirmListener() {
                    @Override
                    public void onConfirmClick() {
                        List<DownLoadBean> list = new ArrayList<>();
                        List<DownLoadBean> dates = adapter.getDates();
                        list.addAll(dates);
                        Iterator<DownLoadBean> iterator = dates.iterator();
                        while (iterator.hasNext()){
                            DownLoadBean next = iterator.next();
                            if (next.getUrl().equals(downLoadBean.getUrl())){
                                list.remove(next);
                                break;
                            }
                        }
                        adapter.setDates(list);
                        downloadManager.delete(downLoadBean);
                    }
                });
                mDeletePopup.show(mRoot);
            }

            @Override
            public void onDownload(DownLoadBean downLoadBean) {
                downloadManager.reStart(downLoadBean);
            }

            @Override
            public void onPause(DownLoadBean downLoadBean) {
                downLoadBean.setTaskStatus(DownLoadStatus.DOWNLOAD_PAUSE);
                downloadManager.pause(downLoadBean);
            }

            @Override
            public void reDownLoad(DownLoadBean downLoadBean) {
                //重新下载
                downLoadBean.setTaskStatus(DownLoadStatus.DOWNLOAD_WAIT);
                downloadManager.reDownload(downLoadBean);
                //刷新界面
                List<DownLoadBean> dates = adapter.getDates();
                List<DownLoadBean> list = new ArrayList<>();
                list.addAll(dates);
                for (DownLoadBean downLoadBean1:list ){
                    if (downLoadBean1.getUrl().equals(downLoadBean.getUrl())){
                        downLoadBean.setTaskStatus(DownLoadStatus.DOWNLOAD_WAIT);
                        break;
                    }
                }
                adapter.setDates(list);
            }
        });

        mUrlInputDialog.setAddUrlListener(new DownloadUrlInputDialog.AddUrlListener() {
            @Override
            public void onUrlAdd(String url) {
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                DownLoadBean downLoadBean = new DownLoadBean();
                downLoadBean.setProgress(0);
                downLoadBean.setTotal(0);
                downLoadBean.setPath(DOWNLOAD_DIR+"/"+fileName);
                downLoadBean.setUrl(url);
                //开始下载
                downloadManager.start(downLoadBean);
            }
        });
        downloadManager.getAllLocalData();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.id_code_add:
                if (checkCallPhonePermission == PackageManager.PERMISSION_GRANTED)
                    codeScanAddress();
                break;
            case R.id.id_new_add:
                if (checkCallPhonePermission == PackageManager.PERMISSION_GRANTED)
                    addNewAddress();
                break;
        }
    }


    /**
     * 跳转到二维码扫描界面
     */
    public void codeScanAddress() {
        Intent intent = new Intent(this, MipcaActivityCapture.class);
        startActivityForResult(intent, qrRequestCode);
    }

    /**
     * 添加新的地址URL地址
     */
    public void addNewAddress() {
        if (!mUrlInputDialog.isAdded()) {
            mUrlInputDialog.show(getSupportFragmentManager(), "EditNameDialog");
        }
    }


    /**
     * 二维码扫面结果返回
     *
     * @param requestCode 启动请求码
     * @param resultCode  二维码返回时提供的结果码
     * @param data        返回数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case qrRequestCode:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");
                    if (result == null) {
                        Toast.makeText(getApplicationContext(), "扫描失败，请扫描正确的播放二维码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String url = result.trim();
                    if (url.startsWith("http") && url.endsWith("ccr")) {
                        String fileName = url.substring(url.lastIndexOf("/") + 1);
                        DownLoadBean downLoadBean = new DownLoadBean();
                        downLoadBean.setProgress(0);
                        downLoadBean.setTotal(0);
                        downLoadBean.setPath(DOWNLOAD_DIR+"/"+fileName);
                        downLoadBean.setUrl(url);
                        downloadManager.start(downLoadBean);
                    } else {
                        Toast.makeText(getApplicationContext(), "扫描失败，请扫描正确的播放二维码", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }


    @Override
    public void getAllDateResult(List<DownLoadBean> date) {
        adapter.setDates(date);
    }

    @Override
    public void error(int status, String url) {
        //错误回调
        int type = 0;
        switch (status){
            case 1://获取所有数据失败

                break;
            case 2://删除失败
                toastOnUiThread("删除失败");
                break;

            case 3://下载失败
                toastOnUiThread("下载失败");
                type  = DownLoadStatus.DOWNLOAD_ERROR;
                update(url,type);
                break;

            case 4://暂停失败
                toastOnUiThread("暂停失败");
                break;

            case 5://解压失败
                toastOnUiThread("解压失败");
                type  = DownLoadStatus.ZIP_ERROR;
                update(url,type);
                break;
            case 6://更新本地数据库失败
                toastOnUiThread("更新本地数据库失败");
                break;
            case 12://重复下载
                toastOnUiThread("重复下载");
                break;
        }

    }
    public void update(String url,int type){
        //更新adapter
        List<DownLoadBean> dates = adapter.getDates();
        List<DownLoadBean> list = new ArrayList<>();
        list.addAll(dates);
        for (DownLoadBean downLoadBean1:list ){
            if (downLoadBean1.getUrl().equals(url)){
                downLoadBean1.setTaskStatus(type);
                break;
            }
        }
        adapter.setDates(list);
    }
    @Override
    public void onProcess(DownLoadBean downLoadBean) {
        //进度改变回调
        List<DownLoadBean> dates = adapter.getDates();
        List<DownLoadBean> list = new ArrayList<>();
        list.addAll(dates);
        for (DownLoadBean downLoadBean1:list ){
            if (downLoadBean1.getUrl().equals(downLoadBean.getUrl())){
                downLoadBean1.setTotal(downLoadBean.getTotal());
                downLoadBean1.setTaskStatus(downLoadBean.getTaskStatus());
                downLoadBean1.setPath(downLoadBean.getPath());
                downLoadBean1.setProgress(downLoadBean.getProgress());
                downLoadBean1.setId(downLoadBean.getId());
                break;
            }
        }
        adapter.setDates(list);
    }

    @Override
    public void statusChange(int downLoadStatus, DownLoadBean downLoadBean1) {
        //状态改变回调
        List<DownLoadBean> dates = adapter.getDates();
        List<DownLoadBean> list = new ArrayList<>();
        list.addAll(dates);
        for (int i = 0;i<list.size();i++){
            DownLoadBean downLoadBean = list.get(i);
            if (downLoadBean.getUrl().equals(downLoadBean1.getUrl())){
                downLoadBean.setTaskStatus(downLoadStatus);
                break;
            }
        }
        adapter.setDates(list);
    }

    @Override
    public void addDateSuccess(DownLoadBean downLoadBean) {
        //刷新界面
        List<DownLoadBean> dates = adapter.getDates();
        List<DownLoadBean> list = new ArrayList<>();
        list.addAll(dates);
        list.add(downLoadBean);
        adapter.setDates(list);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁下载的框架
        if (downloadManager!=null)
            downloadManager.destroy();
    }
}
