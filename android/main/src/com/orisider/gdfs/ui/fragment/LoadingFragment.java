package com.orisider.gdfs.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class LoadingFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog diag = new ProgressDialog(getActivity());
        diag.setIndeterminate(true);
        return diag;
    }
}
