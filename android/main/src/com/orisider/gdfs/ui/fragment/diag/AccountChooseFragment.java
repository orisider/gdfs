package com.orisider.gdfs.ui.fragment.diag;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockDialogFragment;
import com.google.inject.Inject;
import com.orisider.gdfs.R;
import com.orisider.gdfs.ui.event.DialogCanceledEvent;
import com.orisider.gdfs.ui.event.ItemSelectedEvent;
import roboguice.event.EventManager;

public class AccountChooseFragment extends RoboSherlockDialogFragment implements DialogInterface.OnClickListener {

	@Inject
	EventManager evManager;

	String[] accounts;

	private static final String BUNDLE_KEY_ACCOUNTS = "accounts";

	public static AccountChooseFragment newInstance(String[] accountNames) {
		AccountChooseFragment f = new AccountChooseFragment();
		Bundle bdl = new Bundle();
		bdl.putStringArray(BUNDLE_KEY_ACCOUNTS, accountNames);
		f.setArguments(bdl);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accounts = getArguments().getStringArray(BUNDLE_KEY_ACCOUNTS);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setItems(accounts, this);
		alertDialogBuilder.setOnCancelListener(this);
		alertDialogBuilder.setTitle(R.string.title_select_goog_account);
		return alertDialogBuilder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		evManager.fire(new ItemSelectedEvent(Integer.toString(which)));
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		evManager.fire(new DialogCanceledEvent());
	}
}
