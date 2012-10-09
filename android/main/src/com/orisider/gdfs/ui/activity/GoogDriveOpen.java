package com.orisider.gdfs.ui.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.api.services.drive.model.File;
import com.orisider.gdfs.R;
import com.orisider.gdfs.model.AccessToken;
import com.orisider.gdfs.task.GetWebContentLinkTask;
import com.orisider.gdfs.ui.fragment.diag.LoadingFragment;
import com.orisider.gdfs.util.Constant;
import com.orisider.gdfs.util.SessionStore;
import com.orisider.gdfs.util.Util;
import roboguice.inject.InjectView;

public class GoogDriveOpen extends RoboSherlockFragmentActivity {
	private static final int MENU_ID_SHARE = 1;
	private static final String BUNDLE_KEY_INTENT = "intent";
	/**
	 * Drive file ID key.
	 */
	String EXTRA_FILE_ID = "resourceId";

	AccessToken token;

	@InjectView(R.id.file_name)
	TextView fileName;

	@InjectView(R.id.share_url)
	TextView shareUrl;

	@InjectView(R.id.copy_clipboard_btn)
	Button copyToClipboard;

	Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.a_drive_open);

		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);


		copyToClipboard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardManager mgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				mgr.setPrimaryClip(ClipData.newPlainText("goog_drive_file_url", shareUrl.getText()));
				Util.showToast(R.string.url_copied_to_clipboard);
			}
		});

		if (savedInstanceState == null) {
			intent = getIntent();
		} else {
			intent = savedInstanceState.getParcelable(BUNDLE_KEY_INTENT);
		}

		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		// Make sure the Action is DRIVE_OPEN.
		if (Constant.ACTION_DRIVE_OPEN.equals(intent.getAction())) {
			processActionDriveOpen(intent);
		} else if (Constant.ACTION_NOTI_OPEN.equals(intent.getAction())) {
			processNotiOpen(intent);
		} else {
			//wrong access
			finish();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void processNotiOpen(Intent intent) {
		fileName.setText(intent.getStringExtra(Constant.BUNDLE_KEY_FILE_NAME));
		shareUrl.setText(intent.getStringExtra(Constant.BUNDLE_KEY_FILE_SHARE_URL));
	}

	private void processActionDriveOpen(Intent intent) {
		token = SessionStore.getAccountAuthToken();

		if (token == null) {
			startActivityForResult(new Intent(this, GetAccountActivity.class), Constant.REQ_CODE_ACNT);
		} else {
			processFile(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem shareItem = menu.add(Menu.NONE, MENU_ID_SHARE, 0, R.string.share);
		shareItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		shareItem.setIcon(R.drawable.ic_menu_share_holo_dark);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_ID_SHARE) {
			Intent shareIntent = ShareCompat.IntentBuilder.from(GoogDriveOpen.this)
					.setText(shareUrl.getText().toString())
					.setType("text/plain")
					.getIntent();
			startActivity(shareIntent);
		} else if( item.getItemId() == android.R.id.home) {
			startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constant.REQ_CODE_ACNT && resultCode == Activity.RESULT_OK) {
			token = (AccessToken) data.getSerializableExtra(Constant.BUNDLE_KEY_ACCESS_TOKEN);
			if (token != null) {
				processFile(intent);
			} else {
				finish();
			}
		} else {
			finish();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(BUNDLE_KEY_INTENT, intent);
	}

	private void processFile(Intent intent) {

		new GetWebContentLinkTask(this, Util.newGDrive(token.accessToken), intent.getStringExtra(EXTRA_FILE_ID)) {

			@Override
			protected void onPreExecute() throws Exception {
				new LoadingFragment().show(getSupportFragmentManager(), Constant.FRAG_TAG_DIALOG);
			}

			@Override
			protected void onSuccess(File file) throws Exception {
				dismissDialogFragment();
				if (Log.isLoggable(Constant.LOG_TAG, Log.DEBUG)) {
					Log.d(Constant.LOG_TAG, "title:" + file.getTitle());
					Log.d(Constant.LOG_TAG, "download url:" + file.getDownloadUrl());
					Log.d(Constant.LOG_TAG, "kind:" + file.getKind());
					Log.d(Constant.LOG_TAG, "web content link:" + file.getWebContentLink());
				}
				fileName.setText(file.getTitle());
				shareUrl.setText(file.getWebContentLink());
			}

			@Override
			protected void onThrowable(Throwable t) throws RuntimeException {
				dismissDialogFragment();

				Util.showToast(R.string.toast_share_url_fail);
			}
		}.execute();
	}

	private void dismissDialogFragment() {
		FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction().remove(fm.findFragmentByTag(Constant.FRAG_TAG_DIALOG)).commit();
	}


}
