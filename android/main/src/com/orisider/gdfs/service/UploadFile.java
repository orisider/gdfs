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
        if( fileUri != null) {
            try {
                file = new File(new URI(fileUri.toString()));
                if( !file.exists()) {
                    file = null;
                }
            } catch (URISyntaxException e) {
                file = null;
            }
        }
        AccessToken token = SessionStore.getAccountAuthToken();


        if( token == null) {
            showAlert("user not authenticated");
        } else if( file == null) {
            showAlert("file not found");
        } else {
            com.google.api.services.drive.model.File gdFile = new com.google.api.services.drive.model.File();
            gdFile.setTitle(file.getName());
            gdFile.setDescription("GDFS uploaded file");
            String fileMimeType= MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath()));
            gdFile.setMimeType( fileMimeType);


            int notiId= notiKey.incrementAndGet();
            FileContent content = new FileContent(fileMimeType, file);
            NotificationManager notiMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            try {


                notiMgr.notify(NOTI_TAG, notiId,
                        new Notification.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.icon)
                                .setContentText("uploading " + file.getName())
                                .getNotification());


                Drive gDrive = Util.newGDrive(token.accessToken);
                com.google.api.services.drive.model.File uploadedFile = gDrive.files().insert(gdFile, content).execute();



                showAlert("file uploaded!");


                try {
                    uploadedFile = new GetWebContentLinkTask(getApplicationContext(), gDrive, uploadedFile.getId() )
                                .call();

                    Intent notiIntent = new Intent( Constant.ACTION_NOTI_OPEN);
                    notiIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    notiIntent.putExtra(Constant.BUNDLE_KEY_FILE_NAME, uploadedFile.getTitle())
                    .putExtra(Constant.BUNDLE_KEY_FILE_SHARE_URL, uploadedFile.getWebContentLink());

                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notiIntent, PendingIntent.FLAG_ONE_SHOT);

                    Notification noti = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.icon)
                            .setAutoCancel(true)
                            .setContentText("upload finished. url: "
                            + uploadedFile.getWebContentLink()).setContentIntent(contentIntent).getNotification();

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

    private void showAlert(final String msg) {
        GDFSApp.handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "failed to upload file: "+msg,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
