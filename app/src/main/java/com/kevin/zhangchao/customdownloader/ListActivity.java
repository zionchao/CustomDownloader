package com.kevin.zhangchao.customdownloader;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.kevin.zhangchao.downloder.DataWatcher;
import com.kevin.zhangchao.downloder.DownloadEntry;
import com.kevin.zhangchao.downloder.DownloadManager;
import com.kevin.zhangchao.downloder.Trace;

import java.util.ArrayList;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class ListActivity extends Activity {

    private DownloadManager mDownloadManager;
    private ArrayList<DownloadEntry> mDownloadEntries = new ArrayList<>();
    private DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry data) {
            int index = mDownloadEntries.indexOf(data);
            if (index != -1){
                mDownloadEntries.remove(index);
                mDownloadEntries.add(index,data);
                adapter.notifyDataSetChanged();
            }
            Trace.e(data.toString());
        }
    };
    private ListView mDownloadLsv;
    private DownloadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDownloadManager = DownloadManager.getInstance(this);
        setContentView(R.layout.activity_list);
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test0.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test1.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test2.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test3.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test4.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test5.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test6.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test7.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test8.jpg"));
        DownloadEntry realEntry=null;
        DownloadEntry entry=null;
        for (int i=0;i<mDownloadEntries.size();i++){
            entry=mDownloadEntries.get(i);
            realEntry=DownloadManager.getInstance(this).queryDownloadEntry(entry.id);
            if (realEntry!=null){
                mDownloadEntries.remove(i);
                mDownloadEntries.add(i,realEntry);
            }
        }
        mDownloadLsv = (ListView) findViewById(R.id.mDownloadLsv);
        adapter = new DownloadAdapter();
        mDownloadLsv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDownloadManager.addObserver(watcher);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mDownloadManager.removeObserver(watcher);
    }

    class DownloadAdapter extends BaseAdapter {

        private ViewHolder holder;

        @Override
        public int getCount() {
            return mDownloadEntries != null ? mDownloadEntries.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mDownloadEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || convertView.getTag() == null) {
                convertView = LayoutInflater.from(ListActivity.this).inflate(R.layout.activity_list_item, null);
                holder = new ViewHolder();
                holder.mDownloadBtn = (Button) convertView.findViewById(R.id.mDownloadBtn);
                holder.mDownloadLabel = (TextView) convertView.findViewById(R.id.mDownloadLabel);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final DownloadEntry entry = mDownloadEntries.get(position);
            holder.mDownloadLabel.setText(entry.name + " is " + entry.status + " " + entry.currentLength + "/" + entry.totalLength);
            holder.mDownloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (entry.status == DownloadEntry.DownloadStatus.idle) {
                        mDownloadManager.add(entry);
                    } else if (entry.status == DownloadEntry.DownloadStatus.downloading||entry.status== DownloadEntry.DownloadStatus.wating) {
                        mDownloadManager.pause(entry);
                    } else if (entry.status == DownloadEntry.DownloadStatus.paused) {
                        mDownloadManager.resume(entry);
                    }
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        TextView mDownloadLabel;
        Button mDownloadBtn;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if(item.getTitle().equals("pause all")){
                item.setTitle(R.string.action_recover_all);
                mDownloadManager.pauseAll();
            }else {
                item.setTitle(R.string.action_pause_all);
                mDownloadManager.recoverAll();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
