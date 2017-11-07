package org.ilapin.common.android.filebrowser.viewmodel;

public class AwaitingPermissionState extends UnmountableState {

	public AwaitingPermissionState(final FileBrowserViewModel viewModel) {
		super(viewModel);
	}

	@Override
	public void onFsPermissionDenied() {
		super.onFsPermissionDenied();

		if (mViewModel.getPermissionsProvider().shouldShowFsPermissionRationale()) {
			mViewModel.changeState(mViewModel.getPermissionRationaleRequiredState());
		} else {
			mViewModel.changeState(mViewModel.getNoPermissionState());
		}
	}

	@Override
	public void onFsPermissionGranted() {
		super.onFsPermissionGranted();

		mViewModel.changeState(mViewModel.getChangingDirState());
	}

	@Override
	public StateCode getCode() {
		return StateCode.AWAITING_PERMISSION;
	}
}
