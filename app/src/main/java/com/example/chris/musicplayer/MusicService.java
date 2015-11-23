package com.example.chris.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.lang.reflect.Array;
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

    public MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    Random random;
    private boolean shuffle=false;
    private Random rand;



    public void onCreate(){
        super.onCreate(); // create service
        songPosn=0; // initialize position
        player = new MediaPlayer(); // create player
        initMusicPlayer(); //start music player
        rand=new Random();

    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK); // wake lock lets it play when it becomes idle
        player.setAudioStreamType(AudioManager.STREAM_MUSIC); // set stream type to audio

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

    public void setShuffleState() {

        if (shuffle){
            shuffle = false;
        } else {
            shuffle = true;
        }
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

        //get song
        Song playSong = songs.get(songPosn);
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();

    }
    public void playNext(){

        if(shuffle){    // if shuffle is true
            int newSong = songPosn;
            while(newSong==songPosn){
                newSong=rand.nextInt(songs.size()); // go to a random track within songs.size
            }
            songPosn=newSong;
            playSong();
        }
        else{   //else if shuffle is false
            if (songPosn >= 0 && songPosn <= songs.size() - 1) {    //check to see if the next song is within upper bound
                songPosn++; // increment it by one
                playSong(); // play song
            }
            if (songPosn == songs.size() - 1) { // if the next song is the last one
                songPosn = 0;   // set the index to be the first song
                playSong();
            }
        }
        //playSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
    }

    public void setSong(int songIndex){
        songPosn = songIndex;
    }

    public void nextSong() {

        if (songPosn >= 0 && songPosn <= songs.size() - 1) {
        songPosn++;
        playSong();
        }
        if (songPosn == songs.size()-1) {
            songPosn = 0;
            playSong();
        }
    }

    public void prevSong() {
        if (songPosn > 0 && songPosn <= songs.size() - 1) {
            songPosn--;
            playSong();
        }
        if(songPosn == 0) {

            songPosn = songs.size() - 1;
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


    public void shuffleSongs() {
        if(shuffle) shuffle=false;
        else shuffle=true;
    }


    public class MusicBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }
}
