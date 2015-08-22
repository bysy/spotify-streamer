/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.os.Bundle;
import android.os.Handler;
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
public class PlayerDialog extends DialogFragment {
    // TODO: Refactor to use this class as a thin view controller.
    //       Put player state in separate retained fragment.
    //       That should allow binding to service, adding
    //       callbacks and generally make things simpler.
    private static final String TAG = PlayerDialog.class.getSimpleName();
    private static final java.lang.String IS_PLAYING_KEY = "IS_PLAYING_KEY";
    private ImageView mAlbumImageView;
    private TextView mArtistTextView;
    private TextView mAlbumTextView;
    private TextView mSongTextView;
    private boolean mIsPlaying;
    private ImageButton mPlayButton;
    private Player mPlayer;

    public PlayerDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayer = Player.getSharedPlayer(getActivity());

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
            mIsPlaying = false;
            if (songs!=null) {
                mPlayer.setNewPlaylist(songs);
                mPlayer.setCurrentIndex(currentIdx);
                mPlayer.setAutoPlay(true);
                mIsPlaying = true;
            }
        } else {
            // restore
            mIsPlaying = savedInstanceState.getBoolean(IS_PLAYING_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_PLAYING_KEY, mIsPlaying);
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

    private void setPlayButtonView() {
        if (mIsPlaying) {
            mPlayButton.setImageResource(R.drawable.pause_icon);
        } else {
            mPlayButton.setImageResource(R.drawable.play_icon);
        }
    }

    private void onPrevButtonClick() {
        Util.showToast(getActivity(), "Previous clicked");
        mPlayer.previous();
        setViewData();
    }

    private void onPlayButtonClick() {
        Util.showToast(getActivity(), "Play clicked");
        togglePlayState();
        setPlayButtonView();
    }

    private void togglePlayState() {
        if (mIsPlaying) {
            mPlayer.pause();
        } else {
            mPlayer.play();
        }
        mIsPlaying = !mIsPlaying;
    }

    private void onNextButtonClick() {
        Util.showToast(getActivity(), "Next clicked");
        mPlayer.next();
        mIsPlaying = true;
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

        // Don't start another player when the orientation etc changes.
        if (savedInstanceState!=null) {
            return;
        }
        // Be all cute and delay showing the pause icon. Really, it should
        // be swapped in via the onPrepared() callback. Requires binding
        // to the service.
        final int delayMS = 500;
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        setPlayButtonView();
                    }
                }, delayMS);
    }
}
