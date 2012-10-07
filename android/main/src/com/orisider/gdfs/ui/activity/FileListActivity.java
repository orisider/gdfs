package com.orisider.gdfs.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.ipaulpro.afilechooser.FileChooserFragment;
import com.orisider.gdfs.R;
import com.orisider.gdfs.ui.event.GDFSDialogConfirm;
import com.orisider.gdfs.ui.fragment.ConfirmFragment;
import com.orisider.gdfs.util.Constant;
import roboguice.event.Observes;

import java.io.File;

public class FileListActivity extends RoboSherlockFragmentActivity implements FileChooserFragment.FileChooserListener {

    private static final String FRAG_TAG_BASE = "frag_base";
    FileChooserFragment f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);

        if (f == null) {
            f = (FileChooserFragment) getSupportFragmentManager().findFragmentByTag(FRAG_TAG_BASE);
            if (f == null) {
                f = new FileChooserFragment();
            }
        }
        if (savedInstanceState == null) {
            initFragment(f);
        }

        f.setListener(this);
    }

    protected void initFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction().add(R.id.base_content, f, FRAG_TAG_BASE).commit();
    }

    @Override
    public void onBackPressed() {
        if (f != null) {
            f.updateBreadcrumb(false);
        } else {
            finish();
        }
    }

    @Override
    public void onUpRequestedOnRoot() {
        finish();
    }

    @Override
    public void onFilePathChanged(String newFilePath) {
        setTitle(newFilePath);
    }

    @Override
    public void onMediaUnmounted() {
        finish();
    }

    File selectedFile = null;

    @Override
    public void onFileSelected(File file) {
        selectedFile = file;
        ConfirmFragment.newInstance("will you upload & share file " + file.getName() + "?").show
                (getSupportFragmentManager(),
                Constant.FRAG_TAG_DIALOG);
    }

    private void onConfirmed(@Observes GDFSDialogConfirm confirmResult) {
        if( confirmResult.confirm) {
            startService( new Intent().setAction(Constant.ACTION_DRIVE_UPLOAD).putExtra(Constant.FILE_URI
            , Uri.fromFile(selectedFile)));
        } else {

        }
    }
}
