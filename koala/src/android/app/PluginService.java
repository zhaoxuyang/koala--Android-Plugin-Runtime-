package android.app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;

/**
 * 插件service 的封装
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginService extends Service {

	/**
	 * 插件的名称
	 */
	private String pluginName;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		pluginName = intent.getStringExtra(PluginBlankService.PLUGIN_NAME);
		return super.onStartCommand(intent, flags, startId);
	}

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
}
