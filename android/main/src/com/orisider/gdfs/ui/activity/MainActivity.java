package com.orisider.gdfs.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.orisider.gdfs.R;
import com.orisider.gdfs.model.AccessToken;
import com.orisider.gdfs.util.Constant;
import com.orisider.gdfs.util.SessionStore;
import roboguice.inject.InjectView;

public class MainActivity extends RoboSherlockFragmentActivity implements View.OnClickListener {


    @InjectView(R.id.saved_acnt_info)
    View savedAcntInfo;

    @InjectView(R.id.saved_acnt_name)
    TextView savedAcntName;

    @InjectView(R.id.saved_acnt_token)
    TextView savedAcntToken;

    @InjectView(R.id.set_acnt_btn)
    Button acntBtn;

    @InjectView(R.id.launch_gdrive_btn)
    Button launchGdriveBtn;

    AccessToken token;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (token == null) {
            token = SessionStore.getAccountAuthToken();
        }

        initView();
        showPanel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == Constant.REQ_CODE_ACNT && resultCode == Activity.RESULT_OK) {
            token = (AccessToken) data.getSerializableExtra(Constant.BUNDLE_KEY_ACCESS_TOKEN);
            showPanel();
        }
    }

    private void initView() {
        acntBtn.setOnClickListener(this);
        launchGdriveBtn.setOnClickListener(this);
    }

    private void showPanel() {
        if (token == null) {
            acntBtn.setText("set acnt");
            acntBtn.setTag(true);
            savedAcntInfo.setVisibility(View.GONE);
            launchGdriveBtn.setVisibility(View.GONE);
        } else {
            acntBtn.setText("reset acnt");
            acntBtn.setTag(false);
            savedAcntInfo.setVisibility(View.VISIBLE);
            savedAcntName.setText(token.accountName);
            savedAcntToken.setText(token.accessToken);
            launchGdriveBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == acntBtn) {
            if( acntBtn.getTag() == Boolean.TRUE) {
                startActivityForResult(new Intent(getApplicationContext(), GetAccountActivity.class), Constant.REQ_CODE_ACNT);
            } else {
                SessionStore.removeAccessToken();
                token = null;
                showPanel();
            }

        } else if( v== launchGdriveBtn) {
//            startActivity(new Intent().setComponent(new ComponentName("com.google.android.apps.docs", "com.google.android.apps.docs.app.NewMainProxyActivity")));
            startActivity( new Intent(getApplicationContext(), FileListActivity.class));
        }
    }
}

