package com.example.remoteinstall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.remoteinstallutil.AppSendImageThread;
import com.example.remoteinstallutil.AppSendImageThread.MyCallBack;
import com.example.remoteinstallutil.AppSendInfoThread;
import com.example.remoteinstallutil.CurrentTime;

public class AppListActivity extends Activity {

	private ImageView syncImage;
	
	private Button btnAllChoose;
	private Button btnInstall;       //一键安装需要和远程客户机交互
	public static boolean btnVisible = true;   //设置“一键安装”按钮是否显示
	private Button btnProgress;     //安装进度监视
	
	private int[] appIcons = { R.drawable.icon_qq, R.drawable.icon_weixin,
			R.drawable.icon_qqgame, R.drawable.icon_config,
			R.drawable.icon_sougoupinyin, R.drawable.icon_tecentvedio,
			R.drawable.icon_wangyimusic, R.drawable.icon_wps,
			R.drawable.icon_youku,R.drawable.icon_xunlei, R.drawable.icon_other};
	
	private ListView mAppListView;   //App列表需要和服务器软件库交互
	private AppListAdapter mAppAdapter;
	private List<AppInfoItem> mAppDatas;
	
	public static String resultStr = "";
	private String allNames;
	
	private SharedPreferences appInstallSharedPreferences;
	private SharedPreferences.Editor appInstallEditor;
	
	//public String oldReqiredInstallAppNames = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.app_list);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		syncImage = (ImageView) findViewById(R.id.syncImageAppInstall);	
		
		mAppDatas = new ArrayList<AppInfoItem>();
		mAppListView = (ListView) findViewById(R.id.appListViewAppInstall);
		mAppAdapter = new AppListAdapter(AppListActivity.this, mAppDatas);
		mAppListView.setAdapter(mAppAdapter);
		
		btnAllChoose = (Button) findViewById(R.id.btnAllChooseApp);
		btnInstall =(Button) findViewById(R.id.btnInstallApp);
		btnProgress =(Button) findViewById(R.id.btnUninstallApp);
		
		appInstallSharedPreferences = getSharedPreferences("appInstallInfo", Context.MODE_PRIVATE);
		
		if(btnVisible == false)
		{
			ColorStateList color = getResources().getColorStateList(R.drawable.lightred);
			
			//设置“全部安装”按钮禁用
			btnAllChoose.setEnabled(false);
			btnAllChoose.setTextColor(color);
			
			//设置“一键安装”按钮禁用
			btnInstall.setEnabled(false);
			btnInstall.setTextColor(color);
			
			
			//设置“安装进度”按钮MATCH_PARENT
			/*LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 165);
			layoutParams.setMargins(50, 5, 50, 7);//4个参数按顺序分别是左上右下

			btnProgress.setLayoutParams(layoutParams);
			btnProgress.setText("安      装      进      度");*/
			ColorStateList color1 = getResources().getColorStateList(R.drawable.green);
			btnProgress.setTextColor(color1);
		}
		
		
		final AppSendImageThread sendMsgThread = new AppSendImageThread(handlerImage, AppListActivity.this, "appInstallIni", new MyCallBack() 
		{
			@Override
			public void getResult(String application_arrs,Bitmap[] bitmapArrs)
			{
				Message message=handlerImage.obtainMessage();
				if(null==application_arrs||null==bitmapArrs)
				{
					message.what=6;
					handlerImage.sendMessage(message);
				}
				else
				{
					message.obj=bitmapArrs;    //图片
					
					Bundle bundle=new Bundle();
					bundle.putString("appListMsg", application_arrs);
					message.setData(bundle);   //名字
					message.what=2;
					handlerImage.sendMessage(message);
				}
			}
		});
		Thread thread = new Thread(sendMsgThread);
		thread.start();
		
		
		syncImage.setOnClickListener(new OnClickListener() {      //刷新
			
			@Override
			public void onClick(View v) {
				//首先清空ListView以前的数据
				mAppAdapter.clear();
				mAppAdapter.notifyDataSetChanged();
				
				//重置安装文件字符串
				AppListAdapter.reqiredInstallAppNames = "";
				 
				//加载新数据
				Thread thread = new Thread(sendMsgThread);
				thread.start();
			}
		});
		
