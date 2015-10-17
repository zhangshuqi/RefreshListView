package com.itheima13.refreshlistviewdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.itheima13.refreshlistview.RefreshListView;
import com.itheima13.refreshlistview.RefreshListView.OnRefreshDataListener;

public class MainActivity extends Activity {

	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//跟新刷新状态
			rlv.updateState();
		};
	};
	private RefreshListView rlv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		rlv = (RefreshListView) findViewById(R.id.rlv_test);
	
	    rlv.setAdapter(new MyAdapter());
	    
	    
	    rlv.setOnRefreshDataListener(new OnRefreshDataListener() {
			
			@Override
			public void loadMore() {
				// TODO Auto-generated method stub
				mHandler.sendMessageDelayed(mHandler.obtainMessage(), 2000);
			}
			
			@Override
			public void freshData() {
				// TODO Auto-generated method stub
				mHandler.sendMessageDelayed(mHandler.obtainMessage(), 2000);
			}
		});
	}
	private class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 20;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(MainActivity.this);
			tv.setText("andy" + position);
			
			return tv;
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
