package com.example.chris.musicplayer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Created by Chris on 13/11/15.
 */
public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songsList;

    private LayoutInflater songInflater;

    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songsList = theSongs;
        songInflater = LayoutInflater.from(c);
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public int getCount() {
        return songsList.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        //map to song layout
        LinearLayout songLay = (LinearLayout) songInflater.inflate(R.layout.song, parent, false);

        //retrieve artist/title views
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);

        // get current song
        Song currentSong = songsList.get(position);

        // get title and artist text as string
        songView.setText(currentSong.getTitle());
        artistView.setText(currentSong.getArtist());

        //set position as tag
        songLay.setTag(position);
        return songLay;
    }
}
