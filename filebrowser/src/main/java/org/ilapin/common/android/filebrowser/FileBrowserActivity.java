package org.ilapin.common.android.filebrowser;

import android.Manifest;
import android.arch.lifecycle.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import io.reactivex.disposables.CompositeDisposable;
import org.ilapin.common.android.filebrowser.viewmodel.*;

import java.io.File;
import java.util.*;

public class FileBrowserActivity extends AppCompatActivity implements CreateDirectoryDialog.Listener,
		PermissionsProvider, OverwriteConfirmationDialog.Listener {

	public static final String PATH_KEY = "org.ilapin.common.android.filebrowser.FileBrowserActivity.PATH";
	public static final String MODE_KEY = "org.ilapin.common.android.filebrowser.FileBrowserActivity.MODE";
	public static final String DIR_PATH_KEY = "org.ilapin.common.android.filebrowser.FileBrowserActivity.DIR_PATH";
	public static final String FILENAME_KEY = "org.ilapin.common.android.filebrowser.FileBrowserActivity.FILENAME";

	private static final String OVERWRITE_CONFIRMATION_DIALOG_TAG = "OverwriteConfirmationDialog";
	private static final int PERMISSIONS_REQUEST_CODE = 123;
	private static final String[] SIZE_POSTFIXES = new String[] {
			"B",
			"kB",
			"MB",
			"GB",
			"TB"
	};

	public static final int MODE_OPEN = 0;
	public static final int MODE_SAVE = 1;

	private RecyclerView mFilesListRecyclerView;
	private EditText mFilenameEditText;
	private Button mSaveButton;
	private ProgressBar mProgressBar;
	private Button mRequestPermissionButton;
	private TextView mErrorMessageTextView;
	private TextView mFsPermissionRationaleMessageTextView;

	private int mMode;

	private String mCurrentDirPath;
	private FileBrowserViewModel mViewModel;
	private FilesListAdapter mFilesListAdapter;

	private final CompositeDisposable mSubscriptions = new CompositeDisposable();

	private final BroadcastReceiver mExternalStorageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			mViewModel.onStorageStateChanged();
		}
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_browser);

		mFilesListRecyclerView = findViewById(R.id.fsItemsList);
		mFilenameEditText = findViewById(R.id.filename);
		mSaveButton = findViewById(R.id.saveButton);
		mProgressBar = findViewById(R.id.progressBar);
		mRequestPermissionButton = findViewById(R.id.requestPermissionButton);
		mErrorMessageTextView = findViewById(R.id.errorMessage);
		mFsPermissionRationaleMessageTextView = findViewById(R.id.fsPermissionRationaleMessage);

		final String preferredDirPath = getIntent().getStringExtra(DIR_PATH_KEY);
		final String filename = getIntent().getStringExtra(FILENAME_KEY);
		mMode = getIntent().getIntExtra(MODE_KEY, MODE_OPEN);

		if (savedInstanceState == null) {
			mFilenameEditText.setText(filename);
		}

		mViewModel = ViewModelProviders.of(this, new ViewModelProvider.Factory() {

			@NonNull
			@Override
			public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
				if (modelClass == FileBrowserViewModel.class) {
					if (!TextUtils.isEmpty(preferredDirPath)) {
						//noinspection unchecked
						return (T) new FileBrowserViewModel(FileBrowserActivity.this, FileBrowserActivity.this,
								preferredDirPath);
					} else {
						//noinspection unchecked
						return (T) new FileBrowserViewModel(FileBrowserActivity.this, FileBrowserActivity.this);
					}
				} else {
					throw new RuntimeException("Unknown view-model class: " + modelClass.getSimpleName());
				}
			}
		}).get(FileBrowserViewModel.class);

		mFilesListAdapter = new FilesListAdapter();

		mFilesListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mFilesListRecyclerView.setAdapter(mFilesListAdapter);

		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		findViewById(R.id.saveButton).setOnClickListener(v -> {
			final Editable filenameEditable = mFilenameEditText.getText();
			if (filenameEditable != null && !TextUtils.isEmpty(filenameEditable.toString())) {
				final String path = mCurrentDirPath + File.separator + filenameEditable.toString();
				mViewModel.isFileExists(path).subscribe(isFileExists -> {
					if (isFileExists) {
						OverwriteConfirmationDialog
								.newInstance(path)
								.show(getSupportFragmentManager(), OVERWRITE_CONFIRMATION_DIALOG_TAG);
					} else {
						finishWithResult(path);
					}
				});
			} else {
				mFilenameEditText.setError(getString(R.string.file_browser_provide_filename));
			}
		});

		findViewById(R.id.requestPermissionButton).setOnClickListener(v -> requestFsPermission());
	}

	@Override
	protected void onResume() {
		super.onResume();

		mSubscriptions.add(mViewModel.path().subscribe(currentDirPath -> {
			mCurrentDirPath = currentDirPath;
			final File currentDir = new File(currentDirPath);
			final ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				getSupportActionBar().setTitle(currentDir.getName());
			}
		}));

		mSubscriptions.add(mViewModel.state().subscribe(stateCode -> {
			switch (stateCode) {

				case NEW:
				case IDLE:
				case AWAITING_PERMISSION:
					mProgressBar.setVisibility(View.GONE);
					mSaveButton.setEnabled(true);
					mErrorMessageTextView.setText("");
					mErrorMessageTextView.setVisibility(View.GONE);
					mFsPermissionRationaleMessageTextView.setVisibility(View.GONE);
					mRequestPermissionButton.setVisibility(View.GONE);
					updateSaveModeWidgetsVisibility();
					break;

				case CREATING_DIR:
				case CHANGING_DIR:
					mProgressBar.setVisibility(View.VISIBLE);
					mSaveButton.setEnabled(false);
					mErrorMessageTextView.setText("");
					mErrorMessageTextView.setVisibility(View.GONE);
					mFsPermissionRationaleMessageTextView.setVisibility(View.GONE);
					mRequestPermissionButton.setVisibility(View.GONE);
					updateSaveModeWidgetsVisibility();
					break;

				case NO_PERMISSION:
					mProgressBar.setVisibility(View.GONE);
					mSaveButton.setEnabled(true);
					mErrorMessageTextView.setText(getString(R.string.file_browser_no_fs_permission));
					mErrorMessageTextView.setVisibility(View.VISIBLE);
					mFsPermissionRationaleMessageTextView.setVisibility(View.GONE);
					mRequestPermissionButton.setVisibility(View.GONE);
					hideSaveModeWidgets();
					break;

				case PERMISSION_RATIONALE_REQUIRED:
					mProgressBar.setVisibility(View.GONE);
					mSaveButton.setEnabled(true);
					mErrorMessageTextView.setText("");
					mErrorMessageTextView.setVisibility(View.GONE);
					mFsPermissionRationaleMessageTextView.setVisibility(View.VISIBLE);
					mRequestPermissionButton.setVisibility(View.VISIBLE);
					hideSaveModeWidgets();
					break;

				case STORAGE_NOT_MOUNTED:
					mProgressBar.setVisibility(View.GONE);
					mSaveButton.setEnabled(true);
					mErrorMessageTextView.setText(R.string.file_browser_storage_not_mounted);
					mErrorMessageTextView.setVisibility(View.VISIBLE);
					mFsPermissionRationaleMessageTextView.setVisibility(View.GONE);
					mRequestPermissionButton.setVisibility(View.GONE);
					hideSaveModeWidgets();
					break;

				default:
					throw new RuntimeException("Unknown state: " + stateCode);
			}
		}));

		mSubscriptions.add(mViewModel.errors().subscribe(error -> {
			final String msg;
			switch (error) {
				case IO_ERROR:
					msg = getString(R.string.file_browser_io_error);
					break;

				case UNKNOWN:
					msg = getString(R.string.file_browser_unknown_error);
					break;

				default:
					throw new RuntimeException("Invalid error code: " + error);
			}

			Toast.makeText(FileBrowserActivity.this, msg, Toast.LENGTH_SHORT).show();
		}));

		mSubscriptions.add(
				mViewModel.fsItemList().subscribe(fsItems -> mFilesListAdapter.setFsItemsList(fsItems))
		);

		mViewModel.onFsPermissionCouldHaveBeenChanged();

		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		registerReceiver(mExternalStorageReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(mExternalStorageReceiver);
		mSubscriptions.clear();
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
			@NonNull final int[] grantResults) {
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			mViewModel.onFsPermissionGranted();
		} else {
			mViewModel.onFsPermissionDenied();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_file_browser, menu);
		menu.findItem(R.id.create_dir_menu_item).setVisible(mMode == MODE_SAVE);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			finish();
		} else if (itemId == R.id.home_menu_item) {
			mViewModel.goHome();
		} else if (itemId == R.id.create_dir_menu_item) {
			new CreateDirectoryDialog().show(getSupportFragmentManager(), "CreateDirectoryDialog");
		} else {
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	public void onFsItemClicked(final FsItem fsItem) {
		if (fsItem.isUp()) {
			mViewModel.changeCurrentDir(fsItem.getParentPath());
		} else if (fsItem.isDir()) {
			mViewModel.changeCurrentDir(mCurrentDirPath + File.separator + fsItem.getName());
		} else {
			final String path = mCurrentDirPath + File.separator + fsItem.getName();
			if (mMode == MODE_OPEN) {
				finishWithResult(path);
			} else {
				mViewModel.isFileExists(path).subscribe(isFileExists -> {
					if (isFileExists) {
						OverwriteConfirmationDialog
								.newInstance(path)
								.show(getSupportFragmentManager(), OVERWRITE_CONFIRMATION_DIALOG_TAG);
					} else {
						finishWithResult(path);
					}
				});
			}
		}
	}

	@Override
	public void onDirectoryNameProvided(final String name) {
		mViewModel.createDirectory(name);
	}

	@Override
	public boolean isFsPermissionGranted() {
		return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
	}

	@Override
	public void requestFsPermission() {
		ActivityCompat.requestPermissions(
				this,
				new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
				PERMISSIONS_REQUEST_CODE
		);
	}

	@Override
	public boolean shouldShowFsPermissionRationale() {
		return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	@Override
	public void onOverwriteConfirmed(final String path) {
		finishWithResult(path);
	}

	private void finishWithResult(final String path) {
		final Intent intent = new Intent();
		intent.putExtra(PATH_KEY, path);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void updateSaveModeWidgetsVisibility() {
		if (mMode == MODE_OPEN) {
			hideSaveModeWidgets();
		} else {
			mSaveButton.setVisibility(View.VISIBLE);
			mFilenameEditText.setVisibility(View.VISIBLE);
		}
	}

	private void hideSaveModeWidgets() {
		mSaveButton.setVisibility(View.GONE);
		mFilenameEditText.setVisibility(View.GONE);
	}

	public class FilesListAdapter extends RecyclerView.Adapter<FilesListAdapter.ViewHolder> {

		private final List<FsItem> mFsItemsList = new ArrayList<>();
		private final LayoutInflater mInflater = LayoutInflater.from(FileBrowserActivity.this);
		private final java.text.DateFormat mLastModifiedDateFormat =
				android.text.format.DateFormat.getDateFormat(FileBrowserActivity.this);

		private final Drawable mDirDrawable;
		private final Drawable mFileDrawable;

		public FilesListAdapter() {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
				mDirDrawable = getResources().getDrawable(R.drawable.ic_folder_black_48dp, null);
				mFileDrawable = getResources().getDrawable(R.drawable.ic_insert_drive_file_black_48dp, null);
			} else {
				mDirDrawable = getResources().getDrawable(R.drawable.ic_folder_black_48dp);
				mFileDrawable = getResources().getDrawable(R.drawable.ic_insert_drive_file_black_48dp);
			}
		}

		@Override
		public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
			return new ViewHolder(mInflater.inflate(R.layout.view_files_list_item, parent, false));
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, final int position) {
			final FsItem fsItem = mFsItemsList.get(position);

			if (fsItem.isUp()) {
				holder.fsItemIconImageView.setImageDrawable(mDirDrawable);
				holder.filenameTextView.setText("..");
				holder.lastModifiedTextView.setVisibility(View.GONE);
				holder.lastModifiedTextView.setText(null);
				holder.sizeTextView.setVisibility(View.GONE);
				holder.sizeTextView.setText(null);
			} else {
				holder.fsItemIconImageView.setImageDrawable(fsItem.isDir() ? mDirDrawable : mFileDrawable);
				holder.filenameTextView.setText(fsItem.getName());
				holder.lastModifiedTextView.setVisibility(View.VISIBLE);
				holder.lastModifiedTextView.setText(mLastModifiedDateFormat.format(new Date(fsItem.getLastModified())));
				if (!fsItem.isDir()) {
					holder.sizeTextView.setVisibility(View.VISIBLE);
					holder.sizeTextView.setText(formatSize(fsItem.getSize()));
				} else {
					holder.sizeTextView.setVisibility(View.GONE);
					holder.sizeTextView.setText(null);
				}
			}
		}

		@Override
		public int getItemCount() {
			return mFsItemsList.size();
		}

		public void setFsItemsList(final List<FsItem> fsItemsList) {
			mFsItemsList.clear();
			mFsItemsList.addAll(fsItemsList);
			notifyDataSetChanged();
		}

		private String formatSize(final long size) {
			for (int i = 0; i < SIZE_POSTFIXES.length; i++) {
				final double scaledSize = size / Math.pow(1024, i);
				if (scaledSize < 1024) {
					if (i == 0) {
						return String.format(Locale.US, "%d %s", size, SIZE_POSTFIXES[i]);
					} else {
						return String.format(Locale.US, "%.2f %s", scaledSize, SIZE_POSTFIXES[i]);
					}
				}
			}

			final int scale = SIZE_POSTFIXES.length - 1;
			final double scaledSize = size / Math.pow(1024, scale);
			return String.format(Locale.US, "%.2f %s", scaledSize, SIZE_POSTFIXES[scale]);
		}

		public class ViewHolder extends RecyclerView.ViewHolder {

			public final ImageView fsItemIconImageView;
			public final TextView filenameTextView;
			public final TextView sizeTextView;
			public final TextView lastModifiedTextView;

			public ViewHolder(final View itemView) {
				super(itemView);

				fsItemIconImageView = itemView.findViewById(R.id.fsItemIcon);
				filenameTextView = itemView.findViewById(R.id.filename);
				sizeTextView = itemView.findViewById(R.id.size);
				lastModifiedTextView = itemView.findViewById(R.id.lastModified);

				itemView.setOnClickListener(view -> onFsItemClicked(mFsItemsList.get(getAdapterPosition())));
			}
		}
	}
}
