package com.example.android.musicplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import java.util.Random;
import com.example.android.musicplayer.MusicService;

public class Settings extends AppCompatActivity {
    private boolean shuffle=false,autoplay=true;
    private Random rand;
    MusicService musicService;
    MainActivity mainActivity;
    CheckBox checkBox_a,checkBox_s;
    public Settings(){

    }


        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.settings_layout);
                rand=new Random();
                checkBox_a=findViewById(R.id.auto_play);
                checkBox_s=findViewById(R.id.Shuffle);
        }


        public void shuffle(){
            musicService=(MusicService)mainActivity.getMusicService();
            musicService.setShuffle();
        }



}
