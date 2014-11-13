package android.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.Window;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 插件activity的代理
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginBlankActivity extends Activity {

	/**
	 * 传进来插件activity的类名
	 */
	public static final String ACTIVITY_NAME = "activityName";

	/**
	 * 对应的插件名称
	 */
	public static final String PLUGIN_NAME = "pluginname";

	/**
	 * 插件的activity
	 */
	private Activity activity;

	/**
	 * 不解释
	 */
	private static Method onCreate;
	/**
	 * 不解释
	 */
	private static Method onStart;
	/**
	 * 不解释
	 */
	private static Method onRestart;
	/**
	 * 不解释
	 */
	private static Method onResume;
	/**
	 * 不解释
	 */
	private static Method onPause;
	/**
	 * 不解释
	 */
	private static Method onStop;
	/**
	 * 不解释
	 */
	private static Method onDestroy;
	/**
	 * 不解释
	 */
	private static Method onActivityResult;
	/**
	 * 是否第一次
	 */
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 根据intent信息，从插件中找到要载入的activity
		Intent intent = getIntent();
		String activityName = intent.getStringExtra(ACTIVITY_NAME);
		String pluginName = intent.getStringExtra(PLUGIN_NAME);
		if (TextUtils.isEmpty(activityName) || TextUtils.isEmpty(pluginName)) {
			return;
		}

		PluginManagerImpl manager = PluginManagerImpl.getInstance();
		Plugin plugin = manager.getPlugin(pluginName);
		ActivityInfo[] infos = plugin.mPluginInfo.mPackageInfo.activities;
		ActivityInfo info = null;
		int screenOriention = -1;
		for (int i = 0; i < infos.length; i++) {
			info = infos[i];
			screenOriention = info.screenOrientation;
			if (info.name.equals(activityName)) {
				intent = new Intent();
				intent.setComponent(new ComponentName(
						plugin.mPluginInfo.mPackageInfo.packageName,
						activityName));
				intent.putExtras(getIntent());
				// 参照activitygroup的代码，将activity启动起来
				try {
					activity = (Activity) manager.startActivityNow.invoke(manager.mActivityThread,this,
							activityName, intent, info, new LocalBinder(),
							savedInstanceState, null);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		Window win = activity.getWindow();
		getWindow().setAttributes(win.getAttributes());
		setRequestedOrientation(screenOriention);
		super.onCreate(savedInstanceState);
		setContentView(win.getDecorView());
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			if (!isFirst) {
				onStart.invoke(activity, new Object[0]);
			}
			isFirst = false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		try {
			onRestart.invoke(activity, new Object[0]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			onResume.invoke(activity, new Object[0]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			onPause.invoke(activity, new Object[0]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		try {
			onStop.invoke(activity, new Object[0]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			onDestroy.invoke(activity, new Object[0]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			onActivityResult.invoke(
					activity,
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * 插件startactivity会调到这里，这里处理了隐式和显式启动activity (non-Javadoc)
	 * 
	 * @see android.app.Activity#startActivityFromChild(android.app.Activity,
	 * android.content.Intent, int)
	 */
	@Override
	public void startActivityFromChild(Activity child, Intent intent,
			int requestCode) {
		PluginManagerImpl.getInstance().startPluginActivity(intent);
	}

	/**
	 * 占位用。。
	 * @author zhaoxuyang
	 *
	 */
	public static class LocalBinder extends Binder {
	}
}