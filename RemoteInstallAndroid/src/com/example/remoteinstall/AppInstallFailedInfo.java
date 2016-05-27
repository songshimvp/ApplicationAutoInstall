package com.example.remoteinstall;


public class AppInstallFailedInfo {

	int iconIdInstallFailed;
	String mAppInstallFailedName;
	String mAppInstallFailedVersion;
	String mAppInstallFailedTime;
	
	AppInstallFailedInfo(int iconIdInstalling, String mAppInstallingName, String mAppInstallingVersion, String mAppInstallingTime)
	{
		this.iconIdInstallFailed = iconIdInstalling;
		this.mAppInstallFailedName = mAppInstallingName;
		this.mAppInstallFailedVersion = mAppInstallingVersion;
		this.mAppInstallFailedTime = mAppInstallingTime;
	}

	public int getIconIdInstallFailed() {
		return iconIdInstallFailed;
	}

	public void setIconIdInstallFailed(int iconIdInstallFailed) {
		this.iconIdInstallFailed = iconIdInstallFailed;
	}

	public String getmAppInstallFailedName() {
		return mAppInstallFailedName;
	}

	public void setmAppInstallFailedName(String mAppInstallFailedName) {
		this.mAppInstallFailedName = mAppInstallFailedName;
	}

	public String getmAppInstallFailedVersion() {
		return mAppInstallFailedVersion;
	}

	public void setmAppInstallFailedVersion(String mAppInstallFailedVersion) {
		this.mAppInstallFailedVersion = mAppInstallFailedVersion;
	}

	public String getmAppInstallFailedTime() {
		return mAppInstallFailedTime;
	}

	public void setmAppInstallFailedTime(String mAppInstallFailedTime) {
		this.mAppInstallFailedTime = mAppInstallFailedTime;
	}

	
}