		btnAllChoose.setOnClickListener(new OnClickListener() {   // 全部选择/全部取消————全部安装
			
			@Override
			public void onClick(View v) {
				/*if(allChooseFlag)
				{
					for (int i = 0; i < mAppDatas.size(); i++) {  
						AppListAdapter.getmCBFlag().put(i, true);  
	                } 
					for (int i = 0; i < mAppDatas.size(); i++) {  
			            mAppDatas.get(i).checkType = AppInfoItem.TYPE_CHECKED;
			            mAppAdapter.notifyDataSetChanged();  
			        } 
					allChooseFlag = false;
					btnAllChoose.setText("全部取消");
				}
				else
				{
					for (int i = 0; i < mAppDatas.size(); i++) {  
			            mAppDatas.get(i).checkType = AppInfoItem.TYPE_NOCHECKED;
			            mAppAdapter.notifyDataSetChanged();  
			        } 
					allChooseFlag = true;
					btnAllChoose.setText("全部选择");
				}*/
				
				AppListAdapter.reqiredInstallAppNames = "";	   //防止用户已经选择某些应用项，而发生冲突
				
				allNames = "";
				
				if(resultStr!="")
				{
					String[] nameSizeTimeStrs= resultStr.split(",");
					for(int i=0;i<nameSizeTimeStrs.length;i++)
					{
						String[] nameSizeTimeStr = nameSizeTimeStrs[i].split("!!!");
						String tmpName = nameSizeTimeStr[0].substring(0, nameSizeTimeStr[0].indexOf("."));
						allNames += tmpName + ",";
					}
					System.out.println("全部安装："+allNames);
					
					
					AppSendInfoThread sendAppMsg=new AppSendInfoThread(handler, AppListActivity.this, allNames);     //向客户端发送allNames
					Thread thread=new Thread(sendAppMsg);
					thread.start();
				}
				else
				{
					Toast.makeText(AppListActivity.this, "服务器连接错误", Toast.LENGTH_LONG).show();
				}
				
			}
		});
		
		btnInstall.setOnClickListener(new OnClickListener() {     //一键安装
			
			@Override
			public void onClick(View v) {
				
				//“AppListAdapter.reqiredInstallAppNames”是原生的、未做任何判断的、从checkbox直接获得的要求安装的AppName字符串
				//SharedPreferences应用卸载后还在，但应该是卸载后所有东西重置！
				allNames = "";
				
				 
				AppSendInfoThread sendAppMsg=new AppSendInfoThread(handler, AppListActivity.this, AppListAdapter.reqiredInstallAppNames);   //向客户端发送appStrs
				Thread thread=new Thread(sendAppMsg);
				thread.start();							
			}
		});
		
