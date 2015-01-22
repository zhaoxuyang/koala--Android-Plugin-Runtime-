package android.app;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.Provider;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import dalvik.system.DexClassLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 真正pluginmanager的实现
 * 
 * @author zhaoxuyang
 * 
 */
class PluginManagerImpl {

    /**
     * DEBUG
     */
    private static final String TAG = "PLUGIN_MANAGER";

    private static final Pattern PATTERN_SEMICOLON = Pattern.compile(";");

    /**
     * 初始化主程序的context
     */
    private Context mContext;

    /**
     * 单例
     */
    private static PluginManagerImpl mInstance;

    /**
     * 所有已安装插件的hashmap
     */
    private HashMap<String, Plugin> mPlugins = new HashMap<String, Plugin>();

    /**
     * 所有插件信息
     */
    private HashMap<String, PluginInfo> mPluginInfos = new HashMap<String, PluginInfo>();

    /**
     * 主程序的ActivityThread
     */
    public ActivityThread mActivityThread;

    /**
     * 创建Object的方法，将插件注册到ActivityThread中
     */
    public Method getPackageInfo;

    /**
     * 创建application的方法
     */
    public Method makeApplication;

    /**
     * 启动activity的方法
     */
    public Method startActivityNow;

    /**
     * 用于给Object设置classloader
     */
    public Field mClassLoader;

    /**
     * ContextImpl类
     */
    public Class contextImpl;

    /**
     * ContextImpl的初始化方法
     */
    public Method init;

    /**
     * 4.4.3创建context的方法
     */
    public Method createAppContext;

    /**
     * ContextImpl的setOuterContext方法
     */
    public Method setOuterContext;

    /**
     * classloader 的dex输出目录
     */
    private String mDexoutputPath;

    /**
     * 主程序原始的classloader，作为插件classloader的parent
     */
    private ClassLoader mOriginalClassLoader;

    /**
     * 用于和主线程通信
     */
    private Handler mHandler;

    /**
     * 代理broadcastreceiver
     */
    private PluginBlankBroadcastReceiver mReceiver;

    /**
     * 是否注册了receiver
     */
    private boolean mHasRegisterReceiver;

    /**
     * 启动activity的时候用它查找
     */
    private ActivityIntentResolver mActivitys = new ActivityIntentResolver();

    /**
     * 启动service的时候用它查找
     */
    private ServiceIntentResolver mServices = new ServiceIntentResolver();

    /**
     * provider的时候用它查找
     */
    private HashMap<String, PackageParser.Provider> mProviderInfoMap = new HashMap<String, PackageParser.Provider>();

    /**
     * 插件存放的目录
     */
    private File mPluginRootDir;

    private static final String SEPARATOR = "@";

    private static final int STATUS_NORMAL = 0;

    private static final int STATUS_COPY = 1;

    private static final int STATUS_SCAN = 2;

    private static final int STATUS_INSTALLING = 3;

    private int mStatus = STATUS_NORMAL;

    /**
     * 获取单例
     * 
     * @return 插件单例
     */
    static PluginManagerImpl getInstance() {
        if (mInstance == null) {
            mInstance = new PluginManagerImpl();
        }
        return mInstance;
    }

