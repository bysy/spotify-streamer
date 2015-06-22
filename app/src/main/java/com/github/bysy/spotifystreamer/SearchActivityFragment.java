package com.github.bysy.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


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
                disableInput();
                runSearch(searchStr);
                return true;
            }
        });
        return view;
    }

    private void runSearch(String searchStr) {
        SpotifyApi spotApi = new SpotifyApi();
        SpotifyService spot = spotApi.getService();
        spot.searchArtists(searchStr, new Callback<ArtistsPager>() {
            @Override
            public void success(final ArtistsPager pager, Response response) {
                mArtists = pager.artists.items;
                if (mArtists.isEmpty()) {
                    Util.showToast(getActivity(), "Sorry, no artists found with that name");
                    enableInput();
                }
                mAdapter.clear();
                mAdapter.addAll(mArtists);
                ListView lv = (ListView) getActivity().findViewById(R.id.artistListView);
                lv.setSelectionAfterHeaderView();

                // Workaround for ease of use with hardware keyboard
                getActivity().findViewById(R.id.artistListView).requestFocus();
            }
            @Override
            public void failure(RetrofitError error) {
                Util.showToast(getActivity(), "Couldn't connect to Spotify");
            }
        });

    }

    private void disableInput() {
        // Change focus away from EditText to hide cursor and highlighting.
        // See also http://stackoverflow.com/questions/1555109/stop-edittext-from-gaining-focus-at-activity-startup
        getActivity().findViewById(R.id.searchText).clearFocus();

        // Hide keyboard when it's visible. This is tricky.
        // Thanks to http://stackoverflow.com/a/15587937
        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    private void enableInput() {
        EditText et = (EditText) getActivity().findViewById(R.id.searchText);
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
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
