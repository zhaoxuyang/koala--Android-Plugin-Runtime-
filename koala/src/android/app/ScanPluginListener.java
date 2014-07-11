package android.app;

import java.util.ArrayList;

/**
 * 扫描插件的回掉
 * 
 * @author zhaoxuyang
 * 
 */
public interface ScanPluginListener {

	/**
	 * 扫描开始
	 */
	void onScanStart();

	/**
	 * 扫描结束
	 * 
	 * @param paramArrayList
	 *            插件列表
	 */
	void onScanEnd(ArrayList<PluginInfo> paramArrayList);
}