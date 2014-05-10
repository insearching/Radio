package com.sj.radio.app;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import com.sj.radio.app.utils.KeyMap;

import java.io.IOException;

public class PlayerService extends Service {

    private MediaPlayer player;
    private String url = null;
    private boolean isRunning;

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public String getUrl(){
        return url;
    }

    public void stopService(){
        stopSelf();
    }

    public class LocalBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getExtras().containsKey(KeyMap.URL))
            url = intent.getExtras().getString(KeyMap.URL);

        if (!isRunning) {
            isRunning = true;
            new PlayerTask().execute();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    class PlayerTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                player = new MediaPlayer();
                player.setDataSource(url);
                player.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            player.start();
        }
    }

}
