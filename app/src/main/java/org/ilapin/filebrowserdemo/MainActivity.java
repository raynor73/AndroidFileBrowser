package org.ilapin.filebrowserdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.ilapin.common.android.filebrowser.FileBrowserActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

	private static final int OPEN_FILE_REQUEST_CODE = 11;
	private static final int SAVE_FILE_REQUEST_CODE = 12;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);
	}

	@OnClick(R.id.openButton)
	public void onOpenButtonClicked() {
		final Intent intent = new Intent(this, FileBrowserActivity.class);
		intent.putExtra(FileBrowserActivity.MODE_KEY, FileBrowserActivity.MODE_OPEN);
		startActivityForResult(intent, OPEN_FILE_REQUEST_CODE);
	}

	@OnClick(R.id.saveButton)
	public void onSaveButtonClicked() {
		final Intent intent = new Intent(this, FileBrowserActivity.class);
		intent.putExtra(FileBrowserActivity.MODE_KEY, FileBrowserActivity.MODE_SAVE);
		intent.putExtra(FileBrowserActivity.FILENAME_KEY, "test.txt");
		startActivityForResult(intent, SAVE_FILE_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		final File file = new File(data.getStringExtra(FileBrowserActivity.PATH_KEY));

		switch (requestCode) {
			case OPEN_FILE_REQUEST_CODE:
				Toast.makeText(this, "Open file: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
				break;

			case SAVE_FILE_REQUEST_CODE:
				Toast.makeText(this, "Save file: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
				break;

			default:
				throw new RuntimeException("Unknown request code: " + requestCode);
		}
	}
}
