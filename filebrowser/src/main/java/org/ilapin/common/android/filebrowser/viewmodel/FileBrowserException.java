package org.ilapin.common.android.filebrowser.viewmodel;

class FileBrowserException extends Exception {

	private final FileBrowserInternalError mError;

	public FileBrowserException(final FileBrowserInternalError error) {
		mError = error;
	}

	public FileBrowserInternalError getError() {
		return mError;
	}
}
