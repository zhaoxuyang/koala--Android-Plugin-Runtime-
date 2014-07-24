package com.example.pluginapp;

import android.app.PluginApplication;
import android.app.PluginManager;
import android.util.Log;
import android.widget.Toast;

public class MyApplication extends PluginApplication{

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("application", "oncreate");
		PluginManager.getInstance().showToast(this, "application oncreate", Toast.LENGTH_LONG);
	}
}
