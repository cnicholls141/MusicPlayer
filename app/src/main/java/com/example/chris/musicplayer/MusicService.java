package com.example.chris.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Random;

import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by Chris on 15/11/15.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    //create a media payer to control songs
    protected MediaPlayer player;

    // create song list
    private ArrayList<Song> songs;

    //int to keep track of which song
    private int songIndex;

    private final IBinder musicBind = new MusicBinder();
    //Random random;
    private boolean shuffle=false;
    private boolean onComplete = false;
    private Random rand;



    public void onCreate(){
        super.onCreate(); // create service
        songIndex =0; // initialize position
        player = new MediaPlayer(); // create player
        initMusicPlayer(); //start music player
        rand=new Random();

    }

    public int getIndex() {
        return songIndex;
    }

    public void initMusicPlayer() {

        // wake lock lets it play when it becomes idle
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        // set stream type to audio
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //sets listeners to tell the player what to do after its prepared, completed or errored
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

   public int getDuration() {
        return player.getDuration();
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void setShuffleState() {

        shuffle = shuffle ? false: true;

        /*if (shuffle){
            shuffle = false;
        } else {
            shuffle = true;
        }*/
    }
    public IBinder onBind(Intent arg) {

        return musicBind;
    }

    public boolean onUnbind(Intent intent) {
        player.stop();

        player.release();
        return false;
    }


    public void playSong(){
        player.reset();

        //get the song, id and Uri
        Song songPlay = songs.get(songIndex);
        long currentSong = songPlay.getID();
        Uri trackListUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);

        try{
            player.setDataSource(getApplicationContext(), trackListUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();

    }

    public void playNext() {

        //Log.d("songIndex:", Integer.toString(songIndex));
        //Log.d("songs.size is", Integer.toString(songs.size()));

        //check to see if shuffle is true
        if (shuffle) {
            //Log.d("songIndex iftrue:", Integer.toString(songIndex));

            int songNew = songIndex;
            while (songNew == songIndex) {
                songNew = rand.nextInt(songs.size()); // go to a random track within songs.size
            }
            songIndex = songNew;
        }

        else{   // if shuffle is not true, methos to receiv next song in the tracklist

            if (songIndex >= 0 && songIndex <= songs.size() - 1) {    //check to see if the next song is within upper bound
                songIndex++; // increment it by one
                //Log.d("got:", "toindex <=");
                //playSong(); // play song
            }
            if (songIndex == songs.size() ) { // if the next song is the last one
                //Log.d("got:", "to index==");
                songIndex = 0;   // set the index to be the first song

            }
            //Log.d("songindex before play:", Integer.toString(songIndex));

        }
        playSong();

    }

    @Override
    public void onCompletion(MediaPlayer mPlayer) {

        //method for when song is completed
        if(player.getCurrentPosition() > 0){
            mPlayer.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mPlayer, int error, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mPlayer) {
        //start playback
        mPlayer.start();
    }

    public void setSong(int songIndex){
        this.songIndex = songIndex;
    }

    public void prevSong() {

        //same as next song but checking for upper bound and lower bound,
        //setting song to the end song if currently at the start
        if (songIndex > 0 && songIndex <= songs.size() - 1) {
            songIndex--;
            playSong();
        }

        if(songIndex == 0) {

            songIndex = songs.size() - 1;
            playSong();
        }
    }

    public void paused() {
        player.pause();
    }

    public void resume() {
        player.start();
    }


    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs; // pass list of songs to the activity
    }


    public class MusicBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }
}
