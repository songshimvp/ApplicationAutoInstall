package com.example.remoteinstall;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class AppListAdapter extends ArrayAdapter<AppInfoItem> {

	// 用来控制CheckBox的选中状况  
    private static HashMap<Integer, Boolean> isSelected;  
	
	private static List<AppInfoItem> mDatas;     //数据集
	private Context mContext;
	
	private LayoutInflater mInflater;

	public static String reqiredInstallAppNames = "";       //需要安装的Application的名字字符串
	//构造函数
	public AppListAdapter(Context context, List<AppInfoItem> datas) {
		super(context, -1, datas);
		mContext=context;
		mDatas=datas;
		
		mInflater=LayoutInflater.from(context);
		
		isSelected = new HashMap<Integer, Boolean>();  
        // 初始化数据  
        initDate();  
	}

	//Item上控件
	public class ViewHolder {
		public ImageView appLogoImageAppInstall;   //logo
		public TextView appNameAppInstall;         //名字
		public CheckBox appChooseCheck;            //是否选择
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.app_list_items, parent, false);
			
			//控件的初始化,关联组件，提高效率
			holder=new ViewHolder();
			holder.appLogoImageAppInstall= (ImageView) convertView.findViewById(R.id.appLogoImageAppInstall);
			holder.appNameAppInstall=(TextView) convertView.findViewById(R.id.appNameTxtAppInstall);
			holder.appChooseCheck=(CheckBox) convertView.findViewById(R.id.appChooseCheckAppInstall);
			
			convertView.setTag(holder);   //设置标签，标识convertView
		}
		else{
			holder=(ViewHolder) convertView.getTag();
		}
		
		//Item上的Checkbox的监听器初始化
		AppCheckboxListener appCheckboxListener =new AppCheckboxListener(position);
		
		//设置控件的显示内容
		holder.appNameAppInstall.setText(getItem(position).getmAppName());
		
		holder.appLogoImageAppInstall.setImageBitmap(getItem(position).getmAppBitmap());
		
		//根据isSelected来设置checkbox的选中状况  
        //holder.appChooseCheck.setChecked(getIsSelected().get(position));    //??????
		holder.appChooseCheck.setTag(position);
		holder.appChooseCheck.setFocusable(false);
		holder.appChooseCheck.setOnCheckedChangeListener(appCheckboxListener);
		
		return convertView;
	}

	//为软件是否选中（CheckBox）单独监听接口
	@SuppressLint("ShowToast")
	public class AppCheckboxListener implements OnCheckedChangeListener{

		int mPosition;

		public AppCheckboxListener(int position) {
			mPosition = position;
		}

		@SuppressLint("ShowToast")
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			
			//局部“重置”字符串reqiredInstallAppNames
            String tmp = getItem(mPosition).getmAppName();
            //String tmpStr = tmp.substring(0, tmp.lastIndexOf(".")) + ",";   
            String tmpStr = tmp + ",";   
            reqiredInstallAppNames = reqiredInstallAppNames.replaceAll(tmpStr, "");
           
			String tmpName;
			//ViewHolder holder = null;
			if(isChecked == true)
			{
				Toast toast = Toast.makeText(mContext, "选择安装" + getItem(mPosition).getmAppName(), Toast.LENGTH_SHORT);
				showMyToast(toast, 500);
				
				//tmpName = tmp.substring(0, tmp.lastIndexOf(".")) + ",";
				tmpName = tmp  + ",";
				//appName = getItem(mPosition).getmAppName() + ",";
				System.out.println("逐次:"+tmpName);
			}
			else
			{
				Toast toast = Toast.makeText(mContext, "取消安装" + getItem(mPosition).getmAppName(), Toast.LENGTH_SHORT);
				showMyToast(toast, 500);
				
				tmpName = "";
			}
			
			reqiredInstallAppNames += tmpName;
			//reqiredInstallAppNames =reqiredInstallAppNames.substring(0, reqiredInstallAppNames.length()-1);
            System.out.println("全部:"+reqiredInstallAppNames);
		}	
	}
	
	//自定义Toast
	public static void showMyToast(final Toast toast, final int cnt) {
	    final Timer timer = new Timer();
	    timer.schedule(new TimerTask() {
	        @Override
	        public void run() {
	            toast.show();
	        }
	    }, 0, 3000);
	    new Timer().schedule(new TimerTask() {
	        @Override
	        public void run() {
	            toast.cancel();
	            timer.cancel();
	        }
	    }, cnt);
	}
	
	/*
	 * 以下代码辅助实现“全选”“全部取消功能” —— http://blog.csdn.net/onlyonecoder/article/details/8687811
	 */
	// 初始化isSelected的数据  
    private void initDate() {  
        //for (int i = 0; i < isSelected.size(); i++) { 
    	for (int i = 0; i < mDatas.size(); i++) {
            getIsSelected().put(i, false);  
        }  
    }  
	
	public static HashMap<Integer, Boolean> getIsSelected() {  
        return isSelected;  
    }
	
	public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {  
		AppListAdapter.isSelected = isSelected;  
    }
}
