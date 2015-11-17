package com.example.chris.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Chris on 16/11/15.
 */
public class MainMenu extends AppCompatActivity {
    private Intent songList;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

    }

    public void playSongList (View v) {
        songList = new Intent(this, MainActivity.class);
        startActivity(songList);
    }

    public void exitApplication(View v) {
        finish();
    }
}
