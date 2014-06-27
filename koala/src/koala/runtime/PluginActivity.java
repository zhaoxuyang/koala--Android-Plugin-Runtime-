package koala.runtime;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

/**
 * 插件activity，主要是为了启动service
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginActivity extends Activity {
	public ComponentName startService(Intent service) {
		if ((getParent() != null) && (service.getComponent() != null)) {
			ComponentName cn = service.getComponent();
			service.setComponent(new ComponentName(
					getParent().getPackageName(), cn.getClassName()));
		}
		return super.startService(service);
	}
}