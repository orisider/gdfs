package com.orisider.gdfs.util;

public interface Constant {

    /**
     * DRIVE_OPEN Intent action.
     */
    String ACTION_DRIVE_OPEN = "com.google.android.apps.drive.DRIVE_OPEN";
    String ACTION_DRIVE_UPLOAD ="com.orisider.gdfs.upload";
    String ACTION_NOTI_OPEN ="com.orisider.gdfs.noti_open";

    String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/drive";

    String LOG_TAG = "goog_drive_open";

    String BUNDLE_KEY_ACCESS_TOKEN="acc_token";
    int REQ_CODE_ACNT = 1;
    String FRAG_TAG_DIALOG = "frag_diag";
    String FILE_URI ="file_uri";

    String BUNDLE_KEY_FILE_NAME = "file_name";
    String BUNDLE_KEY_FILE_SHARE_URL = "file_share_url" ;
	String MIME_TYPE_DRIVE_FOLDER = "application/vnd.google-apps.folder";
	String PARENT_ID_ROOT = "root";

    String PKG_GDRIVE = "com.google.android.apps.docs";
}
