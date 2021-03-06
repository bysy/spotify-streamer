/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import com.github.bysy.spotifystreamer.data.ArtistInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Search for and display artists. Parent activity should implement
 * OnArtistSelected interface to receive callbacks when an
 * artist is selected.
 */
public class ArtistSearchFragment extends Fragment {
    private ArtistSearchStateFragment mRetainedState;

    private static class Key {
        private static final String SHOULD_SEARCH = "SHOULD_SEARCH";  // whether user searched for the current input
        private static final String SEARCH_TEXT = "SEARCH_TEXT";
    }
    private static final String TAG = ArtistSearchFragment.class.getSimpleName();

    private ListView mArtistsListView;
    private EditText mSearchText;
    private ArtistAdapter mAdapter;
    private final SpotifyApi mSpotApi = new SpotifyApi();
    private int mLastTotal;

    interface OnArtistSelected {
        void onArtistSelected(ArtistInfo artist);
    }

    public ArtistSearchFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the search text and whether the user has run this search.
        boolean shouldSearch = false;
        if (mSearchText!=null) {
            final String search = mSearchText.getText().toString();
            if (!search.isEmpty()) {
                outState.putString(Key.SEARCH_TEXT, search);
                if (search.equals(mRetainedState.getLastSearch())) {
                    shouldSearch = true;
                }
            }
        }
        outState.putBoolean(Key.SHOULD_SEARCH, shouldSearch);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_search, container, false);

        mArtistsListView = (ListView) view.findViewById(R.id.artistListView);
        mArtistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id) {
                final Activity parentActivity = getActivity();
                if (parentActivity instanceof OnArtistSelected) {
                    final ArtistInfo artist = (ArtistInfo) parentAdapter.getItemAtPosition(position);
                    ((OnArtistSelected) parentActivity).onArtistSelected(artist);
                } else {
                    Log.d(TAG, "Containing activity should implement OnArtistSelected");
                }
            }
        });
        mSearchText = (EditText) view.findViewById(R.id.searchText);
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                unfocusInput();
                runSearch(searchStr);
                return true;
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRetainedState = ArtistSearchStateFragment.getAssociatedInstance(getActivity());
        mAdapter = new ArtistAdapter(getActivity(), R.layout.single_artist,
                mRetainedState.getArtists());
        mArtistsListView.setAdapter(mAdapter);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState!=null) {
            String searchStr = savedInstanceState.getString(Key.SEARCH_TEXT);
            if (searchStr!=null && !searchStr.equals(mRetainedState.getLastSearch())) {
                //                 ^ false on rotation
                Log.d(TAG, "restoring from saved state");
                mSearchText.setText(searchStr);
                boolean doSearch = savedInstanceState.getBoolean(Key.SHOULD_SEARCH);
                if (doSearch) {
                    runSearch(searchStr);
                }
            }
        }
    }

    private void runSearch(String searchStr) {
        Log.d(TAG, "searching for " + searchStr);
        mRetainedState.setLastSearch(searchStr);
        SpotifyService spot = mSpotApi.getService();
        spot.searchArtists(searchStr, new Callback<ArtistsPager>() {
            @Override
            public void success(final ArtistsPager pager, Response response) {
                mLastTotal = pager.artists.total;
                mRetainedState.setArtists(ArtistInfo.listOf(pager.artists.items));
                final List<ArtistInfo> artists = mRetainedState.getArtists();
                if (artists.isEmpty()) {
                    Util.showToast(getActivity(), "Sorry, no artists found with that name");
                    focusInput();
                }
                mAdapter.clear();
                Util.adapterAddAll(mAdapter, artists);
                mArtistsListView.setSelectionAfterHeaderView();

                // Workaround for ease of use with hardware keyboard
                mArtistsListView.requestFocus();
            }
            @Override
            public void failure(RetrofitError error) {
                Util.showToast(getActivity(), "Couldn't connect to Spotify");
            }
        });
    }

    private void retrieveMoreArtists() {
        final int offset = mAdapter.getCount();
        if (offset>=mLastTotal) {
            return;
        }
        final String searchStr = mRetainedState.getLastSearch();
        Log.d(TAG, "searching for " + searchStr + " with offset " + offset);
        SpotifyService spot = mSpotApi.getService();
        Map<String,Object> options = new HashMap<>();
        options.put("offset", Integer.toString(offset));
        spot.searchArtists(searchStr, options, new Callback<ArtistsPager>() {
            @Override
            public void success(final ArtistsPager pager, Response response) {
                ArrayList<ArtistInfo> newArtists = ArtistInfo.listOf(pager.artists.items);
                mRetainedState.addArtists(newArtists);
                Util.adapterAddAll(mAdapter, newArtists);
            }
            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Failed to retrieve additional artists");
            }
        });
    }

    /** Change focus away from EditText to hide cursor and hide soft keyboard. */
    private void unfocusInput() {
        // See also http://stackoverflow.com/questions/1555109/stop-edittext-from-gaining-focus-at-activity-startup
        mSearchText.clearFocus();

        // Hide keyboard when it's visible. This is tricky.
        // Thanks to http://stackoverflow.com/a/15587937
        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    /** Focus on EditText and show soft keyboard. */
    private void focusInput() {
        mSearchText.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mSearchText, InputMethodManager.SHOW_IMPLICIT);
    }


    class ArtistAdapter extends ArrayAdapter<ArtistInfo> {
        private final int mResource;

        ArtistAdapter(Context context, int resource, List<ArtistInfo> artists) {
            super(context, resource, artists);
            mResource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View item = convertView;
            if (item==null) {
                LayoutInflater li = getActivity().getLayoutInflater();
                item = li.inflate(mResource, parent, false);
                item.setTag(new ViewHolder(item));
            }
            if (position==(getCount()-1-5)) {  // 5 before last makes for smoother scrolling
                Log.d(TAG, "retrieving more artists at position " + Integer.toString(position));
                ArtistSearchFragment.this.retrieveMoreArtists();
            }
            ViewHolder views = (ViewHolder) item.getTag();
            ArtistInfo artist = getItem(position);
            views.artistName.setText(artist.name);
            String imageUrl = artist.smallImageUrl;
            Util.loadImageInto(getContext(), imageUrl, views.artistImage);
            return item;
        }

        class ViewHolder {
            final ImageView artistImage;
            final TextView artistName;

            ViewHolder(@NonNull View view) {
                artistImage = (ImageView) view.findViewById(R.id.artistImageView);
                artistName = (TextView) view.findViewById(R.id.artistNameView);
                if (!isInitialized()) {
                    throw new IllegalArgumentException("Invalid layout");
                }
            }

            private boolean isInitialized() {
                return artistImage!=null &&
                        artistName!=null;
            }
        }
    }
}
