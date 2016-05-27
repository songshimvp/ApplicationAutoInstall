package com.example.remoteinstall;


public class AppInstallSucInfo {

	int iconIdInstallSuc;
	String mAppInstallSucName;
	String mAppInstallSucVersion;
	String mAppInstallSucTime;
	
	AppInstallSucInfo(int iconIdInstalling, String mAppInstallingName, String mAppInstallingVersion, String mAppInstallingTime)
	{
		this.iconIdInstallSuc = iconIdInstalling;
		this.mAppInstallSucName = mAppInstallingName;
		this.mAppInstallSucVersion = mAppInstallingVersion;
		this.mAppInstallSucTime = mAppInstallingTime;
	}

	public int getIconIdInstallSuc() {
		return iconIdInstallSuc;
	}

	public void setIconIdInstallSuc(int iconIdInstallSuc) {
		this.iconIdInstallSuc = iconIdInstallSuc;
	}

	public String getmAppInstallSucName() {
		return mAppInstallSucName;
	}

	public void setmAppInstallSucName(String mAppInstallSucName) {
		this.mAppInstallSucName = mAppInstallSucName;
	}

	public String getmAppInstallSucVersion() {
		return mAppInstallSucVersion;
	}

	public void setmAppInstallSucVersion(String mAppInstallSucVersion) {
		this.mAppInstallSucVersion = mAppInstallSucVersion;
	}

	public String getmAppInstallSucTime() {
		return mAppInstallSucTime;
	}

	public void setmAppInstallSucTime(String mAppInstallSucTime) {
		this.mAppInstallSucTime = mAppInstallSucTime;
	}

}
