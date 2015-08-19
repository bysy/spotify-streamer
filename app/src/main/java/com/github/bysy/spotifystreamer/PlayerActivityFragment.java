/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

/**
 * Play a song.
 */
public class PlayerActivityFragment extends Fragment implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

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

        // Don't start another player when the orientation etc changes.
        if (savedInstanceState!=null) {
            return;
        }
        String previewUrl = in.getStringExtra(TopSongsActivityFragment.Key.SONG_PREVIEW_URL);
        if (previewUrl==null) {
            return;
        }
        MediaPlayer player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "Trying to play ".concat(previewUrl));
        try {
            player.setDataSource(previewUrl);
        } catch (IOException e) {
            Log.e(TAG, "Setting of preview url failed.");
            e.printStackTrace();
        }
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.prepareAsync();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
    }
}
