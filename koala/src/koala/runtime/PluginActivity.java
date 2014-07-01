package koala.runtime;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

/**
 * 插件中的activity如果需要启动service，则应继承该activity，否则service启动失败
 * 
 * @author zhaoÏxuyang
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