package com.orisider.gdfs.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ParentReference;
import com.orisider.gdfs.R;
import com.orisider.gdfs.model.AccessToken;
import com.orisider.gdfs.task.GetWebContentLinkTask;
import com.orisider.gdfs.util.Constant;
import com.orisider.gdfs.util.SessionStore;
import com.orisider.gdfs.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UploadFile extends IntentService {

	private static final String SERVICE_NAME = "upload_service";
	private static final String NOTI_TAG = "noti_tag";
	private static final AtomicInteger notiKey = new AtomicInteger(1);
	private static final String BUNDLE_KEY_TOAST_MSG = "toast_msg";

	public UploadFile() {
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Uri fileUri = (Uri) intent.getParcelableExtra(Constant.FILE_URI);
		File file = null;
		if (fileUri != null) {
			try {
				file = new File(new URI(fileUri.toString()));
				if (!file.exists()) {
					file = null;
				}
			} catch (URISyntaxException e) {
				file = null;
			}
		}
		AccessToken token = SessionStore.getAccountAuthToken();

		if (token == null) {
			showToast(R.string.warn_no_goog_account);
		} else if (file == null) {
			showToast(R.string.warn_no_file_found);
		} else {
			processUpload(file, token);
		}
	}

	private void processUpload(File file, AccessToken token) {
		Drive gDrive = Util.newGDrive(token.accessToken);

		NotificationManager notiMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		int notiId = notiKey.incrementAndGet();

		try {
			registerUploadNoti(file, notiMgr, notiId);

			String fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
					MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath()));

			com.google.api.services.drive.model.File uploadedFile = gDrive.files().insert(generateUploadFileInfo
					(file, gDrive, fileMimeType), new FileContent(fileMimeType, file)).execute();

			showToast(R.string.noti_title_upload_finish);

			try {
				uploadedFile = new GetWebContentLinkTask(getApplicationContext(), gDrive, uploadedFile.getId())
						.call();

				registerUploadDoneNoti(notiMgr, notiId, uploadedFile);
			} catch (Exception e) {
				showToast(R.string.toast_share_url_fail);
			}
		} catch (IOException e) {
			notiMgr.cancel(NOTI_TAG, notiId);
			Log.w(Constant.LOG_TAG, "failed to upload file", e);
			showToast(R.string.toast_upload_fail);
		}
	}

	private void registerUploadDoneNoti(NotificationManager notiMgr, int notiId, com.google.api.services.drive.model
			.File uploadedFile) {
		Intent notiIntent = new Intent(Constant.ACTION_NOTI_OPEN);
		notiIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		notiIntent.putExtra(Constant.BUNDLE_KEY_FILE_NAME, uploadedFile.getTitle())
				.putExtra(Constant.BUNDLE_KEY_FILE_SHARE_URL, uploadedFile.getWebContentLink());

		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notiIntent,
				PendingIntent.FLAG_ONE_SHOT);


		notiMgr.notify(NOTI_TAG, notiId, Util.hasHoneycomb() ? NotiMakerPostHoneycomb.makeUploadFinishNoti
				(getApplicationContext(), uploadedFile, contentIntent) :
				NotiMakerPreHoneycomb.makeUploadFinishNoti(getApplicationContext(), uploadedFile, contentIntent));
	}


	private static class NotiMakerPreHoneycomb {
		@SuppressWarnings("deprecation")
		static Notification makeUploadNoti(Context ctx, File file) {
			Notification noti = new Notification(R.drawable.noti_icon, ctx.getResources().getString(R.string
					.noti_ticker_upload_start), System.currentTimeMillis());
			noti.setLatestEventInfo(ctx, ctx.getString(R.string
					.noti_title_upload), file.getName(), PendingIntent.getActivity(ctx, 0, new Intent(), 0));
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			return noti;
		}

		@SuppressWarnings("deprecation")
		static Notification makeUploadFinishNoti(Context ctx, com.google.api.services.drive.model.File uploadedFile,
												 PendingIntent contentIntent) {
			Notification noti = new Notification(R.drawable.noti_icon, ctx.getResources().getString(R.string
					.noti_ticker_upload_finish), System.currentTimeMillis());
			noti.setLatestEventInfo(ctx, ctx.getString(R.string.noti_title_upload_finish),
					uploadedFile.getWebContentLink(), contentIntent);
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			return noti;
		}

	}

	private static class NotiMakerPostHoneycomb {
		static Notification makeUploadNoti(Context ctx, File file) {
			return new Notification.Builder(ctx)
					.setSmallIcon(R.drawable.noti_icon)
					.setContentTitle(ctx.getResources().getString(R.string
							.noti_title_upload))
					.setContentText(file.getName())
					.setProgress(100, 0, true)
					.setTicker(ctx.getResources().getString(R.string
							.noti_ticker_upload_start))
					.getNotification();
		}

		static Notification makeUploadFinishNoti(Context ctx, com.google.api.services.drive.model.File uploadedFile,
												 PendingIntent contentIntent) {
			return new Notification.Builder(ctx)
					.setSmallIcon(R.drawable.noti_icon)
					.setAutoCancel(true)
					.setContentTitle(ctx.getString(R.string.noti_title_upload_finish))
					.setTicker(ctx.getResources().getString(R.string
							.noti_ticker_upload_finish))
					.setContentText(
							uploadedFile.getWebContentLink()).setContentIntent(contentIntent).getNotification();
		}

	}


	private void registerUploadNoti(File file, NotificationManager notiMgr, int notiId) {
		Notification noti = Util.hasHoneycomb() ? NotiMakerPostHoneycomb.makeUploadNoti(getApplicationContext(), file) :
				NotiMakerPreHoneycomb.makeUploadNoti(getApplicationContext(), file);
		notiMgr.notify(NOTI_TAG, notiId, noti);
	}

	private com.google.api.services.drive.model.File generateUploadFileInfo(File file, Drive gDrive,
																			String fileMimeType) throws IOException {
		com.google.api.services.drive.model.File gdFile = new com.google.api.services.drive.model.File();
		gdFile.setTitle(file.getName());
		gdFile.setDescription("GDFS uploaded file");
		gdFile.setMimeType(fileMimeType);
		setParent(gdFile, createOrGetGdfsFolder(gDrive));
		return gdFile;
	}

	private String createOrGetGdfsFolder(Drive gDrive) throws IOException {
		Drive.Files.List files = gDrive.files().list().setQ("title = 'gdfs' and mimeType = '" + Constant
				.MIME_TYPE_DRIVE_FOLDER + "'");
		List<com.google.api.services.drive.model.File> fileList = files.execute().getItems();
		if (fileList.isEmpty()) {
			//create gdfs folder
			com.google.api.services.drive.model.File gdfsFolderFile = new com.google.api.services.drive.model.File();
			gdfsFolderFile.setTitle("gdfs");
			gdfsFolderFile.setDescription("GDFS upload folder");
			gdfsFolderFile.setMimeType(Constant.MIME_TYPE_DRIVE_FOLDER);
			setParent(gdfsFolderFile, Constant.PARENT_ID_ROOT);

			return gDrive.files().insert(gdfsFolderFile).execute().getId();
		} else {
			return fileList.get(0).getId();
		}
	}

	private void showToast(final int msgId) {
		Message msg = handler.obtainMessage();
		Bundle bdl = new Bundle();
		bdl.putSerializable(BUNDLE_KEY_TOAST_MSG, "GDFS: " + getApplicationContext().getResources().getString(msgId));
		msg.setData(bdl);
		handler.sendMessage(msg);
	}

	private void setParent(com.google.api.services.drive.model.File file, String parentId) {
		List<ParentReference> parents = new ArrayList<ParentReference>();
		ParentReference rootRef = new ParentReference();
		rootRef.setId(parentId);
		parents.add(rootRef);
		file.setParents(parents);
	}

	private final Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			Util.showToast((String) msg.getData().getSerializable(BUNDLE_KEY_TOAST_MSG));
			return true;
		}
	});


}
