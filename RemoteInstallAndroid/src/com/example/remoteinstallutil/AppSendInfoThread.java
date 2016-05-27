package com.example.remoteinstallutil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

public class AppSendInfoThread implements Runnable {

	private final int TIME_OUT = 100 * 1000;// 最大响应时间，超时设置
	public static Socket socketClient = null;
	Handler handler = null;
	Activity activity;
	private String requestStr;
	
	public AppSendInfoThread(Handler handler, Activity activity,String requestStr) {
		super();
		this.handler = handler;
		this.activity = activity;
		this.requestStr = requestStr;
	}	

	@Override
	public void run() {
		Message message = handler.obtainMessage();

		String dstAddress = "192.168.1.107";        // 客户机IP地址
		int dstPort = 12123;

		socketClient = new Socket();
		SocketAddress socAddress = new InetSocketAddress(dstAddress, dstPort);
		try {
			socketClient.connect(socAddress, TIME_OUT);
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socketClient.getOutputStream())),
					true);
			out.println(requestStr);
						
			message.what = 2;  // 成功发送请求消息
			handler.sendMessage(message);
		} catch (ConnectException e) {
			message.what = 0; // Connection refused
			handler.sendMessage(message);
		} catch (SocketException e) {
			message.what = 1; // Socket is closed
			handler.sendMessage(message);
		} catch (IOException e) {
			message.what = 5; // I/O发生错误
			handler.sendMessage(message);
		}
	}
}
