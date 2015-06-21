package com.github.bysy.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;



/**
 * Fragment to show the top songs of an artist.
 */
public class TopSongsActivityFragment extends Fragment {
    SongsAdapter mAdapter = null;
    List<Track> mSongs = new ArrayList<>();

    public TopSongsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SongsAdapter(getActivity(), R.layout.song_list_item, mSongs);
        Intent in = getActivity().getIntent();
        String id = in.getStringExtra(SearchActivity.ARTIST_ID);
        if (id==null) {
            Log.d("BysySpot", "id is null");
        } else {
            Log.d("BysySpot", "Searching for ID: ".concat(id));
        }
        SpotifyApi spotApi = new SpotifyApi();
        SpotifyService spot = spotApi.getService();
        Map<String,Object> options = new HashMap<>();
        options.put("country", "US");
        spot.getArtistTopTrack(id, options, new Callback<Tracks>() {
            @Override
            public void success(final Tracks tracks, Response response) {
                // Documentation suggests the callback runs on the UI thread but
                // in practice it runs on the Retrofit idle thread.
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSongs = tracks.tracks;
                        mAdapter.clear();
                        mAdapter.addAll(mSongs);
                    }
                });
            }
            @Override
            public void failure(RetrofitError error) {
                Log.d("BysySpot", "Top tracks failure: ".concat(error.toString()));
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_songs, container, false);
        ListView lv = (ListView) view.findViewById(R.id.songsListView);
        lv.setAdapter(mAdapter);
        return view;
    }


    class SongsAdapter extends ArrayAdapter<Track> {
        private final int mResource;

        SongsAdapter(Context context, int resource, List<Track> songs) {
            super(context, resource, songs);
            mResource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View item = convertView;
            if (item==null) {
                LayoutInflater li = getActivity().getLayoutInflater();
                item = li.inflate(mResource, parent, false);
            }
            Track song = getItem(position);
            TextView tv = (TextView) item.findViewById(R.id.songNameView);
            tv.setText(song.name);
            tv = (TextView) item.findViewById(R.id.albumNameView);
            tv.setText(song.album.name);
            ImageView iv = (ImageView) item.findViewById(R.id.albumImageView);
            String imageUrl = getImageUrl(song);
            if (imageUrl!=null) {
                Picasso.with(getActivity()).load(imageUrl).into(iv);
            }
            return item;
        }
    }

    // TODO: remove duplication
    @Nullable
    private static String getImageUrl(Track song) {
        if (song.album.images.isEmpty()) return null;
        String url = song.album.images.get(0).url;
        // http://stackoverflow.com/questions/5617749/how-to-validate-a-url-website-name-in-edittext-in-android
        if (!Patterns.WEB_URL.matcher(url).matches()) return null;
        return url;
    }
}