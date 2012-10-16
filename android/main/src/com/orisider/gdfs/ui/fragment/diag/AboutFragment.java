package com.orisider.gdfs.ui.fragment.diag;

import android.app.Dialog;
import android.os.Bundle;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockDialogFragment;
import com.orisider.gdfs.R;

public class AboutFragment extends RoboSherlockDialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog diag = new Dialog(getActivity());
		diag.setTitle(R.string.about_title);
		diag.setContentView(R.layout.f_about);
        diag.setCanceledOnTouchOutside(true);
		return diag;
	}
}
