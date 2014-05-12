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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

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
    private View currentRow;

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

                    // player is not running
                    if (mService == null || mService.getUrl() == null) {
                        processStartService(url);
                        currentRow = view;
                        view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    }
                    // player is playing station
                    else {
                        currentUrl = mService.getUrl();
                        unbindService(mConnection);
                        stopService(new Intent(RadioActivity.this, PlayerService.class));
                        mService = null;

                        // same station being selected, then stop playback
                        if (currentUrl != null && currentUrl.equals(url)) {
                            ImageView iv = (ImageView) view.findViewById(R.id.playIv);
                            iv.setVisibility(View.INVISIBLE);
                            currentUrl = null;
                        }
                        // another station selected
                        else {
                            processStartService(url);
                            currentRow.findViewById(R.id.playIv).setVisibility(View.INVISIBLE);
                            view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                            currentRow = view;
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



    class PlayerReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            boolean isPlaying = b.getBoolean(KeyMap.PLAYING, false);

            if (currentRow != null && isPlaying) {
                currentRow.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

                ImageView iv = (ImageView) currentRow.findViewById(R.id.playIv);
                iv.setImageResource(R.drawable.ic_play);
                iv.setVisibility(View.VISIBLE);
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
