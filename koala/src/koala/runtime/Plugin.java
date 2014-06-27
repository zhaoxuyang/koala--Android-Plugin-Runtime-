package koala.runtime;

import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;

/**
 * 加载后的插件
 * 
 * @author zhaoxuyang
 * 
 */
class Plugin {
	public PackageInfo mPackageInfo;
	public Object mRealPackageInfo;
	public String enterClass;
	public ClassLoader mClassLoader;
	public PackageParser.Package mPackageObj;
}