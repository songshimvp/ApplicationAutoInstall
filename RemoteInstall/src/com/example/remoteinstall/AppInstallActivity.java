package com.example.remoteinstall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class AppInstallActivity extends Activity {

	private String appInstallingNames = "";
	private String[] appInstallingName = {};
	private ArrayAdapter<String> arrayAdapterInstalling;
	private ListView installingListview;

	private String successAppNames;
	private String[] successAppName;
	private ListView installSuccessListView;
	
	private String failedAppNames;
	private String[] failedAppName;
	private ListView installFailedListView;
	
	private SharedPreferences appInstallSharedPreferences;
	
	private String responseTime; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setIcon(null);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appinstall_activity);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		appInstallSharedPreferences = getSharedPreferences("appInstallInfo", Context.MODE_PRIVATE);		
		responseTime = appInstallSharedPreferences.getString("responseTime", "");

		// 正在安装的ListView
		appInstallingNames = appInstallSharedPreferences.getString("appInstalling", "");
		if (!("").equals(appInstallingNames)) {
			appInstallingName = appInstallingNames.split(",");

			installingListview = (ListView) findViewById(R.id.AppInstallingListView);
			installingListview.setVisibility(View.VISIBLE);

			arrayAdapterInstalling = new ArrayAdapter<String>(this,
					R.layout.appinstalling_list_item,
					R.id.appinstalling_list_item_text, appInstallingName);

			installingListview.setAdapter(arrayAdapterInstalling);
			setHeight(arrayAdapterInstalling, installingListview);
		}	
		
		//安装成功的ListView
		successAppNames = appInstallSharedPreferences.getString("successNames", "");
		if(!successAppNames.equals(""))
		{
			successAppName = successAppNames.split(",");

			installSuccessListView = (ListView) findViewById(R.id.AppInstallSuccessListView);
			installSuccessListView.setVisibility(View.VISIBLE);

			SimpleAdapter successAdapter = new SimpleAdapter(
					AppInstallActivity.this,
					getFSuccessData(),
					R.layout.appinstallsuccess_list_item,
					new String[] { "successAppName", "successTime" },
					new int[] {
							R.id.appinstallsuccess_list_item_nametext,
							R.id.appinstallsuccess_list_item_timetext });

			installSuccessListView.setAdapter(successAdapter);
			setHeight(successAdapter, installSuccessListView);
		}
		
		//安装失败的ListView
		failedAppNames = appInstallSharedPreferences.getString("failedNames", "");
		if(!failedAppNames.equals(""))
		{
			failedAppName = failedAppNames.split(",");
			installFailedListView = (ListView) findViewById(R.id.AppInstallFailedListView);
			installFailedListView.setVisibility(View.VISIBLE);

			SimpleAdapter failedAdapter = new SimpleAdapter(
					AppInstallActivity.this,
					getFailedData(),
					R.layout.appinstallfailed_list_item,
					new String[] { "failedAppName", "failedTime" },
					new int[] {
							R.id.appinstallfailed_list_item_nametext,
							R.id.appinstallfailed_list_item_timetext });

			installFailedListView.setAdapter(failedAdapter);
			setHeight(failedAdapter, installFailedListView);
		}
	}

	// 获取安装失败的列表的数据
	private List<Map<String, Object>> getFailedData() {
		int failedLength = failedAppName.length;

		List<Map<String, Object>> failedList = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		for (int i = 0; i < failedLength; i++) {
			map = new HashMap<String, Object>();
			map.put("failedAppName", failedAppName[i]);
			map.put("failedTime", responseTime);
			failedList.add(map);
		}

		return failedList;
	}

	//获取安装成功的列表的数据
	private List<Map<String, Object>> getFSuccessData() {
		int failedLength = successAppName.length;
		
		List<Map<String, Object>> successList = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		for (int i = 0; i < failedLength; i++) {
			map = new HashMap<String, Object>();
			map.put("successAppName", successAppName[i]);
			map.put("successTime", responseTime);
			successList.add(map);
		}

		return successList;
	}
	
	// 动态设置ArrayAdapterListView的高度
	public void setHeight(ArrayAdapter<String> arrayAdapter, ListView listview) {
		int totalHeightInstalling = 0;
		for (int i = 0; i < arrayAdapter.getCount(); i++)
		{
			View listItem = arrayAdapter.getView(i, null, installingListview);
			listItem.measure(0, 0);
			totalHeightInstalling += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listview.getLayoutParams();

		params.height = totalHeightInstalling
				+ (listview.getDividerHeight() * (arrayAdapter.getCount() - 1));

		listview.setLayoutParams(params);
	}

	// 动态设置SimpleAdapterListView的高度
	public void setHeight(SimpleAdapter failedAdapter, ListView listview) {
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
}
