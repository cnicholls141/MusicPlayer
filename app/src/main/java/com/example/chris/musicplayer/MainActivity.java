package com.example.chris.musicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ListView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.chris.musicplayer.MusicService.MusicBinder;




public class MainActivity extends AppCompatActivity {

private ArrayList<Song> songList;
    private ListView songView;

    public MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private Intent controlIntent;
    Button play;
    Button pause;
    private boolean isPlaying;
    private boolean isShuffle;
    //Button playPause = (Button) findViewById(R.id.playPause);
    final Button shuffle = new Button(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        isShuffle = false;
        isPlaying = false;

        getSongList();

       // pause.setVisibility(View.INVISIBLE);
       // play.setVisibility(View.INVISIBLE);

        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });


        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
    }

    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            // get columns
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();


        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }        return super.onOptionsItemSelected(item);
    }

    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        setContentView(R.layout.song_control);
        play = (Button) findViewById(R.id.play_button);
        pause = (Button) findViewById(R.id.pause_button);
        play.setVisibility(View.INVISIBLE);

    }

    public void getNextSong(View v) {

        musicSrv.playNext();
    }

    public void getPrevSong(View v) {
        musicSrv.prevSong();
    }

    public void pause(View v) {
        musicSrv.paused();

        play.setVisibility(View.VISIBLE);
        pause.setVisibility(View.INVISIBLE);
    }

    public void play(View v) {
        musicSrv.resume();

        pause.setVisibility(View.VISIBLE);
        play.setVisibility(View.INVISIBLE);
    }

    public void shuffleToggle(View v) {
        musicSrv.setShuffleState();
        shuffle.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_state));
    }

    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }


}
