package android.app;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

/**
 * 插件service对应的代理类
 * @author zhaoxuyang
 *
 */
public class PluginBlankService extends Service {

	/**
	 * 插件service的类名
	 */
	public static final String SERVICE_NAME = "servicename";

	/**
	 * 插件的名称
	 */
	public static final String PLUGIN_NAME = "pluginname";

	/**
	 * 类型
	 */
	public static final String TYPE = "type";

	/**
	 * 启动service
	 */
	public static final int START_TYPE = 1;

	/**
	 * 停止service
	 */
	public static final int STOP_TYPE = 2;

	/**
	 * 已启动的service
	 */
	private HashMap<String, Service> mCreateServices = new HashMap<String, Service>();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String serviceName = intent.getStringExtra(SERVICE_NAME);
		String pluginName = intent.getStringExtra(PLUGIN_NAME);
		int type = intent.getIntExtra(TYPE, -1);

		if (TextUtils.isEmpty(serviceName) || TextUtils.isEmpty(pluginName)
				|| (type != START_TYPE && type != STOP_TYPE)) {
			return super.onStartCommand(intent, flags, startId);
		}

		if (mCreateServices.containsKey(serviceName)) {
			Service service = mCreateServices.get(serviceName);
			if (type == START_TYPE) {
				service.onStartCommand(intent, flags, startId);
			} else {
				service.onDestroy();
				mCreateServices.remove(serviceName);
			}
		} else if (type == START_TYPE) {
			PluginManagerImpl manager = PluginManagerImpl.getInstance();
			Plugin plugin = manager.getPlugin(pluginName);
			try {
				ActivityThread thread = manager.mActivityThread;
				Instrumentation instrumentation = thread.getInstrumentation();
				Object packageInfo = plugin.mRealPackageInfo;
				Context context = this;
				if(manager.init!=null){
					Constructor construct = manager.contextImpl
							.getDeclaredConstructor();
					construct.setAccessible(true);
					context = (Context) construct.newInstance();
					manager.init.invoke(context, packageInfo, null, thread);
				}else{
					context = (Context) manager.createAppContext.invoke(null, thread,packageInfo);
				}
				Service service = (Service) plugin.mClassLoader.loadClass(
						serviceName).newInstance();
				manager.setOuterContext.invoke(context, service);
				service.attach(this, thread, serviceName, new LocalBinder(),
						(Application)manager.makeApplication.invoke(packageInfo,false, instrumentation),
						ActivityManagerNative.getDefault());
				service.onCreate();
				service.onStartCommand(intent, flags, startId);
				mCreateServices.put(serviceName, service);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Iterator<Service> iter = mCreateServices.values().iterator();
		while (iter.hasNext()) {
			iter.next().onDestroy();
		}
		mCreateServices.clear();
	}

	/**
	 * 占位的
	 * @author zhaoxuyang
	 *
	 */
	public static class LocalBinder extends Binder {
	}
}
