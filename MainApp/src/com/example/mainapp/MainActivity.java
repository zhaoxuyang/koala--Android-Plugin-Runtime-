package com.example.mainapp;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ApkFile;
import android.app.CopyPluginListener;
import android.app.InstallPluginListener;
import android.app.PluginInfo;
import android.app.PluginManager;
import android.app.ProgressDialog;
import android.app.ScanPluginListener;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements ScanPluginListener, OnItemClickListener, InstallPluginListener, CopyPluginListener{

    private ProgressDialog mDialog;

    private ListView mListView;

    private PluginsAdapter mAdapter;

    private ArrayList<ApkFile> apks = new ArrayList<ApkFile>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.list);
        mListView.setOnItemClickListener(this);
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("scanning");
        File dir = getFilesDir();
        dir = new File(dir, "koala");
        PluginManager.getInstance().init(this, getDir("dexout", Context.MODE_PRIVATE).getAbsolutePath(),
                dir.getAbsolutePath());

        ApkFile apk = new ApkFile();
        apk.apkName = "CppEmptyTest.apk";
        apk.name = "cocos2dx";
        apk.nativeLibs.add("libcpp_empty_test.so");
        apk.version = 1.0f;
        apks.add(apk);

        apk = new ApkFile();
        apk.apkName = "PluginApp.apk";
        apk.name = "simpledemo";
        apk.nativeLibs.add("libhello-jni.so");
        apk.version = 1.0f;
        apks.add(apk);
        PluginManager.getInstance().copyApksFromAsset(apks, getAssets(), this);
    }

    @Override
    public void onScanEnd(ArrayList<PluginInfo> arg0) {
        mAdapter = new PluginsAdapter(this, arg0);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onScanStart() {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        PluginManager.getInstance().startPlugin((PluginInfo) mAdapter.getItem(pos));
    }

    static class PluginsAdapter extends BaseAdapter {

        private Activity mContext;

        private ArrayList<PluginInfo> mDatas = new ArrayList<PluginInfo>();

        private LayoutInflater mInflater;

        public PluginsAdapter(Activity context, ArrayList<PluginInfo> datas) {
            if (datas == null) {
                return;
            }
            this.mContext = context;
            this.mDatas = datas;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int arg0) {
            return mDatas.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.plugin_item, parent, false);
            }
            TextView tv = (TextView) convertView.findViewById(R.id.name);
            final PluginInfo info = mDatas.get(pos);
            tv.setText(info.applicationName);
            Button btn = (Button) convertView.findViewById(R.id.status);
            btn.setFocusable(false);
            if (info.isInstalled) {
                btn.setText("uninstall");
                btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        PluginManager.getInstance().uninstallPlugin(mContext, info.packageName);
                        notifyDataSetChanged();
                    }
                });
            } else {
                btn.setText("install");
                btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        PluginManager.getInstance().installPlugin(info, (InstallPluginListener) mContext);
                        ;
                    }
                });
            }
            return convertView;
        }

    }

    @Override
    public void onInstallEnd(PluginInfo arg0) {
        mAdapter.notifyDataSetChanged();
        PluginManager.getInstance().startPlugin(arg0);
    }

    @Override
    public void onInstallStart(PluginInfo arg0) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.add("menu1");
        menu.add("menu2");
        menu.add("menu3");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCopyStart() {
        
    }

    @Override
    public void onCopyEnd() {
        PluginManager.getInstance().scanApks(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PluginManager.getInstance().destory();
    }

}
