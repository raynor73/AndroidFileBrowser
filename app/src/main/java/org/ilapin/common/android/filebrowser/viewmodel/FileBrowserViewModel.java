package org.ilapin.common.android.filebrowser.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import org.ilapin.common.android.viewmodelprovider.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class FileBrowserViewModel implements ViewModel {

	static final String CURRENT_DIR_PATH_KEY = "CURRENT_DIR_PATH";

	private static final String PREFERENCES_NAME = "FileBrowserViewModelPreferences";

	private final SharedPreferences mPreferences;
	private final PermissionsProvider mPermissionsProvider;

	private final BehaviorSubject<StateCode> mStateCodeSubject;
	private final BehaviorSubject<List<FsItem>> mFsItemsSubject;
	private final BehaviorSubject<String> mCurrentDirPathSubject;
	private final PublishSubject<FileBrowserError> mErrorSubject;

	private final NewState mNewState;
	private final IdleState mIdleState;
	private final ChangingDirState mChangingDirState;
	private final CreatingDirState mCreatingDirState;
	private final AwaitingPermissionState mAwaitingPermissionState;
	private final NoPermissionState mNoPermissionState;
	private final PermissionRationaleRequiredState mPermissionRationaleRequiredState;
	private final StorageNotMountedState mStorageNotMountedState;

	private State mState;

	private String mNewDirName;

	public FileBrowserViewModel(final Context context, final PermissionsProvider permissionsProvider) {
		mPreferences = context.getSharedPreferences(PREFERENCES_NAME, 0);
		mPermissionsProvider = permissionsProvider;

		mNewState = new NewState(this);
		mIdleState = new IdleState(this);
		mChangingDirState = new ChangingDirState(this);
		mCreatingDirState = new CreatingDirState(this);
		mAwaitingPermissionState = new AwaitingPermissionState(this);
		mNoPermissionState = new NoPermissionState(this);
		mPermissionRationaleRequiredState = new PermissionRationaleRequiredState(this);
		mStorageNotMountedState = new StorageNotMountedState(this);

		mFsItemsSubject = BehaviorSubject.createDefault(new ArrayList<>());
		mCurrentDirPathSubject = BehaviorSubject.createDefault("");
		mErrorSubject = PublishSubject.create();

		mStateCodeSubject = BehaviorSubject.createDefault(StateCode.NEW);
		mState = mNewState;

		mState.restore(null);
	}

	public FileBrowserViewModel(final Context context, final PermissionsProvider permissionsProvider,
			final String preferredPath) {
		mPreferences = context.getSharedPreferences(PREFERENCES_NAME, 0);
		mPermissionsProvider = permissionsProvider;

		mNewState = new NewState(this);
		mIdleState = new IdleState(this);
		mChangingDirState = new ChangingDirState(this);
		mCreatingDirState = new CreatingDirState(this);
		mAwaitingPermissionState = new AwaitingPermissionState(this);
		mNoPermissionState = new NoPermissionState(this);
		mPermissionRationaleRequiredState = new PermissionRationaleRequiredState(this);
		mStorageNotMountedState = new StorageNotMountedState(this);

		mFsItemsSubject = BehaviorSubject.createDefault(new ArrayList<>());
		mCurrentDirPathSubject = BehaviorSubject.createDefault("");
		mErrorSubject = PublishSubject.create();

		mStateCodeSubject = BehaviorSubject.createDefault(StateCode.NEW);
		mState = mNewState;

		mState.restore(preferredPath);
	}

	public Observable<StateCode> state() {
		return mStateCodeSubject;
	}

	public Observable<String> path() {
		return mCurrentDirPathSubject;
	}

	public Observable<List<FsItem>> fsItemList() {
		return mFsItemsSubject;
	}

	public Observable<FileBrowserError> errors() {
		return mErrorSubject;
	}

	public void goHome() {
		mState.goHome();
	}

	public void createDirectory(final String name) {
		mState.createDirectory(name);
	}

	public void changeCurrentDir(final String currentDirPath) {
		mState.changeCurrentDir(currentDirPath);
	}

	public Single<Boolean> isFileExists(final String path) {
		return Single.fromCallable(() -> {
			final File file = new File(path);
			return file.exists();
		}).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
	}

	public void onFsPermissionDenied() {
		mState.onFsPermissionDenied();
	}

	public void onFsPermissionGranted() {
		mState.onFsPermissionGranted();
	}

	public void onFsPermissionCouldHaveBeenChanged() {
		mState.onFsPermissionCouldHaveBeenChanged();
	}

	public void onStorageStateChanged() {
		if (isStorageMounted()) {
			mState.onStorageMounted();
		} else {
			mState.onStorageUnmounted();
		}
	}

	SharedPreferences getPreferences() {
		return mPreferences;
	}

	PermissionsProvider getPermissionsProvider() {
		return mPermissionsProvider;
	}

	String getNewDirName() {
		return mNewDirName;
	}

	void setNewDirName(final String newDirName) {
		mNewDirName = newDirName;
	}

	BehaviorSubject<String> getCurrentDirPathSubject() {
		return mCurrentDirPathSubject;
	}

	PublishSubject<FileBrowserError> getErrorSubject() {
		return mErrorSubject;
	}

	BehaviorSubject<List<FsItem>> getFsItemsSubject() {
		return mFsItemsSubject;
	}

	IdleState getIdleState() {
		return mIdleState;
	}

	ChangingDirState getChangingDirState() {
		return mChangingDirState;
	}

	CreatingDirState getCreatingDirState() {
		return mCreatingDirState;
	}

	AwaitingPermissionState getAwaitingPermissionState() {
		return mAwaitingPermissionState;
	}

	NoPermissionState getNoPermissionState() {
		return mNoPermissionState;
	}

	PermissionRationaleRequiredState getPermissionRationaleRequiredState() {
		return mPermissionRationaleRequiredState;
	}

	StorageNotMountedState getStorageNotMountedState() {
		return mStorageNotMountedState;
	}

	String getDefaultPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	boolean isStorageMounted() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

	void changeState(final State newState) {
		if (mState == newState) {
			throw new RuntimeException("Already has state: " + mState.getName());
		}

		Log.i("FileBrowserViewModel", "Changing state: " + mState.getName() + " -> " + newState.getName());

		mState.onLeave();
		mState = newState;
		mState.onEnter();
		mStateCodeSubject.onNext(mState.getCode());
	}

	@Override
	public void onCleared() {
		// do nothing
	}
}
