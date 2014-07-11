package android.app;

import java.util.HashMap;

import android.content.Context;

/**
 * 加载后的插件
 * 
 * @author zhaoxuyang
 * 
 */
class Plugin {
	/**
	 * 插件信息
	 */
	public PluginInfo mPluginInfo;
	/**
	 * 插件的入口activity
	 */
	public String enterClass;
	/**
	 * 插件的classloader
	 */
	public ClassLoader mClassLoader;
	/**
	 * 加载到主程序后对应的packageinfo
	 */
	public LoadedApk mRealPackageInfo;
	/**
	 * 插件对应的application
	 */
	public Application mApplication;
	/**
	 * 插件中注册的receiver
	 */
	public HashMap<Context, LocalBroadcastManager> mLocalBroadCastManagers = new HashMap<Context, LocalBroadcastManager>();
}