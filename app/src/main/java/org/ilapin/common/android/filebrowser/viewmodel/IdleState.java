package org.ilapin.common.android.filebrowser.viewmodel;

public class IdleState extends UnmountableState {

	public IdleState(final FileBrowserViewModel viewModel) {
		super(viewModel);
	}

	@Override
	public void changeCurrentDir(final String currentDirPath) {
		super.changeCurrentDir(currentDirPath);

		mViewModel.getCurrentDirPathSubject().onNext(currentDirPath);
		mViewModel.changeState(mViewModel.getChangingDirState());
	}

	@Override
	public void createDirectory(final String name) {
		super.createDirectory(name);

		mViewModel.setNewDirName(name);
		mViewModel.changeState(mViewModel.getCreatingDirState());
	}

	@Override
	public void goHome() {
		super.goHome();

		mViewModel.getCurrentDirPathSubject().onNext(mViewModel.getDefaultPath());
		mViewModel.changeState(mViewModel.getChangingDirState());
	}

	@Override
	public StateCode getCode() {
		return StateCode.IDLE;
	}
}
