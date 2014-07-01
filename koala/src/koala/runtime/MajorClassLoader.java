package koala.runtime;

import dalvik.system.DexClassLoader;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

/**
 * 主classloader，如果一个类在主loader没有找到，则会在子loader中查看
 * 
 * @author zhaoxuyang
 * 
 */
@SuppressWarnings("rawtypes")
class MajorClassLoader extends DexClassLoader {
	public static Method getLdLibraryPath;
	public static Method findClass;
	public static Method findLibrary;
	public static Method findResource;
	public static Method getPackage;
	public static Method findResources;
	private MajorClassLoaderHandler mHandler = new MajorClassLoaderHandler();

	static {
		Class clazz = null;
		try {
			clazz = Class.forName("dalvik.system.BaseDexClassLoader");
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		if (clazz != null) {
			try {
				getLdLibraryPath = clazz.getDeclaredMethod("getLdLibraryPath",
						new Class[0]);
				getLdLibraryPath.setAccessible(true);
			} catch (Exception localException) {
			}
			try {
				getPackage = clazz.getDeclaredMethod("getPackage",
						new Class[] { String.class });
				getPackage.setAccessible(true);
			} catch (Exception localException1) {
			}
			try {
				findClass = clazz.getDeclaredMethod("findClass",
						new Class[] { String.class });
				findClass.setAccessible(true);
			} catch (Exception localException2) {
			}
			try {
				findLibrary = clazz.getDeclaredMethod("findLibrary",
						new Class[] { String.class });
				findLibrary.setAccessible(true);
			} catch (Exception localException3) {
			}
			try {
				findResource = clazz.getDeclaredMethod("findResource",
						new Class[] { String.class });
				findResource.setAccessible(true);
			} catch (Exception localException4) {
			}
			try {
				findResources = clazz.getDeclaredMethod("findResources",
						new Class[] { String.class });
				findResources.setAccessible(true);
			} catch (Exception localException5) {
			}
		} else {
			try {
				clazz = DexClassLoader.class;
				getPackage = clazz.getDeclaredMethod("getPackage",
						new Class[] { String.class });
				getPackage.setAccessible(true);
				findClass = clazz.getDeclaredMethod("findClass",
						new Class[] { String.class });
				findClass.setAccessible(true);
				findLibrary = clazz.getDeclaredMethod("findLibrary",
						new Class[] { String.class });
				findLibrary.setAccessible(true);
				findResource = clazz.getDeclaredMethod("findResource",
						new Class[] { String.class });
				findResource.setAccessible(true);

				clazz = ClassLoader.class;
				findResources = clazz.getDeclaredMethod("findResources",
						new Class[] { String.class });
				findResources.setAccessible(true);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	public MajorClassLoader(String dexPath, String optimizedDirectory,
			String libraryPath, ClassLoader parent) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class clazz = null;
		try {
			clazz = super.findClass(name);
		} catch (Exception e1) {
			try {
				clazz = (Class) this.mHandler.invoke(this, findClass,
						new Object[] { name });
			} catch (Exception e) {
				throw new ClassNotFoundException(name);
			}
		}

		return clazz;
	}

	@Override
	public String findLibrary(String libname) {
		String lib = null;
		try {
			lib = super.findLibrary(libname);
		} catch (Exception localException) {
		}
		if (lib == null)
			try {
				lib = (String) this.mHandler.invoke(this, findLibrary,
						new Object[] { libname });
			} catch (Exception localException1) {
			}
		return lib;
	}

	@Override
	protected URL findResource(String name) {
		URL url = null;
		try {
			url = super.findResource(name);
		} catch (Exception localException) {
		}
		if (url == null)
			try {
				url = (URL) this.mHandler.invoke(this, findResource,
						new Object[] { name });
			} catch (Exception localException1) {
			}
		return url;
	}

	@Override
	protected Package getPackage(String name) {
		Package pack = null;
		try {
			pack = super.getPackage(name);
		} catch (Exception localException) {
		}
		if (pack == null)
			try {
				pack = (Package) this.mHandler.invoke(this, getPackage,
						new Object[] { name });
			} catch (Exception localException1) {
			}
		return pack;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Enumeration<URL> findResources(String resName) {
		Enumeration enu = null;
		try {
			enu = super.findResources(resName);
		} catch (Exception localException) {
		}
		if (enu == null)
			try {
				enu = (Enumeration) this.mHandler.invoke(this, findResources,
						new Object[] { resName });
			} catch (Exception localException1) {
			}
		return enu;
	}

	public String getLdLibraryPath() {
		String str = null;
		if (getLdLibraryPath != null)
			try {
				str = (String) getLdLibraryPath.invoke(this, new Object[0]);
				if (str == null)
					this.mHandler.invoke(this, getLdLibraryPath, null);
			} catch (Exception localException) {
			}
		return str;
	}

	public MajorClassLoaderHandler getHandler() {
		return this.mHandler;
	}

	public void setHandler(MajorClassLoaderHandler handler) {
		this.mHandler = handler;
	}
}