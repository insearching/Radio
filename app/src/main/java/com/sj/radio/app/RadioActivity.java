package com.sj.radio.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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


public class RadioActivity extends Activity implements GETClient.GETListener{

    private String mToken = null;
    private static final String GET_RADIOS_URL = "http://android-course.comli.com/radios.php?token=";

    private ProgressBar mProgressView;
    private PullToRefreshListView mListview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        initViews();

        Bundle extras = null;
        if(getIntent().getExtras() != null){
            extras = getIntent().getExtras();
        } else if (savedInstanceState == null) {
            extras = savedInstanceState;
        }

        if(extras != null && extras.containsKey(KeyMap.TOKEN)){
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

    private void initViews(){
        mProgressView = (ProgressBar) findViewById(R.id.progressBar);
        mListview = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_listview);
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

            RadioAdapter adapter = new RadioAdapter(this, radioList);
            mListview.setAdapter(adapter);
            mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(RadioActivity.this, PlayerService.class);
                    intent.putExtra(KeyMap.URL, radioList.get(position).getUrl());
                    startService(intent);

                    Toast.makeText(RadioActivity.this, "Now playing " + radioList.get(position).getName(), Toast.LENGTH_LONG).show();
                }
            });
            mListview.onRefreshComplete();

            showProgress(false);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private void showProgress(boolean flag){
        mListview.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        mProgressView.setVisibility(flag ? View.VISIBLE : View.INVISIBLE);
    }
}
