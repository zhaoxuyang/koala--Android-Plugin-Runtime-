package com.example.pluginapp;

import koala.runtime.PluginActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends PluginActivity implements OnClickListener{
	
	static{
		System.loadLibrary("hello-jni");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.second_activity).setOnClickListener(this);
		findViewById(R.id.third_activity).setOnClickListener(this);
		findViewById(R.id.start_service).setOnClickListener(this);
		findViewById(R.id.test_native).setOnClickListener(this);

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
		case R.id.test_native:
			TextView tv = (TextView) findViewById(R.id.res);
			tv.setText("jnires:"+TestNative.testNative());
			break;
		}
	}
}
