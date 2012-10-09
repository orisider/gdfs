package com.orisider.gdfs.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.ActionBar;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.ipaulpro.afilechooser.FileChooserFragment;
import com.orisider.gdfs.R;
import com.orisider.gdfs.ui.event.DialogConfirmEvent;
import com.orisider.gdfs.ui.fragment.diag.ConfirmFragment;
import com.orisider.gdfs.ui.fragment.MainF;
import com.orisider.gdfs.util.Constant;
import com.orisider.gdfs.util.Util;
import roboguice.event.Observes;
import roboguice.inject.InjectView;

import java.io.File;

public class MainActivity extends RoboSherlockFragmentActivity implements FileChooserFragment.FileChooserListener {
	private static final String BUNDLE_KEY_SELECTED_TAB = "selected_tab";
	private static final String BUNDLE_KEY_SELECTED_FILE = "selected_file";
	private static final String DEFAULT_TITLE = "GDFS";

	private int selectedTabIndex = 0;
	private File selectedFile = null;

	@InjectView(R.id.pager)
	ViewPager pager;
	TabPagerAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setDisplayShowHomeEnabled(true);

		if (savedInstanceState != null) {
			selectedTabIndex = savedInstanceState.getInt(BUNDLE_KEY_SELECTED_TAB, 0);
			selectedFile = (File) savedInstanceState.getSerializable(BUNDLE_KEY_SELECTED_FILE);
		}

		setTitle(DEFAULT_TITLE);

		setContentView(R.layout.a_main);

		initTab();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(BUNDLE_KEY_SELECTED_TAB, selectedTabIndex);
		outState.putSerializable(BUNDLE_KEY_SELECTED_FILE, selectedFile);
	}

	private void initTab() {
		final ActionBar ab = getSupportActionBar();
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				ab.setSelectedNavigationItem(position);
			}
		};

		pager.setOnPageChangeListener(pageChangeListener);

		adapter = new TabPagerAdapter(this);
		pager.setAdapter(adapter);


		ab.addTab(
				getSupportActionBar().newTab().setText(R.string.tab_home).setTabListener(new CustomTabListener(pager, this)));
		ab.addTab(
				getSupportActionBar().newTab().setText(R.string.tab_explorer).setTabListener(new CustomTabListener(pager, this)));
	}

	@Override
	public void onUpRequestedOnRoot() {
		finish();
	}

	private String filePath;

	@Override
	public void onFilePathChanged(String newFilePath) {
		filePath = newFilePath;
		if (pager.getCurrentItem() == 1) {
			setTitle(filePath);
		}
	}

	@Override
	public void onMediaUnmounted() {
	}


	@Override
	public void onFileSelected(File file) {
		selectedFile = file;
		ConfirmFragment.newInstance(getString(R.string.confirm_upload, file.getName())).show
				(getSupportFragmentManager(),
						Constant.FRAG_TAG_DIALOG);
	}

	@SuppressWarnings("UnusedDeclaration")
	private void onConfirmed(@Observes DialogConfirmEvent confirmResult) {
		if (confirmResult.confirm && selectedFile != null) {
			Util.showToast(R.string.toast_upload_start);
			startService(new Intent().setAction(Constant.ACTION_DRIVE_UPLOAD).putExtra(Constant.FILE_URI
					, Uri.fromFile(selectedFile)));
		}
	}

	@Override
	public void onBackPressed() {
		if (getSupportActionBar().getSelectedNavigationIndex() == 0) {
			super.onBackPressed();
		} else {
			getFileChooseFragment().updateBreadcrumb(false);
		}
	}

	private FileChooserFragment getFileChooseFragment() {
		return (FileChooserFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + pager.getId() + ":1");
	}

	private static class CustomTabListener implements ActionBar.TabListener {

		private final ViewPager pager;
		private final MainActivity act;

		CustomTabListener(ViewPager pager, MainActivity act) {
			this.pager = pager;
			this.act = act;
		}

		@Override
		public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fakeFt) {
			pager.setCurrentItem(tab.getPosition());
			if (tab.getPosition() == 0) {
				act.setTitle(DEFAULT_TITLE);
			} else {
				act.setTitle(act.filePath);
			}
		}

		@Override
		public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
		}
	}

	private static class TabPagerAdapter extends FragmentPagerAdapter {
		final int PAGE_COUNT = 2;
		private final MainActivity act;


		public TabPagerAdapter(MainActivity act) {
			super(act.getSupportFragmentManager());
			this.act = act;

		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
				case 0:
					return new MainF();
				case 1:
					FileChooserFragment f = new FileChooserFragment();
					f.setListener(act);
					return f;
				default:
					return null;
			}
		}


		@Override
		public int getCount() {
			return PAGE_COUNT;
		}
	}
}