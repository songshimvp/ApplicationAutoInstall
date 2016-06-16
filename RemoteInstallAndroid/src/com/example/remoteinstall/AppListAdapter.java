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

public class AppListAdapter extends ArrayAdapter<AppInfoItem>  {

	// 用来控制CheckBox的选中状况  
    public static HashMap<Integer, Boolean> mCheckFlag = null;  
	
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
		
		mCheckFlag = new HashMap<Integer, Boolean>();  
        // 初始化数据  
        initDate();  
	}

	//Item上控件
	public class ViewHolder {
		public ImageView appLogoImageAppInstall;   //logo
		public TextView appNameAppInstall;         //名字
		public TextView appSizeAppInstall;         //大小
		public TextView appVersionAppInstall;      //版本（时间）
		public CheckBox appChooseCheck;            //是否选择
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		AppCheckboxListener appCheckboxListener;
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.app_list_items, parent, false);
			
			//控件的初始化,关联组件，提高效率
			holder=new ViewHolder();
			holder.appLogoImageAppInstall= (ImageView) convertView.findViewById(R.id.appLogoImageAppInstall);
			holder.appNameAppInstall=(TextView) convertView.findViewById(R.id.appNameTxtAppInstall);
			holder.appSizeAppInstall = (TextView) convertView.findViewById(R.id.appSizeAppInstall);
			holder.appVersionAppInstall = (TextView) convertView.findViewById(R.id.appVersionAppInstall);
			holder.appChooseCheck=(CheckBox) convertView.findViewById(R.id.appChooseCheckAppInstall);
	
			convertView.setTag(holder);   //设置标签，标识convertView
		}
		else{
			holder=(ViewHolder) convertView.getTag();
		}
		
		//Item上的CheckBox的监听器初始化
		appCheckboxListener =new AppCheckboxListener(position);
		
		//设置控件的显示内容
		holder.appNameAppInstall.setText(getItem(position).getmAppName());
		holder.appLogoImageAppInstall.setImageResource(getItem(position).getIconId());
		holder.appSizeAppInstall.setText(getItem(position).getmAppSize());
		holder.appVersionAppInstall.setText(getItem(position).getmAppVersion());
		
		//根据isSelected来设置CheckBox的选中状况  
		//holder.appChooseCheck.setTag(position);   
		holder.appChooseCheck.setFocusable(false);
		holder.appChooseCheck.setOnCheckedChangeListener(appCheckboxListener);

		//holder.appChooseCheck.setChecked(getmCBFlag().get(position)); 
		if(mDatas.get(position).checkType == AppInfoItem.TYPE_CHECKED)    //一定要把添加监听器的方法加到初始化view中checkBox状态的代码之前.
		{
			holder.appChooseCheck.setChecked(true);
		}
		else
		{
			holder.appChooseCheck.setChecked(false);
		}
		
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
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {			
			
			//局部“重置”字符串reqiredInstallAppNames
            String tmp = getItem(mPosition).getmAppName();
            //String tmpSize = getItem(mPosition).getmAppSize();
            //String tmpVersion = getItem(mPosition).getmAppVersion();
            
            //String tmpStr = tmp.substring(0, tmp.lastIndexOf(".")) + ",";   
            String tmpStr = tmp + ",";   
            reqiredInstallAppNames = reqiredInstallAppNames.replaceAll(tmpStr, "");
           
			String tmpName;
			//ViewHolder holder = null;
			if(isChecked == true)
			{
				getmCBFlag().put(mPosition, true);
				mDatas.get(mPosition).checkType = AppInfoItem.TYPE_CHECKED;
				
				//Toast toast = Toast.makeText(mContext, "选择安装" + getItem(mPosition).getmAppName(), Toast.LENGTH_SHORT);
				//showMyToast(toast, 500);
				
				tmpName = tmp  + ",";
				System.out.println("逐次:"+tmpName);
			}
			else
			{
				getmCBFlag().put(mPosition, false);
			    mDatas.get(mPosition).checkType = AppInfoItem.TYPE_NOCHECKED;
				//Toast toast = Toast.makeText(mContext, "取消安装" + getItem(mPosition).getmAppName(), Toast.LENGTH_SHORT);
				//showMyToast(toast, 500);
				
				tmpName = "";
			}
			
			reqiredInstallAppNames += tmpName;
			
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
	// 初始化mCheckFlag的数据  
    private void initDate() {  
    	for (int i = 0; i < mDatas.size(); i++) {
    		getmCBFlag().put(i, false);  
        }  
    }  
	
    public static HashMap<Integer, Boolean> getmCBFlag() {  
        return mCheckFlag;  
    }  
  
    public static void setmCBFlag(HashMap<Integer, Boolean> mCBFlag) {  
        AppListAdapter.mCheckFlag = mCBFlag;  
    }  
    
    @Override  
    public int getCount() {  
        return mDatas.size();  
    }  
  
    @Override  
    public AppInfoItem getItem(int i) {  
        return mDatas.get(i);  
    }  
  
    @Override  
    public long getItemId(int i) {  
        return i;  
    }  
}
