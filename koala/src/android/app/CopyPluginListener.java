package android.app;

/**
 * 扫描插件的回掉
 * 
 * @author zhaoxuyang
 * 
 */
public interface CopyPluginListener {

	/**
	 * 扫描开始
	 */
	void onCopyStart();

	/**
	 * 扫描结束
	 * 
	 * @param paramArrayList
	 *            插件列表
	 */
	void onCopyEnd();
}