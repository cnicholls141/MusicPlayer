package com.example.chris.musicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ListView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.chris.musicplayer.MusicService.MusicBinder;



public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    // array to hold songs and populate list
    private ListView songView;
    private ArrayList<Song> listOfSongs;


    //music service instance, made public so that I can control mediaplayer and access methods from main class
    public MusicService musicServ;

    //intent to start service and bind
    private Intent playSongIntent;
    private boolean musicBound = false;

    //button initialisatons
    private ImageButton play;
    private ImageButton nextSong;
    private ImageButton prevSong;
    private ImageButton shuffleButton;

    //variables to check for shuffle state
    private boolean isPlaying;
    private boolean isShuffle;


    private SeekBar seekBar;


    private TextView songID;
    private TextView songDuration;


    private double timeElapsed = 0, finalTime = 0;
    Handler mHandler = new Handler();
    private String getSongID;
    private boolean isMusicThreadFinished;
    private String getSongTitle;
    private String getSongArtist;

    private ImageButton btnPlaylist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
       startActivity();
    }

    public void startActivity() {
        setContentView(R.layout.song_list);

        songView = (ListView) findViewById(R.id.song_list);

        listOfSongs = new ArrayList<Song>();

        isMusicThreadFinished = false;

        retrieveSongs();

        //sorts songs alphabetically
        Collections.sort(listOfSongs, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });


        SongAdapter songAdapt = new SongAdapter(this, listOfSongs);
        songView.setAdapter(songAdapt);
    }


    //method to bind class to service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            //get service
            musicServ = binder.getService();
            //pass list
            musicServ.setList(listOfSongs);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    // method to receive song list
    public void retrieveSongs() {

        ContentResolver songResolver = getContentResolver();
        Uri musicPath = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = songResolver.query(musicPath, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()) {
            // get columns
            int titleColumn = songCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = songCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = songCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = songCursor.getLong(idColumn);
                String thisTitle = songCursor.getString(titleColumn);
                String thisArtist = songCursor.getString(artistColumn);
                listOfSongs.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (songCursor.moveToNext());
        }
    }

    protected void onStart() {
        super.onStart();
        if (playSongIntent == null) {
            playSongIntent = new Intent(this, MusicService.class);
            bindService(playSongIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playSongIntent);
        }
    }

    public void InitialiseControlViews() {

        // change to control layout
        setContentView(R.layout.song_control);

        // initialise buttons
        play = (ImageButton) findViewById(R.id.play_button);
        shuffleButton = (ImageButton) findViewById(R.id.shuffle_button);
        nextSong = (ImageButton) findViewById(R.id.next_button);
        prevSong = (ImageButton) findViewById(R.id.prev_button);

        // ID, title and artist views
        songID = (TextView) findViewById(R.id.songTitle);

        // current time and time elapsed
        songDuration = (TextView) findViewById(R.id.songTotalDurationLabel);

        //variables to display duration
        finalTime = musicServ.getDuration();
        timeElapsed = 0;

        //play.setVisibility(View.INVISIBLE);
        isShuffle = false;
        isPlaying = false;

        //seekbar methods
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setProgress(0);// To set initial progress
        seekBar.setMax(musicServ.getDuration());


        // on click listeners
        play.setOnClickListener(this);
        shuffleButton.setOnClickListener(this);
        nextSong.setOnClickListener(this);
        prevSong.setOnClickListener(this);

    }


    Runnable run = new Runnable()

    {
        @Override
        public void run() {
            if (musicServ != null) {
                timeElapsed = musicServ.getCurrentPosition();    // update timeElapsed to current time
                seekBarUpdate();    // run seekbar on repeat
                //Log.d("CurrentTime", Integer.toString(musicServ.getCurrentPosition()));
            }
        }
    };


    public void songPicked(View view) {
        musicServ.setSong(Integer.parseInt(view.getTag().toString()));
        musicServ.playSong();
        //Log.d("songindex main:", Integer.toString(musicServ.getIndex() + 1));
        InitialiseControlViews();
        play.setImageResource(R.drawable.pause_button);
        seekBarUpdate();
    }


    // method to convert the current time into hours/mins/seconds, received help from:
    // http://stackoverflow.com/questions/7049009/getting-millisecond-format
    private String getTimeAsString(long millis) {

        StringBuffer stringBuf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        stringBuf
                .append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return stringBuf.toString();
    }

    // method for seekbar, runs on repeat in the run thread
    public void seekBarUpdate() {

        //updates song ID, title and artist
        getSongID = Integer.toString(musicServ.getIndex() + 1);
        getSongTitle = listOfSongs.get(musicServ.getIndex()).getTitle();
        getSongArtist = listOfSongs.get(musicServ.getIndex()).getArtist();
        songID.setText(getSongID + "  " + getSongTitle + "  " + getSongArtist);

        // gets current song position from servie and converts it into hh/mm/ss format with time method
        int mCurrentPosition = musicServ.getCurrentPosition();
        songDuration.setText(getTimeAsString(mCurrentPosition));

        // initialises the seekbar max to be the max duration of the song
        seekBar.setMax(musicServ.getDuration());
        //continuously updates the progress of the seekbar to the currentposition
        seekBar.setProgress(musicServ.getCurrentPosition());
        // time delay between run loop
        mHandler.postDelayed(run, 100);
    }

    @Override
    public void onBackPressed() {

        // goes back to the list view when you press back so you can select song
        startActivity();

        //moveTaskToBack(true);
    }

    protected void onDestroy() {
        stopService(playSongIntent);
        musicServ = null;
        super.onDestroy();
    }

    // button methods

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.play_button:

                if (musicServ.isPlaying()) {
                    Toast.makeText(getApplicationContext(), "Paused",
                            Toast.LENGTH_SHORT).show();
                    play.setImageResource(R.drawable.play_button);
                    musicServ.paused();
                } else {

                    Toast.makeText(getApplicationContext(), "Resumed",
                            Toast.LENGTH_SHORT).show();
                    play.setImageResource(R.drawable.pause_button);
                    musicServ.resume();
                }
                break;

            case R.id.shuffle_button:
                musicServ.setShuffleState();

                //Log.d("shuffle is", Boolean.toString(isShuffle));
                if (!isShuffle) {
                    Toast.makeText(getApplicationContext(), "Shuffle on",
                            Toast.LENGTH_SHORT).show();
                    shuffleButton.setImageResource(R.drawable.shuffle_on);
                    //shuffleButton.setBackgroundResource(R.drawable.shuffle_on);
                    isShuffle = true;
                } else {
                    //Log.d("shuffle is", Boolean.toString(isShuffle));

                    Toast.makeText(getApplicationContext(), "Shuffle off",
                            Toast.LENGTH_SHORT).show();
                    shuffleButton.setImageResource(R.drawable.shuffle);
                    //shuffleButton.setBackgroundResource(R.drawable.shuffle_default);
                    isShuffle = false;
                }
                break;

            case R.id.next_button:
                //mHandler.postDelayed(run, 2000);
                //Log.d("next and shuffle is", Boolean.toString(isShuffle));
                //Log.d("next is pressed", "true");

                Toast.makeText(getApplicationContext(), "Next song",
                        Toast.LENGTH_SHORT).show();

                musicServ.playNext();
                seekBar.setProgress(0);// To set initial progress, i.e zero in starting of treate();
                seekBar.setMax(musicServ.getDuration());
                //Log.d("shuffle in main is", Boolean.toString(isShuffle));
                break;

            case R.id.prev_button:

                Toast.makeText(getApplicationContext(), "Previous song",
                        Toast.LENGTH_SHORT).show();

                musicServ.prevSong();
                break;
        }
    }


    // seekbar methods

    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        try {
            if (musicServ.isPlaying() || musicServ != null) {
                if (fromUser)
                    musicServ.player.seekTo(progress);
            } else if (musicServ == null) {
                seekBar.setProgress(0);
            }
        } catch (Exception e) {
            //Log.e("seek bar", "" + e);
            seekBar.setEnabled(false);

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}





