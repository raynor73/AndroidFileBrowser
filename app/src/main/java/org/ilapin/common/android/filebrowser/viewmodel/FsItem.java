package org.ilapin.common.android.filebrowser.viewmodel;

public class FsItem {

	private boolean mIsUp;
	private String mParentPath;
	private boolean mIsDir;
	private String mName;
	private long mSize;
	private long mLastModified;

	public boolean isUp() {
		return mIsUp;
	}

	public void setUp(final boolean up) {
		mIsUp = up;
	}

	public String getParentPath() {
		return mParentPath;
	}

	public void setParentPath(final String parentPath) {
		mParentPath = parentPath;
	}

	public boolean isDir() {
		return mIsDir;
	}

	public void setDir(final boolean dir) {
		mIsDir = dir;
	}

	public String getName() {
		return mName;
	}

	public void setName(final String name) {
		this.mName = name;
	}

	public long getSize() {
		return mSize;
	}

	public void setSize(final long size) {
		this.mSize = size;
	}

	public long getLastModified() {
		return mLastModified;
	}

	public void setLastModified(final long lastModified) {
		this.mLastModified = lastModified;
	}

	public static FsItem createUpItem(final String parentPath) {
		final FsItem item = new FsItem();
		item.mIsUp = true;
		item.mParentPath = parentPath;
		return item;
	}
}
