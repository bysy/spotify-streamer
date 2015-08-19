/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Play a song.
 */
public class PlayerActivityFragment extends Fragment {

    private static final String TAG = PlayerActivityFragment.class.getSimpleName();
    private ImageView mAlbumImageView;

    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        mAlbumImageView = (ImageView) view.findViewById(R.id.playerAlbumImage);

        Intent in = getActivity().getIntent();
        TextView artistTextView = (TextView) view.findViewById(R.id.artistNameView);
        artistTextView.setText(in.getStringExtra(TopSongsActivityFragment.Key.ARTIST_NAME));
        TextView albumTextView = (TextView) view.findViewById(R.id.albumNameView);
        albumTextView.setText(in.getStringExtra(TopSongsActivityFragment.Key.ALBUM_NAME));
        TextView songTextView = (TextView) view.findViewById(R.id.songNameView);
        songTextView.setText(in.getStringExtra(TopSongsActivityFragment.Key.SONG_NAME));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Intent in = getActivity().getIntent();
        String imageUrl = in.getStringExtra(TopSongsActivityFragment.Key.ALBUM_IMAGE_URL);
        Util.loadImageInto(getActivity(), imageUrl, mAlbumImageView);
    }
}
