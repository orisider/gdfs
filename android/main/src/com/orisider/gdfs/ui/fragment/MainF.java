package com.orisider.gdfs.ui.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.orisider.gdfs.R;
import com.orisider.gdfs.model.AccessToken;
import com.orisider.gdfs.ui.activity.GetAccountActivity;
import com.orisider.gdfs.ui.fragment.diag.AboutFragment;
import com.orisider.gdfs.util.Constant;
import com.orisider.gdfs.util.SessionStore;
import roboguice.inject.InjectView;

public class MainF extends RoboSherlockFragment implements View.OnClickListener {

	@InjectView(R.id.saved_acnt_name)
	TextView savedAcntName;

	@InjectView(R.id.set_acnt_btn)
	Button acntBtn;

	@InjectView(R.id.launch_gdrive_btn)
	Button launchGdriveBtn;

	@InjectView(R.id.about_btn)
	Button aboutBtn;

	AccessToken token;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (token == null) {
			token = SessionStore.getAccountAuthToken();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.f_main, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		updatePanel();
		initView();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constant.REQ_CODE_ACNT && resultCode == Activity.RESULT_OK) {
			token = (AccessToken) data.getSerializableExtra(Constant.BUNDLE_KEY_ACCESS_TOKEN);
			updatePanel();
		}
	}

	private void initView() {
		acntBtn.setOnClickListener(this);
		launchGdriveBtn.setOnClickListener(this);
		aboutBtn.setOnClickListener(this);
	}

	private void updatePanel() {
		if (token == null) {
			acntBtn.setText(R.string.btn_account_set);
			acntBtn.setTag(true);
			savedAcntName.setText(R.string.acnt_empty);
		} else {
			acntBtn.setText(R.string.btn_account_del);
			acntBtn.setTag(false);
			savedAcntName.setText(token.accountName);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == acntBtn) {
			if (acntBtn.getTag() == Boolean.TRUE) {
				startActivityForResult(new Intent(getActivity(), GetAccountActivity.class), Constant.REQ_CODE_ACNT);
			} else {
				SessionStore.removeAccessToken();
				token = null;
				updatePanel();
			}

		} else if (v == launchGdriveBtn) {
			//TODO check gdrive installed
			startActivity(new Intent().setComponent(new ComponentName("com.google.android.apps.docs", "com.google.android.apps.docs.app.NewMainProxyActivity")));
		} else if (v == aboutBtn) {
			new AboutFragment().show(getActivity().getSupportFragmentManager(), Constant.FRAG_TAG_DIALOG);
		}
	}
}
