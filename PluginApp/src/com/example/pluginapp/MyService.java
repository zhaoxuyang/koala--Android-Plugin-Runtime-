package com.example.pluginapp;

import android.app.PluginManager;
import android.app.PluginService;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class MyService extends PluginService{

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		PluginManager.getInstance().showToast(this, "service start", Toast.LENGTH_LONG);
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	@Override
	public void onDestroy() {
		PluginManager.getInstance().showToast(this, "service destory", Toast.LENGTH_LONG);
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
