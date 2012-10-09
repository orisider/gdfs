package com.orisider.gdfs.ui.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.orisider.gdfs.R;
import com.orisider.gdfs.model.AccessToken;
import com.orisider.gdfs.ui.event.DialogCanceledEvent;
import com.orisider.gdfs.ui.event.ItemSelectedEvent;
import com.orisider.gdfs.ui.fragment.diag.AccountChooseFragment;
import com.orisider.gdfs.ui.fragment.diag.LoadingFragment;
import com.orisider.gdfs.util.Constant;
import com.orisider.gdfs.util.Util;
import roboguice.event.Observes;

public class GetAccountActivity extends RoboSherlockFragmentActivity {
	private AccountManager accountManager;
	private Account[] accounts;

	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountManager = AccountManager.get(this);
		accounts = accountManager.getAccountsByType("com.google");

		if (handler == null) {
			handler = new Handler();
		}

		if (accounts == null || accounts.length == 0) {
			Util.showToast(R.string.warn_no_goog_account);
		} else if (accounts.length == 1) {
			processAccountSelected(accounts[0]);
		} else {
			AccountChooseFragment.newInstance(getAccountNames(accounts)).show(
					getSupportFragmentManager(), Constant.FRAG_TAG_DIALOG);
		}
	}

	private String[] getAccountNames(Account[] accounts) {
		String[] names = new String[accounts.length];

		for (int i = 0; i < accounts.length; i++) {
			names[i] = accounts[i].name;
		}
		return names;
	}

	private void processAccountSelected(Account account) {
		if (account != null) {
			new LoadingFragment().show(getSupportFragmentManager(), Constant.FRAG_TAG_DIALOG);

			Util.getAccountToken(accountManager, account, this, handler, new Util.AccessTokenCallback() {
				@Override
				public void onTokenGetSuccess(AccessToken token) {
					dismissLoading();
					setResult(Activity.RESULT_OK, new Intent().putExtra(Constant.BUNDLE_KEY_ACCESS_TOKEN, token));
					finish();
				}

				private void dismissLoading() {
					getSupportFragmentManager().beginTransaction().remove(
							getSupportFragmentManager().findFragmentByTag(Constant.FRAG_TAG_DIALOG)).commit();
				}

				@Override
				public void onTokenGetFailed(Throwable e) {
					Util.showToast(R.string.warn_token_get_fail);
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
			});
		}
	}

	@SuppressWarnings("UnusedDeclaration")
	private void onItemSelected(@Observes ItemSelectedEvent e) {
		processAccountSelected(accounts[Integer.parseInt(e.selectedValue)]);
	}

	@SuppressWarnings("UnusedDeclaration")
	private void onDialogCanceled(@Observes DialogCanceledEvent e) {
		finish();
	}
}
