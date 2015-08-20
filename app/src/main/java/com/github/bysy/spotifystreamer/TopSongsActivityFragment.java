/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.bysy.spotifystreamer.data.SongInfo;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;



/**
 * Fragment to show the top songs of an artist.
 */
public class TopSongsActivityFragment extends Fragment {
    public static class Key {
        public static final String SONGS_PARCEL = "SONGS_PARCEL";
        public static final String CURRENT_SONG = "CURRENT_SONG";
    }
    static private final String TAG = TopSongsActivityFragment.class.getSimpleName();
    private SongsAdapter mAdapter;
    private ArrayList<SongInfo> mSongs;

    public TopSongsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore state
        if (savedInstanceState!=null) {
            mSongs = savedInstanceState.getParcelableArrayList(Key.SONGS_PARCEL);
            mAdapter = new SongsAdapter(getActivity(), R.layout.song_list_item, mSongs);
            return;
        }
        mSongs = new ArrayList<>(10);
        mAdapter = new SongsAdapter(getActivity(), R.layout.song_list_item, mSongs);
        // First run: Query Spotify
        Intent in = getActivity().getIntent();
        String artistId = in.getStringExtra(SearchActivity.Key.ARTIST_ID);
        if (artistId==null || artistId.isEmpty()) {
            Log.e(TAG, SearchActivity.Key.ARTIST_ID + " is missing");
            return;
        }
        updateSongs(artistId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Key.SONGS_PARCEL, mSongs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_songs, container, false);
        ListView lv = (ListView) view.findViewById(R.id.songsListView);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Open player activity at this song
                Intent i = new Intent(getActivity(), PlayerActivity.class);
                i.putExtra(Key.SONGS_PARCEL, mSongs);
                i.putExtra(Key.CURRENT_SONG, position);
                startActivity(i);
            }
        });
        return view;
    }

    /** Retrieve top tracks from Spotify and update mSongs and UI accordingly. */
    private void updateSongs(String artistId) {
        Log.d(TAG, "Searching for ID: ".concat(artistId));
        SpotifyApi spotApi = new SpotifyApi();
        SpotifyService spot = spotApi.getService();
        final String country = Util.getCountryCode();
        Log.d(TAG, "Country is ".concat(country));
        Map<String,Object> options = new HashMap<>();
        options.put("country", country);
        spot.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
            @Override
            public void success(final Tracks tracks, Response response) {
                mSongs = SongInfo.listOf(tracks.tracks);
                if (mSongs.isEmpty()) {
                    Util.showToast(getActivity(), "No songs available for selected artist.");
                }
                mAdapter.clear();
                mAdapter.addAll(mSongs);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Top tracks failure: ".concat(error.toString()));
                Util.showToast(getActivity(), "Couldn't connect to Spotify");
            }
        });
    }


    class SongsAdapter extends ArrayAdapter<SongInfo> {
        private final int mResource;

        SongsAdapter(Context context, int resource, List<SongInfo>songs) {
            super(context, resource, songs);
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
            ViewHolder views = (ViewHolder) item.getTag();
            SongInfo song = getItem(position);
            views.songName.setText(song.name);
            views.albumName.setText(song.albumName);
            String imageUrl = song.albumImageUrl;
            Util.loadImageInto(getContext(), imageUrl, views.albumImage);
            return item;
        }

        class ViewHolder {
            final ImageView albumImage;
            final TextView albumName;
            final TextView songName;

            ViewHolder(@NonNull View view) {
                albumImage = (ImageView) view.findViewById(R.id.albumImageView);
                albumName = (TextView) view.findViewById(R.id.albumNameView);
                songName = (TextView) view.findViewById(R.id.songNameView);

                if (!isInitialized()) {
                    throw new InvalidParameterException("Invalid layout");
                }
            }

            boolean isInitialized() {
                return albumImage!=null &&
                        albumName!=null &&
                        songName!=null;
            }
        }
    }
}
