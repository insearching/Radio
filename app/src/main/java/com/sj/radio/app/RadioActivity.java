package com.sj.radio.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sj.radio.app.entity.Radio;
import com.sj.radio.app.utils.GETClient;
import com.sj.radio.app.utils.KeyMap;
import com.sj.radio.app.utils.XMLParser;

import org.xmlpull.v1.XmlPullParserException;

import java.util.ArrayList;


public class RadioActivity extends Activity implements GETClient.GETListener {

    private String mToken = null;
    private static final String GET_RADIOS_URL = "http://android-course.comli.com/radios.php?token=";

    private ProgressBar mProgressView;
    private PullToRefreshListView mListview;
    private boolean bound = false;
    private String LOG_TAG = "SERVICE TAG";
    private String currentRadio = null;
    private PlayerService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        initViews();

        Bundle extras = null;
        if (getIntent().getExtras() != null) {
            extras = getIntent().getExtras();
        } else if (savedInstanceState == null) {
            extras = savedInstanceState;
        }

        if (extras != null && extras.containsKey(KeyMap.TOKEN)) {
            mToken = extras.getString(KeyMap.TOKEN);
            GETClient client = new GETClient(this);
            client.execute(GET_RADIOS_URL + mToken);
            showProgress(true);
        }

        PullToRefreshListView pullToRefreshView = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_listview);
        pullToRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                GETClient client = new GETClient(RadioActivity.this);
                client.execute(GET_RADIOS_URL + mToken);
            }
        });
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            PlayerService.LocalBinder b = (PlayerService.LocalBinder) binder;
            mService = b.getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    private void initViews() {
        mProgressView = (ProgressBar) findViewById(R.id.progressBar);
        mListview = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_listview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, mConnection,
                BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KeyMap.TOKEN, mToken);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.radio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return true;
    }

    @Override
    public void onRemoteCallComplete(String xml) {
        try {
            XMLParser parser = new XMLParser(xml);
            final ArrayList<Radio> radioList = parser.getRadioList();

            final RadioAdapter adapter = new RadioAdapter(this, radioList);
            mListview.setAdapter(adapter);
            mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    position -= 1;
                    String url = radioList.get(position).getUrl();

                    if (mService == null || mService.getUrl() == null)
                        processStartService(url);
                    else {
                        Toast.makeText(RadioActivity.this, "Now playing " + mService.getUrl(), Toast.LENGTH_LONG).show();
                        mService.stopSelf();
                    }
                    //Toast.makeText(RadioActivity.this, "Now playing " + radioList.get(position).getName(), Toast.LENGTH_LONG).show();
                }
            });

            mListview.onRefreshComplete();

            showProgress(false);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private void processStartService(final String url) {
        Intent intent = new Intent(RadioActivity.this, PlayerService.class);
        intent.putExtra(KeyMap.URL, url);
        intent.addCategory(url);

        if (startService(intent) != null) {
            Toast.makeText(getBaseContext(), "Service is already running", Toast.LENGTH_SHORT).show();
            stopService(intent);
        } else {
            Toast.makeText(getBaseContext(), "There is no service running, starting service..", Toast.LENGTH_SHORT).show();
        }

        //bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    class Hold extends AsyncTask<Void, Void, Void>{

        Intent i;
        public Hold (Intent i){
            this.i = i;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            stopService(i);
        }
    }

    private void processStopService(final String url) {
        Intent intent = new Intent(RadioActivity.this, PlayerService.class);
        intent.putExtra(KeyMap.URL, url);
        intent.addCategory(url);
        stopService(intent);
    }

    private void showProgress(boolean flag) {
        mListview.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        mProgressView.setVisibility(flag ? View.VISIBLE : View.INVISIBLE);
    }
}
