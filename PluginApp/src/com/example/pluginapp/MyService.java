package com.example.pluginapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class MyService extends Service{

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service start", Toast.LENGTH_LONG).show();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
