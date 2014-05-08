package com.sj.radio.app;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.sj.radio.app.utils.KeyMap;

import java.io.IOException;

public class PlayerService extends Service {

    private MediaPlayer player;
    private String url = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getExtras().containsKey(KeyMap.URL))
            url = intent.getExtras().getString(KeyMap.URL);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        android.os.Debug.waitForDebugger();
        try {
            player = new MediaPlayer();
            player.setDataSource(url);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
