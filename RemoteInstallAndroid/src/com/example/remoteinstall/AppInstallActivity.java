package com.example.remoteinstall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class AppInstallActivity extends Activity {

	private String appInstallingNames = "";
	private String[] appInstallingName = {};
	private SimpleAdapter arrayAdapterInstalling;
	List<Map<String, Object>> installingList;
	private ListView installingListview;

	private String successAppNames;
	private String[] successAppName;
	private String responseSuccessTimes; 
	private String[] responseSuccessTime; 
	private AppInstallSucAdapter successAdapter;
	List<AppInstallSucInfo> successList;
	private ListView installSuccessListView;
	
	private String failedAppNames;
	private String[] failedAppName;
	private String responseFailedTimes; 
	private String[] responseFailedTime; 
	private AppInstallFailedAdapter failedAdapter;
	List<AppInstallFailedInfo> failedList;
	private ListView installFailedListView;
	
	private SharedPreferences appInstallSharedPreferences;
	private SharedPreferences.Editor appInstallEditor;
	
	private ImageView refreshImage;
	private ImageView deleteFailedImage;
	private ImageView deleteSuccessImage;
	
	private int[] appIcons = { R.drawable.icon_qq, R.drawable.icon_weixin,
			R.drawable.icon_qqgame, R.drawable.icon_config,
			R.drawable.icon_sougoupinyin, R.drawable.icon_tecentvedio,
			R.drawable.icon_wangyimusic, R.drawable.icon_wps,
			R.drawable.icon_youku, R.drawable.icon_xunlei, R.drawable.icon_other};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//getActionBar().setDisplayShowHomeEnabled(false);
		//getActionBar().setIcon(null);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.appinstall_activity);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		refreshImage= (ImageView) findViewById(R.id.refreshAppInstallList);
		deleteSuccessImage = (ImageView) findViewById(R.id.clearSuccessList);
		deleteFailedImage=(ImageView) findViewById(R.id.clearFailedList);
		
		appInstallSharedPreferences = getSharedPreferences("appInstallInfo", Context.MODE_PRIVATE);		
		
		// 正在安装的ListView
		installingList = new ArrayList<Map<String,Object>>();
		appInstallingNames = appInstallSharedPreferences.getString("appInstalling", "");
		if (!("").equals(appInstallingNames)) {
			appInstallingName = appInstallingNames.split(",");

			installingListview = (ListView) findViewById(R.id.AppInstallingListView);
			installingListview.setVisibility(View.VISIBLE);

			arrayAdapterInstalling = new SimpleAdapter(
					AppInstallActivity.this,
					getInsltallData(),
					R.layout.appinstalling_list_item,
					new String[] {"installingImage", "installingAppName", "installingAppVersion", "installingState"},
					new int[] { R.id.appLogoImageAppInstalling,
							R.id.appinstalling_list_item_nametext,
							R.id.appVersionAppInstalling, R.id.txtInstallState});

			installingListview.setAdapter(arrayAdapterInstalling);
			setHeight(arrayAdapterInstalling, installingListview);
		}	
		
		//安装成功的ListView
		successList = new ArrayList<AppInstallSucInfo>();
		successAppNames = appInstallSharedPreferences.getString("successNames", "");
		responseSuccessTimes = appInstallSharedPreferences.getString("responseSuccessTimes", "");
		
		if(!successAppNames.equals(""))
		{
			successAppName = successAppNames.split(",");
			responseSuccessTime = responseSuccessTimes.split(",,");
			
			installSuccessListView = (ListView) findViewById(R.id.AppInstallSuccessListView);
			installSuccessListView.setVisibility(View.VISIBLE);

			successAdapter = new AppInstallSucAdapter(this, successList);
			installSuccessListView.setAdapter(successAdapter);
			
			getSuccessData();
			
			setHeightSuc(successAdapter, installSuccessListView);
		}
		
		//安装失败的ListView
		failedList = new ArrayList<AppInstallFailedInfo>();
		failedAppNames = appInstallSharedPreferences.getString("failedNames", "");
		responseFailedTimes = appInstallSharedPreferences.getString("responseFailedTimes", "");
		
		if(!failedAppNames.equals(""))
		{
			failedAppName = failedAppNames.split(",");
			responseFailedTime = responseFailedTimes.split(",,");
			
			installFailedListView = (ListView) findViewById(R.id.AppInstallFailedListView);
			installFailedListView.setVisibility(View.VISIBLE);

			failedAdapter = new AppInstallFailedAdapter(this, failedList);
			installFailedListView.setAdapter(failedAdapter);
			
			getFailedData();
			setHeightFail(failedAdapter, installFailedListView);
		}
		
		//刷新页面
		refreshImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onCreate(null);
			}
		});
		
		//清空安装成功的历史记录
		deleteSuccessImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				appInstallEditor = appInstallSharedPreferences.edit();
				appInstallEditor.putString("successNames", "");
				appInstallEditor.putString("responseSuccessTimes", "");
				
				appInstallEditor.commit();
				
				if(successList.size()!=0)
				{
					successList.clear();
					successAdapter.notifyDataSetChanged();
					setHeightSuc(successAdapter, installSuccessListView);
				}
			}
		});
		
		//清空安装失败的历史记录
		deleteFailedImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				appInstallEditor = appInstallSharedPreferences.edit();
				appInstallEditor.putString("failedNames", "");
				appInstallEditor.putString("responseFailedTimes", "");
				
				appInstallEditor.commit();
				
				if(failedList.size()!=0)
				{
					failedList.clear();
					failedAdapter.notifyDataSetChanged();
					setHeightFail(failedAdapter, installFailedListView);
				}
				
			}
		});
		
	}

	// 获取正在安装的列表的数据
	private List<Map<String, Object>> getInsltallData() {
		int installingLength = appInstallingName.length;

		Map<String, Object> map;
		for (int i = 0; i < installingLength; i++) {
			map = new HashMap<String, Object>();
			
			int imageId = appIcons[AppListActivity.adaptLocalImage(appInstallingName[i])];
			map.put("installingImage", imageId);
			
			map.put("installingAppName", appInstallingName[i]);
			
			String tmpVersion = getAppVersion(appInstallingName[i]);
			map.put("installingAppVersion", tmpVersion);
			
			if(i == 0)
			{
				map.put("installingState", "正在安装");
			}
			else
			{
				map.put("installingState", "等待安装");
			}
			installingList.add(map);
		}

		return installingList;
	}
	
	// 获取安装成功的列表的数据
	private void getSuccessData() {
		int successLength = successAppName.length;

		for (int i = 0; i < successLength; i++) {
			
			int imageId = appIcons[AppListActivity.adaptLocalImage(successAppName[i])];
			String tmpVersion = getAppVersion(successAppName[i]);
			
			AppInstallSucInfo appInstallingInfo = new AppInstallSucInfo(imageId, successAppName[i], tmpVersion, responseSuccessTime[i]); 
			
			successList.add(appInstallingInfo);
			successAdapter.notifyDataSetChanged();
		}
	}
		
	// 获取安装失败的列表的数据
	private void getFailedData() {
		int failedLength = failedAppName.length;
		
		Map<String, Object> map;
		for (int i = 0; i < failedLength; i++) {
			
			int imageId = appIcons[AppListActivity.adaptLocalImage(failedAppName[i])];
			String tmpVersion = getAppVersion(failedAppName[i]);
			
			AppInstallFailedInfo appInstallFailedInfo = new AppInstallFailedInfo(imageId, failedAppName[i], tmpVersion, responseFailedTime[i]); 
			
			failedList.add(appInstallFailedInfo);
			failedAdapter.notifyDataSetChanged();
		}
	}

	// 动态设置arrayAdapterInstalling的高度
	public void setHeight(SimpleAdapter arrayAdapterInstalling2, ListView listview) {
		int totalHeightInstalling = 0;
		for (int i = 0; i < arrayAdapterInstalling2.getCount(); i++) {
			View listItem = arrayAdapterInstalling2.getView(i, null, installingListview);
			listItem.measure(0, 0);
			totalHeightInstalling += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listview.getLayoutParams();

		params.height = totalHeightInstalling
				+ (listview.getDividerHeight() * (arrayAdapterInstalling2.getCount() - 1));

		listview.setLayoutParams(params);
	}

	// 动态设置AppInstallSucAdapter的高度
	public void setHeightSuc(AppInstallSucAdapter sucAdapter, ListView listview) {
		int totalHeightInstalling = 0;
		for (int i = 0; i < sucAdapter.getCount(); i++)
		{
			View listItem = sucAdapter.getView(i, null, installingListview);
			listItem.measure(0, 0);
			totalHeightInstalling += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listview.getLayoutParams();

		params.height = totalHeightInstalling
				+ (listview.getDividerHeight() * (sucAdapter.getCount() - 1));

		listview.setLayoutParams(params);
	}

	// 动态设置AppInstallFailedAdapter的高度
	public void setHeightFail(AppInstallFailedAdapter failedAdapter, ListView listview) {
		int totalHeightInstalling = 0;
		for (int i = 0; i < failedAdapter.getCount(); i++) {
			View listItem = failedAdapter.getView(i, null, installingListview);
			listItem.measure(0, 0);
			totalHeightInstalling += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listview.getLayoutParams();

		params.height = totalHeightInstalling
				+ (listview.getDividerHeight() * (failedAdapter.getCount() - 1));

		listview.setLayoutParams(params);
	}	
	
	// 重写“返回键”
	@Override
	public void onBackPressed() {
		startActivity(new Intent(AppInstallActivity.this, AppListActivity.class));
		AppListAdapter.reqiredInstallAppNames = "";
	}
	
	// 此处应该在从服务器获取到以后存入数据库，然后直接从数据库中取相应字段
	// 获取相应应用的大小
	private String getAppSize(String appName) {
		
		String appSize = "";
		if (!AppListActivity.resultStr.equals("")) {
			String[] nameSizeTimeStrs = AppListActivity.resultStr.split(",");

			for (int i = 0; i < nameSizeTimeStrs.length; i++) {
				String[] nameSizeTimeStr = nameSizeTimeStrs[i].split("!!!");
				String tmpName = nameSizeTimeStr[0].substring(0,nameSizeTimeStr[0].indexOf("."));
				if (tmpName.equals(appName)) {
					appSize = nameSizeTimeStr[1] + "M";
				}
			}
		}
		return appSize;
	}
	
	// 获取相应应用的版本
	private String getAppVersion(String appName) {
		String appVersion = "";
		if(!AppListActivity.resultStr.equals(""))
		{
			String[] nameSizeTimeStrs= AppListActivity.resultStr.split(",");
			
			
			for(int i=0;i<nameSizeTimeStrs.length;i++)
			{
				String[] nameSizeTimeStr = nameSizeTimeStrs[i].split("!!!");
				String tmpName = nameSizeTimeStr[0].substring(0, nameSizeTimeStr[0].indexOf("."));
				if(tmpName.equals(appName))
				{
					appVersion = nameSizeTimeStr[2];
				}
				
			}
		}
		return appVersion;
	}
}
