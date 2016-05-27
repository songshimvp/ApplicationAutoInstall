package com.example.remoteinstall;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remoteinstall.AppInstallSucAdapter.ViewHolder;
import com.example.remoteinstall.AppListAdapter.AppCheckboxListener;

public class AppInstallFailedAdapter extends ArrayAdapter<AppInstallFailedInfo> {
	
	private static List<AppInstallFailedInfo> mDatas;     //数据集
	private Context mContext;
	private LayoutInflater mInflater;

	//构造函数
	public AppInstallFailedAdapter(Context context, List<AppInstallFailedInfo> datas) {
		super(context, -1, datas);
		mContext=context;
		mDatas=datas;
		
		mInflater=LayoutInflater.from(context);
	}

	// Item上控件
	public class ViewHolder {
		public ImageView appLogoImageAppInstall; // logo
		public TextView appNameAppInstall; // 名字
		public TextView appVersionAppInstall; // 版本（时间）
		public TextView appFailedTime; // 安装失败时间
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.appinstallfailed_list_item, parent,
					false);

			// 控件的初始化,关联组件，提高效率
			holder = new ViewHolder();
			holder.appLogoImageAppInstall = (ImageView) convertView.findViewById(R.id.appLogoImageAppInstallfailed);
			holder.appNameAppInstall = (TextView) convertView.findViewById(R.id.appinstallfailed_list_item_nametext);
			holder.appVersionAppInstall = (TextView) convertView.findViewById(R.id.appVersionAppInstallFailed);
			holder.appFailedTime = (TextView) convertView.findViewById(R.id.appinstallfailed_list_item_timetext);

			convertView.setTag(holder); // 设置标签，标识convertView
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 设置控件的显示内容
		holder.appLogoImageAppInstall.setImageResource(getItem(position).getIconIdInstallFailed());
		holder.appNameAppInstall.setText(getItem(position).getmAppInstallFailedName());
		holder.appVersionAppInstall.setText(getItem(position).getmAppInstallFailedVersion());
		holder.appFailedTime.setText(getItem(position).getmAppInstallFailedTime());
		return convertView;
	}
	
}
