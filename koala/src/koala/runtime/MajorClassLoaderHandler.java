package koala.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * ÕæÕýµÄhandler
 * 
 * @author zhaoxuyang
 * 
 */
class MajorClassLoaderHandler implements InvocationHandler {

	private ArrayList<Plugin> mPlugins = new ArrayList<Plugin>();

	public void addPlugin(Plugin classloader) {
		this.mPlugins.add(classloader);
	}

	public void removePlugin(Plugin classloader) {
		this.mPlugins.remove(classloader);
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Exception {
		Object obj = null;
		int size = this.mPlugins.size();

		for (int i = 0; i < size; i++) {
			Plugin plugin = (Plugin) this.mPlugins.get(i);
			if (method == MajorClassLoader.findClass) {

				ClassLoader loader = plugin.mClassLoader;
				try {
					obj = method.invoke(loader, args);
				} catch (Exception localException) {
				}
				if (obj != null) {
					break;
				}
			}

		}
		if (obj == null) {
			throw new Exception(method.getName());
		}

		return obj;
	}

}