package com.orisider.gdfs.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.orisider.gdfs.GDFSApp;
import com.orisider.gdfs.R;
import com.orisider.gdfs.model.AccessToken;

public class Util {

	public static void showToast(int textResId) {
		showToast(GDFSApp.ctx.getResources().getString(textResId));
	}

	public static void showToast(String text) {
		Toast.makeText(GDFSApp.ctx, text, Toast.LENGTH_SHORT).show();
	}

	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() < 1;
	}


	public static void getAccountToken(AccountManager acntMgr, final Account acnt, Activity act, Handler handler,
									   final AccessTokenCallback callback) {
		acntMgr.getAuthToken(acnt, Constant.AUTH_TOKEN_TYPE, null, act, new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				try {
					Bundle bundle = future.getResult();
					String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

					if (!Util.isEmpty(authToken)) {
						AccessToken token = new AccessToken(acnt.name, authToken);
						SessionStore.saveAccountAuthToken(token);
						callback.onTokenGetSuccess(token);
						return;
					}

					callback.onTokenGetFailed(new Exception("token is empty"));
				} catch (Throwable e) {
					callback.onTokenGetFailed(e);
				}
			}
		}, handler);
	}

	public static interface AccessTokenCallback {
		public void onTokenGetSuccess(AccessToken token);

		public void onTokenGetFailed(Throwable e);
	}

	public static Drive newGDrive(String accessToken) {
		final HttpTransport transport = AndroidHttp.newCompatibleTransport();
		final JsonFactory jsonFactory = new AndroidJsonFactory();

		String clientApiId = GDFSApp.ctx.getResources().getString(R.string.client_api_id);
		String clientApiSecret = GDFSApp.ctx.getResources().getString(R.string.client_api_secret);
		String simpleApiKey = GDFSApp.ctx.getResources().getString(R.string.simple_api_key);

		GoogleCredential credential =
				new GoogleCredential.Builder()
						.setClientSecrets(clientApiId, clientApiSecret).build();

		credential.setAccessToken(accessToken);
		return new Drive.Builder(transport, jsonFactory, credential)
				.setApplicationName(GDFSApp.ctx.getResources().getString(R.string.app_name))
				.setJsonHttpRequestInitializer(new GoogleKeyInitializer(simpleApiKey))
				.build();
	}

}
