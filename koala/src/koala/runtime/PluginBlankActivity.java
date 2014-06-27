package koala.runtime;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.os.Binder;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * ´úÀíactivity
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginBlankActivity extends Activity {
	private Activity activity;
	private static Method onCreate;
	private static Method onStart;
	private static Method onRestart;
	private static Method onResume;
	private static Method onPause;
	private static Method onStop;
	private static Method onDestroy;
	private static Method onActivityResult;
	private boolean isFirst = true;

	static {
		try {
			onCreate = Activity.class.getDeclaredMethod("onCreate",
					new Class[] { Bundle.class });
			onCreate.setAccessible(true);
			onStart = Activity.class.getDeclaredMethod("onStart", new Class[0]);
			onStart.setAccessible(true);
			onRestart = Activity.class.getDeclaredMethod("onRestart",
					new Class[0]);
			onRestart.setAccessible(true);
			onResume = Activity.class.getDeclaredMethod("onResume",
					new Class[0]);
			onResume.setAccessible(true);
			onPause = Activity.class.getDeclaredMethod("onPause", new Class[0]);
			onPause.setAccessible(true);
			onStop = Activity.class.getDeclaredMethod("onStop", new Class[0]);
			onStop.setAccessible(true);
			onDestroy = Activity.class.getDeclaredMethod("onDestroy",
					new Class[0]);
			onDestroy.setAccessible(true);
			onActivityResult = Activity.class.getDeclaredMethod(
					"onActivityResult", new Class[] { Integer.TYPE,
							Integer.TYPE, Intent.class });
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		String name = intent.getStringExtra("activityName");
		PluginManagerImpl manager = PluginManagerImpl.getInstance();
		Plugin plugin = manager.getCurrentPlugin();
		ActivityInfo[] infos = plugin.mPackageInfo.activities;
		ActivityInfo info = null;
		for (int i = 0; i < infos.length; i++) {
			info = infos[i];
			if (info.name.equals(name)) {
				intent = new Intent();
				intent.setComponent(new ComponentName(manager
						.getCurrentPlugin().mPackageInfo.packageName, name));
				intent.putExtras(getIntent());
				try {
					this.activity = ((Activity) manager.startActivityNow
							.invoke(manager.mActivityThread,
									new Object[] { this, name, intent, info,
											new LocalBinder(),
											savedInstanceState, null }));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}

		setRequestedOrientation(info.screenOrientation);
		getWindow().setSoftInputMode(info.softInputMode);
		super.onCreate(savedInstanceState);
		System.out.println("activity:" + this.activity);
		setContentView(this.activity.getWindow().getDecorView());
	}

	protected void onStart() {
		super.onStart();
		try {
			if (!this.isFirst) {
				onStart.invoke(this.activity, new Object[0]);
			}
			this.isFirst = false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	protected void onRestart() {
		super.onRestart();
		try {
			onRestart.invoke(this.activity, new Object[0]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	protected void onResume() {
		super.onResume();
		try {
			onResume.invoke(this.activity, new Object[0]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	protected void onPause() {
		super.onPause();
		try {
			onPause.invoke(this.activity, new Object[0]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	protected void onStop() {
		super.onStop();
		try {
			onStop.invoke(this.activity, new Object[0]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		try {
			onDestroy.invoke(this.activity, new Object[0]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			onActivityResult.invoke(
					this.activity,
					new Object[] { Integer.valueOf(requestCode),
							Integer.valueOf(resultCode), data });
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void startActivityFromChild(Activity child, Intent intent,
			int requestCode) {
		ComponentName cn = intent.getComponent();
		String className = null;
		if (cn != null) {
			className = cn.getClassName();
			Intent newIntent = new Intent(getBaseContext(),
					PluginBlankActivity.class);
			newIntent.putExtras(intent);
			newIntent.putExtra("activityName", className);
			newIntent.addFlags(268435456);
			startActivity(newIntent);
		} else {
			PluginManagerImpl manager = PluginManagerImpl.getInstance();
			Plugin plugin = manager.getCurrentPlugin();
			ArrayList<android.content.pm.PackageParser.Activity> activitys = plugin.mPackageObj.activities;
			Iterator<android.content.pm.PackageParser.Activity> iter = activitys
					.iterator();
			while (iter.hasNext()) {
				PackageParser.Activity activity = (PackageParser.Activity) iter
						.next();
				ArrayList<ActivityIntentInfo> infos = activity.intents;
				Iterator<ActivityIntentInfo> i = infos.iterator();
				while (i.hasNext()) {
					PackageParser.ActivityIntentInfo info = (PackageParser.ActivityIntentInfo) i
							.next();
					int res = info.match(intent.getAction(), intent.getType(),
							intent.getScheme(), intent.getData(),
							intent.getCategories(), "");
					if (res > 0) {
						Intent newIntent = new Intent(getBaseContext(),
								PluginBlankActivity.class);
						newIntent.putExtras(intent);
						newIntent.putExtra("activityName", activity.className);
						newIntent.addFlags(268435456);
						startActivity(newIntent);
						return;
					}
				}

			}
		}
	}

	public ComponentName startService(Intent intent) {
		ComponentName cn = intent.getComponent();
		if (cn != null) {
			intent.setComponent(new ComponentName(getPackageName(), cn
					.getClassName()));
		}
		intent.setPackage(getPackageName());
		return super.startService(intent);
	}

	public static class LocalBinder extends Binder {
	}
}