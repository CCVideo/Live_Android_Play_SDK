package com.bokecc.dwlivedemo.download;

import android.os.Handler;
import android.util.JsonReader;
import android.util.Log;

import com.bokecc.download.HdDownloadListener;
import com.bokecc.download.HdHuodeException;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;
import com.bokecc.sdk.mobile.live.pojo.TemplateInfo;
import com.bokecc.sdk.mobile.live.replay.local.LocalTaskAck;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayBroadCastMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayDrawData;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayDrawInterface;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLiveInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPageInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQAMsg;
import com.bokecc.sdk.mobile.live.util.json.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.ContentValues.TAG;
import static com.bokecc.sdk.mobile.live.replay.local.LocalTaskAck.GET_BROADCAST;
import static com.bokecc.sdk.mobile.live.replay.local.LocalTaskAck.GET_CHATMSG;
import static com.bokecc.sdk.mobile.live.replay.local.LocalTaskAck.GET_QAMSG;

public class DownLoadManager {
    /**
     * 下载的缓存队列 解压的缓存队列
     */
    private LinkedList<DownLoadBean> downLoadQuery,unZipquery;
    /**
     * 正在下载的队列
     */
    private LinkedHashMap<String, HdDwonLoadUtils> downLoaddingQuery;
    /**
     * 数据库帮助类
     */
    private static TasksManagerDBController tasksManagerDBController;
    /**
     * handler 主要作用就是将回调回抛到主线程
     */
    private Handler handler;
    /**
     * 回调
     */
    private DownLoadTaskListener downLoadTaskListener;
    /**
     * 解压器
     */
    private UnZiper unZiper;
    /**
     * 是否正在解压
     */
    private AtomicBoolean isUnZipping = new AtomicBoolean(false);
    public void setDownLoadTaskListener(DownLoadTaskListener downLoadTaskListener) {
        this.downLoadTaskListener = downLoadTaskListener;
    }

