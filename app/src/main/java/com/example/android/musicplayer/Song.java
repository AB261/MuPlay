package com.example.android.musicplayer;

public class Song
{
    long id,audio_ad;
    String name,auth;

    public Song(String nm, long audio_res,String author)
    {

        name=nm;
        audio_ad=audio_res;
        auth=author;

    }
    public Song(String nm, long audio_res)
    {

        name=nm;
        audio_ad=audio_res;
        auth="Unknown Author";
    }

    public long getAudio_ad() {
        return audio_ad;
    }

    public String getName() {
        return name;
    }

    public String getAuth() {
        return auth;
    }
}
