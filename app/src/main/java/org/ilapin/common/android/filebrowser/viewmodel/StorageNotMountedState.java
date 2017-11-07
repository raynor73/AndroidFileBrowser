package org.ilapin.common.android.filebrowser.viewmodel;

public class StorageNotMountedState extends BaseState {

	public StorageNotMountedState(final FileBrowserViewModel viewModel) {
		super(viewModel);
	}

	@Override
	public void onStorageMounted() {
		super.onStorageMounted();

		mViewModel.getCurrentDirPathSubject().onNext(mViewModel.getDefaultPath());
		mViewModel.changeState(mViewModel.getChangingDirState());
	}

	@Override
	public StateCode getCode() {
		return StateCode.STORAGE_NOT_MOUNTED;
	}
}
