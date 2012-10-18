package com.ipaulpro.afilechooser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FileChooserFragment extends ListFragment {

	private static final boolean DEBUG = true; // Set to false to disable logging
	private static final String TAG = "ChooserActivity"; // The log tag

	public static final int REQUEST_CODE = 6384; // onActivityResult request code
	public static final String MIME_TYPE_ALL = "*/*"; // Filter for all MIME types

	private static final String PATH = "path";
	private static final String BREADCRUMB = "breadcrumb";
	private static final String POSTIION = "position";
	private static final String HIDDEN_PREFIX = ".";

	private String mPath; // The current file path
	private ArrayList<String> mBreadcrumb = new ArrayList<String>(); // Path history

	private boolean mExternalStorageAvailable = false;
	private boolean mExternalStorageWriteable = false;

	private File mExternalDir;
	private ArrayList<File> mList = new ArrayList<File>();

	private FileChooserListener listener;

	/**
	 * File (not directories) filter.
	 */
	private FileFilter mFileFilter = new FileFilter() {
		public boolean accept(File file) {
			final String fileName = file.getName();
			// Return files only (not directories) and skip hidden files
			return file.isFile() && !fileName.startsWith(HIDDEN_PREFIX);
		}
	};

	/**
	 * Folder (directories) filter.
	 */
	private FileFilter mDirFilter = new FileFilter() {
		public boolean accept(File file) {
			final String fileName = file.getName();
			// Return directories only and skip hidden directories
			return file.isDirectory() && !fileName.startsWith(HIDDEN_PREFIX);
		}
	};

	/**
	 * File and folder comparator.
	 * TODO Expose sorting option method
	 */
	private Comparator<File> mComparator = new Comparator<File>() {
		public int compare(File f1, File f2) {
			// Sort alphabetically by lower case, which is much cleaner
			return f1.getName().toLowerCase().compareTo(
					f2.getName().toLowerCase());
		}
	};

	/**
	 * External storage state broadcast receiver.
	 */
	private BroadcastReceiver mExternalStorageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (DEBUG) Log.d(TAG, "External storage broadcast recieved: "
					+ intent.getData());
			updateExternalStorageState();
		}
	};


	/**
	 * Activities extending FileChooserActivity must check against this, and implement
	 * the associated Intent Filter in AndroidManifest.xml.
	 *
	 * @return True if the Intent Action is android.intent.action.GET_CONTENT.
	 */
	protected boolean isIntentGetContent() {
		//TODO ?
		return true;
	}

	/**
	 * Display the Intent Chooser.
	 *
	 * @param title Chooser Dialog title.
	 * @param type  Explicit MIME data type filter.
	 */
	protected void showFileChooser(String title, String type) {
		if (TextUtils.isEmpty(title)) title = getString(R.string.select_file);
		if (TextUtils.isEmpty(type)) type = MIME_TYPE_ALL;

		// Implicitly allow the user to select a particular kind of data
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		// Specify the MIME data type filter (Must be lower case)
		intent.setType(type.toLowerCase());
		// Only return URIs that can be opened with ContentResolver
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		// Display intent chooser
		try {
			startActivityForResult(
					Intent.createChooser(intent, title), REQUEST_CODE);
		} catch (android.content.ActivityNotFoundException e) {
			onFileError(e);
		}
	}

	/**
	 * Convenience method to show the File Chooser with the default
	 * title and have it return all file types.
	 */
	protected void showFileChooser() {
		showFileChooser(null, null);
	}

	/**
	 * Fill the list with the current directory contents.
	 */
	private void fillList(int position) {
		if (DEBUG) Log.d(TAG, "Current path: " + this.mPath);

		// Set the cuttent path as the Activity title
		setTitle(this.mPath);
		// Clear the list adapter
		((FileListAdapter) getListAdapter()).clear();

		// Our current directory File instance
		final File pathDir = new File(mPath);

		// List file in this directory with the directory filter
		final File[] dirs = pathDir.listFiles(mDirFilter);
		if (dirs != null) {
			// Sort the folders alphabetically
			Arrays.sort(dirs, mComparator);
			// Add each folder to the File list for the list adapter
			for (File dir : dirs) mList.add(dir);
		}

		// List file in this directory with the file filter
		final File[] files = pathDir.listFiles(mFileFilter);
		if (files != null) {
			// Sort the files alphabetically
			Arrays.sort(files, mComparator);
			// Add each file to the File list for the list adapter
			for (File file : files) mList.add(file);
		}

		if (dirs == null && files == null) {
			if (DEBUG) Log.d(TAG, "Directory is empty");
		}

		// Assign the File list items as our adapter items
		((FileListAdapter) getListAdapter()).setListItems(mList);
		// Update the ListView
		((FileListAdapter) getListAdapter()).notifyDataSetChanged();
		// Jump to the top of the list
		getListView().setSelection(position);
	}

	private void setTitle(String mPath) {
		if (listener != null) {
			listener.onFilePathChanged(mPath);
		}
	}

	/**
	 * Keep track of the directory hierarchy.
	 *
	 * @param add Add the current path to the directory stack.
	 */
	public void updateBreadcrumb(boolean add) {
		if (add) {
			// Add the current path to the stack
			this.mBreadcrumb.add(this.mPath);
		} else {
			if (this.mExternalDir.getAbsolutePath().equals(this.mPath)) {
				// If at the base directory, exit the Activity
				onFileSelectCancel();
				listener.onUpRequestedOnRoot();
			} else {
				// Otherwise, remove the last path from the stack
				int size = this.mBreadcrumb.size();
				if (size > 1) {
					this.mBreadcrumb.remove(size - 1);
					this.mPath = this.mBreadcrumb.get(size - 2);

					// Display the new directory contents
					fillList(0);
				}
			}
		}
	}

	/**
	 * Update the external storage member variables.
	 */
	private void updateExternalStorageState() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			this.mExternalStorageAvailable = this.mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			this.mExternalStorageAvailable = true;
			this.mExternalStorageWriteable = false;
		} else {
			this.mExternalStorageAvailable = this.mExternalStorageWriteable = false;
		}

		handleExternalStorageState(this.mExternalStorageAvailable,
				this.mExternalStorageWriteable);
	}

	/**
	 * Register the external storage BroadcastReceiver.
	 */
	private void startWatchingExternalStorage() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		getActivity().registerReceiver(this.mExternalStorageReceiver, filter);

		if (isIntentGetContent())
			updateExternalStorageState();
	}

	/**
	 * Unregister the external storage BroadcastReceiver.
	 */
	private void stopWatchingExternalStorage() {
		getActivity().unregisterReceiver(this.mExternalStorageReceiver);
	}

	/**
	 * Respond to a change in the external storage state
	 *
	 * @param available
	 * @param writeable
	 */
	private void handleExternalStorageState(boolean available, boolean writeable) {
		if (!available && isIntentGetContent()) {
			if (DEBUG) Log.d(TAG, "External Storage was disconnected");
			onFileDisconnect();
			//TODO
			listener.onMediaUnmounted();
		}
	}

	/**
	 * Called when a file is successfully selected by the user.
	 *
	 * @param file The file selected.
	 */
	protected void onFileSelect(File file) {
		if (DEBUG) Log.d(TAG, "File selected: " + file.getAbsolutePath());
	}

	/**
	 * Called when there is an error selecting a file.
	 *
	 * @param e The error encountered during file selection.
	 */
	protected void onFileError(Exception e) {
		if (DEBUG) Log.e(TAG, "Error selecting file", e);
	}

	/**
	 * Called when the user backs out of the file selection process.
	 */
	protected void onFileSelectCancel() {
		if (DEBUG) Log.d(TAG, "File selection canceled");
	}

	/**
	 * Called when the external storage (SD) is disconnected.
	 */
	protected void onFileDisconnect() {
		if (DEBUG) Log.d(TAG, "External storage disconnected");
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the external storage directory.
		this.mExternalDir = Environment.getExternalStorageDirectory();

		if (getListAdapter() == null) {
			// Assign the list adapter to the ListView
			setListAdapter(new FileListAdapter(getActivity()));
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			restoreMe(savedInstanceState);
		} else {
			// Set the external storage directory as the current path
			this.mPath = this.mExternalDir.getAbsolutePath();
			// Add the current path to the breadcrumb
			updateBreadcrumb(true);
			fillList(0);
		}


	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.explorer, container, false);
	}


	@Override
	public void onResume() {
		super.onResume();
		// Set the Broadcast Receiver to listen for storage mount changes
		startWatchingExternalStorage();
	}

	@Override
	public void onPause() {
		super.onPause();
		// Remove the Broadcast Receiver listening for storage mount changes
		stopWatchingExternalStorage();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// Get the file that was selected from the file list
		File file = this.mList.get(position);
		// Save the path as our current member variable
		this.mPath = file.getAbsolutePath();
		if (DEBUG) Log.d(TAG, "Selected file: " + this.mPath);

		if (file != null) {
			if (file.isDirectory()) {
				// If the selected item is a folder, update UI
				updateBreadcrumb(true);
				fillList(0);
			} else {
				listener.onFileSelected(file);
			}
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Save the current path and breadcrumb when the activity is interrupted.
		outState.putString(PATH, mPath);
		outState.putStringArrayList(BREADCRUMB, mBreadcrumb);
		outState.putInt(POSTIION, getListView().getFirstVisiblePosition());
	}

	/**
	 * If the activity was interrupted, restore the previous path and breadcrumb
	 *
	 * @param state
	 */
	private void restoreMe(Bundle state) {
		// Restore the previous path. Defaults to base external storage dir
		this.mPath = (state.containsKey(PATH)) ?
				state.getString(PATH) : mExternalDir.getAbsolutePath();
		// Restore the previous breadcrumb
		this.mBreadcrumb = state.getStringArrayList(BREADCRUMB);
		fillList(state.getInt(POSTIION));
	}

	public static interface FileChooserListener {
		void onUpRequestedOnRoot();

		void onFilePathChanged(String newFilePath);

		void onMediaUnmounted();

		void onFileSelected(File file);
	}

	public void setListener(FileChooserListener listener) {
		this.listener = listener;
	}
}