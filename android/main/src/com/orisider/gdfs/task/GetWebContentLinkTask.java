package com.orisider.gdfs.task;

import android.content.Context;
import android.util.Log;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.orisider.gdfs.GDFSApp;
import com.orisider.gdfs.util.Constant;
import roboguice.util.RoboAsyncTask;

import java.io.IOException;

public class GetWebContentLinkTask extends RoboAsyncTask<File> {

    private final Drive drive;
    private final String fileId;

    public GetWebContentLinkTask(Context ctx, Drive drive, String fileId) {
        super(ctx, GDFSApp.handler);
        this.drive = drive;
        this.fileId = fileId;
    }

    @Override
    public File call() throws Exception {
        PermissionList list = drive.permissions().list(fileId).execute();
        for (Object v : list.values()) {
            Log.d(Constant.LOG_TAG, "perm item:" + v);
        }

        insertAnyoneReadPermission();

        return drive.files().get(fileId).execute();
    }

    private void insertAnyoneReadPermission() throws IOException {
        Permission anyoneReadPerm = new Permission();
        anyoneReadPerm.setType("anyone");
        anyoneReadPerm.setRole("reader");
        drive.permissions().insert(fileId, anyoneReadPerm).execute();
    }
}
