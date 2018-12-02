package com.example.android.musicplayer;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;

import com.example.android.musicplayer.MusicService.MusicBinder;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    public MusicController controller;
    Settings settings;
    private ArrayList<Song> songs;
    private AudioManager audioManager;
    private MusicService musicService;
    private Intent intent;
    private boolean paused = true;
    private boolean playback_paused = false;
    private boolean musicBound = false;
    private BroadcastReceiver onPreparereceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            controller.show(0);
        }
    };
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            //get service
            musicService = binder.getService();
            //pass list
            musicService.setList(songs);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    start();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    stop_playing();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    pause();

                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    pause();
                    break;
            }

        }
    };


    public MainActivity() {

    }

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        if (musicUri != null) {
            Cursor musicCursor;
            musicCursor = musicResolver.query(musicUri, null, MediaStore.Audio.Media.DATA + " like ?", new String[]{"%Music%"}, null);
            if (musicCursor != null && musicCursor.moveToFirst()) {
                int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

                do {
                    long id = musicCursor.getLong(idColumn);
                    String title = musicCursor.getString(titleColumn);
                    String artist = musicCursor.getString(artistColumn);
                    songs.add(new Song(title, id, artist));

                } while (musicCursor.moveToNext());
                musicCursor.close();
            }
        }


    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 9999:
                Uri columnuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Log.i("Test", "Column Uri" + columnuri);
                musicUri = data.getData();
                Uri newuri = Uri.parse(musicUri.toString());
                Log.i("Test", "New Uri" + newuri);
                Log.i("Test", "Result URI " + data.getData());
                break;

        }

    }*/


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_list);
        ListView listView;
        settings = new Settings();
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle("MuPlay");
        myToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(myToolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);


// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
// app-defined int constant

                return;
            }
        }
        /*Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999);*/
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        listView = findViewById(R.id.listview);
        controller = new MusicController(this);
        songs = new ArrayList<>();
        getSongList();
        setController();
        SongAdapter songAdapter = new SongAdapter(this, songs);
        listView.setAdapter(songAdapter);


    }

    public void songPicked(View view) {
        int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            musicService.setSong(Integer.parseInt(view.getTag().toString()));
            musicService.playSong();
            if (playback_paused) {
                setController();
                controller.show();
                playback_paused = false;
            }

            controller.show(0);
        }


    }

    public Service getMusicService() {
        return musicService;
    }


    public void stop_playing() {
        musicService.stop();
        musicBound = false;
        unbindService(musicConnection);
        musicService = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (intent == null) {

            intent = new Intent(this, MusicService.class);
            bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(intent);

        }

    }

    @Override
    protected void onStop() {
        controller.hide();
        audioManager.abandonAudioFocus(audioFocusChangeListener);
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            stopService(intent);
            musicService = null;
            Intent settingsIntent = new Intent(this, Settings.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopService(intent);
        if (musicBound)
            unbindService(musicConnection);
        musicService = null;
        audioManager.abandonAudioFocus(audioFocusChangeListener);
        super.onDestroy();
    }


    private void setController() {

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playnext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playprev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.list));
        controller.setEnabled(true);
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(onPreparereceiver, new IntentFilter("MEDIA_PLAYER_PREPARED"));
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).registerReceiver(onPreparereceiver, new IntentFilter("Paused"));

        super.onPause();

    }

    private void playnext() {
        musicService.playNext();
        if (playback_paused) {
            playback_paused = false;
            setController();
        }
        controller.show(0);
    }

    private void playprev() {
        musicService.playPrev();
        if (playback_paused) {
            setController();
            playback_paused = false;
        }
        controller.show(0);
    }

    @Override
    public void start() {
        musicService.go();
        controller.show();
    }


    @Override
    public void pause() {
        musicService.pausePlayer();
        playback_paused = true;


    }

    @Override
    public void onBackPressed() { // a function in MainActivity
        moveTaskToBack(true);
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPng())
            return musicService.getDur();
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPng()) {
            return musicService.getSongPosn();
        } else {
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);

    }

    @Override
    public boolean isPlaying() {
        return musicService != null && musicBound && musicService.isPng();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
