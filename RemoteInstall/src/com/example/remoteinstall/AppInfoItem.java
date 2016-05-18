package com.example.remoteinstall;

import android.graphics.Bitmap;

public class AppInfoItem {

	Bitmap mAppBitmap;
	String mAppName;
	boolean bool;
	
	public AppInfoItem(Bitmap mAppMap, String mAppName/*, boolean bool*/) {
		super();
		this.mAppBitmap = mAppMap;
		this.mAppName = mAppName;
		//this.bool = bool;
	}
	
	/*public boolean getBool() {
		return bool;
	}

	public void setBool(boolean bool) {
		this.bool = bool;
	}*/

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
