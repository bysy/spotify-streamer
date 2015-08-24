/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.bysy.spotifystreamer.data.SongInfo;

import java.util.ArrayList;

/** Play a song. */
public class PlayerActivity extends AppCompatActivity implements PlayerDialog.GetPlayer {
    Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        // Initialize the player here so it can start the service
        mPlayer = Player.getInstance();
        mPlayer.initialize(this);
        if (savedInstanceState!=null) {
            return;
        }
        // First run, create the player fragment
        final Intent in = getIntent();
        final ArrayList<SongInfo> songs =
                in.getParcelableArrayListExtra(TopSongsFragment.Key.SONGS_PARCEL);
        final boolean openedFromNowPlaying = songs==null;
        if (!openedFromNowPlaying) {
            final int currentIndex = in.getIntExtra(TopSongsFragment.Key.CURRENT_SONG, 0);
            mPlayer.setNewPlaylist(songs);
            mPlayer.setCurrentIndex(currentIndex);
            mPlayer.setAutoPlay(true);
        }
        PlayerDialog playerDialog = new PlayerDialog();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_container, playerDialog)
                .commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Player getPlayer() {
        return mPlayer;
    }
}
