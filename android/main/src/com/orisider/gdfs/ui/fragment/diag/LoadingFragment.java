package com.orisider.gdfs.ui.fragment.diag;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.orisider.gdfs.R;

public class LoadingFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setMessage(getActivity().getString(R.string.loading));
        dialog.setIndeterminate(true);
		dialog.setCancelable(false);
        return dialog;
    }
}
