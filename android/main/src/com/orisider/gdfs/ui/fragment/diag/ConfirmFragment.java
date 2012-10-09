package com.orisider.gdfs.ui.fragment.diag;

import android.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockDialogFragment;
import com.google.inject.Inject;
import com.orisider.gdfs.ui.event.DialogConfirmEvent;
import roboguice.event.EventManager;

public class ConfirmFragment extends RoboSherlockDialogFragment {

    private static final String ARG_MESSAGE = "message";

    @Inject
    EventManager evManager;

    public static ConfirmFragment newInstance(String message) {
        ConfirmFragment f = new ConfirmFragment();
        Bundle bdl = new Bundle();
        bdl.putString(ARG_MESSAGE, message);
        f.setArguments(bdl);
        return f;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity()).setMessage(getArguments().getString(ARG_MESSAGE))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        evManager.fire(new DialogConfirmEvent(true));
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        evManager.fire(new DialogConfirmEvent(false));
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        evManager.fire(new DialogConfirmEvent(false));
                    }
                }).create();
    }
}
