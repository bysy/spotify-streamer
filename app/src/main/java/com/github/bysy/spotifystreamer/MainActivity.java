/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.bysy.spotifystreamer.data.ArtistInfo;


public class MainActivity extends AppCompatActivity
        implements ArtistSearchFragment.OnArtistSelected,
        TopSongsFragment.ShouldLaunchDialogPlayer {

    @Override
    public boolean shouldLaunchDialogPlayer() {
        return true;  // mIsMultiPane is always true when we own the detail fragment
    }

    static class Key {
        static final String ARTIST_NAME = "ARTIST_NAME";
        static final String ARTIST_ID = "ARTIST_ID";
    }
    private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";

    private boolean mIsMultiPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIsMultiPane = findViewById(R.id.multipane_detail_container)!=null;
        // In multi-pane mode, the player is tied to our fragment manager
        if (mIsMultiPane) {
            Player.getSharedPlayer(this).initialize(this);
        }
    }

    @Override
    public void onArtistSelected(ArtistInfo artist) {
        if (mIsMultiPane) {
            TopSongsFragment detailFragment = new TopSongsFragment();
            Bundle args = new Bundle();
            args.putString(Key.ARTIST_ID, artist.id);
            detailFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.multipane_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent i = new Intent(this, TopSongsActivity.class);
            i.putExtra(Key.ARTIST_NAME, artist.name);
            i.putExtra(Key.ARTIST_ID, artist.id);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_song_selection, menu);
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
}
