/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.bysy.spotifystreamer.service.PlayerService;

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

        View buttonBar = view.findViewById(R.id.buttons);
        ImageButton prevButton = (ImageButton) buttonBar.findViewById(R.id.previousButton);
        ImageButton playButton = (ImageButton) buttonBar.findViewById(R.id.playButton);
        ImageButton nextButton = (ImageButton) buttonBar.findViewById(R.id.nextButton);

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPrevButtonClick();
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayButtonClick();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButtonClick();
            }
        });
        return view;
    }

    private void onPrevButtonClick() {
        Util.showToast(getActivity(), "Previous clicked");
        sendPlayerCommand(PlayerService.ACTION_PREVIOUS);
    }

    private void onPlayButtonClick() {
        Util.showToast(getActivity(), "Play clicked");
    }

    private void onNextButtonClick() {
        Util.showToast(getActivity(), "Next clicked");
        sendPlayerCommand(PlayerService.ACTION_NEXT);
    }

    private void sendPlayerCommand(String action) {
        final Context appContext = getActivity().getApplicationContext();
        Intent playerIntent = new Intent(appContext, PlayerService.class);
        playerIntent.setAction(action);
        appContext.startService(playerIntent);
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
        final Context appContext = getActivity().getApplicationContext();
        Intent playerIntent = new Intent(appContext, PlayerService.class);
        playerIntent.setAction(PlayerService.ACTION_NEW_PLAYLIST);
        playerIntent.fillIn(in, 0);
        appContext.startService(playerIntent);
    }
}
