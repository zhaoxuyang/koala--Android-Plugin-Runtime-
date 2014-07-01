package koala.runtime;

import android.text.TextUtils;
import java.util.ArrayList;

/**
 * 插件信息
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginInfo {
	public String name;
	public String apkName;
	public String enterClass;
	public String apkPath;
	public ArrayList<String> nativeLibraryPaths = new ArrayList<String>();
	public boolean isInstalled;

	public boolean checkApk() {
		if ((!TextUtils.isEmpty(this.name))
				&& (!TextUtils.isEmpty(this.apkName))
				&& (!TextUtils.isEmpty(this.enterClass))) {
			return true;
		}
		return false;
	}
}