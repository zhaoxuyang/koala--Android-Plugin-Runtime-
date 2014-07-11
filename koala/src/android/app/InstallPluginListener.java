package android.app;

/**
 * 插件安装的回掉
 * 
 * @author zhaoxuyang
 * 
 */
public interface InstallPluginListener {

	/**
	 * 插件开始安装
	 * 
	 * @param info
	 *            插件信息
	 */
	void onInstallStart(PluginInfo info);

	/**
	 * 插件安装完成
	 * 
	 * @param info
	 *            插件信息
	 */
	void onInstallEnd(PluginInfo info);
}