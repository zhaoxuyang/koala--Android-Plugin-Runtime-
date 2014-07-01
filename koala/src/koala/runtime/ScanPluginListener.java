package koala.runtime;

import java.util.ArrayList;

/**
 * 扫描插件的回掉
 * @author zhaoxuyang
 *
 */
public interface ScanPluginListener {
	public abstract void onScanStart();

	public abstract void onScanEnd(ArrayList<PluginInfo> paramArrayList);
}