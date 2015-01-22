package android.app;

import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.content.IContentProvider;

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
	 * 插件的classloader
	 */
	public ClassLoader mClassLoader;
	/**
	 * 加载到主程序后对应的packageinfo
	 */
	public Object mRealPackageInfo;
	/**
	 * 插件对应的application
	 */
	public Application mApplication;
	/**
	 * 插件中注册的receiver
	 */
	public HashMap<Context, LocalBroadcastManager> mLocalBroadCastManagers = new HashMap<Context, LocalBroadcastManager>();
	
    /**
     * 插件中的contentprovider
     */
    public HashMap<String, IContentProvider> mProviderMap = new HashMap<String, IContentProvider>();

}