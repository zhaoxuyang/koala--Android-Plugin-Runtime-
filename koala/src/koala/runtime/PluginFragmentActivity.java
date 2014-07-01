package koala.runtime;

import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

/**
 * 同PluginActivity
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginFragmentActivity extends FragmentActivity {
	public ComponentName startService(Intent service) {
		if ((getParent() != null) && (service.getComponent() != null)) {
			ComponentName cn = service.getComponent();
			service.setComponent(new ComponentName(
					getParent().getPackageName(), cn.getClassName()));
		}
		return super.startService(service);
	}
}