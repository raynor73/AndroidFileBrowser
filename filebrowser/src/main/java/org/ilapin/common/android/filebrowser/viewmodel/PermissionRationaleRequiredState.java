package org.ilapin.common.android.filebrowser.viewmodel;

public class PermissionRationaleRequiredState extends UnmountableState {

	public PermissionRationaleRequiredState(final FileBrowserViewModel viewModel) {
		super(viewModel);
	}

	@Override
	public void onFsPermissionDenied() {
		super.onFsPermissionDenied();

		if (!mViewModel.getPermissionsProvider().shouldShowFsPermissionRationale()) {
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
		return StateCode.PERMISSION_RATIONALE_REQUIRED;
	}
}
