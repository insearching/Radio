package com.sj.radio.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sj.radio.app.entity.AuthResponse;
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

    private String currentUrl = null;
    private PlayerService mService;
    private PlayerReciever mReciever;

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
        mReciever = new PlayerReciever();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.ACTION_PLAYER);
        registerReceiver(mReciever, intentFilter);

        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, mConnection,
                BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReciever);
        unbindService(mConnection);

        super.onPause();
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

    RadioAdapter mAdapter;

    @Override
    public void onRemoteCallComplete(String xml) {
        try {
            XMLParser parser = new XMLParser(xml);
            AuthResponse response = parser.getAuthResponse();
            if (response != null) {
                if (response.getCode() == 0) {
                    mToken = response.getToken();
                    GETClient client = new GETClient(RadioActivity.this);
                    client.execute(GET_RADIOS_URL + mToken);
                }
                else if (response.getCode() == 1) {
                    String user = getIntent().getExtras().getString(KeyMap.USER);
                    String pass = getIntent().getExtras().getString(KeyMap.PASS);

                    GETClient client = new GETClient(this);
                    client.execute("http://android-course.comli.com/login.php?username=" + user + "&password=" + pass);
                }
                return;
            }

            parser = new XMLParser(xml);
            final ArrayList<Radio> radioList = parser.getRadioList();

            mAdapter = new RadioAdapter(this, radioList);
            mListview.setAdapter(mAdapter);
            restoreListView();

            mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    position -= 1;
                    String url = mAdapter.getItem(position).getUrl();

                    // player is not running
                    if (mService == null || mService.getUrl() == null) {
                        processStartService(url);
                        mAdapter.setItemStatus(position, RadioAdapter.PlaybackStatus.LOADING);
                        mAdapter.notifyDataSetChanged();
                    }

                    // player is playing station
                    else {
                        currentUrl = mService.getUrl();
                        unbindService(mConnection);
                        stopService(new Intent(RadioActivity.this, PlayerService.class));
                        mService = null;

                        // same station being selected, then stop playback
                        if (currentUrl != null && currentUrl.equals(url)) {
                            mAdapter.setItemStatus(position, RadioAdapter.PlaybackStatus.NONE);
                            mAdapter.notifyDataSetChanged();
                            currentUrl = null;
                        }

                        // another station selected
                        else {
                            processStartService(url);
                            int pos = findItemByUrl(currentUrl);

                            if (pos != -1)
                                mAdapter.setItemStatus(pos, RadioAdapter.PlaybackStatus.NONE);

                            mAdapter.setItemStatus(position, RadioAdapter.PlaybackStatus.LOADING);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });

            mListview.onRefreshComplete();

            showProgress(false);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private void restoreListView() {
        if (mService != null && mService.getUrl() != null) {
            int position = findItemByUrl(mService.getUrl());
            if (position > -1) {
                mAdapter.setItemStatus(position, RadioAdapter.PlaybackStatus.PLAYING);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private int findItemByUrl(String url) {
        int position = -1;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mAdapter.getItem(i).getUrl().equals(url))
                position = i;
        }
        return position;
    }

    class PlayerReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            boolean isPlaying = bundle.getBoolean(KeyMap.PLAYING, false);
            String url = bundle.getString(KeyMap.URL);

            int position = findItemByUrl(url);
            if (isPlaying && position != -1) {
                mAdapter.setItemStatus(position, RadioAdapter.PlaybackStatus.PLAYING);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void processStartService(final String url) {
        Intent intent = new Intent(RadioActivity.this, PlayerService.class);
        intent.putExtra(KeyMap.URL, url);
        intent.addCategory(url);
        startService(intent);

        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private void showProgress(boolean flag) {
        mListview.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        mProgressView.setVisibility(flag ? View.VISIBLE : View.INVISIBLE);
    }
}
