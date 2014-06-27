package com.example.pluginapp;

import koala.runtime.PluginActivity;
import android.os.Bundle;

public class SecondActivity extends PluginActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.second_activity);
	}

}
