package org.ilapin.common.android.filebrowser;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;
import com.afollestad.materialdialogs.MaterialDialog;

public class CreateDirectoryDialog extends DialogFragment {

	private EditText mDirectoryNameEditText;

	private Listener mListener;

	@Override
	public void onAttach(final Context context) {
		super.onAttach(context);

		mListener = (Listener) getContext();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());

		builder
				.title(R.string.file_browser_create_folder)
				.customView(R.layout.dialog_create_directory, false)
				.positiveText(android.R.string.ok)
				.negativeText(android.R.string.cancel)
				.onPositive(((dialog, which) -> {
					final Editable nameEditable = mDirectoryNameEditText.getText();
					if (nameEditable != null) {
						final String name = nameEditable.toString();
						if (!TextUtils.isEmpty(name)) {
							mListener.onDirectoryNameProvided(name);
						}
					}
				}));

		final MaterialDialog dialog = builder.build();
		mDirectoryNameEditText = dialog.getContentView().findViewById(R.id.directoryName);
		return dialog;
	}

	public interface Listener {

		void onDirectoryNameProvided(String name);
	}
}
