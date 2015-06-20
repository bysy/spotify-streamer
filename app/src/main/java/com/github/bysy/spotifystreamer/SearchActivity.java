package com.github.bysy.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import retrofit.RetrofitError;


public class SearchActivity extends AppCompatActivity {
    private List<Artist> mArtists = new ArrayList<>();
    private ArtistAdapter mAdapter;
    static final String ARTIST_NAME = "artist_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mAdapter = new ArtistAdapter(this, R.layout.single_artist, mArtists);

        ListView lv = (ListView) findViewById(R.id.artistListView);
        lv.setAdapter(mAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist x = (Artist) parent.getItemAtPosition(position);
                Intent i = new Intent(getApplicationContext(), TopSongsActivity.class);
                i.putExtra(ARTIST_NAME, x.name);
                startActivity(i);
            }
        });
        EditText et = (EditText) findViewById(R.id.searchText);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Handle Search action from soft keyboard and
                // enter key down from hardware keyboard.
                // Ignore all other actions.
                // See also http://stackoverflow.com/questions/1489852/android-handle-enter-in-an-edittext
                if (! ( (actionId == EditorInfo.IME_ACTION_SEARCH) ||
                        (actionId == EditorInfo.IME_NULL &&
                          event.getAction() == KeyEvent.ACTION_DOWN &&
                          event.getKeyCode() == KeyEvent.KEYCODE_ENTER) )) {
                    return false;
                }
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

    class ArtistsSearcher extends AsyncTask<String, Void, List<Artist> > {
        @Override
        protected List<Artist>
        doInBackground(String... strings) {
            try {
                if (strings.length == 0) return null;
                String searchStr = strings[0];
                SpotifyApi spotApi = new SpotifyApi();
                SpotifyService spot = spotApi.getService();
                return spot.searchArtists(searchStr).artists.items;

            } catch (RetrofitError e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Artist> res) {
            if (res==null) {
                showToast("Couldn't connect to Spotify");
                return;
            }
            if (res.isEmpty()) {
                showToast("Sorry, no artists found with that name");
            }
            mArtists = res;
            mAdapter.clear();
            mAdapter.addAll(res);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private static final int[] MOCK_COLORS = {0xff4dd0e1, 0xfffff176, 0xffffa726,
            0xff4db6ac, 0xfff06292, 0xff90a4ae};


    class ArtistAdapter extends ArrayAdapter<Artist> {
        private final int mResource;

        ArtistAdapter(Context context, int resource, List<Artist> artists) {
            super(context, resource, artists);
            mResource = resource;
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
            tv.setText(artist.name);
            ImageView iv = (ImageView) item.findViewById(R.id.artistImageView);
            iv.setBackgroundColor(MOCK_COLORS[position % MOCK_COLORS.length]);
            String imageUrl = getImageUrl(artist);
            if (imageUrl!=null) {
                Picasso.with(SearchActivity.this).load(imageUrl).into(iv);
            }
            return item;
        }
    }

    @Nullable
    private static String getImageUrl(Artist artist) {
        if (artist.images.isEmpty()) return null;
        String url = artist.images.get(0).url;
        // http://stackoverflow.com/questions/5617749/how-to-validate-a-url-website-name-in-edittext-in-android
        if (!Patterns.WEB_URL.matcher(url).matches()) return null;
        return url;
    }
}
