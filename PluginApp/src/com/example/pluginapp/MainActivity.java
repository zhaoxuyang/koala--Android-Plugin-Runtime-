package com.example.pluginapp;

import android.app.AlertDialog;
import android.app.PluginActivity;
import android.app.PluginManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends PluginActivity implements OnClickListener{
	
	static{
		System.loadLibrary("hello-jni");
	}
	
	private MyReceiver receiver;
	
	private boolean hasRegisted;
	
	private AlertDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.second_activity).setOnClickListener(this);
		findViewById(R.id.third_activity).setOnClickListener(this);
		findViewById(R.id.start_service).setOnClickListener(this);
		findViewById(R.id.stopservice).setOnClickListener(this);
		findViewById(R.id.registreceiver).setOnClickListener(this);
		findViewById(R.id.unregistreceiver).setOnClickListener(this);
		findViewById(R.id.sendbroadcat).setOnClickListener(this);
		findViewById(R.id.showdialog).setOnClickListener(this);
		findViewById(R.id.test_native).setOnClickListener(this);
		receiver = new MyReceiver();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("test").setMessage("dialog").setPositiveButton("ok", null)
		.setNegativeButton("cancel", null);
		dialog = builder.create();
	}

	public void onClick(View view) {

		int id = view.getId();

		switch (id) {
		case R.id.second_activity:
			Intent intent = new Intent(this, SecondActivity.class);
			startActivity(intent);
			break;
		case R.id.third_activity:
			intent = new Intent("com.example.pluginapp.ThirdActivity");
			startActivity(intent);
			break;
		case R.id.start_service:
			startService(new Intent(this, MyService.class));
			break;
		case R.id.stopservice:
			stopService(new Intent("com.example.pluginapp.MyService"));
			break;
		case R.id.registreceiver:
			if(hasRegisted){
				PluginManager.getInstance().showToast(this, "has registed", Toast.LENGTH_LONG);
				return;
			}
			registerReceiver(receiver, new IntentFilter("com.example.pluginapp.MyReceiver"));
			hasRegisted = true;
			break;
		case R.id.unregistreceiver:
			if(!hasRegisted){
				PluginManager.getInstance().showToast(this, "not registed", Toast.LENGTH_LONG);
				return;
			}
			unregisterReceiver(receiver);
			hasRegisted = false;
			break;
		case R.id.sendbroadcat:
			sendBroadcast(new Intent("com.example.pluginapp.MyReceiver"));
			break;
		case R.id.showdialog:
			if(!dialog.isShowing()){
				dialog.show();
			}
			break;
		case R.id.test_native:
			TextView tv = (TextView) findViewById(R.id.res);
			tv.setText("jnires:"+TestNative.testNative());
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(hasRegisted){
			unregisterReceiver(receiver);
			hasRegisted = false;
		}
	}
	
	
	public static class MyReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			PluginManager.getInstance().showToast(arg0, "动态注册的收到了", Toast.LENGTH_LONG);
		}
		
	}
}