    /**
     * 初始化
     * 
     * @param context
     *            上下文
     * @param dop
     *            插件dex的存放目录
     */
    void init(ContextWrapper context, String dop, String pluginRootDir) {
        mContext = context.getBaseContext();
        mDexoutputPath = dop;
        if (!TextUtils.isEmpty(pluginRootDir)) {
            mPluginRootDir = new File(pluginRootDir);
            if (!mPluginRootDir.exists()) {
                mPluginRootDir.mkdirs();
            }
        }
        mHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "start init environment");
        initEnvironment();
        Log.d(TAG, "after init environment");
    }

    private boolean checkExists(String[] names, ApkFile apk) {
        for (int j = 0; j < names.length; j++) {
            String name = names[j];
            int index = name.indexOf(SEPARATOR);
            if (index <= 0) {
                return false;
            }
            float version = Float.parseFloat(name.substring(index + 1));
            name = name.substring(0, index);
            if (name.equals(apk.name) && version == apk.version) {
                return true;
            }

        }
        return false;
    }

    void copyApksFromAsset(final ArrayList<ApkFile> apks, final AssetManager asset, final CopyPluginListener listener) {
        if (STATUS_COPY == mStatus) {
            return;
        }
        mStatus = STATUS_COPY;
        new AsyncTask<Void, Void, Integer>() {

            protected void onPreExecute() {
                if (listener != null) {
                    listener.onCopyStart();
                }
            };

            @Override
            protected Integer doInBackground(Void... arg0) {

                String[] names = mPluginRootDir.list();
                for (int i = 0; i < apks.size(); i++) {
                    try {
                        ApkFile apk = apks.get(i);
                        if (checkExists(names, apk)) {
                            continue;
                        }
                        String name = apk.apkName + SEPARATOR + apk.version;
                        File demo = new File(mPluginRootDir, name);
                        if (!demo.exists()) {
                            demo.mkdirs();
                        }
                        InputStream is = asset.open(apk.name + "/" + apk.apkName);
                        File file = new File(demo, apk.apkName);
                        OutputStream os = new FileOutputStream(file);
                        copyFile(is, os);
                        is.close();
                        os.close();

                        File libs = new File(demo, "libs");
                        if (!libs.exists()) {
                            libs.mkdirs();
                        }
                        String abi = Build.CPU_ABI;
                        File temp = new File(libs, abi);
                        if (!temp.exists()) {
                            temp.mkdirs();
                        }

                        for (int j = 0; j < apk.nativeLibs.size(); j++) {
                            is = asset.open(apk.name + "/libs/" + abi + "/" + apk.nativeLibs.get(j));
                            file = new File(temp, apk.nativeLibs.get(j));
                            os = new FileOutputStream(file);
                            copyFile(is, os);
                            is.close();
                            os.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return 0;

            }

            private void copyFile(InputStream src, OutputStream des) throws IOException {
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = src.read(bytes)) > 0) {
                    des.write(bytes, 0, len);
                }
                des.flush();
            }

            protected void onPostExecute(Integer result) {
                mStatus = STATUS_NORMAL;
                if (listener != null) {
                    listener.onCopyEnd();
                }
            };

        }.execute();
    }

    /**
     * 初始化环境，需要反射一些类，处理不同版本的差异等
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void initEnvironment() {

        // 得到主程序的Object
        Object packageInfo = null;
        Class packageClass = null;

        try {
            packageClass = Class.forName("android.app.LoadedApk");
        } catch (ClassNotFoundException e3) {
            e3.printStackTrace();
        }

        try {
            mClassLoader = packageClass.getDeclaredField("mClassLoader");
            mClassLoader.setAccessible(true);
        } catch (NoSuchFieldException e3) {
            e3.printStackTrace();
        }

        try {
            contextImpl = Class.forName("android.app.ContextImpl");
        } catch (ClassNotFoundException e3) {
            e3.printStackTrace();
        }

        try {
            init = contextImpl.getDeclaredMethod("init", packageClass, IBinder.class, ActivityThread.class);
            init.setAccessible(true);
        } catch (NoSuchMethodException e3) {
            try {
                createAppContext = contextImpl
                        .getDeclaredMethod("createAppContext", ActivityThread.class, packageClass);
                createAppContext.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            setOuterContext = contextImpl.getDeclaredMethod("setOuterContext", Context.class);
            setOuterContext.setAccessible(true);
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        }

        Field f = null;
        try {
            f = contextImpl.getDeclaredField("mPackageInfo");
            f.setAccessible(true);
        } catch (NoSuchFieldException e3) {
            e3.printStackTrace();
        }

        try {
            packageInfo = (Object) f.get(mContext);
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        }

        try {
            mOriginalClassLoader = (ClassLoader) mClassLoader.get(packageInfo);
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        }

        try {
            makeApplication = packageClass.getDeclaredMethod("makeApplication", boolean.class, Instrumentation.class);
            makeApplication.setAccessible(true);
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        }

        // 获得主程序的activitythread对象，并反射出getPackageInfo和startActivityNow两个方法。
        // getPackageInfo用于将插件加载到主程序的mPackages里面
        // startActivityNow 参照activitygroup的方法，用于启动activity。
        if (packageClass != null) {

            try {
                f = packageClass.getDeclaredField("mActivityThread");
                f.setAccessible(true);
                mActivityThread = (ActivityThread) f.get(packageInfo);
            } catch (NoSuchFieldException e3) {
                e3.printStackTrace();
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            }

            try {
                Class clazz = Class.forName("android.content.res.CompatibilityInfo");
                getPackageInfo = mActivityThread.getClass().getDeclaredMethod("getPackageInfoNoCheck",
                        new Class[] { ApplicationInfo.class, clazz });
                getPackageInfo.setAccessible(true);
                Log.d(TAG, "find method getPackageInfoNoCheck and os is high version");
            } catch (Exception e) {
                try {
                    getPackageInfo = mActivityThread.getClass().getDeclaredMethod("getPackageInfoNoCheck",
                            new Class[] { ApplicationInfo.class });
                    getPackageInfo.setAccessible(true);
                    Log.d(TAG, "find method getPackageInfoNoCheck and os is low version");
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                }
            }

            try {
                startActivityNow = mActivityThread.getClass().getDeclaredMethod(
                        "startActivityNow",
                        new Class[] { Activity.class, String.class, Intent.class, ActivityInfo.class, IBinder.class,
                                Bundle.class, Object.class });
            } catch (NoSuchMethodException e1) {
                try {
                    Class localClass = Class.forName("android.app.Activity$NonConfigurationInstances");
                    startActivityNow = mActivityThread.getClass().getDeclaredMethod(
                            "startActivityNow",
                            new Class[] { Activity.class, String.class, Intent.class, ActivityInfo.class,
                                    IBinder.class, Bundle.class, localClass });
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                } catch (ClassNotFoundException e2) {
                    e2.printStackTrace();
                }

            }

            mReceiver = new PluginBlankBroadcastReceiver();

        }
    }

    /**
     * 安装插件并启动
     * 
     * @param info
     *            插件信息
     * @param listener
     *            插件安装的回掉
     */
    void installPlugin(final PluginInfo info, final InstallPluginListener listener) {
        if (!mPlugins.containsKey(info.packageName)) {
            beginInstall(info, listener);
            realInstallPluin(info);
            afterInstall(info, listener);
        }
    }

    /**
     * 开始安装
     * 
     * @param info
     *            插件信息
     * @param listener
     *            安装回掉
     */
    private void beginInstall(final PluginInfo info, final InstallPluginListener listener) {
        if (listener != null) {
            listener.onInstallStart(info);
        }
    }

    /**
     * 结束安装
     * 
     * @param info
     *            插件信息
     * @param listener
     *            安装回掉
     */
    private void afterInstall(final PluginInfo info, final InstallPluginListener listener) {
        if (listener != null) {
            listener.onInstallEnd(info);
        }
    }

    /**
     * 启动插件
     * 
     * @param info
     *            插件信息
     */
    void startPlugin(PluginInfo info) {
        if (info == null) {
            return;
        }
        if (!mPlugins.containsKey(info.packageName)) {
            Log.e(TAG, "no plugin or not install");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        List res = mActivitys.queryIntentForPackage(intent, null, 0, info.mPackageObj.activities);
        if (res != null && res.size() > 0) {
            PackageParser.Activity activity = (android.content.pm.PackageParser.Activity) res.get(0);
            String className = activity.className;
            Intent newIntent = new Intent(mContext, PluginBlankActivity.class);
            newIntent.putExtra(PluginBlankActivity.ACTIVITY_NAME, className);
            newIntent.putExtra(PluginBlankActivity.PLUGIN_NAME, info.packageName);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(newIntent);
        } else {
            showToast(mContext, "this plugin no enter class", Toast.LENGTH_LONG);
        }

    }

    /**
     * 启动一个插件的内的activity
     * 
     * @param intent
     *            请求信息
     */
    void startPluginActivity(Intent intent) {

        ComponentName cn = intent.getComponent();
        String className = null;
        PluginInfo info = null;

        if (cn != null) {
            PackageParser.Activity activity = mActivitys.mActivities.get(cn);
            className = activity.className;
            info = mPluginInfos.get(activity.getComponentName().getPackageName());
        } else {
            String packageName = intent.getPackage();
            if (packageName != null) {
                PluginInfo temp = mPluginInfos.get(packageName);
                List<PackageParser.Activity> res = mActivitys.queryIntentForPackage(intent, null, 0,
                        info.mPackageObj.activities);
                if (res != null && res.size() > 0) {
                    info = temp;
                    className = res.get(0).className;
                }
            } else {
                List<PackageParser.Activity> res = mActivitys.queryIntent(intent, null, 0);
                if (res != null && res.size() > 0) {
                    packageName = res.get(0).getComponentName().getPackageName();
                    info = mPluginInfos.get(packageName);
                    className = res.get(0).className;
                }
            }
        }

        if (info != null) {
            installPlugin(info, null);
            Intent newIntent = new Intent(mContext, PluginBlankActivity.class);
            newIntent.putExtras(intent);
            newIntent.putExtra(PluginBlankActivity.ACTIVITY_NAME, className);
            newIntent.putExtra(PluginBlankActivity.PLUGIN_NAME, info.packageName);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(newIntent);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }

    }

    /**
     * 启动一个插件的内的service
     * 
     * @param intent
     *            请求信息
     * @return 启动service的ComponentName
     */
    ComponentName startPluginService(Intent intent) {

        ComponentName cn = deliverPluginService(intent, PluginBlankService.START_TYPE);
        if (cn != null) {
            return cn;
        }
        return mContext.startService(intent);

    }

    /**
     * 关闭一个插件的内的service
     * 
     * @param intent
     *            请求信息
     * @return 是否成功
     */
    boolean stopPluginService(Intent intent) {

        ComponentName cn = deliverPluginService(intent, PluginBlankService.STOP_TYPE);
        if (cn != null) {
            return true;
        }
        return mContext.stopService(intent);

    }

    /**
     * 将请求派发给代理service
     * 
     * @param intent
     *            请求信息
     * @param type
     *            请求类型
     * @return ComponentName
     */
    private ComponentName deliverPluginService(Intent intent, int type) {
        ComponentName cn = intent.getComponent();
        String className = null;
        PluginInfo info = null;
        ComponentName rescn = null;
        if (cn != null) {
            PackageParser.Service service = mServices.mServices.get(cn);
            className = service.className;
            rescn = service.getComponentName();
            info = mPluginInfos.get(rescn.getPackageName());
        } else {

            String packageName = intent.getPackage();
            if (packageName != null) {
                PluginInfo temp = mPluginInfos.get(packageName);
                List<PackageParser.Service> res = mServices.queryIntentForPackage(intent, null, 0,
                        info.mPackageObj.services);
                if (res != null && res.size() > 0) {
                    info = temp;
                    PackageParser.Service s = res.get(0);
                    rescn = s.getComponentName();
                    className = s.className;
                }
            } else {
                List<PackageParser.Service> res = mServices.queryIntent(intent, null, 0);
                if (res != null && res.size() > 0) {
                    rescn = res.get(0).getComponentName();
                    packageName = rescn.getPackageName();
                    info = mPluginInfos.get(packageName);
                    className = res.get(0).className;
                }
            }
        }
        if (info != null) {
            installPlugin(info, null);
            Intent newIntent = new Intent(mContext, PluginBlankService.class);
            newIntent.putExtras(intent);
            newIntent.putExtra(PluginBlankService.SERVICE_NAME, className);
            newIntent.putExtra(PluginBlankService.PLUGIN_NAME, info.packageName);
            newIntent.putExtra(PluginBlankService.TYPE, type);
            mContext.startService(newIntent);
            return rescn;
        }
        return null;
    }

    /**
     * 派发广播到插件
     * 
     * @param intent
     *            收到的广播信息
     */
    void onPluginReceive(Intent intent) {
        Iterator<Plugin> iter = mPlugins.values().iterator();
        while (iter.hasNext()) {
            Plugin plugin = iter.next();
            Iterator<LocalBroadcastManager> iter1 = plugin.mLocalBroadCastManagers.values().iterator();
            while (iter1.hasNext()) {
                LocalBroadcastManager manager = iter1.next();
                manager.sendBroadcast(intent);
            }
        }
    }

    /**
     * 注册广播
     * 
     * @param context
     *            上下文
     * @param pluginname
     *            插件名称
     * @param receiver
     *            receiver
     * @param filter
     *            filter
     * @return Intent
     */
    Intent registerReceiver(Context context, String pluginname, BroadcastReceiver receiver, IntentFilter filter) {
        Plugin plugin = getPlugin(pluginname);
        if (plugin != null) {
            LocalBroadcastManager manager = plugin.mLocalBroadCastManagers.get(context);
            if (manager == null) {
                manager = new LocalBroadcastManager(context);
                plugin.mLocalBroadCastManagers.put(context, manager);
            }
            manager.registerReceiver(receiver, filter);
        }
        mHasRegisterReceiver = true;
        return mContext.registerReceiver(mReceiver, filter);
    }

    /**
     * 同上
     * 
     * @param context
     *            context
     * @param pluginname
     *            pluginname
     * @param receiver
     *            receiver
     * @param filter
     *            filter
     * @param broadcastPermission
     *            broadcastPermission
     * @param scheduler
     *            scheduler
     * @return Intent
     */
    Intent registerReceiver(Context context, String pluginname, BroadcastReceiver receiver, IntentFilter filter,
            String broadcastPermission, Handler scheduler) {
        return registerReceiver(context, pluginname, receiver, filter);
    }

    /**
     * 解除注册
     * 
     * @param context
     *            context
     * @param pluginname
     *            pluginname
     * @param receiver
     *            receiver
     */
    void unregisterReceiver(Context context, String pluginname, BroadcastReceiver receiver) {
        Plugin plugin = getPlugin(pluginname);
        if (plugin != null) {
            LocalBroadcastManager manager = plugin.mLocalBroadCastManagers.get(context);
            if (manager != null) {
                manager.unregisterReceiver(receiver);
                if (manager.getSize() == 0) {
                    plugin.mLocalBroadCastManagers.remove(context);
                }
            }
        }
    }

    /**
     * @param name
     * @return
     */
    IContentProvider getContentProvider(String name) {
        IContentProvider ip = getExistContentProvider(name);
        if (ip != null) {
            return ip;
        }

        if (mProviderInfoMap.containsKey(name)) {
            PackageParser.Provider p = mProviderInfoMap.get(name);
            String packageName = p.info.applicationInfo.packageName;
            PluginInfo info = mPluginInfos.get(packageName);
            if (info != null) {
                installPlugin(info, null);
                Plugin plugin = mPlugins.get(info.packageName);
                return plugin.mProviderMap.get(name);
            } 
        }
        return mContext.getContentResolver().acquireProvider(name);
    }

    IContentProvider getExistContentProvider(String name) {
        Iterator<Plugin> iter = mPlugins.values().iterator();
        while (iter.hasNext()) {
            Plugin plugin = iter.next();
            if (plugin.mProviderMap.containsKey(name)) {
                return plugin.mProviderMap.get(name);
            }
        }
        return null;
    }

    /**
     * 真正安装插件的代码，核心方法
     * 
     * @param info
     *            插件信息
     */
    private void realInstallPluin(PluginInfo info) {
        try {
            Plugin plugin = new Plugin();
            plugin.mPluginInfo = info;

            PackageInfo packageInfo = info.mPackageInfo;
            packageInfo.applicationInfo.uid = Process.myUid();
            packageInfo.applicationInfo.sourceDir = info.apkPath;
            packageInfo.applicationInfo.publicSourceDir = info.apkPath;
            packageInfo.applicationInfo.dataDir = mContext.getDir(info.packageName, 0).getAbsolutePath();
            packageInfo.applicationInfo.flags &= ApplicationInfo.FLAG_HAS_CODE;

            Object realPackageInfo = null;
            try {
                realPackageInfo = (Object) getPackageInfo.invoke(mActivityThread, new Object[] {
                        packageInfo.applicationInfo, null });
            } catch (Exception e) {
                realPackageInfo = (Object) getPackageInfo.invoke(mActivityThread,
                        new Object[] { packageInfo.applicationInfo });
            }
            plugin.mRealPackageInfo = realPackageInfo;

            // 本地库的路径
            StringBuilder sb = new StringBuilder();
            int size = info.nativeLibraryPaths.size();
            for (int j = 0; j < size; j++) {
                sb.append(info.nativeLibraryPaths.get(j));
                if (j < size - 1) {
                    sb.append(":");
                }
            }

            // 实例化插件的classloader
            DexClassLoader classLoader = new DexClassLoader(info.apkPath, mDexoutputPath, sb.toString(),
                    mOriginalClassLoader);
            mClassLoader.set(realPackageInfo, classLoader);
            plugin.mClassLoader = classLoader;

            // 调用Application
            plugin.mApplication = (Application) makeApplication.invoke(realPackageInfo, false, null);
            if (plugin.mApplication instanceof PluginApplication) {
                PluginApplication pa = (PluginApplication) plugin.mApplication;
                pa.setPluginName(plugin.mPluginInfo.packageName);
            }
            plugin.mApplication.onCreate();

            Context context = plugin.mApplication.getApplicationContext();
            LocalBroadcastManager lbm = new LocalBroadcastManager(context);
            plugin.mLocalBroadCastManagers.put(context, lbm);

            // 注册manifest里面的receiver
            ArrayList<PackageParser.Activity> receivers = info.mPackageObj.receivers;
            for (int i = 0; i < receivers.size(); i++) {
                android.content.pm.PackageParser.Activity receiver = receivers.get(i);
                String receiverName = receiver.className;
                BroadcastReceiver broadcastreceiver = (BroadcastReceiver) plugin.mClassLoader.loadClass(receiverName)
                        .newInstance();
                ArrayList<ActivityIntentInfo> intentinfos = receiver.intents;
                for (int j = 0; j < intentinfos.size(); j++) {
                    IntentFilter filter = intentinfos.get(j);
                    lbm.registerReceiver(broadcastreceiver, filter);
                    mContext.registerReceiver(mReceiver, filter);
                    mHasRegisterReceiver = true;
                }
            }

            // 注册contentprovider
            ArrayList<PackageParser.Provider> providers = info.mPackageObj.providers;
            for (int i = 0; i < providers.size(); i++) {
                android.content.pm.PackageParser.Provider provider = providers.get(i);
                String providerName = provider.className;
                ContentProvider localProvider = (ContentProvider) plugin.mClassLoader.loadClass(providerName)
                        .newInstance();
                localProvider.attachInfo(plugin.mApplication, provider.info);
                IContentProvider realProvier = localProvider.getIContentProvider();
                String names[] = PATTERN_SEMICOLON.split(provider.info.authority);
                for (int j = 0; j < names.length; j++) {
                    plugin.mProviderMap.put(names[i], realProvier);
                }

            }

            // 恭喜，插件安装了
            plugin.mPluginInfo.isInstalled = true;

            mPlugins.put(info.packageName, plugin);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 遍历目录下的所有插件
     * 
     * @param dir
     *            dir
     * @param listener
     *            listener
     */
    void scanApks(final ScanPluginListener listener) {

        if (mPluginRootDir == null) {
            return;
        }

        if (STATUS_SCAN == mStatus) {
            return;
        }
        mStatus = STATUS_SCAN;

        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (listener != null) {
                    listener.onScanStart();
                }
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                // 先清掉
                mPluginInfos.clear();
                mPlugins.clear();

                File[] files = mPluginRootDir.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    String str = file.getName();

                    PluginInfo pluginInfo = new PluginInfo();
                    pluginInfo.applicationName = str;

                    File[] files2 = file.listFiles();
                    for (int j = 0; j < files2.length; j++) {
                        File temp = files2[j];
                        str = temp.getName();
                        if (str.toLowerCase().endsWith(".apk")) {
                            pluginInfo.apkName = str;
                            pluginInfo.apkPath = temp.getAbsolutePath();
                        } else if (str.equals("libs")) {
                            temp = new File(temp, Build.CPU_ABI);
                            if (temp.exists()) {
                                pluginInfo.nativeLibraryPaths.add(temp.getAbsolutePath());
                            }
                        }
                    }

                    if (pluginInfo.checkApk()) {

                        getPackageInfo(pluginInfo);

                        mPluginInfos.put(pluginInfo.packageName, pluginInfo);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                mStatus = STATUS_NORMAL;
                if (listener != null) {
                    listener.onScanEnd(new ArrayList<PluginInfo>(mPluginInfos.values()));
                }
            }
        }.execute();

    }

    /**
     * 通过名称得到plugin
     * 
     * @param name
     *            插件名称
     * @return 插件
     */
    Plugin getPlugin(String name) {
        return mPlugins.get(name);
    }

    /**
     * 解析APK的manifest
     * 
     * @param info
     *            插件信息
     */
    private void getPackageInfo(PluginInfo info) {

        int flags = PackageManager.GET_ACTIVITIES | PackageManager.GET_CONFIGURATIONS
                | PackageManager.GET_INSTRUMENTATION | PackageManager.GET_PERMISSIONS | PackageManager.GET_PROVIDERS
                | PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES | PackageManager.GET_SIGNATURES;

        // 需要获取Package对象，主要是处理隐式启动插件中的activity
        PackageParser parser = new PackageParser(info.apkPath);
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        File sourceFile = new File(info.apkPath);
        PackageParser.Package pack = parser.parsePackage(sourceFile, info.apkPath, metrics, 0);

        // 因为PackagePaser的generatePackageInfo方法不同版本参数相差太多，所以还是用packagemanager的api
        // 但这样导致APK被解析了两次，上面获取Package是一次
        PackageInfo packageInfo = mContext.getPackageManager().getPackageArchiveInfo(info.apkPath, flags);

        info.packageName = packageInfo.packageName;
        info.mPackageObj = pack;
        info.mPackageInfo = packageInfo;

        ArrayList<PackageParser.Activity> activitys = pack.activities;
        int size = activitys.size();
        for (int i = 0; i < size; i++) {
            mActivitys.addActivity(activitys.get(i));
        }

        ArrayList<PackageParser.Service> services = pack.services;
        size = services.size();
        for (int i = 0; i < size; i++) {
            mServices.addService(services.get(i));
        }

        ArrayList<PackageParser.Provider> providers = pack.providers;
        size = providers.size();
        for (int i = 0; i < size; i++) {
            Provider p = providers.get(i);
            String names[] = PATTERN_SEMICOLON.split(p.info.authority);
            for (int j = 0; j < names.length; j++) {
                mProviderInfoMap.put(names[i], p);
            }
        }
    }

    /**
     * 卸载插件
     * 
     * @param activity
     *            外部的activity
     * @param name
     *            插件名称
     */
    void uninstallPlugin(Activity activity, String name) {
        if (mPlugins.containsKey(name)) {
            Plugin plugin = (Plugin) mPlugins.get(name);
            if (plugin.mPluginInfo.nativeLibraryPaths.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity).setMessage("检测到该插件包含本地库，如卸载需要重启程序")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Process.killProcess(Process.myPid());
                            }
                        });
                builder.show();
                return;
            }
            mPlugins.remove(name);
            plugin.mRealPackageInfo = null;
            plugin.mPluginInfo.isInstalled = false;
            plugin.mLocalBroadCastManagers.clear();
            plugin.mProviderMap.clear();
            System.gc();
        }
    }

    /**
     * @param context
     * @param text
     * @param duration
     */
    void showToast(Context context, String text, int duration) {
        Toast.makeText(mContext, text, duration).show();
    }

    /**
     * @param context
     * @param resid
     * @param duration
     */
    void showToast(Context context, int resid, int duration) {
        String text = context.getResources().getString(resid);
        Toast.makeText(mContext, text, duration).show();
    }

    /**
     * 销毁
     */
    void destory() {
        if (mHasRegisterReceiver) {
            mContext.unregisterReceiver(mReceiver);
        }
        mPlugins.clear();
        mPluginInfos.clear();
        Process.killProcess(Process.myPid());
    }

}