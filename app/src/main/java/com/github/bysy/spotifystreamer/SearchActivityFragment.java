package com.github.bysy.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import retrofit.RetrofitError;


/**
 * Fragment to search for and display artists.
 */
public class SearchActivityFragment extends ImageListViewFragment {
    private List<Artist> mArtists = new ArrayList<>();
    private ArtistAdapter mAdapter;

    public SearchActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mAdapter = new ArtistAdapter(getActivity(), R.layout.single_artist, mArtists);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        ListView lv = (ListView) view.findViewById(R.id.artistListView);
        lv.setAdapter(mAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist x = (Artist) parent.getItemAtPosition(position);
                Intent i = new Intent(getActivity(), TopSongsActivity.class);
                i.putExtra(SearchActivity.ARTIST_NAME, x.name);
                i.putExtra(SearchActivity.ARTIST_ID, x.id);
                startActivity(i);
            }
        });
        final EditText et = (EditText) view.findViewById(R.id.searchText);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Handle Search action from soft keyboard and
                // enter key down from hardware keyboard.
                // Ignore all other actions.
                // See also http://stackoverflow.com/questions/1489852/android-handle-enter-in-an-edittext
                if (!((actionId == EditorInfo.IME_ACTION_SEARCH) ||
                        (actionId == EditorInfo.IME_NULL &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                    return false;
                }
                String searchStr = v.getText().toString();
                if (searchStr.length() == 0) return true;
                ArtistsSearcher search = new ArtistsSearcher();
                search.execute(searchStr);
                hideKeyboard();
                disableInput(et);
                return true;
            }
        });
        et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                enableInput(et);
                return false;
            }
        });
        return view;
    }

    // These methods provide one way to turn off/on the cursor that works even
    // with orientation changes or app switching.
    // Source: discussion on multiple SO pages, docs.

    static private void disableInput(EditText editTextView) {
        editTextView.setFocusableInTouchMode(false);
        editTextView.setFocusable(false);
    }

    static private void enableInput(EditText editTextView) {
        editTextView.setFocusableInTouchMode(true);
        editTextView.setFocusable(true);
    }

    private void hideKeyboard() {
        // This is tricky. There are many ways that work but this one fits
        // nicely here. And yes, it actually hides the keyboard instead of
        // just toggling it.
        // Thanks to http://stackoverflow.com/a/15587937
        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
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
                Util.showToast(getActivity(), "Couldn't connect to Spotify");
                return;
            }
            if (res.isEmpty()) {
                Util.showToast(getActivity(), "Sorry, no artists found with that name");
            }
            mArtists = res;
            mAdapter.clear();
            mAdapter.addAll(res);
            mAdapter.notifyDataSetChanged();

            // Workaround for ease of use with hardware keyboard
            getActivity().findViewById(R.id.artistListView).requestFocus();
        }
    }


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
                LayoutInflater li = getActivity().getLayoutInflater();
                item = li.inflate(mResource, parent, false);
            }
            Artist artist = getItem(position);
            TextView tv = (TextView) item.findViewById(R.id.artistNameView);
            tv.setText(artist.name);
            ImageView iv = (ImageView) item.findViewById(R.id.artistImageView);
            String imageUrl = Util.getImageUrl(artist.images);
            loadImageInto(imageUrl, iv);
            return item;
        }
    }
}
