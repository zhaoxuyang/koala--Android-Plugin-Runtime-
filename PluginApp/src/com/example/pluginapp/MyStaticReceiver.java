package com.example.pluginapp;

import android.app.PluginManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class MyStaticReceiver extends BroadcastReceiver{

	private Handler handler = new Handler(Looper.getMainLooper());
	
	@Override
	public void onReceive(final Context arg0, Intent arg1) {
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				PluginManager.getInstance().showToast(arg0, "静态注册的收到了", Toast.LENGTH_LONG);
			}
		}, 5000);
		
	}

}
