package com.github.bysy.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;


public class SearchActivity extends AppCompatActivity {
    private Artist[] mArtists;
    private ArtistAdapter mAdapter;
    static final String ARTIST_NAME = "artist_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // TODO: replace mock with live data
        mArtists = sMockArtists;
        mAdapter = new ArtistAdapter(this, R.layout.single_artist, mArtists);

        ListView lv = (ListView) findViewById(R.id.artistListView);
        lv.setAdapter(mAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist x = (Artist) parent.getItemAtPosition(position);
                Intent i = new Intent(getApplicationContext(), TopSongsActivity.class);
                i.putExtra(ARTIST_NAME, x.getName());
                startActivity(i);
            }
        });
        EditText et = (EditText) findViewById(R.id.searchText);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
                String searchStr = v.getText().toString();
                if (searchStr.length() == 0) return true;
                ArtistsSearcher search = new ArtistsSearcher();
                search.execute(searchStr);
                hideKeyboard();
                return true;
            }
        });
    }

    private void hideKeyboard() {
        // This is tricky. There are many ways that work but this one fits
        // nicely here. And yes, it actually hides the keyboard instead of
        // just toggling it.
        // Thanks to http://stackoverflow.com/a/15587937
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
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

    class ArtistsSearcher extends AsyncTask<String, Void, List<kaaes.spotify.webapi.android.models.Artist> > {
        @Override
        protected List<kaaes.spotify.webapi.android.models.Artist>
        doInBackground(String... strings) {
            if (strings.length==0) return null;
            String searchStr = strings[0];
            SpotifyApi spotApi = new SpotifyApi();
            SpotifyService spot = spotApi.getService();

            List<kaaes.spotify.webapi.android.models.Artist> res;
            res = spot.searchArtists(searchStr).artists.items;
            return res;
        }
        @Override
        protected void onPostExecute(List<kaaes.spotify.webapi.android.models.Artist> res) {
            if (mArtists.length==0) return;
            // TODO: this loop is for testing only; replace
            int idx = 0;
            for (kaaes.spotify.webapi.android.models.Artist a : res) {
                mArtists[idx] = new Artist(a.name, mArtists[idx].getImage());
                ++idx;
                if (idx==mArtists.length) {
                    break;
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private static final int[] MOCK_COLORS = {0xff4dd0e1, 0xfffff176, 0xffffa726,
            0xff4db6ac, 0xfff06292, 0xff90a4ae};
    private static final String[] MOCK_NAMES = {"Abc artist", "Def singer-songwriter", "Ghi super-group",
            "Jkl star", "Mno MC", "Pqr pop star"};

    private static final Artist[] sMockArtists = makeMockArtists();
    private static Artist[] makeMockArtists() {
        final int w = 256;
        final int h = 256;
        final Bitmap.Config c = Bitmap.Config.ARGB_8888;
        final int len = Math.min(MOCK_COLORS.length, MOCK_NAMES.length);
        Artist[] artists = new Artist[len];
        for (int i = 0; i<len; ++i) {
            Bitmap b = Bitmap.createBitmap(w, h, c);
            b.eraseColor(MOCK_COLORS[i]);
            artists[i] = new Artist(MOCK_NAMES[i], b);
        }
        return artists;
    }

    class ArtistAdapter extends ArrayAdapter<Artist> {
        private Artist[] mArtists;
        private final int mResource;

        ArtistAdapter(Context context, int resource, Artist[] artists) {
            super(context, resource, artists);
            mResource = resource;
            mArtists = artists;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View item = convertView;
            if (item==null) {
                LayoutInflater li = getLayoutInflater();
                item = li.inflate(mResource, parent, false);
            }
            Artist artist = getItem(position);
            TextView tv = (TextView) item.findViewById(R.id.artistNameView);
            tv.setText(artist.getName());
            ImageView iv = (ImageView) item.findViewById(R.id.artistImageView);
            iv.setImageBitmap(artist.getImage());
            return item;
        }

    }
}

class Artist {
    private final String name;
    private final Bitmap image;

    public Artist(String name, Bitmap image) {
        this.name = name;
        this.image = image;
    }

    String getName() { return name; }
    Bitmap getImage() { return image; }

}
