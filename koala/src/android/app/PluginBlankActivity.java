package android.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 插件activity的代理
 * 
 * @author zhaoxuyang
 * 
 */
public class PluginBlankActivity extends Activity {

    /**
     * 传进来插件activity的类名
     */
    public static final String ACTIVITY_NAME = "activityName";

    /**
     * 对应的插件名称
     */
    public static final String PLUGIN_NAME = "pluginname";

    /**
     * 插件的activity
     */
    private Activity activity;

   
    private static Method getFeatures;
    /**
     * 是否第一次
     */
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 根据intent信息，从插件中找到要载入的activity
        Intent intent = getIntent();
        String activityName = intent.getStringExtra(ACTIVITY_NAME);
        String pluginName = intent.getStringExtra(PLUGIN_NAME);
        if (TextUtils.isEmpty(activityName) || TextUtils.isEmpty(pluginName)) {
            return;
        }

        PluginManagerImpl manager = PluginManagerImpl.getInstance();
        Plugin plugin = manager.getPlugin(pluginName);
        ActivityInfo[] infos = plugin.mPluginInfo.mPackageInfo.activities;
        ActivityInfo info = null;
        int screenOriention = -1;
        for (int i = 0; i < infos.length; i++) {
            info = infos[i];
            screenOriention = info.screenOrientation;
            if (info.name.equals(activityName)) {
                intent = new Intent();
                intent.setComponent(new ComponentName(plugin.mPluginInfo.mPackageInfo.packageName, activityName));
                intent.putExtras(getIntent());
                // 参照activitygroup的代码，将activity启动起来
                try {
                    activity = (Activity) manager.startActivityNow.invoke(manager.mActivityThread, this, activityName,
                            intent, info, new LocalBinder(), savedInstanceState, null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        Window win = activity.getWindow();
        getWindow().setAttributes(win.getAttributes());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(screenOriention);
        super.onCreate(savedInstanceState);
        setContentView(win.getDecorView());
    }

    @Override
    protected void onStart() {
        super.onStart();
        activity.onStart();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        activity.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        activity.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        activity.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity.onDestroy();
    }
    
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        activity.onSaveInstanceState(outState);
    }


    @Override
    void dispatchActivityResult(String who, int requestCode, int resultCode, Intent data) {
        activity.onActivityResult(requestCode, resultCode, data);
        super.dispatchActivityResult(who, requestCode, resultCode, data);
    }

    /*
     * 插件startactivity会调到这里，这里处理了隐式和显式启动activity (non-Javadoc)
     * 
     * @see android.app.Activity#startActivityFromChild(android.app.Activity,
     * android.content.Intent, int)
     */
    @Override
    public void startActivityFromChild(Activity child, Intent intent, int requestCode) {
        PluginManagerImpl.getInstance().startPluginActivity(intent);
    }

    /**
     * 占位用。。
     * 
     * @author zhaoxuyang
     * 
     */
    public static class LocalBinder extends Binder {
    }
}