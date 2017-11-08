package org.ilapin.common.android.filebrowser.viewmodel;

import android.content.SharedPreferences;
import android.text.TextUtils;

public class NewState extends BaseState {

	public NewState(final FileBrowserViewModel viewModel) {
		super(viewModel);
	}

	@Override
	public void restore(final String preferredPath) {
		super.restore(preferredPath);

		final SharedPreferences preferences = mViewModel.getPreferences();
		if (!TextUtils.isEmpty(preferredPath)) {
			mViewModel.getCurrentDirPathSubject().onNext(preferredPath);
		} else if (preferences.contains(FileBrowserViewModel.CURRENT_DIR_PATH_KEY)) {
			mViewModel.getCurrentDirPathSubject().onNext(
					preferences.getString(FileBrowserViewModel.CURRENT_DIR_PATH_KEY, "")
			);
		} else {
			mViewModel.getCurrentDirPathSubject().onNext(mViewModel.getDefaultPath());
		}

		mViewModel.changeState(mViewModel.getChangingDirState());
	}

	@Override
	public StateCode getCode() {
		return StateCode.NEW;
	}
}
