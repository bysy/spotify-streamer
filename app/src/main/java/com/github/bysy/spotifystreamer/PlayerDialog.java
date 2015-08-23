/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.bysy.spotifystreamer.data.SongInfo;

import java.util.ArrayList;

/**
 * Play a song.
 */
public class PlayerDialog extends DialogFragment implements Player.OnPlayStateChange {
    //  Player state is in separate retained fragment.
    // That makes it easier to bind to service and use
    // callbacks and generally makes things simpler.
    private static final String TAG = PlayerDialog.class.getSimpleName();
    private ImageView mAlbumImageView;
    private TextView mArtistTextView;
    private TextView mAlbumTextView;
    private TextView mSongTextView;
    private ImageButton mPlayButton;
    private Player mPlayer;

    public PlayerDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayer = Player.getSharedPlayer(getActivity());
        mPlayer.registerPlayChangeListener(this);

        if (savedInstanceState==null) {
            // first run
            Bundle args = getArguments();
            ArrayList<SongInfo> songs = null;
            int currentIdx = 0;
            if (args!=null) {
                songs = args.getParcelableArrayList(TopSongsFragment.Key.SONGS_PARCEL);
                currentIdx = args.getInt(TopSongsFragment.Key.CURRENT_SONG, 0);
            }
            if (songs==null) {
                Log.e(TAG, "Created without songs argument.");
            }
            if (songs!=null) {
                mPlayer.setAutoPlay(true);
                mPlayer.setNewPlaylist(songs);
                mPlayer.setCurrentIndex(currentIdx);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_dialog, container, false);
        mAlbumImageView = (ImageView) view.findViewById(R.id.playerAlbumImage);

        mArtistTextView = (TextView) view.findViewById(R.id.artistNameView);
        mAlbumTextView = (TextView) view.findViewById(R.id.albumNameView);
        mSongTextView = (TextView) view.findViewById(R.id.songNameView);

        View buttonBar = view.findViewById(R.id.buttons);
        ImageButton prevButton = (ImageButton) buttonBar.findViewById(R.id.previousButton);
        mPlayButton = (ImageButton) buttonBar.findViewById(R.id.playButton);
        ImageButton nextButton = (ImageButton) buttonBar.findViewById(R.id.nextButton);

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPrevButtonClick();
            }
        });
        mPlayButton.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public void onPause() {
        mPlayer.unregisterPlayChangeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        mPlayer.registerPlayChangeListener(this);
        setPlayButtonView();
        super.onResume();
    }

    private void setPlayButtonView() {
        setPlayButtonView(mPlayer.isPlaying());
    }

    private void setPlayButtonView(boolean isPlaying) {
        if (isPlaying) {
            mPlayButton.setImageResource(R.drawable.pause_icon);
        } else {
            mPlayButton.setImageResource(R.drawable.play_icon);
        }
    }

    private void onPrevButtonClick() {
        mPlayer.previous();
        setViewData();
        setPlayButtonView();
    }

    private void onPlayButtonClick() {
        mPlayer.togglePlayState();
        setPlayButtonView();
    }

    private void onNextButtonClick() {
        mPlayer.next();
        setViewData();
        setPlayButtonView();
    }

    private void setViewData() {
        final SongInfo currentSong = mPlayer.getCurrentSong();
        mArtistTextView.setText(currentSong.primaryArtistName);
        mAlbumTextView.setText(currentSong.albumName);
        mSongTextView.setText(currentSong.name);
        final String imageUrl = currentSong.albumImageUrl;
        Util.loadImageInto(getActivity(), imageUrl, mAlbumImageView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setViewData();
    }

    @Override
    public void onPlayStateChange(boolean isPlaying) {
        if (mPlayButton==null) return;
        setPlayButtonView(isPlaying);
    }
}
