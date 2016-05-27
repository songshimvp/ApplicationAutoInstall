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

import com.example.remoteinstall.AppListAdapter.AppCheckboxListener;

public class AppInstallSucAdapter extends ArrayAdapter<AppInstallSucInfo> {
	
	private static List<AppInstallSucInfo> mDatas;     //数据集
	private Context mContext;
	private LayoutInflater mInflater;

	//构造函数
	public AppInstallSucAdapter(Context context, List<AppInstallSucInfo> datas) {
		super(context, -1, datas);
		mContext=context;
		mDatas=datas;
		
		mInflater=LayoutInflater.from(context);
	}

	//Item上控件
	public class ViewHolder {
		public ImageView appLogoImageAppInstall;   //logo
		public TextView appNameAppInstall;         //名字
		public TextView appVersionAppInstall;      //版本（时间）
		public TextView appSucTime;                //安装成功时间
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.appinstallsuccess_list_item, parent, false);
			
			//控件的初始化,关联组件，提高效率
			holder=new ViewHolder();
			holder.appLogoImageAppInstall= (ImageView) convertView.findViewById(R.id.appLogoImageAppInstallSuccess);
			holder.appNameAppInstall=(TextView) convertView.findViewById(R.id.appinstallsuccess_list_item_nametext);
			holder.appVersionAppInstall = (TextView) convertView.findViewById(R.id.appVersionAppInstallSuccess);
	        holder.appSucTime = (TextView) convertView.findViewById(R.id.appinstallsuccess_list_item_timetext);
	        
			convertView.setTag(holder);   //设置标签，标识convertView
		}
		else{
			holder=(ViewHolder) convertView.getTag();
		}
		
		//设置控件的显示内容
		holder.appLogoImageAppInstall.setImageResource(getItem(position).getIconIdInstallSuc());
		holder.appNameAppInstall.setText(getItem(position).getmAppInstallSucName());
		holder.appVersionAppInstall.setText(getItem(position).getmAppInstallSucVersion());
		holder.appSucTime.setText(getItem(position).getmAppInstallSucTime());
		return convertView;
	}
	
}
