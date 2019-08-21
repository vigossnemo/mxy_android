package com.gurunzhixun.watermeter;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.gurunzhixun.watermeter.util.APPUtil;
import com.gurunzhixun.watermeter.util.Constant;
import com.gurunzhixun.watermeter.util.SPUtil;
import com.gurunzhixun.watermeter.util.ToolKit;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class DownLoadService extends Service {


	public static final int HANDLE_DOWNLOAD = 0x001;
	public static final String BUNDLE_KEY_DOWNLOAD_URL = "url";
	public static final float UNBIND_SERVICE = 1.0F;

	private Activity activity;
	private DownloadBinder binder;
	private DownloadManager downloadManager;
	private DownloadChangeObserver downloadObserver;
	//private BroadcastReceiver downLoadBroadcast;
	private ScheduledExecutorService scheduledExecutorService;


	private String downloadUrl;
	private OnProgressListener onProgressListener;

	private CompleteReceiver receiver;
	private String apkDoc;
	private String apkName;
	private DownloadManager downManager;
	private long downManagerid = 0;
	public Handler downLoadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (onProgressListener != null && HANDLE_DOWNLOAD == msg.what) {
				//被除数可以为0，除数必须大于0
				if (msg.arg1 >= 0 && msg.arg2 > 0) {
					onProgressListener.onProgress(msg.arg1 / (float) msg.arg2);
				}
			}
		}
	};

	private Runnable progressRunnable = new Runnable() {
		@Override
		public void run() {
			updateProgress();
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		binder = new DownloadBinder();
		apkDoc= ToolKit.getPackageName(this)+"/";
		apkName=ToolKit.getAppName(this)+ToolKit.currentAPKVersionCode(this)+".apk";
	}

	@Override
	public IBinder onBind(Intent intent) {
		downloadUrl = intent.getStringExtra(BUNDLE_KEY_DOWNLOAD_URL);
		downloadApk(downloadUrl);
		return binder;
	}

	/**
	 * 下载最新APK
	 */
	private void downloadApk(String url) {
		downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		downloadObserver = new DownloadChangeObserver();
		registerContentObserver();
		receiver = new CompleteReceiver();
		// 注册广播
		registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		DownloadManager.Request request = new DownloadManager.Request(
				Uri.parse(url));
		// 设置在什么网络情况下进行下载
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
				| DownloadManager.Request.NETWORK_MOBILE);
		// 设置通知栏标题
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
		request.setTitle(" "+ToolKit.getAppName(this)+" ");//设置通知栏下载标题
		// request.setDescription("今日头条正在下载bt1");
		request.setAllowedOverRoaming(false);
		// 设置文件存放目录
		request.setDestinationInExternalPublicDir(apkDoc, apkName);
		downManagerid = downloadManager.enqueue(request);
	}
	/**
	 * 广播接收下载完成
	 */
	class CompleteReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
//			isUpdate = true;
			long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			File file = new File(Environment.getExternalStorageDirectory(),apkDoc+apkName);
			installApk(file);
			if(downManagerid ==completeDownloadId){
				downManagerid = 0;
			}
		}
	}
	/**
	 * 安装apk
	 */
	protected void installApk(File file) {
		Intent intent = new Intent();
		// 执行动作
		intent.setAction(Intent.ACTION_VIEW);
		// 执行的数据类型
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");// 编者按：此处Android应为android，否则造成安装不了
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
//	/**
//	 * 注册广播
//	 */
//	private void registerBroadcast() {
//		/**注册service 广播 1.任务完成时 2.进行中的任务被点击*/
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//		intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
//		registerReceiver(downLoadBroadcast = new DownLoadBroadcast(), intentFilter);
//	}

	/**
	 * 注销广播
	 */
	private void unregisterBroadcast() {
		if (receiver != null) {
			unregisterReceiver(receiver);
			receiver = null;
		}
	}

	/**
	 * 注册ContentObserver
	 */
	private void registerContentObserver() {
		/** observer download change **/
		if (downloadObserver != null) {
			getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), false, downloadObserver);
		}
	}

	/**
	 * 注销ContentObserver
	 */
	private void unregisterContentObserver() {
		if (downloadObserver != null) {
			getContentResolver().unregisterContentObserver(downloadObserver);
		}
	}

	/**
	 * 关闭定时器，线程等操作
	 */
	private void close() {
		if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
			scheduledExecutorService.shutdown();
		}

		if (downLoadHandler != null) {
			downLoadHandler.removeCallbacksAndMessages(null);
		}
	}

	/**
	 * 发送Handler消息更新进度和状态
	 */
	private void updateProgress() {
		int[] bytesAndStatus = getBytesAndStatus(downManagerid);
		downLoadHandler.sendMessage(downLoadHandler.obtainMessage(HANDLE_DOWNLOAD, bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]));
	}

	/**
	 * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
	 *
	 * @param downloadId
	 * @return
	 */
	private int[] getBytesAndStatus(long downloadId) {
		int[] bytesAndStatus = new int[]{
				-1, -1, 0
		};
		DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
		Cursor cursor = null;
		try {
			cursor = downloadManager.query(query);
			if (cursor != null && cursor.moveToFirst()) {
				//已经下载文件大小
				bytesAndStatus[0] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
				//下载文件的总大小
				bytesAndStatus[1] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
				//下载状态
				bytesAndStatus[2] = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bytesAndStatus;
	}

	/**
	 * 绑定此DownloadService的Activity实例
	 *
	 * @param activity
	 */
	public void setTargetActivity(Activity activity) {
		this.activity = activity;
	}

	/**
//	 * 接受下载完成广播
//	 */
//	private class DownLoadBroadcast extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//			switch (intent.getAction()) {
//				case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
//					if (downloadId == downId && downId != -1 && downloadManager != null) {
//						Uri downIdUri = downloadManager.getUriForDownloadedFile(downloadId);
//
//						close();
//
//						if (downIdUri != null) {
//							SPUtil.put(Constant.SP_DOWNLOAD_PATH, downIdUri.getPath());
//							APPUtil.installApk(context, downIdUri);
//						}
//						if (onProgressListener != null) {
//							onProgressListener.onProgress(UNBIND_SERVICE);
//						}
//					}
//					break;
//
//				default:
//					break;
//			}
//		}
//	}

	/**
	 * 监听下载进度
	 */
	private class DownloadChangeObserver extends ContentObserver {

		public DownloadChangeObserver() {
			super(downLoadHandler);
			scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		}

		/**
		 * 当所监听的Uri发生改变时，就会回调此方法
		 *
		 * @param selfChange 此值意义不大, 一般情况下该回调值false
		 */
		@Override
		public void onChange(boolean selfChange) {
			scheduledExecutorService.scheduleAtFixedRate(progressRunnable, 0, 2, TimeUnit.SECONDS);
		}
	}

	public class DownloadBinder extends Binder {
		/**
		 * 返回当前服务的实例
		 *
		 * @return
		 */
		public DownLoadService getService() {
			return DownLoadService.this;
		}

	}

	public interface OnProgressListener {
		/**
		 * 下载进度
		 *
		 * @param fraction 已下载/总大小
		 */
		void onProgress(float fraction);
	}

	/**
	 * 对外开发的方法
	 *
	 * @param onProgressListener
	 */
	public void setOnProgressListener(OnProgressListener onProgressListener) {
		this.onProgressListener = onProgressListener;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterBroadcast();
		unregisterContentObserver();
	}

}
