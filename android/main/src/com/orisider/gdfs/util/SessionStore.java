package com.orisider.gdfs.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.orisider.gdfs.GDFSApp;
import com.orisider.gdfs.model.AccessToken;

public class SessionStore {

    private static final String KEY_STORE_NAME = "goog_drv_share";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_ACCOUNT_NAME = "account_name";

    public static void saveAccountAuthToken(AccessToken token) {
        getSFEditor(GDFSApp.ctx).putString(KEY_ACCOUNT_NAME, token.accountName).putString(KEY_ACCESS_TOKEN, token.accessToken).commit();
    }

    /**
     * get saved account info
     * @return account info. null if account info is not saved
     */
    public static AccessToken getAccountAuthToken()
    {
        SharedPreferences sf = getSF(GDFSApp.ctx);
        String accountName = sf.getString( KEY_ACCOUNT_NAME, null);
        String accessToken = sf.getString(KEY_ACCESS_TOKEN, null);
        if( accountName != null && accessToken != null) {
            return new AccessToken(accountName, accessToken);
        } else {
            return null;
        }
    }

    private static SharedPreferences getSF(Context context) {
        return context.getSharedPreferences(KEY_STORE_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getSFEditor(Context context) {
        return context.getSharedPreferences(KEY_STORE_NAME, Context.MODE_PRIVATE).edit();
    }

    public static void removeAccessToken() {
        getSFEditor(GDFSApp.ctx).remove(KEY_ACCOUNT_NAME).remove(KEY_ACCESS_TOKEN).commit();
    }


}
