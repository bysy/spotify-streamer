package com.github.bysy.spotifystreamer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;


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
            return item;
        }
    }
}
