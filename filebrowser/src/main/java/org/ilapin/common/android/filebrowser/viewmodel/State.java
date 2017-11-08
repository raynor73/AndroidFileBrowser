package org.ilapin.common.android.filebrowser.viewmodel;

public interface State {

	void onEnter();

	void onLeave();

	StateCode getCode();

	String getName();

	void restore(final String preferredPath);

	void goHome();

	void changeCurrentDir(final String currentDirPath);

	void createDirectory(final String name);

	void onFsPermissionDenied();

	void onFsPermissionGranted();

	void onFsPermissionCouldHaveBeenChanged();

	void onStorageMounted();

	void onStorageUnmounted();
}
