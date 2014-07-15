package android.app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.text.TextUtils;
import java.util.ArrayList;

/**
 * 插件信息
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginInfo {
	
	/**
	 * 应用名
	 */
	public String applicationName;
	/**
	 * 插件包名，唯一标识
	 */
	public String packageName;
	/**
	 * APK文件的名称
	 */
	public String apkName;
	/**
	 * 入口类名
	 */
	public String enterClass;
	/**
	 * 插件的路径
	 */
	public String apkPath;
	/**
	 * 插件本地库的路径
	 */
	public ArrayList<String> nativeLibraryPaths = new ArrayList<String>();
	/**
	 * 是否已安装
	 */
	public boolean isInstalled;

	/**
	 * 通过packagemanager获取的packageinfo
	 */
	public PackageInfo mPackageInfo;
	
	/**
	 * 完整的packageinfo
	 */
	public PackageParser.Package mPackageObj;

	/**
	 * 检查插件的完整性
	 * @return 是否完整
	 */
	public boolean checkApk() {
		if ((!TextUtils.isEmpty(this.apkName))
				&& (!TextUtils.isEmpty(this.enterClass))) {
			return true;
		}
		return false;
	}
}