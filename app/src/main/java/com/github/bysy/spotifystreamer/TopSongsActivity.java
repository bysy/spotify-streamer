package com.github.bysy.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class TopSongsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_songs);
        Intent in = getIntent();
        String name = in.getStringExtra(SearchActivity.ARTIST_NAME);
        if (name==null) {
            // TODO: handling of missing name should be more user-friendly
            finish();
        }
        ActionBar ab = getSupportActionBar();
        if (ab!=null) {
            ab.setTitle(name);
        }
        Song[] songs = sMockSongs;
        SongsAdapter adapter = new SongsAdapter(this, R.layout.song_list_item, songs);
        ListView lv = (ListView) findViewById(R.id.songsListView);
        lv.setAdapter(adapter);
    }

    static private final Song[] sMockSongs = makeMockSongs();
    private static Song[] makeMockSongs() {
        return new Song[]{new Song("Great Song", "Sweet Album"),
                          new Song("Splendiferous Song", "Sweet Album"),
                          new Song("OK Song", "Second Album"),
                          new Song("More of the Same", "Third Album"),
                          new Song("'Tis What We Do", "Sweet Album")};
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_songs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class SongsAdapter extends ArrayAdapter<Song> {
        private final int mResource;

        SongsAdapter(Context context, int resource, Song[] songs) {
            super(context, resource, songs);
            mResource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View item = convertView;
            if (item==null) {
                LayoutInflater li = getLayoutInflater();
                item = li.inflate(mResource, parent, false);
            }
            Song song = getItem(position);
            TextView tv = (TextView) item.findViewById(R.id.songNameView);
            tv.setText(song.name);
            tv = (TextView) item.findViewById(R.id.albumNameView);
            tv.setText(song.album);
            return item;
        }
    }
}


class Song {
    public final String name;
    public final String album;

    Song (String name, String album) {
        this.name = name;
        this.album = album;
    }
}