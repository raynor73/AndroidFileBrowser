package org.ilapin.common.android.filebrowser.viewmodel;

public interface PermissionsProvider {

	boolean isFsPermissionGranted();

	void requestFsPermission();

	boolean shouldShowFsPermissionRationale();
}
