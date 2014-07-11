package android.app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

/**
 * 插件对Application的包装
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginApplication extends Application {

	/**
	 * 对应插件的名称
	 */
	private String pluginName;

	@Override
	public void startActivity(Intent intent) {
		PluginManagerImpl.getInstance().startPluginActivity(intent);
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

	/**
	 * 设置对应的插件名称
	 * @param pluginName
	 */
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

}
