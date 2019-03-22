package com.bokecc.mobile.localreplay.util.download;


import com.bokecc.sdk.mobile.live.Exception.DWLiveException;

/**
 * 
 * 下载状态监听接口
 * 
 * @author CC视频
 * 
 * **/
public interface DownloadListener {

	/**
	 * 下载过程中会回调该方法通知文件的总长度
	 *
	 * @param videoLength
	 *            文件大小
	 * @param tag
	 *            视频标识，表示返回的start和end对应的是哪个下载任务的
	 */
	public void handleVideoLength(long videoLength, String tag);

	/**
	 * 下载过程中会回调该方法，获取已下载的字节数和文件总字节数，可以重写该方法对控件进行刷新
	 * 
	 * @param start
	 *            下载的起始位置
	 * @param end
	 *            下载的终止位置
	 * @param tag
	 *            视频标识，表示返回的start和end对应的是哪个下载任务的
	 */
	public void handleProcess(long start, long end, String tag);

	/**
	 * 
	 * 下载出现异常时会回调此方法
	 * 
	 * @param exception
	 *            异常对象，调用getMessage()方法得到的message值为如下的三个值中的一个：
	 *                1. "下载失败，ErrorCode:INVALID_REQUEST" 
	 *                2. "下载失败，ErrorCode:NETWORK_ERROR"
	 *                3. "下载失败，ErrorCode:PROCESS_FAIL"
	 * @param status
	 *            当前下载状态
	 */
	public void handleException(DWLiveException exception, int status);

	/**
	 * 下载状态切换时会回调此方法
	 * 
	 * @param tag
	 *            视频ID，表示哪个任务状态发生改变
	 * @param status
	 *            当前下载状态
	 * */
	public void handleStatus(String tag, int status);

	/**
	 * 取消下载会回调该方法
	 * 
	 * @param tag
	 *            视频ID，表示删除的是哪个下载任务
	 */
	public void handleCancel(String tag);
	
}
