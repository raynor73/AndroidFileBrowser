package org.ilapin.common.android.filebrowser.viewmodel;

public abstract class UnmountableState extends BaseState {

	public UnmountableState(final FileBrowserViewModel viewModel) {
		super(viewModel);
	}

	@Override
	public void onStorageUnmounted() {
		super.onStorageUnmounted();

		mViewModel.changeState(mViewModel.getStorageNotMountedState());
	}
}
