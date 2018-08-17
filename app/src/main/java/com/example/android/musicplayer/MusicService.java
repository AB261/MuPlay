package com.example.android.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private final IBinder musicBind = new MusicBinder();
    MainActivity mainActivity;
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private String songTitle = "";
    private boolean shuffle = false;
    private Random rand;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> a) {
        songs = a;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }

    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        Toast.makeText(this, "Unable to play MediaPlayer", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mainActivity = new MainActivity();
        mp.start();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.baseline_play_arrow_white_24dp)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();
        Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);

        int NOTIFY_ID = 1;
        startForeground(NOTIFY_ID, not);


    }

    public void setShuffle() {
        shuffle = !shuffle;
    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        stopForeground(true);
    }

    public void playSong() {
        player.reset();
        //get song
        Song playSong = songs.get(songPosn);
        songTitle = playSong.getName();
//get id
        long currSong = playSong.getAudio_ad();
//set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);

        }
        player.prepareAsync();

    }

    public int getSongPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
        Intent OnPauseIntent = new Intent("Paused");
        LocalBroadcastManager.getInstance(this).sendBroadcast(OnPauseIntent);

    }

    public void seek(int pos) {
        player.seekTo(pos);
    }

    public void go() {
        player.start();
    }

    public void stop() {
        player.stop();
    }

    public void playPrev() {
        songPosn--;
        if (songPosn == -1) {
            songPosn = songs.size() - 1;
        }
        playSong();
    }

    public void playNext() {
        if (shuffle) {
            int newSong = songPosn;
            while (newSong == songPosn) {
                newSong = rand.nextInt(songs.size());
            }
            songPosn = newSong;
        } else {
            songPosn++;
            if (songPosn >= songs.size()) songPosn = 0;
        }
        playSong();
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

}
