/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.bysy.spotifystreamer.data.ArtistInfo;


public class MainActivity extends AppCompatActivity
        implements ArtistSearchFragment.OnArtistSelected,
        TopSongsFragment.ShouldLaunchDialogPlayer {

    @Override
    public boolean shouldLaunchDialogPlayer() {
        return mIsMultiPane;
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
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateNowPlaying(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id==R.id.action_now_playing) {
            handleNowPlaying(this, shouldLaunchDialogPlayer());
        }
        return super.onOptionsItemSelected(item);
    }

    static void updateNowPlaying(Menu menu) {
        menu.findItem(R.id.action_now_playing).setEnabled(Player.nowPlaying());
    }

    static void handleNowPlaying(FragmentActivity activity, boolean showDialog) {
        if (showDialog) {
            PlayerDialog playerDialog = new PlayerDialog();
            playerDialog.show(activity.getSupportFragmentManager(),
                    TopSongsFragment.PLAYER_FRAGMENT_TAG);
        } else {
            Intent intent = new Intent(activity, PlayerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        }
    }
}