    public DownLoadManager() {
        downLoadQuery = new LinkedList<>();
        unZipquery = new LinkedList<>();
        downLoaddingQuery = new LinkedHashMap<>();
        tasksManagerDBController = new TasksManagerDBController();
        handler = new Handler();
    }
    /**
     * 获取数据库所有数据
     */
    public void getAllLocalDate() {
        ThreadPoolUtils.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final List<DownLoadBean> allTasks = tasksManagerDBController.getAllTasks();

                for (final DownLoadBean downLoadBean :allTasks){
                    if (downLoadBean.getTaskStatus()==DownLoadStatus.DOWNLOAD_WAIT||downLoadBean.getTaskStatus()==DownLoadStatus.DOWNLOADING){
                        //如果是等待下载或者正在下载 直接去下载
                        ThreadPoolUtils.getInstance().execute(new Runnable() {
                            @Override
                            public void run() {
                                startDownload(downLoadBean);
                            }
                        });
                    }else if (downLoadBean.getTaskStatus()==DownLoadStatus.ZIP_WAIT||downLoadBean.getTaskStatus()==DownLoadStatus.ZIPING){
                        //如果是等待解压或者正在解压 直接去解压
                        startUnzip(downLoadBean);

                    }else if (downLoadBean.getTaskStatus()==DownLoadStatus.PARSE_START||downLoadBean.getTaskStatus()==DownLoadStatus.PARSE_FAIL){
                        //如果是等待下载或者正在下载 直接去下载
                        ThreadPoolUtils.getInstance().execute(new Runnable() {
                            @Override
                            public void run() {
                                startParse(downLoadBean);
                            }
                        });
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (downLoadTaskListener != null) {
                            downLoadTaskListener.getAllDateResult(allTasks);
                        }
                    }
                });
                ThreadPoolUtils.getInstance().cancel(this);
            }
        });
    }
    public void reStart(final DownLoadBean downLoadBean){
        ThreadPoolUtils.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                startDownload(downLoadBean);
                ThreadPoolUtils.getInstance().cancel(this);
            }
        });
    }
    /**
     * 开始下载
     * @param downLoadBean
     */
    private void startDownload(final DownLoadBean downLoadBean) {
        if (downLoaddingQuery.size() < 5) {
            downLoadBean.setTaskStatus(DownLoadStatus.DOWNLOAD_START);
            tasksManagerDBController.update(downLoadBean);
            statusChange(DownLoadStatus.DOWNLOAD_START,downLoadBean);

            //判断文件是否存在
            File file = new File(downLoadBean.getPath());
            if (!file.exists()) {
                File fileParent = file.getParentFile();//返回的是File类型,可以调用exsit()等方法
                if (!fileParent.exists()) {
                    fileParent.mkdirs();// 能创建多级目录
                }
                if (!file.exists()) {
                    try {
                        file.createNewFile();//有路径才能创建文件
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            HdDwonLoadUtils hdDownloader = new HdDwonLoadUtils(downLoadBean.getUrl(), downLoadBean.getPath(), downLoadBean);
            hdDownloader.setDownloadListener(new HdDownloadListener() {
                @Override
                public void handleProcess(final long start, final long end, final String url) {
                    final DownLoadBean downLoadBean1 = downLoaddingQuery.get(url).getDownLoadBean();
                    if (downLoadBean1!=null){
                        downLoadBean1.setProgress(start);
                        downLoadBean1.setTotal(end);
                        //更新数据库
                        tasksManagerDBController.update(downLoadBean1);
                    }
                    if (start == end) {
                        downLoadBean1.setTaskStatus(DownLoadStatus.DOWNLOAD_FINISH);
                        tasksManagerDBController.update(downLoadBean1);
                        statusChange(DownLoadStatus.DOWNLOAD_FINISH,downLoadBean1);
                        startUnzip(downLoadBean1);
                        downLoaddingQuery.remove(url);
                        //自动下载下一个
                        if (downLoadQuery.size() > 0) {
                            DownLoadBean downLoadBean2 = downLoadQuery.pollFirst();
                            if (downLoadBean2 != null) {
                                startDownload(downLoadBean2);
                            }
                        }
                    }
                    if (downLoadTaskListener!=null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                downLoadTaskListener.onProcess(downLoadBean1);
                            }
                        });
                }

                @Override
                public void handleException(HdHuodeException e, final int i, final String url) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (i == 300) {//下载失败
                                HdDwonLoadUtils hdDwonLoadUtils = downLoaddingQuery.get(url);
                                if (hdDwonLoadUtils!=null){
                                    final DownLoadBean downLoadBean1 = hdDwonLoadUtils.getDownLoadBean();
                                    downLoadBean1.setTaskStatus(DownLoadStatus.DOWNLOAD_ERROR);
                                    tasksManagerDBController.update(downLoadBean1);
                                    if (downLoadTaskListener!=null)
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                downLoadTaskListener.error(3,url);
                                            }
                                        });
                                }
                            }

                        }
                    });
                }

                @Override
                public void handleStatus(final String url, final int status) {
                    if (status == 200) {//下载中
                        final DownLoadBean downLoadBean1 = downLoaddingQuery.get(url).getDownLoadBean();
                        downLoadBean1.setTaskStatus(DownLoadStatus.DOWNLOADING);
                        tasksManagerDBController.update(downLoadBean1);
                        statusChange(DownLoadStatus.DOWNLOADING,downLoadBean1);
                    }
                }

                @Override
                public void handleCancel(String url) {

                }
            });
            hdDownloader.setReconnectLimit(1);
            hdDownloader.start();
            downLoaddingQuery.put(downLoadBean.getUrl(),hdDownloader);
        } else {
            downLoadBean.setTaskStatus(DownLoadStatus.DOWNLOADING);
            tasksManagerDBController.update(downLoadBean);
            statusChange(DownLoadStatus.DOWNLOAD_WAIT,downLoadBean);
            //加到缓存队列
            downLoadQuery.add(downLoadBean);
        }
    }
    /**
     * 开始解压
     * @param downLoadBean
     */
    private void startUnzip(final DownLoadBean downLoadBean) {
        if (isUnZipping.get()){
            downLoadBean.setTaskStatus(DownLoadStatus.ZIP_WAIT);
            tasksManagerDBController.update(downLoadBean);
            statusChange(DownLoadStatus.ZIP_WAIT,downLoadBean);
            unZipquery.add(downLoadBean);
        }else{
            downLoadBean.setTaskStatus(DownLoadStatus.ZIPING);
            tasksManagerDBController.update(downLoadBean);
            statusChange(DownLoadStatus.ZIPING,downLoadBean);
            File ccrFile = new File(downLoadBean.getPath());
            unZiper = new UnZiper(new UnZiper.UnZipListener() {
                @Override
                public void onError(int errorCode, String message) {
                    downLoadBean.setTaskStatus(DownLoadStatus.ZIP_ERROR);
                    tasksManagerDBController.update(downLoadBean);
                    isUnZipping.set(false);
                    if (unZipquery.size()>0){
                        startUnzip(unZipquery.pollFirst());
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (downLoadTaskListener != null) {
                                downLoadTaskListener.error(5,downLoadBean.getUrl());
                            }
                        }
                    });
                }

                @Override
                public void onUnZipFinish() {
                    downLoadBean.setTaskStatus(DownLoadStatus.ZIP_FINISH);
                    tasksManagerDBController.update(downLoadBean);
                    isUnZipping.set(false);
                    statusChange(DownLoadStatus.ZIP_FINISH,downLoadBean);
                    startParse(downLoadBean);
                    if (unZipquery.size()>0){
                        startUnzip(unZipquery.pollFirst());
                    }
                }
            }, ccrFile, FileUtil.getUnzipDir(ccrFile));
            isUnZipping.set(true);
            unZiper.unZipFile();
        }
    }
    private void statusChange(final int status,final DownLoadBean downLoadBean){
        if (downLoadTaskListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    downLoadTaskListener.statusChange(status, downLoadBean);
                }
            });
        }
    }
    /**
     * 解析数据
     * @param downLoadBean
     */
    public void startParse(final DownLoadBean downLoadBean) {
        String unzipDir = FileUtil.getUnzipDir(new File(downLoadBean.getPath()));
        String json = null;
        try{
            downLoadBean.setTaskStatus(DownLoadStatus.PARSE_START);
            statusChange(DownLoadStatus.PARSE_START,downLoadBean);
            try {
                json = readLocalMetas(unzipDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (json == null) {
                downLoadBean.setTaskStatus(DownLoadStatus.PARSE_FAIL);
                tasksManagerDBController.update(downLoadBean);
                statusChange(DownLoadStatus.PARSE_FAIL,downLoadBean);
                return;
            }
            JSONObject replayInfosJsonObject = null;
            try {
                replayInfosJsonObject = new JSONObject(json);
            } catch (JSONException e) {
                downLoadBean.setTaskStatus(DownLoadStatus.PARSE_FAIL);
                tasksManagerDBController.update(downLoadBean);
                statusChange(DownLoadStatus.PARSE_FAIL,downLoadBean);
                return;
            }
            boolean success = false;
            try {
                success = replayInfosJsonObject.getBoolean("success");
            } catch (JSONException e) {
                e.printStackTrace();
                success = false;
            }
            if (!success){
                downLoadBean.setTaskStatus(DownLoadStatus.PARSE_FAIL);
                statusChange(DownLoadStatus.PARSE_FAIL,downLoadBean);
                return;
            }
            JSONObject datas = null;
            try {
                datas = replayInfosJsonObject.getJSONObject("datas");
            } catch (JSONException e) {
                downLoadBean.setTaskStatus(DownLoadStatus.PARSE_FAIL);
                statusChange(DownLoadStatus.PARSE_FAIL,downLoadBean);
                return;
            }
            try {
                String template = datas.getJSONObject("template").toString();
                writeString(template,unzipDir + "/template.json");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                ReplayLiveInfo replayLiveInfo = new ReplayLiveInfo();
                if (datas.has("startTime")) {
                    replayLiveInfo.setStartTime(datas.getString("startTime"));
                }
                if (datas.has("endTime")) {
                    replayLiveInfo.setEndTime(datas.getString("endTime"));
                }
                String liveinfo = JSON.toJSONString(replayLiveInfo);
                writeString(liveinfo,unzipDir + "/liveinfo.json");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                String room = datas.getJSONObject("room").toString();
                writeString(room,unzipDir + "/room.json");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject metas = null;
            try {
                metas = datas.getJSONObject("meta");
                if (metas.has("pageChange")){
                    JSONArray pageChange = metas.getJSONArray("pageChange");
                    writeString(pageChange.toString(),unzipDir + "/pageChange.json");
                }
                if (metas.has("draw")){
                    JSONArray draw = metas.getJSONArray("draw");
                    writeString(draw.toString(),unzipDir + "/draw.json");
                }
                if (metas.has("question")){
                    JSONArray question = metas.getJSONArray("question");
                    writeString(question.toString(),unzipDir + "/question.json");
                }
                if (metas.has("answer")){
                    JSONArray answer = metas.getJSONArray("answer");
                    writeString(answer.toString(),unzipDir + "/answer.json");
                }
                if (metas.has("chatLog")){
                    JSONArray chatLog = metas.getJSONArray("chatLog");
                    writeString(chatLog.toString(),unzipDir + "/chatLog.json");
                }
                if (metas.has("broadcast")){
                    JSONArray broadcast = metas.getJSONArray("broadcast");
                    writeString(broadcast.toString(),unzipDir + "/broadcast.json");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            downLoadBean.setTaskStatus(DownLoadStatus.PARSE_SUCCESS);
            tasksManagerDBController.update(downLoadBean);
            statusChange(DownLoadStatus.PARSE_SUCCESS,downLoadBean);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void writeString(String content,String file) throws IOException {
        FileWriter fileWriter = new FileWriter(new File(file));
        fileWriter.write(content);
        fileWriter.flush();
        fileWriter.close();
    }
    private String readLocalMetas(String dir) throws IOException {
        StringBuilder sb = new StringBuilder();
        File file = new File(dir + "/meta.json");
        FileInputStream in = null;
        InputStreamReader inReader = null;
        BufferedReader bufReader = null;
        try {
            in = new FileInputStream(file);
            inReader = new InputStreamReader(in, "UTF-8");
            bufReader = new BufferedReader(inReader, 5 * 1024 * 1024);
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                sb.append(line);
                //sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            if (inReader != null) {
                inReader.close();
            }
            if (bufReader != null) {
                bufReader.close();
            }
        }
        return sb.toString();
    }
    /**
     * 下载新的数据
     * @param downLoadBean
     */
    public void start(final DownLoadBean downLoadBean) {
        ThreadPoolUtils.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final int b = tasksManagerDBController.addTask(downLoadBean);
                if (b==0){
                    if (downLoadTaskListener!=null){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                downLoadTaskListener.addDateSuccess(downLoadBean);
                            }
                        });
                    }
                    startDownload(downLoadBean);
                }else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (downLoadTaskListener!=null){
                                if (b == -1|| b ==-3){
                                    downLoadTaskListener.error(3,downLoadBean.getUrl());
                                }else{
                                    downLoadTaskListener.error(12,downLoadBean.getUrl());
                                }

                            }

                        }
                    });

                }
                ThreadPoolUtils.getInstance().cancel(this);
            }
        });
    }
    public void reDownload(final DownLoadBean downLoadBean){
        ThreadPoolUtils.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                //重新去下载
//                final int b = tasksManagerDBController.update(downLoadBean);
                startDownload(downLoadBean);
                ThreadPoolUtils.getInstance().cancel(this);
            }
        });
    }
    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public  void recursionDeleteFile(File file) {
        if(file.getAbsoluteFile().exists()) {
            @SuppressWarnings("unused")
            File[] f = file.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    File currFile = new File(dir,name);
                    if(currFile.isDirectory()) {
                        recursionDeleteFile(currFile.getAbsoluteFile());
                    }else {
                        currFile.delete();
                    }
                    currFile.delete();
                    return false;
                }
            });
        }
        file.delete();
