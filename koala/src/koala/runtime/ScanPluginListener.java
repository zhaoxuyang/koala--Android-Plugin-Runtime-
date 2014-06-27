package koala.runtime;

import java.util.ArrayList;

/**
 * É¨Ãè²å¼þµÄ¼àÌý
 * @author zhaoxuyang
 *
 */
public interface ScanPluginListener {
	public abstract void onScanStart();

	public abstract void onScanEnd(ArrayList<PluginInfo> paramArrayList);
}