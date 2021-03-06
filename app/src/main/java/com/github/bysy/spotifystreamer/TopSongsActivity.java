/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


/** Activity to show top songs. Used in single-pane layout mode. */
public class TopSongsActivity extends AppCompatActivity {
    private static final String TAG = TopSongsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_songs);
        final Intent in = getIntent();
        final String id = handleIntent(in);
        if (id==null) return;
        if (savedInstanceState==null) {
            updateFragment(id);
        }
    }

    private void updateFragment(String id) {
        final TopSongsFragment fragment = new TopSongsFragment();
        final Bundle args = new Bundle();
        args.putString(MainActivity.Key.ARTIST_ID, id);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.top_songs_container, fragment)
                .commit();
    }

    @Nullable
    private String handleIntent(Intent intent) {
        String name = intent.getStringExtra(MainActivity.Key.ARTIST_NAME);
        if (name==null) name = "";
        if (name.isEmpty()) {
            Log.e(TAG, MainActivity.Key.ARTIST_NAME + " is missing");
            name = getString(R.string.app_name);
        }
        final ActionBar ab = getSupportActionBar();
        if (ab!=null) {
            ab.setTitle(name);
        }
        final String id = intent.getStringExtra(MainActivity.Key.ARTIST_ID);
        if (id==null || id.isEmpty()) {
            Log.e(TAG, MainActivity.Key.ARTIST_ID.concat(" is missing"));
            return null;
        }
        return id;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final String id = handleIntent(intent);
        updateFragment(id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_song_selection, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MainActivity.updateNowPlaying(menu);
        return super.onPrepareOptionsMenu(menu);
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
        } else if (id==R.id.action_now_playing) {
            final boolean showDialog = false;
            MainActivity.handleNowPlaying(this, showDialog);
        }

        return super.onOptionsItemSelected(item);
    }
}
