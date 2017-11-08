package org.ilapin.common.android.filebrowser.viewmodel;

import android.util.Log;

public abstract class BaseState implements State {

	protected final FileBrowserViewModel mViewModel;

	public BaseState(final FileBrowserViewModel viewModel) {
		mViewModel = viewModel;
	}

	@Override
	public void onEnter() {
		Log.i(getClass().getSimpleName(), "onEnter");

		// do nothing
	}

	@Override
	public void onLeave() {
		Log.i(getClass().getSimpleName(), "onLeave");

		// do nothing
	}

	@Override
	public String getName() {
		return getCode().toString();
	}

	@Override
	public void restore(final String preferredPath) {
		Log.i(getClass().getSimpleName(), "restore");

		// do nothing
	}

	@Override
	public void goHome() {
		Log.i(getClass().getSimpleName(), "goHome");

		// do nothing
	}

	@Override
	public void changeCurrentDir(final String currentDirPath) {
		Log.i(getClass().getSimpleName(), "changeCurrentDir");

		// do nothing
	}

	@Override
	public void createDirectory(final String name) {
		Log.i(getClass().getSimpleName(), "changeCurrentDir");

		// do nothing
	}

	@Override
	public void onFsPermissionDenied() {
		Log.i(getClass().getSimpleName(), "onFsPermissionDenied");

		// do nothing
	}

	@Override
	public void onFsPermissionGranted() {
		Log.i(getClass().getSimpleName(), "onFsPermissionGranted");

		// do nothing
	}

	@Override
	public void onFsPermissionCouldHaveBeenChanged() {
		Log.i(getClass().getSimpleName(), "onFsPermissionCouldHaveBeenChanged");

		// do nothing
	}

	@Override
	public void onStorageMounted() {
		Log.i(getClass().getSimpleName(), "onStorageMounted");

		// do nothing
	}

	@Override
	public void onStorageUnmounted() {
		Log.i(getClass().getSimpleName(), "onStorageUnmounted");

		// do nothing
	}
}
