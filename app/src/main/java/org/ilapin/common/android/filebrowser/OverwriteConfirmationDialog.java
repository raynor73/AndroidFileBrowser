package org.ilapin.common.android.filebrowser;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;

public class OverwriteConfirmationDialog extends DialogFragment {

	private static final String PATH_KEY = "PATH";

	private Listener mListener;

	public static OverwriteConfirmationDialog newInstance(final String path) {
		final Bundle args = new Bundle();
		final OverwriteConfirmationDialog fragment = new OverwriteConfirmationDialog();

		args.putString(PATH_KEY, path);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onAttach(final Context context) {
		super.onAttach(context);

		mListener = (Listener) context;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());

		final File file = new File(getArguments().getString(PATH_KEY));
		final String content = getString(R.string.file_browser_overwrite_confirmation_dialog_content, file.getName());
		builder
				.title(R.string.file_browser_overwrite_confirmation_title)
				.content(content)
				.positiveText(android.R.string.ok)
				.negativeText(android.R.string.cancel)
				.onPositive(((dialog, which) -> mListener.onOverwriteConfirmed(file.getAbsolutePath())));

		return builder.build();
	}

	interface Listener {

		void onOverwriteConfirmed(String path);
	}
}
