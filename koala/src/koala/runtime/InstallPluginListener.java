package koala.runtime;

/**
 * 插件安装的回掉
 * 
 * @author zhaoxuyang
 * 
 */
public interface InstallPluginListener {
	
	public abstract void onInstallStart();

	public abstract void onInstallEnd();
}