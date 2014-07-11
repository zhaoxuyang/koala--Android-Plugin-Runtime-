package com.example.pluginapp;

import android.app.PluginApplication;
import android.util.Log;
import android.widget.Toast;

public class MyApplication extends PluginApplication{

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("application", "oncreate");
		Toast.makeText(getBaseContext(), "application oncreate", Toast.LENGTH_LONG).show();
	}
}
