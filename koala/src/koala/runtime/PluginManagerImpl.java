package koala.runtime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.DisplayMetrics;
import dalvik.system.DexClassLoader;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 核心类，用来加载插件，启动插件
 * @author zhaoxuyang
 *
 */
class PluginManagerImpl {
	private Context mContext;
	private static PluginManagerImpl mInstance;
	private HashMap<String, Plugin> mPlugins = new HashMap<String, Plugin>();
	private Plugin mCurrentPlugin;
	public Object mActivityThread;
	public Method startActivityNow;
	public Method getPackageInfo;
	public Field mClassLoader;
	private String mDexoutputPath;
	private ClassLoader mOriginalClassLoader;
	private MajorClassLoader mMajorClassLoader;
	private MajorClassLoaderHandler mMajorHandler;
	private Handler mHandler;

	public static PluginManagerImpl getInstance() {
		if (mInstance == null) {
			mInstance = new PluginManagerImpl();
		}
		return mInstance;
	}

	public void init(Context context, String dop) {
		this.mContext = context;
		this.mDexoutputPath = dop;
		this.mHandler = new Handler(Looper.getMainLooper());
		initEnvironment();
	}

	private void initEnvironment() {
		Object packageInfo = null;
		Class packageClass = null;
		try {
			packageClass = Class.forName("android.app.LoadedApk");
			this.mClassLoader = packageClass.getDeclaredField("mClassLoader");
			this.mClassLoader.setAccessible(true);
			Class contextClass = Class.forName("android.app.ContextImpl");
			Field f = contextClass.getDeclaredField("mPackageInfo");
			f.setAccessible(true);
			packageInfo = f.get(this.mContext);
		} catch (ClassNotFoundException e) {
			try {
				packageClass = Class
						.forName("android.app.ActivityThread$PackageInfo");
				this.mClassLoader = packageClass
						.getDeclaredField("mClassLoader");
				this.mClassLoader.setAccessible(true);
				Field f = this.mContext.getClass().getDeclaredField(
						"mPackageInfo");
				f.setAccessible(true);
				packageInfo = f.get(this.mContext);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (packageInfo != null) {
			try {
				this.mOriginalClassLoader = getClass().getClassLoader();
				this.mMajorHandler = new MajorClassLoaderHandler();
				this.mMajorClassLoader = new MajorClassLoader("",
						this.mDexoutputPath, null, this.mOriginalClassLoader);
				this.mMajorClassLoader.setHandler(this.mMajorHandler);
				this.mClassLoader.set(packageInfo, this.mMajorClassLoader);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		if (packageClass != null)
			try {
				Field f = packageClass.getDeclaredField("mActivityThread");
				f.setAccessible(true);
				this.mActivityThread = f.get(packageInfo);
				try {
					this.startActivityNow = this.mActivityThread.getClass()
							.getDeclaredMethod(
									"startActivityNow",
									new Class[] { Activity.class, String.class,
											Intent.class, ActivityInfo.class,
											IBinder.class, Bundle.class,
											Object.class });
				} catch (NoSuchMethodException e) {
					try {
						Class clazz = Class
								.forName("android.app.Activity$NonConfigurationInstances");
						this.startActivityNow = this.mActivityThread.getClass()
								.getDeclaredMethod(
										"startActivityNow",
										new Class[] { Activity.class,
												String.class, Intent.class,
												ActivityInfo.class,
												IBinder.class, Bundle.class,
												clazz });
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					} catch (NoSuchMethodException e1) {
						e1.printStackTrace();
					}
				}

				try {
					Class clazz = Class
							.forName("android.content.res.CompatibilityInfo");
					this.getPackageInfo = this.mActivityThread
							.getClass()
							.getDeclaredMethod(
									"getPackageInfoNoCheck",
									new Class[] { ApplicationInfo.class, clazz });
					this.getPackageInfo.setAccessible(true);
				} catch (Exception e) {
					try {
						this.getPackageInfo = this.mActivityThread.getClass()
								.getDeclaredMethod("getPackageInfoNoCheck",
										new Class[] { ApplicationInfo.class });
						this.getPackageInfo.setAccessible(true);
					} catch (NoSuchMethodException e1) {
						e1.printStackTrace();
					}
				}

			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
	}

	public void installPlugin(final PluginInfo info,
			final InstallPluginListener listener) {
		if (!checkInstalled(info.name)) {
			new Thread() {
				public void run() {
					PluginManagerImpl.this.mHandler.post(new Runnable() {
						public void run() {
							listener.onInstallStart();
						}
					});
					PluginManagerImpl.this.realInstallPluin(info);
					PluginManagerImpl.this.mHandler.post(new Runnable() {
						public void run() {
							PluginManagerImpl.this.setCurrentPlugin(info.name);
							PluginManagerImpl.this.startCurrentPlugin();
							listener.onInstallEnd();
						}
					});
				}
			}.start();
		} else {
			setCurrentPlugin(info.name);
			startCurrentPlugin();
		}
	}

	private void startCurrentPlugin() {
		if (this.mCurrentPlugin == null) {
			return;
		}
		String className = this.mCurrentPlugin.enterClass;
		Intent newIntent = new Intent(this.mContext, PluginBlankActivity.class);
		newIntent.putExtra("activityName", className);
		newIntent.addFlags(268435456);
		this.mContext.startActivity(newIntent);
	}

	private void realInstallPluin(PluginInfo info) {
		try {
			Plugin plugin = new Plugin();
			this.mPlugins.put(info.name, plugin);

			plugin.enterClass = info.enterClass;
			int flags = 1103;

			PackageParser parser = new PackageParser(info.apkPath);
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();
			File sourceFile = new File(info.apkPath);
			PackageParser.Package pack = parser.parsePackage(sourceFile,
					info.apkPath, metrics, 0);

			PackageInfo packageInfo = this.mContext.getPackageManager()
					.getPackageArchiveInfo(info.apkPath, flags);
			packageInfo.applicationInfo.uid = Process.myUid();
			packageInfo.applicationInfo.sourceDir = info.apkPath;
			packageInfo.applicationInfo.publicSourceDir = info.apkPath;
			packageInfo.applicationInfo.dataDir = this.mContext.getDir(
					info.name, 0).getAbsolutePath();
			packageInfo.applicationInfo.flags &= 4;
			Object realPackageInfo = null;
			try {
				realPackageInfo = this.getPackageInfo.invoke(
						this.mActivityThread, new Object[] {
								packageInfo.applicationInfo, null });
			} catch (Exception e) {
				realPackageInfo = this.getPackageInfo.invoke(
						this.mActivityThread,
						new Object[] { packageInfo.applicationInfo });
			}

			StringBuilder sb = new StringBuilder();
			int size = info.nativeLibraryPaths.size();
			for (int j = 0; j < size; j++) {
				sb.append((String) info.nativeLibraryPaths.get(j));
				if (j < size - 1) {
					sb.append(":");
				}
			}

			DexClassLoader classLoader = new DexClassLoader(info.apkPath,
					this.mDexoutputPath, sb.toString(),
					this.mOriginalClassLoader);
			this.mClassLoader.set(realPackageInfo, classLoader);

			plugin.mPackageInfo = packageInfo;
			plugin.mRealPackageInfo = realPackageInfo;
			plugin.mClassLoader = classLoader;
			plugin.mPackageObj = pack;
			this.mMajorHandler.addPlugin(plugin);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public boolean checkInstalled(String name) {
		if (this.mPlugins.containsKey(name)) {
			return true;
		}
		return false;
	}

	public void scanApks(final File dir, final ScanPluginListener listener) {
		final ArrayList<PluginInfo> plugins = new ArrayList<PluginInfo>();
		new Thread() {
			public void run() {
				PluginManagerImpl.this.mHandler.post(new Runnable() {
					public void run() {
						listener.onScanStart();
					}
				});
				File[] files = dir.listFiles();
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					PluginInfo pluginInfo = new PluginInfo();
					String str = file.getName();
					pluginInfo.name = str;
					File[] files2 = file.listFiles();
					for (int j = 0; j < files2.length; j++) {
						file = files2[j];
						str = file.getName();
						if (str.toLowerCase().endsWith(".apk")) {
							pluginInfo.apkName = str;
							pluginInfo.apkPath = file.getAbsolutePath();
						} else if (str.toLowerCase().endsWith(".so")) {
							pluginInfo.nativeLibraryPaths.add(file.getParent());
						} else if (str.toLowerCase().endsWith(".enter")) {
							pluginInfo.enterClass = str.substring(0,
									str.length() - 6);
						}
					}
					if (pluginInfo.checkApk()) {
						plugins.add(pluginInfo);
					}
				}
				PluginManagerImpl.this.mHandler.post(new Runnable() {
					public void run() {
						listener.onScanEnd(plugins);
					}
				});
			}
		}.start();
	}

	public void setCurrentPlugin(String key) {
		this.mCurrentPlugin = ((Plugin) this.mPlugins.get(key));
	}

	public Plugin getCurrentPlugin() {
		return this.mCurrentPlugin;
	}

	public void uninstallPlugin(String name) {
		if (this.mPlugins.containsKey(name)) {
			Plugin plugin = (Plugin) this.mPlugins.remove(name);
			this.mMajorHandler.removePlugin(plugin);
			plugin.mRealPackageInfo = null;
			System.gc();
		}
	}
}