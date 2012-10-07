package com.orisider.gdfs.ui.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.orisider.gdfs.util.Constant;
import com.orisider.gdfs.R;
import com.orisider.gdfs.util.Util;
import com.orisider.gdfs.model.AccessToken;

public class GetAccountActivity extends Activity implements DialogInterface.OnClickListener {


    private AccountManager accountManager;
    private Account[] accounts;


    private final int DIALOG_ACCOUNTCHOSER = 1;
    private final int DIAG_LOADING = 2;

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
            showDialog(DIALOG_ACCOUNTCHOSER);
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case DIALOG_ACCOUNTCHOSER:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                String[] names = new String[accounts.length];

                for (int i = 0; i < accounts.length; i++) {
                    names[i] = accounts[i].name;
                }

                alertDialogBuilder.setItems(names, this);
                alertDialogBuilder.setTitle("Select a Google account");
                return alertDialogBuilder.create();

            case DIAG_LOADING:
                ProgressDialog progDiag = new ProgressDialog(this);
                progDiag.setIndeterminate(true);
                progDiag.setCancelable(false);
                return progDiag;
        }

        return null;
    }


    private void processAccountSelected(Account account) {
        if (account != null) {
            showDialog(DIAG_LOADING);

            Util.getAccountToken( accountManager, account, this, handler, new Util.AccessTokenCallback() {
                @Override
                public void onTokenGetSuccess(AccessToken token) {
                    dismissDialog(DIAG_LOADING);
                    setResult(Activity.RESULT_OK, new Intent().putExtra(Constant.BUNDLE_KEY_ACCESS_TOKEN, token));
                    finish();
                }

                @Override
                public void onTokenGetFailed(Throwable e) {
                    dismissDialog(DIAG_LOADING);
                    Util.showToast("failed to get access token");
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            });
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        processAccountSelected(accounts[which]);
    }
}
