package com.example.remoteinstall;

import android.graphics.Bitmap;

public class AppInfoItem {

	public static final int TYPE_CHECKED = 1;
	public static final int TYPE_NOCHECKED = 0;
	
	Bitmap mAppBitmap;
	String mAppName;
	String mAppSize;
	String mAppVersion;
	int iconId;
	int checkType;
	
	public AppInfoItem(int iconId, String mAppName, String mAppSize, String mAppVersion, int checkType) {
		super();
		//this.mAppBitmap = mAppMap;
		this.iconId = iconId;
		this.mAppName = mAppName;
		this.mAppSize = mAppSize;
		this.mAppVersion = mAppVersion;
		this.checkType = checkType;
		//this.bool = bool;
	}

	public String getmAppSize() {
		return mAppSize;
	}

	public void setmAppSize(String mAppSize) {
		this.mAppSize = mAppSize;
	}

	public String getmAppVersion() {
		return mAppVersion;
	}

	public void setmAppVersion(String mAppVersion) {
		this.mAppVersion = mAppVersion;
	}

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	public Bitmap getmAppBitmap() {
		return mAppBitmap;
	}

	public void setmAppBitmap(Bitmap mAppBitmap) {
		this.mAppBitmap = mAppBitmap;
	}

	public String getmAppName() {
		return mAppName;
	}

	public void setmAppName(String mAppName) {
		this.mAppName = mAppName;
	}

}
