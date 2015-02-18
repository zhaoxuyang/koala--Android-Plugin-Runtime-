package android.app;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Instrumentation;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

/**
 * 插件service对应的代理类
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginBlankService extends Service {

    /**
     * 插件service的类名
     */
    public static final String SERVICE_NAME = "servicename";

    /**
     * 插件的名称
     */
    public static final String PLUGIN_NAME = "pluginname";

    /**
     * ServiceConnection
     */
    public static final String SERVICE_CONNECTION = "serviceconnection";

    /**
     * 类型
     */
    public static final String TYPE = "type";

    /**
     * 启动service
     */
    public static final int START_TYPE = 1;

    /**
     * 停止service
     */
    public static final int STOP_TYPE = 2;

    /**
     * bind service
     */
    public static final int BIND_TYPE = 3;

    /**
     * unbind service
     */
    public static final int UNBIND_TYPE = 4;

    /**
     * 已启动的service
     */
    private HashMap<String, ServiceRecord> mCreateServices = new HashMap<String, ServiceRecord>();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String serviceName = intent.getStringExtra(SERVICE_NAME);
        String pluginName = intent.getStringExtra(PLUGIN_NAME);
        int type = intent.getIntExtra(TYPE, -1);
        IBinder binder = intent.getParcelableExtra(SERVICE_CONNECTION);
        String key = pluginName + "-" + serviceName;

        if (TextUtils.isEmpty(serviceName) || TextUtils.isEmpty(pluginName)
                || (type != START_TYPE && type != STOP_TYPE && type != BIND_TYPE && type != UNBIND_TYPE)) {
            return super.onStartCommand(intent, flags, startId);
        }

        if (mCreateServices.containsKey(key)) {
            ServiceRecord sr = mCreateServices.get(key);
            switch (type) {
            case START_TYPE:
                sr.mService.onStartCommand(intent, flags, startId);
                break;
            case STOP_TYPE:
                if (sr.mConnections.size() == 0) {
                    sr.mService.onDestroy();
                    mCreateServices.remove(key);
                }
                break;
            case BIND_TYPE:
                if (binder != null) {
                    IServiceConnection conn = IServiceConnection.Stub.asInterface(binder);
                    sr.mConnections.add(conn);
                    if (sr.mIbinder == null) {
                        sr.mIbinder = sr.mService.onBind(intent);
                    }
                    try {
                        conn.connected(new ComponentName(pluginName, serviceName), sr.mIbinder);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case UNBIND_TYPE:
                if (binder != null) {
                    IServiceConnection conn = IServiceConnection.Stub.asInterface(binder);
                    sr.mConnections.remove(conn);
                    try {
                        conn.connected(new ComponentName(pluginName, serviceName), null);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if (sr.mConnections.size() == 0) {
                        sr.mIbinder = null;
                        sr.mService.onUnbind(intent);
                        sr.mService.onDestroy();
                        mCreateServices.remove(key);
                    }
                }
                break;
            }

        } else {
            if (type == START_TYPE || type == BIND_TYPE) {
                ServiceRecord sr = createService(pluginName, serviceName, key);
                if (sr != null) {
                    if (type == START_TYPE) {
                        sr.mService.onStartCommand(intent, flags, startId);
                    }

                    if (type == BIND_TYPE) {
                        if (binder != null) {
                            IServiceConnection conn = IServiceConnection.Stub.asInterface(binder);
                            sr.mConnections.add(conn);
                            if (sr.mIbinder == null) {
                                sr.mIbinder = sr.mService.onBind(intent);
                            }
                            try {
                                conn.connected(new ComponentName(pluginName, serviceName), sr.mIbinder);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private ServiceRecord createService(String pluginName, String serviceName, String key) {
        ServiceRecord sr = null;
        try {
            PluginManagerImpl manager = PluginManagerImpl.getInstance();
            Plugin plugin = manager.getPlugin(pluginName);
            ActivityThread thread = manager.mActivityThread;
            Instrumentation instrumentation = thread.getInstrumentation();
            Object packageInfo = plugin.mRealPackageInfo;
            Context context = this;
            if (manager.init != null) {
                Constructor construct = manager.contextImpl.getDeclaredConstructor();
                construct.setAccessible(true);
                context = (Context) construct.newInstance();
                manager.init.invoke(context, packageInfo, null, thread);
            } else {
                context = (Context) manager.createAppContext.invoke(null, thread, packageInfo);
            }
            Service service = (Service) plugin.mClassLoader.loadClass(serviceName).newInstance();
            manager.setOuterContext.invoke(context, service);
            service.attach(this, thread, serviceName, new LocalBinder(),
                    (Application) manager.makeApplication.invoke(packageInfo, false, instrumentation),
                    ActivityManagerNative.getDefault());
            service.onCreate();
            sr = new ServiceRecord();
            sr.mService = service;
            mCreateServices.put(key, sr);
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sr;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Iterator<ServiceRecord> iter = mCreateServices.values().iterator();
        while (iter.hasNext()) {
            iter.next().mService.onDestroy();
        }
        mCreateServices.clear();
    }

    /**
     * 占位的
     * 
     * @author zhaoxuyang
     * 
     */
    public static class LocalBinder extends Binder {
    }

    /**
     * 
     * @author zhaoxuyang
     * @since 2015-2-18
     */
    class ServiceRecord {
        public Service mService;
        public HashSet<IServiceConnection> mConnections = new HashSet<IServiceConnection>();
        public IBinder mIbinder;
    }

    public static class PluginServiceConnection extends IServiceConnection.Stub {

        public ServiceConnection conn;

        @Override
        public void connected(ComponentName cn, IBinder binder) throws RemoteException {
            if (conn != null) {
                if (binder != null) {
                    conn.onServiceConnected(cn, binder);
                } else {
                    conn.onServiceDisconnected(cn);
                }

            }
        }

    }

}
