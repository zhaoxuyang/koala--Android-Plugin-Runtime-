package android.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

/**
 * 插件中的activity如果需要启动service，则应继承该activity，否则service启动失败
 * 
 * @author zhaoÏxuyang
 * 
 */
public class PluginActivity extends Activity {

	/**
	 * 对应的插件名称
	 */
	private String pluginName;
	
	/**
	 * contentresolver
	 */
	private PluginContentResolver mContentResolver;

	@Override
	protected void onCreate(Bundle arg0) {
		pluginName = getIntent()
				.getStringExtra(PluginBlankActivity.PLUGIN_NAME);
		super.onCreate(arg0);
	}

	@Override
	public ComponentName startService(Intent service) {
		return PluginManagerImpl.getInstance().startPluginService(service);
	}

	@Override
	public boolean stopService(Intent name) {
		return PluginManagerImpl.getInstance().stopPluginService(name);
	}

	@Override
	public Intent registerReceiver(BroadcastReceiver receiver,
			IntentFilter filter) {
		return PluginManagerImpl.getInstance().registerReceiver(this,
				pluginName, receiver, filter);
	}

	@Override
	public Intent registerReceiver(BroadcastReceiver receiver,
			IntentFilter filter, String broadcastPermission, Handler scheduler) {
		return PluginManagerImpl.getInstance().registerReceiver(this,
				pluginName, receiver, filter, broadcastPermission, scheduler);
	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) {
		PluginManagerImpl.getInstance().unregisterReceiver(this, pluginName,
				receiver);
	}
	
	@Override
	public ContentResolver getContentResolver() {
	    if(mContentResolver == null){
	        mContentResolver = new PluginContentResolver(this);
	    }
	    return mContentResolver;
	}
}