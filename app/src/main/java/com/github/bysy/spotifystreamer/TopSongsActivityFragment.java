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


/**
 * Fragment to show the top songs of an artist.
 */
public class TopSongsActivityFragment extends Fragment {
    SongsAdapter mAdapter = null;

    public TopSongsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Song[] songs = sMockSongs;
        mAdapter = new SongsAdapter(getActivity(), R.layout.song_list_item, songs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_songs, container, false);
        ListView lv = (ListView) view.findViewById(R.id.songsListView);
        lv.setAdapter(mAdapter);
        return view;
    }


    static private final Song[] sMockSongs = makeMockSongs();
    private static Song[] makeMockSongs() {
        return new Song[]{new Song("Great Song", "Sweet Album"),
                new Song("Splendiferous Song", "Sweet Album"),
                new Song("OK Song", "Second Album"),
                new Song("More of the Same", "Third Album"),
                new Song("'Tis What We Do", "Sweet Album")};
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
                LayoutInflater li = getActivity().getLayoutInflater();
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