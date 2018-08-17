package com.example.android.musicplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.musicplayer.R;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends ArrayAdapter<Song> {
    private ArrayList<Song>songs;
    public SongAdapter(Context context, ArrayList<Song>thesongs)
    {
        super(context,0,thesongs);
        songs=thesongs;
    }


    @Override
    public int getCount() {
        return songs.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listview=convertView;
        if(listview==null)
        {
            listview=LayoutInflater.from(getContext()).inflate(R.layout.list_item,parent,false);
        }
        Song currentSong=getItem(position);
        TextView title=(TextView)listview.findViewById(R.id.title);
        title.setText(currentSong.getName());
        TextView author=(TextView)listview.findViewById(R.id.author);
        author.setText(currentSong.auth);
        listview.setTag(position);
        return listview;

    }
}
