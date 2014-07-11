package android.app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

/**
 * 同PluginActivity
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginFragmentActivity extends FragmentActivity {
	
	@Override
	public ComponentName startService(Intent service) {
		if ((getParent() != null) && (service.getComponent() != null)) {
			ComponentName cn = service.getComponent();
			service.setComponent(new ComponentName(
					getParent().getPackageName(), cn.getClassName()));
		}
		return super.startService(service);
	}

	@Override
	public Intent registerReceiver(BroadcastReceiver receiver,
			IntentFilter filter) {
		if ((getParent() != null)) {
			return getParent().registerReceiver(receiver, filter);
		}
		return super.registerReceiver(receiver, filter);
	}

	@Override
	public Intent registerReceiver(BroadcastReceiver receiver,
			IntentFilter filter, String broadcastPermission, Handler scheduler) {
		if ((getParent() != null)) {
			return getParent().registerReceiver(receiver, filter,
					broadcastPermission, scheduler);
		}
		return super.registerReceiver(receiver, filter, broadcastPermission,
				scheduler);
	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) {
		if ((getParent() != null)) {
			getParent().unregisterReceiver(receiver);
			return;
		}
		super.unregisterReceiver(receiver);
	}
}