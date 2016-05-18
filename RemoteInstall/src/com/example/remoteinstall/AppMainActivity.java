package com.example.remoteinstall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AppMainActivity extends Activity {

	private EditText ipText;
	private EditText pwdText;
	private EditText portText;
	
	private Button btnLogin;
	private Button btnCancel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ipText=(EditText) findViewById(R.id.homeServiceIpApp);
		pwdText=(EditText) findViewById(R.id.homeServicePwdApp);
		portText=(EditText) findViewById(R.id.homeServicePortApp);
		
		ipText.setText("192.168.1.116");
		pwdText.setText("1234");
		portText.setText("12123");
		
		btnLogin = (Button) findViewById(R.id.btnLoginApp);
		btnCancel = (Button) findViewById(R.id.btnCancelApp);
		
		btnLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(ipText.getText().toString().equals("192.168.1.116") && pwdText.getText().toString().equals("1234") && portText.getText().toString().equals("12123") )
				{
					startActivity(new Intent(AppMainActivity.this, AppListActivity.class));
				}
				else
				{
					Toast.makeText(AppMainActivity.this, "IP、密码或端口号输入不正确!", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	
}
