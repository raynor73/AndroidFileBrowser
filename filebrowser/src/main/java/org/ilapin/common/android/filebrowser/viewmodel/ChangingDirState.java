package org.ilapin.common.android.filebrowser.viewmodel;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChangingDirState extends UnmountableState {

	private Disposable mChangeDirSubscription;

	public ChangingDirState(final FileBrowserViewModel viewModel) {
		super(viewModel);
	}

	@Override
	public void onEnter() {
		super.onEnter();

		if (!mViewModel.isStorageMounted()) {
			mViewModel.changeState(mViewModel.getStorageNotMountedState());
			return;
		}

		mChangeDirSubscription = changeDir(mViewModel.getCurrentDirPathSubject().getValue())
				.onErrorResumeNext(throwable -> {
					final String defaultPath = mViewModel.getDefaultPath();
					mViewModel.getCurrentDirPathSubject().onNext(defaultPath);
					return changeDir(defaultPath);
				})
				.subscribe(
					fsItems -> {
						mViewModel.getFsItemsSubject().onNext(fsItems);
						mViewModel.changeState(mViewModel.getIdleState());
					},
					throwable -> {
						if (throwable instanceof FileBrowserException) {
							final FileBrowserException e = (FileBrowserException) throwable;
							switch (e.getError()) {
								case NO_PERMISSION:
									if (mViewModel.getPermissionsProvider().shouldShowFsPermissionRationale()) {
										mViewModel.changeState(mViewModel.getPermissionRationaleRequiredState());
									} else {
										mViewModel.getPermissionsProvider().requestFsPermission();
										mViewModel.changeState(mViewModel.getAwaitingPermissionState());
									}
									break;

								case NO_DIR:
								case IS_FILE:
								case IO_ERROR:
									mViewModel.getErrorSubject().onNext(FileBrowserError.IO_ERROR);
									mViewModel.changeState(mViewModel.getIdleState());
									break;

								case UNKNOWN:
									mViewModel.getErrorSubject().onNext(FileBrowserError.UNKNOWN);
									mViewModel.changeState(mViewModel.getIdleState());
									break;

								default:
									throw new RuntimeException("Unknown File Browser Error: " + e.getError());
							}
						} else {
							mViewModel.getErrorSubject().onNext(FileBrowserError.UNKNOWN);
						}
					}
				);
	}

	@Override
	public void onLeave() {
		super.onLeave();

		if (mChangeDirSubscription != null && !mChangeDirSubscription.isDisposed()) {
			mChangeDirSubscription.dispose();
			mChangeDirSubscription = null;
		}
	}

	@Override
	public StateCode getCode() {
		return StateCode.CHANGING_DIR;
	}

	private Single<List<FsItem>> changeDir(final String currentDirPath) {
		return Single.fromCallable(() -> {
			if (!mViewModel.getPermissionsProvider().isFsPermissionGranted()) {
				throw new FileBrowserException(FileBrowserInternalError.NO_PERMISSION);
			}

			final File dir = new File(currentDirPath);

			if (!dir.exists()) {
				throw new FileBrowserException(FileBrowserInternalError.NO_DIR);
			}

			if (dir.isFile()) {
				throw new FileBrowserException(FileBrowserInternalError.IS_FILE);
			}

			final String[] namesArray = dir.list();
			if (namesArray == null) {
				throw new FileBrowserException(FileBrowserInternalError.IO_ERROR);
			}

			final String parentPath = dir.getParent();
			final FsItem upItem = FsItem.createUpItem(parentPath);
			final Observable<FsItem> fsItemsStream = Observable.fromIterable(Arrays.asList(namesArray))
					.sorted()
					.map(filename -> {
						final FsItem fsItem = new FsItem();
						final File file = new File(currentDirPath + File.separator + filename);

						fsItem.setUp(false);
						fsItem.setParentPath(parentPath);
						fsItem.setName(filename);
						fsItem.setDir(file.isDirectory());
						fsItem.setLastModified(file.lastModified());
						fsItem.setSize(file.length());

						return fsItem;
					});

			final List<FsItem> fsItems;
			if (parentPath != null) {
				fsItems = fsItemsStream.startWith(upItem).toList().blockingGet();
			} else {
				fsItems = fsItemsStream.toList().blockingGet();
			}

			return fsItems;
		}).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
	}
}
