package org.ilapin.common.android.filebrowser.viewmodel;

public class NoPermissionState extends UnmountableState {

	public NoPermissionState(final FileBrowserViewModel viewModel) {
		super(viewModel);
	}

	@Override
	public void onFsPermissionCouldHaveBeenChanged() {
		super.onFsPermissionCouldHaveBeenChanged();

		if (mViewModel.getPermissionsProvider().isFsPermissionGranted()) {
			mViewModel.changeState(mViewModel.getChangingDirState());
		}
	}

	@Override
	public StateCode getCode() {
		return StateCode.NO_PERMISSION;
	}
}