		btnProgress.setOnClickListener(new OnClickListener() {   //打开安装进度监视
			
			@Override
			public void onClick(View v) {
				
				Intent AppInstallActivityIntent = new Intent();
				AppInstallActivityIntent.setClass(AppListActivity.this, AppInstallActivity.class);
				
				/*oldReqiredInstallAppNames = AppListAdapter.reqiredInstallAppNames;
				Bundle bundle = new Bundle();
				bundle.putString("requiredInstallAppNames", oldReqiredInstallAppNames);
				AppInstallActivityIntent.putExtras(bundle);*/
				startActivity(AppInstallActivityIntent);
			}
		});
		
	}
	
	//与服务器交互安装文件图片、名字的Handler
	public Handler handlerImage=new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch(msg.what)
			{
				case 0:
					Toast.makeText(AppListActivity.this, "服务器IP地址不正确", Toast.LENGTH_LONG).show();
					break;
				case 1:
					Toast.makeText(AppListActivity.this, "服务器已关闭", Toast.LENGTH_LONG).show();
					break;
				case 2: //连接成功				
					Bitmap[] bitmapArrs=(Bitmap[]) msg.obj;   // 从网络上软件服务器获取图片
					
					resultStr=msg.getData().getString("appListMsg");
					String[] nameSizeTimeStrs= resultStr.split(",");
					
					for(int i=0;i<nameSizeTimeStrs.length;i++)
					{
						String[] nameSizeTimeStr = nameSizeTimeStrs[i].split("!!!");
						String tmpName = nameSizeTimeStr[0].substring(0, nameSizeTimeStr[0].indexOf("."));
						int iconNum = appIcons[adaptLocalImage(tmpName)];    //使用本地图片
						
						//AppInfoItem appInfoItem = new AppInfoItem(bitmapArrs[i], tmp/*, false*/);
						String tmpSize = nameSizeTimeStr[1] + "M";
						String tmpVersion = nameSizeTimeStr[2];
						
						AppInfoItem appInfoItem = new AppInfoItem(iconNum, tmpName, tmpSize, tmpVersion, AppInfoItem.TYPE_NOCHECKED);   //初始化时都是未选中状态
						
						mAppDatas.add(appInfoItem);
						mAppAdapter.notifyDataSetChanged();
					}	
					
					break;	
				case 5:
					Toast.makeText(AppListActivity.this, "连接服务器发生错误", Toast.LENGTH_LONG).show();
					break;
				default:
					Toast.makeText(AppListActivity.this, "未获取服务器软件列表字符串", Toast.LENGTH_LONG).show();
					break;
			}
		}
	};
	
	//与客户机交互需要安装的Application的Handler
	private Handler handler=new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
				switch (msg.what)
				{
					case 0:
						Toast.makeText(AppListActivity.this, "对不起，远程客户机拒绝连接", Toast.LENGTH_SHORT).show();
						break;
					case 1:
						Toast.makeText(AppListActivity.this, "对不起，远程客户机已关闭连接", Toast.LENGTH_LONG).show();
						break;
					case 2:
						//连接成功进行处理
						if(AppListAdapter.reqiredInstallAppNames!="")
						{
							String tmp = AppListAdapter.reqiredInstallAppNames;
							tmp = tmp.substring(0, tmp.length()-1);
							Toast toast = Toast.makeText(AppListActivity.this, "正在远程安装：" + tmp, Toast.LENGTH_LONG);
							AppListAdapter.showMyToast(toast, 3000);
							
							ReceiveAsyncTask receiveAsyncTask =new ReceiveAsyncTask();
							receiveAsyncTask.execute(AppSendInfoThread.socketClient);    //异步接收Windows回应的消息
		
							appInstallEditor = appInstallSharedPreferences.edit();
							appInstallEditor.putString("appInstalling", AppListAdapter.reqiredInstallAppNames);
							appInstallEditor.commit();
							
							//finish();
							Intent AppInstallActivityIntent = new Intent();
							AppInstallActivityIntent.setClass(AppListActivity.this, AppInstallActivity.class);
							startActivity(AppInstallActivityIntent);	
							
							btnVisible = false;
							
							/*// 遍历list的长度，将AppListAdapter中的checkbox全部设为false  
			                for (int i = 0; i < mAppDatas.size(); i++) {  
			                	//((CheckBox)mAppListView.getChildAt(i).findViewById(R.id.appChooseCheckAppInstall)).setChecked(false);
			                	AppListAdapter.getIsSelected().put(i, false);  
			                }                  
							//重置安装文件字符串
							AppListAdapter.reqiredInstallAppNames = "";*/
						}
						else if(!allNames.equals(""))
						{
							String tmp = allNames;
							tmp = tmp.substring(0, tmp.length()-1);
							Toast toast = Toast.makeText(AppListActivity.this, "正在远程安装：" + tmp, Toast.LENGTH_LONG);
							AppListAdapter.showMyToast(toast, 3000);
							
							ReceiveAsyncTask receiveAsyncTask =new ReceiveAsyncTask();
							receiveAsyncTask.execute(AppSendInfoThread.socketClient);    //异步接收Windows回应的消息
		
							appInstallEditor = appInstallSharedPreferences.edit();
							appInstallEditor.putString("appInstalling", allNames);
							appInstallEditor.commit();
							
							//finish();
							Intent AppInstallActivityIntent = new Intent();
							AppInstallActivityIntent.setClass(AppListActivity.this, AppInstallActivity.class);
							startActivity(AppInstallActivityIntent);	
							
							btnVisible = false;
						}
						else
						{
							Toast.makeText(AppListActivity.this, "未选择任何应用", Toast.LENGTH_LONG).show();	
						}
						
						break;		
					case 5:
						Toast.makeText(AppListActivity.this, "对不起，远程客户机发生错误", Toast.LENGTH_LONG).show();
						
						break;
					default:
						break;
				}
		}
		
	};
	
	// 异步接收Windows回应消息
	public class ReceiveAsyncTask extends AsyncTask<Socket, String, String> {

		private String windowsResponseInfo;

		/**
		 * 运行在UI线程中，在调用doInBackground()之前执行
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		/**
		 * 后台运行的方法，可以运行非UI线程，可以执行耗时的方法
		 */
		@Override
		protected String doInBackground(Socket... socketClient) {
			/*
			 * Thread thread = new Thread(){  //不要阻塞doInBackground()，应该新建一个线程来完成可能导致阻塞的操作。
			 * public void run() {
			 * }; 
			 * thread.start(); 
			 * try { 
			 * thread.join(); //阻塞当前线程，等待thread执行完毕，可以带参数，最多等待多长时间
			 * } catch (Exception e) 
			 * { //
			 * Thread.join()可以被interrupt，调用AsyncTask.cancel(true);即可退出等待 
			 * return null; }
			 */
			String installingNames = "";
			if(!AppListAdapter.reqiredInstallAppNames.equals(""))
			{
				installingNames = AppListAdapter.reqiredInstallAppNames;
			}
			else
			{
				installingNames = allNames;
			}
			
			boolean closeFlag = true;
			while(closeFlag)
			{
				try {
					InputStream inputStream = socketClient[0].getInputStream();
					BufferedReader buffer = new BufferedReader(
							new InputStreamReader(inputStream));
					// private final int TIME_OUT = 2 * 1000;// 最大响应时间，超时设置
					// socketClient.setSoTimeout(TIME_OUT);
					// 此处不设置时延————远程下载、安装时间不定

					windowsResponseInfo = buffer.readLine();
					System.out.println("Windows客户机响应:" + windowsResponseInfo);

					if(windowsResponseInfo != null && !windowsResponseInfo.equals(""))
					{
						//处理失败字符串
						String tmp1 = windowsResponseInfo.split("!!!")[0];
						String failedNames;
						if(tmp1.equals("failed"))
						{
							failedNames = "";
						}else if(tmp1.equals("error"))    // 客户机出现异常
						{
							failedNames = "远程安装异常";
						}else
						{
							failedNames = tmp1;
						}
						
						//处理成功字符串
						String tmp2 = windowsResponseInfo.split("!!!")[1];
						String successNames;
						if(tmp2.equals("success"))
						{
							successNames = "";
						}else if(tmp2.equals("error"))   // 客户机出现异常
						{
							successNames = "";
						}else
						{
							successNames = tmp2;
						}
						
						//处理时间字符串
						String responseTime =  CurrentTime.getCurrentTime();
						String responseFailedTime = responseTime + ",,";
						String responseSuccessTime = responseTime + ",,";
						int length1 = failedNames.split(",").length;
						System.out.println("length1:::"+length1);
						for(int i=1; i<length1; i++)
						{
							responseFailedTime += responseFailedTime;
							responseFailedTime += ",,";
						}
						responseFailedTime = responseFailedTime.replaceAll(",,,,", ",,");
						
						int length2 = successNames.split(",").length;
						System.out.println("length2:::"+length2);
						for(int i=1; i<length2; i++)
						{
							responseSuccessTime += responseSuccessTime;
							responseSuccessTime += ",,";
						}
						responseSuccessTime = responseSuccessTime.replaceAll(",,,,", ",,");
						
						//处理正在安装字符串
						installingNames= installingNames.replaceAll(failedNames, "");
						installingNames = installingNames.replaceAll(successNames, "");
						
						String oldFailedNames = appInstallSharedPreferences.getString("failedNames", "");
						String oldSuccessNames = appInstallSharedPreferences.getString("successNames", "");
						String oldResponseFailedTimes = appInstallSharedPreferences.getString("responseFailedTimes", "");
						String oldResponseSuccessTimes = appInstallSharedPreferences.getString("responseSuccessTimes", "");
						
						appInstallEditor = appInstallSharedPreferences.edit();
						appInstallEditor.putString("failedNames", failedNames + oldFailedNames);
						appInstallEditor.putString("successNames", successNames + oldSuccessNames);
						appInstallEditor.putString("responseFailedTimes", responseFailedTime + oldResponseFailedTimes);
						appInstallEditor.putString("responseSuccessTimes", responseSuccessTime + oldResponseSuccessTimes);
						
						System.out.println("failedNames:"+failedNames);
						System.out.println("successNames:"+successNames);
						System.out.println("responseFailedTime + oldResponseFailedTimes:"+responseFailedTime + oldResponseFailedTimes);
						System.out.println("responseSuccessTime + oldResponseSuccessTimes:"+responseSuccessTime + oldResponseSuccessTimes);
						
						appInstallEditor.putString("appInstalling", installingNames);
						
						appInstallEditor.commit();
						
						String tmpIn = appInstallSharedPreferences.getString("appInstalling", "ing");
						System.out.println("Installing+++:"+tmpIn);
						if("".equals(tmpIn))
						{
							//安装完毕
							System.out.println("+++:+++");
							btnVisible = true;    //显示“全部安装”和“一键安装”按钮
							closeFlag = false;    //不再接收客户机回应
							try {
								if (socketClient[0] != null)
									socketClient[0].close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							startActivity(new Intent(AppListActivity.this, AppInstallActivity.class));
						}
					}
					else
					{
						btnVisible = true;    //显示“全部安装”和“一键安装”按钮
						closeFlag = false;    //不再接收客户机回应
						try {
							if (socketClient[0] != null)
								socketClient[0].close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						appInstallEditor = appInstallSharedPreferences.edit();
                        appInstallEditor.putString("appInstalling", "");
						appInstallEditor.commit();
						
						startActivity(new Intent(AppListActivity.this, AppInstallActivity.class));
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					//Toast.makeText(AppListActivity.this, "客户机无响应",Toast.LENGTH_SHORT).show();
					startActivity(new Intent(AppListActivity.this, AppListActivity.class));
				} 
			} //while END!

			try {
				if (socketClient[0] != null)
					socketClient[0].close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return windowsResponseInfo;
		}

		/**
		 * 运行在ui线程中，在doInBackground()执行完毕后执行
		 */
		@SuppressLint("ShowToast")
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			Toast toast = Toast.makeText(AppListActivity.this, "所请求的应用远程安装完毕", Toast.LENGTH_LONG);
			showMyToast(toast, 8000);
		}

		/**
		 * 在publishProgress()被调用以后执行，publishProgress()用于更新数据
		 */
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
		}
	}
	
	/*private int[] appIcons = { R.drawable.icon_qq, R.drawable.icon_weixin,
			R.drawable.icon_qqgame, R.drawable.icon_config,
			R.drawable.icon_sougoupinyin, R.drawable.icon_tecentvedio,
			R.drawable.icon_wangyimusic, R.drawable.icon_wps,
			R.drawable.icon_youku };*/
	
	public static int adaptLocalImage(String appName) {
		if (appName.contains("腾讯QQ"))
		{
			return 0;
		} else if (appName.contains("微信"))
		{
			return 1;
		} else if (appName.contains("QQ游戏"))
		{
			return 2;
		} else if (appName.contains("APPInstall") || appName.contains("APPDownload"))
		{
			return 3;
		} else if (appName.contains("搜狗拼音输入法"))
		{
			return 4;
		} else if (appName.contains("腾讯视频"))
		{
			return 5;
		} else if (appName.contains("网易云音乐"))
		{
			return 6;
		} else if (appName.contains("WPS Office"))
		{
			return 7;
		} else if (appName.contains("优酷"))
		{
			return 8;
		} else if (appName.contains("迅雷"))
		{
			return 9;
		}
		else
		{
			return 10;
		}
	}
	
	public static void setImageBackground(Bitmap bitmap, ImageView imageView, int width,  
            int height) 
	{  
        //计算最佳缩放倍数,以填充宽高为目标  
		float scaleX = (float) width / bitmap.getWidth();  
		float scaleY = (float) height / bitmap.getHeight();  
		float bestScale = scaleX > scaleY ? scaleX : scaleY;  
		//以填充高度的前提下，计算最佳缩放倍数  
		float subX = (width - bitmap.getWidth() * bestScale) / 2;  
		float subY = (height - bitmap.getHeight() * bestScale) / 2;  

		Matrix imgMatrix = new Matrix();  
		imageView.setScaleType(ImageView.ScaleType.MATRIX);  
		//缩放最佳大小  
		imgMatrix.postScale(bestScale, bestScale);  
		//移动到居中位置显示  
		imgMatrix.postTranslate(subX, subY);  
		//设置矩阵  
		imageView.setImageMatrix(imgMatrix);  
		imageView.setImageBitmap(bitmap);  
	}  
	
	public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight)
    {
 	   Bitmap resizedBitmap=bm;
 	   try
 	   {
			int width = bm.getWidth();
			int height = bm.getHeight();
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,matrix, false);
 	    }
 	   catch(Exception e )
 	   {
 		  e.printStackTrace();
 	   }        	   
 	    return resizedBitmap;
 	}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {
	 
	    // 首先设置 inJustDecodeBounds=true 来获取图片尺寸
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);
	 
	    // 计算 inSampleSize 的值
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	 
	    // 根据计算出的 inSampleSize 来解码图片生成Bitmap
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// 原始图片的宽高
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// 在保证解析出的bitmap宽高分别大于目标尺寸宽高的前提下，取可能的inSampleSize的最大值
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
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
}
