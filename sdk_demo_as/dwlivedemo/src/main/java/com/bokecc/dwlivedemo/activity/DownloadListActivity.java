package com.bokecc.dwlivedemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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
import com.bokecc.dwlivedemo.download.DownloadItemClickListener;
import com.bokecc.dwlivedemo.download.DownloadListAdapter;
import com.bokecc.dwlivedemo.download.DownloadView;
import com.bokecc.dwlivedemo.download.TasksManager;
import com.bokecc.dwlivedemo.download.TasksManagerModel;
import com.bokecc.dwlivedemo.popup.DownloadInfoDeletePopup;
import com.bokecc.dwlivedemo.popup.DownloadUrlInputDialog;
import com.bokecc.dwlivedemo.scan.qr_codescan.MipcaActivityCapture;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadLog;

import java.lang.ref.WeakReference;


/**
 * 离线回放下载列表页
 */
public class DownloadListActivity extends BaseActivity implements View.OnClickListener, DownloadView {

    public static String DOWNLOAD_DIR = Environment.getExternalStorageDirectory().getPath() + "/CCDownload/";

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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideActionBar();
        setContentView(R.layout.activity_down_load_list);

        FileDownloadLog.NEED_LOG = true;
        FileDownloader.setup(this);
        //注册TaskManager
        TasksManager.getImpl().onCreate(new WeakReference<DownloadView>(this));

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

        //初始化下载列表
        initDownloadList();

        //检测权限
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

    }

    @Override
    public void postNotifyDataChanged() {
        if (adapter != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }


    private void initDownloadList() {
        mDownloadListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadListAdapter(this);
        mDownloadListView.setAdapter(adapter);
        mDownloadListView.addItemDecoration(new DividerItemDecoration(DownloadListActivity.this, LinearLayout.VERTICAL));

        adapter.setItemClickListener(new DownloadItemClickListener() {
            @Override
            public void onFinishTaskClick(int taskId) {
                TasksManagerModel task = TasksManager.getImpl().getById(taskId);
                Intent intent = new Intent(DownloadListActivity.this, LocalReplayPlayActivity.class);
                intent.putExtra("fileName", task.getName());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(final int taskId) {
                mDeletePopup.setListener(new DownloadInfoDeletePopup.ConfirmListener() {
                    @Override
                    public void onConfirmClick() {
                        TasksManager.getImpl().removeTask(taskId);
                        adapter.notifyDataSetChanged();
                    }
                });
                mDeletePopup.show(mRoot);
            }
        });

        mUrlInputDialog.setAddUrlListener(new DownloadUrlInputDialog.AddUrlListener() {
            @Override
            public void onUrlAdd(String url) {
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                int ret = TasksManager.getImpl().addTask(fileName, url, DOWNLOAD_DIR);
                handleAddTaskRet(ret);
            }
        });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.id_code_add:
                codeScanAddress();
                break;
            case R.id.id_new_add:
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
                        int ret = TasksManager.getImpl().addTask(fileName, url, DOWNLOAD_DIR);
                        handleAddTaskRet(ret);

                    } else {
                        Toast.makeText(getApplicationContext(), "扫描失败，请扫描正确的播放二维码", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }


    public void handleAddTaskRet(int ret) {
        switch (ret) {
            case TasksManager.CODE_OK:
                postNotifyDataChanged();
                break;
            case TasksManager.CODE_TASK_ALREADY_EXIST:
                toastOnUiThread("任务已存在");
                break;
            case TasksManager.CODE_URL_ERROR:
                toastOnUiThread("任务Url错误");
                break;
            case TasksManager.INSERT_DATA_BASE_ERROR:
                toastOnUiThread("数据库发生错误");
                break;
        }
    }


    @Override
    protected void onDestroy() {
        TasksManager.getImpl().onDestroy();
        adapter = null;
        FileDownloader.getImpl().pauseAll();
        super.onDestroy();
    }


}
