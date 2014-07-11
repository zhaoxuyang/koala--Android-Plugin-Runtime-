package com.example.mainapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.InstallPluginListener;
import android.app.PluginInfo;
import android.app.PluginManager;
import android.app.ProgressDialog;
import android.app.ScanPluginListener;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

public class MainActivity extends Activity implements ScanPluginListener,
		OnItemClickListener, InstallPluginListener {

	private ProgressDialog mDialog;

	private ListView mListView;
	
	private PluginsAdapter mAdapter;
	
	private boolean mIsFirst = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setOnItemClickListener(this);
		mDialog = new ProgressDialog(this);
		mDialog.setMessage("scanning");
		PluginManager.getInstance().init(this,
				getDir("dexout", Context.MODE_PRIVATE).getAbsolutePath());

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mIsFirst){
			mIsFirst = false;
			new AsyncTask<Void, Void, File>() {

				@Override
				protected File doInBackground(Void... arg0) {
					File dir = Environment.getExternalStorageDirectory();
					dir = new File(dir, "koala");
					if (dir.exists()) {
						try {
							Process process = Runtime.getRuntime().exec("rm -rf "+dir.getAbsolutePath());
							if(process.waitFor()!=0){
								return null;
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					dir.mkdirs();
					try {
						File demo = new File(dir, "demo");
						if(!demo.exists()){
							demo.mkdirs();
							InputStream is = getAssets().open("demo/PluginApp.apk");
							File file = new File(demo,"PluginApp.apk");
							OutputStream os = new FileOutputStream(file);
							copyFile(is, os);
							is.close();
							os.close();
							
							String abi = Build.CPU_ABI;
							is = getAssets().open("demo/libs/"+abi+"/libhello-jni.so");
							File libs = new File(demo,"libs");
							if(!libs.exists()){
								libs.mkdirs();
							}
							File temp = new File(libs,abi);
							if(!temp.exists()){
								temp.mkdirs();
							}
							file = new File(temp,"libhello-jni.so");
							os = new FileOutputStream(file);
							copyFile(is, os);
							is.close();
							os.close();
							
							is = getAssets().open("demo/com.example.pluginapp.MainActivity.enter");
							file = new File(demo,"com.example.pluginapp.MainActivity.enter");
							os = new FileOutputStream(file);
							copyFile(is, os);
							is.close();
							os.close();
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					return dir;
				}
				
				private void copyFile(InputStream src, OutputStream des) throws IOException{
					byte[] bytes = new byte[1024];
					int len = 0;
					while((len=src.read(bytes))>0){
						des.write(bytes, 0, len);
					}
					des.flush();
				}
				
				protected void onPostExecute(File result) {
					PluginManager.getInstance().scanApks(result, MainActivity.this);
				};

			}.execute();
		}
		
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
				convertView = mInflater.inflate(R.layout.plugin_item, parent,false);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.name);
			final PluginInfo info = mDatas.get(pos);
			tv.setText(info.name);
			Button btn = (Button) convertView.findViewById(R.id.status);
			btn.setFocusable(false);
			if(info.isInstalled){
				btn.setText("uninstall");
				btn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						PluginManager.getInstance().uninstallPlugin(mContext,info.name);
						notifyDataSetChanged();
					}
				});
			}else{
				btn.setText("install");
				btn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						PluginManager.getInstance().installPlugin(info, (InstallPluginListener) mContext);;
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

}
