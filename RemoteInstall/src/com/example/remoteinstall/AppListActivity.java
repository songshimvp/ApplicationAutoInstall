package com.example.remoteinstall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.remoteinstallutil.AppSendImageThread;
import com.example.remoteinstallutil.CurrentTime;
import com.example.remoteinstallutil.AppSendImageThread.MyCallBack;
import com.example.remoteinstallutil.AppSendInfoThread;

public class AppListActivity extends Activity {

	private ImageView syncImage;
	
	private Button btnInstall;       //一键安装需要和远程客户机交互
	private Button btnUninstall;     //安装进度监视
	
	private ListView mAppListView;   //App列表需要和服务器软件库交互
	private ArrayAdapter<AppInfoItem> mAppAdapter;
	private List<AppInfoItem> mAppDatas = new ArrayList<AppInfoItem>();
	
	private String resultStr = null;
	
	private SharedPreferences appInstallSharedPreferences;
	private SharedPreferences.Editor appInstallEditor;
	
	//public static String reqiredInstallAppNames = "";  
	public String oldReqiredInstallAppNames = "";
	
	public static boolean windowsResponseFlag = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.app_list);
		
		syncImage = (ImageView) findViewById(R.id.syncImageAppInstall);	
		
		mAppListView = (ListView) findViewById(R.id.appListViewAppInstall);
		mAppAdapter = new AppListAdapter(AppListActivity.this, mAppDatas);
		mAppListView.setAdapter(mAppAdapter);
		
		btnInstall =(Button) findViewById(R.id.btnInstallApp);
		btnUninstall =(Button) findViewById(R.id.btnUninstallApp);
		
		appInstallSharedPreferences = getSharedPreferences("appInstallInfo", Context.MODE_PRIVATE);
		
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
				//首先清空Listview以前的数据
				mAppAdapter.clear();
				mAppAdapter.notifyDataSetChanged();
				
				//重置安装文件字符串
				AppListAdapter.reqiredInstallAppNames = "";
				 
				//加载新数据
				Thread thread = new Thread(sendMsgThread);
				thread.start();
			}
		});
		
		btnInstall.setOnClickListener(new OnClickListener() {     //一键安装
			
			@Override
			public void onClick(View v) {
				
				//“AppListAdapter.reqiredInstallAppNames”是原生的、未做任何判断的、从checkbox直接获得的要求安装的AppName字符串
				//SharedPreferences应用卸载后还在，但应该是卸载后所有东西重置！
				
				AppSendInfoThread sendAppMsg=new AppSendInfoThread(handler, AppListActivity.this, AppListAdapter.reqiredInstallAppNames);   //向客户端发送appStrs
				Thread thread=new Thread(sendAppMsg);
				thread.start();							
			}
		});
		
		btnUninstall.setOnClickListener(new OnClickListener() {   //打开安装进度监视
			
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
					Bitmap[] bitmapArrs=(Bitmap[]) msg.obj;
					
					resultStr=msg.getData().getString("appListMsg");
					String[] nameStr= resultStr.split(",");
					
					for(int i=0;i<nameStr.length;i++)
					{
						String tmp = nameStr[i].substring(0, nameStr[i].indexOf("."));
						AppInfoItem appInfoItem = new AppInfoItem(bitmapArrs[i], tmp/*, false*/);
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
							Toast.makeText(AppListActivity.this, "正在远程安装：" + AppListAdapter.reqiredInstallAppNames, Toast.LENGTH_LONG).show();
								
							ReceiveAsyncTask receiveAsyncTask =new ReceiveAsyncTask();
							receiveAsyncTask.execute(AppSendInfoThread.socketClient);    //异步接收Windows回应的消息
		
							appInstallEditor = appInstallSharedPreferences.edit();
							appInstallEditor.putString("appInstalling", AppListAdapter.reqiredInstallAppNames);
							appInstallEditor.commit();
							
							//finish();
							Intent AppInstallActivityIntent = new Intent();
							AppInstallActivityIntent.setClass(AppListActivity.this, AppInstallActivity.class);
							startActivity(AppInstallActivityIntent);	
														
							/*// 遍历list的长度，将AppListAdapter中的checkbox全部设为false  
			                for (int i = 0; i < mAppDatas.size(); i++) {  
			                	//((CheckBox)mAppListView.getChildAt(i).findViewById(R.id.appChooseCheckAppInstall)).setChecked(false);
			                	AppListAdapter.getIsSelected().put(i, false);  
			                }                  
							//重置安装文件字符串
							AppListAdapter.reqiredInstallAppNames = "";*/
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
			// TODO Auto-generated method stub
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
			 *  } catch (Exception e) 
			 *  { //
			 * Thread.join()可以被interrupt，调用AsyncTask.cancel(true);即可退出等待 
			 * return null; }
			 */
			try {
				InputStream inputStream = socketClient[0].getInputStream();
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(inputStream));
				// private final int TIME_OUT = 2 * 1000;// 最大响应时间，超时设置
				// socketClient.setSoTimeout(TIME_OUT);
				// 此处不设置时延————远程下载、安装时间不定

				windowsResponseInfo = buffer.readLine();
				System.out.println("Windows客户机响应:" + windowsResponseInfo);

				String failedNames = windowsResponseInfo.split("!!!")[0];
				String successNames = windowsResponseInfo.split("!!!")[1];								
				String responseTime =  CurrentTime.getCurrentTime();
			
				String installingNames= AppListAdapter.reqiredInstallAppNames.replaceAll(failedNames, "");
				installingNames = installingNames.replaceAll(successNames, "");
				
				appInstallEditor = appInstallSharedPreferences.edit();
				appInstallEditor.putString("failedNames", failedNames);
				appInstallEditor.putString("successNames", successNames);
				appInstallEditor.putString("responseTime", responseTime);
				
				appInstallEditor.putString("appInstalling", installingNames);
				
				appInstallEditor.commit();
				
				windowsResponseFlag = true;
				/*while (windowsResponseInfo != null) {
					publishProgress(windowsResponseInfo);
				}*/
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(AppListActivity.this, "客户机无响应",Toast.LENGTH_SHORT).show();
			} finally {
				try {
					if (socketClient[0] != null)
						socketClient[0].close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return windowsResponseInfo;
		}

		/**
		 * 运行在ui线程中，在doInBackground()执行完毕后执行
		 */
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

		/**
		 * 在publishProgress()被调用以后执行，publishProgress()用于更新数据
		 */
		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
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
}
