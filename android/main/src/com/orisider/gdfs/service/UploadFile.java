package com.orisider.gdfs.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.orisider.gdfs.GDFSApp;
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
			showAlert("user not authenticated");
		} else if (file == null) {
			showAlert("file not found");
		} else {
			Drive gDrive = Util.newGDrive(token.accessToken);

			NotificationManager notiMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			int notiId = notiKey.incrementAndGet();

			try {
				notiMgr.notify(NOTI_TAG, notiId,
						new Notification.Builder(getApplicationContext())
								.setSmallIcon(R.drawable.icon)
								.setContentTitle("Uploading file to Google Drive")
								.setContentText(file.getName())
								.setProgress(100, 0, true)
								.getNotification());

				String fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
						MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath()));

				com.google.api.services.drive.model.File uploadedFile = gDrive.files().insert(generateUploadFileInfo(file, gDrive, fileMimeType), new FileContent(fileMimeType, file)).execute();

				showAlert("file uploaded!");

				try {
					uploadedFile = new GetWebContentLinkTask(getApplicationContext(), gDrive, uploadedFile.getId())
							.call();

					Intent notiIntent = new Intent(Constant.ACTION_NOTI_OPEN);
					notiIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					notiIntent.putExtra(Constant.BUNDLE_KEY_FILE_NAME, uploadedFile.getTitle())
							.putExtra(Constant.BUNDLE_KEY_FILE_SHARE_URL, uploadedFile.getWebContentLink());

					PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notiIntent, PendingIntent.FLAG_ONE_SHOT);

					Notification noti = new Notification.Builder(getApplicationContext())
							.setSmallIcon(R.drawable.icon)
							.setAutoCancel(true)
							.setContentTitle("Upload finished")
							.setContentText(
									uploadedFile.getWebContentLink()).setContentIntent(contentIntent).getNotification();

					notiMgr.notify(NOTI_TAG, notiId, noti);
				} catch (Exception e) {
					showAlert("failed to get sharing url");
				}
			} catch (IOException e) {
				notiMgr.cancel(NOTI_TAG, notiId);
				Log.w(Constant.LOG_TAG, "failed to upload file", e);
				showAlert("failed to upload file");
			}
		}
	}

	private com.google.api.services.drive.model.File generateUploadFileInfo(File file, Drive gDrive, String fileMimeType) throws IOException {
		com.google.api.services.drive.model.File gdFile = new com.google.api.services.drive.model.File();
		gdFile.setTitle(file.getName());
		gdFile.setDescription("GDFS uploaded file");
		gdFile.setMimeType(fileMimeType);
		setParent(gdFile, createOrGetGdfsFolder(gDrive));
		return gdFile;
	}

	private String createOrGetGdfsFolder(Drive gDrive) throws IOException {
		Drive.Files.List files = gDrive.files().list().setQ("title = 'gdfs' and mimeType = '" + Constant.MIME_TYPE_DRIVE_FOLDER + "'");
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

	private void showAlert(final String msg) {
		GDFSApp.handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "failed to upload file: " + msg,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void setParent(com.google.api.services.drive.model.File file, String parentId) {
		List<ParentReference> parents = new ArrayList<ParentReference>();
		ParentReference rootRef = new ParentReference();
		rootRef.setId(parentId);
		parents.add(rootRef);
		file.setParents(parents);
	}
}
