/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import com.github.bysy.spotifystreamer.data.ArtistInfo;
import com.github.bysy.spotifystreamer.data.SongInfo;


public class MainActivity extends AppCompatActivity
        implements ArtistSearchFragment.OnArtistSelected,
        TopSongsFragment.ShouldLaunchDialogPlayer,
        PlayerDialog.HasPlayer {

    static class Key {
        static final String ARTIST_NAME = "ARTIST_NAME";
        static final String ARTIST_ID = "ARTIST_ID";
    }
    private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";
    private static final String TAG = MainActivity.class.getSimpleName();
    private Player mPlayer = null;  // only available in multi-pane mode
    private ShareActionProvider mShareActionProvider;
    private boolean mIsMultiPane = false;

    @Override
    public boolean shouldLaunchDialogPlayer() {
        return mIsMultiPane;
    }

    @NonNull
    @Override
    public Player getPlayer() {
        if (!mIsMultiPane) {
            throw new IllegalStateException(
                    TAG.concat(": Cannot provide player in single-pane mode."));
        }
        return mPlayer;
    }

    @Override
    public void onNewShareIntent(@NonNull Intent shareIntent) {
        if (mShareActionProvider!=null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        setContentView(R.layout.activity_main);
        mIsMultiPane = findViewById(R.id.multipane_detail_container)!=null;
        // In multi-pane mode, the player is tied to us
        if (mIsMultiPane) {
            mPlayer = Player.getInstance();
            mPlayer.initialize(this);
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
        getMenuInflater().inflate(R.menu.menu_song_selection, menu);
        if (mIsMultiPane) {
            MenuItem item = menu.findItem(R.id.action_share);
            item.setVisible(true);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            final SongInfo song = mPlayer.getCurrentSong();
            if (mShareActionProvider!=null && song!=null) {
                mShareActionProvider.setShareIntent(PlayerDialog.getShareIntent(this, song));
            }
        }
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
        final Player player = Player.getInstance();
        menu.findItem(R.id.action_now_playing).setEnabled(player.hasPlaylist());
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
