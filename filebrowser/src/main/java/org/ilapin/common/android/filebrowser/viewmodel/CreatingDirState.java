package org.ilapin.common.android.filebrowser.viewmodel;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CreatingDirState extends UnmountableState {

	private Disposable mCreatingDirSubscription;

	public CreatingDirState(final FileBrowserViewModel viewModel) {
		super(viewModel);
	}

	@Override
	public void onEnter() {
		super.onEnter();

		if (!mViewModel.isStorageMounted()) {
			mViewModel.changeState(mViewModel.getStorageNotMountedState());
			return;
		}

		final PermissionsProvider permissionsProvider = mViewModel.getPermissionsProvider();
		if (!permissionsProvider.isFsPermissionGranted()) {
			if (permissionsProvider.shouldShowFsPermissionRationale()) {
				mViewModel.changeState(mViewModel.getPermissionRationaleRequiredState());
			} else {
				permissionsProvider.requestFsPermission();
				mViewModel.changeState(mViewModel.getAwaitingPermissionState());
			}
		} else {
			final String path = mViewModel.getCurrentDirPathSubject().getValue() + File.separator +
					mViewModel.getNewDirName();
			mCreatingDirSubscription = Single.fromCallable(() -> {
				final File dir = new File(path);
				return dir.mkdir();
			}).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(isSuccess -> {
				if (isSuccess) {
					mViewModel.getCurrentDirPathSubject().onNext(path);
					mViewModel.changeState(mViewModel.getChangingDirState());
				} else {
					mViewModel.changeState(mViewModel.getChangingDirState());
				}
			});
		}
	}

	@Override
	public void onLeave() {
		super.onLeave();

		if (mCreatingDirSubscription != null && !mCreatingDirSubscription.isDisposed()) {
			mCreatingDirSubscription.dispose();
			mCreatingDirSubscription = null;
		}
	}

	@Override
	public StateCode getCode() {
		return StateCode.CREATING_DIR;
	}
}