//        if (file.isFile()) {
//            file.delete();
//            return;
//        }
//        if (file.isDirectory()) {
//            File[] childFile = file.listFiles();
//            if (childFile == null || childFile.length == 0) {
//                file.delete();
//                return;
//            }
//            for (File f : childFile) {
//                recursionDeleteFile(f);
//            }
//        }
    }
    /**
     * 移除本地的所有数据 ----- 就是彻底删除
     */
    public void delete(final DownLoadBean downLoadBean) {
        ThreadPoolUtils.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final int i = tasksManagerDBController.removeTask(downLoadBean.getUrl());
                if (downLoaddingQuery!=null&&downLoaddingQuery.get(downLoadBean.getUrl())!=null){
                    downLoaddingQuery.get(downLoadBean.getUrl()).cancel();
                    downLoaddingQuery.remove(downLoadBean.getUrl());
                }
                File unzipDir = new File(downLoadBean.getPath().replace(".ccr",""));
                recursionDeleteFile(unzipDir);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (downLoadTaskListener != null) {
                            if (i <= 0){
                                downLoadTaskListener.error(2,downLoadBean.getUrl());
                            }
                        }
                    }
                });
                ThreadPoolUtils.getInstance().cancel(this);
            }
        });
    }
    /**
     * 暂停
     * @param downLoadBean
     */
    public void pause(DownLoadBean downLoadBean){
        HdDwonLoadUtils hdDwonLoadUtils = downLoaddingQuery.get(downLoadBean.getUrl());
        if (hdDwonLoadUtils!=null){
            hdDwonLoadUtils.cancel();
            downLoadBean.setTaskStatus(DownLoadStatus.DOWNLOAD_PAUSE);
            statusChange(DownLoadStatus.DOWNLOAD_PAUSE,downLoadBean);
        }else{
            if (downLoadTaskListener!=null)
                downLoadTaskListener.error(4,downLoadBean.getUrl());
        }
    }


    /**
     * 释放资源
     */
    public void destroy() {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        ThreadPoolUtils.getInstance().destroy();
        tasksManagerDBController.onDestroy();
        if (unZiper!=null){
            unZiper.unzipThread.interrupt();
        }
        if (downLoaddingQuery!=null){
            for(Map.Entry<String, HdDwonLoadUtils> entry : downLoaddingQuery.entrySet()){
                HdDwonLoadUtils mapValue = entry.getValue();
                mapValue.cancel();
            }
        }
    }
}
