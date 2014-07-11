package android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 插件broadcastreceiver的代理类
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginBlankBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		PluginManagerImpl.getInstance().onPluginReceive(arg1);
	}

}
