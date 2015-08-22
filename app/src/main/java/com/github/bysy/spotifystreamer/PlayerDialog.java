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
    private static final String SONG_PARCEL_KEY = "SONG_PARCEL_KEY";
    private static final String CURRENT_INDEX_KEY = "CURRENT_INDEX_KEY";
    private static final java.lang.String IS_PLAYING_KEY = "IS_PLAYING_KEY";
    private ImageView mAlbumImageView;
    private TextView mArtistTextView;
    private TextView mAlbumTextView;
    private TextView mSongTextView;
    private ArrayList<SongInfo> mSongs;
    private int mCurrentIndex;
    private boolean mIsPlaying;
    private ImageButton mPlayButton;
    private Player mPlayer;

    public PlayerDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args!=null) {
            mSongs = args.getParcelableArrayList(TopSongsFragment.Key.SONGS_PARCEL);
        } else {
            mSongs = null;
        }
        if (mSongs==null) {
            Log.e(TAG, "Created without songs argument.");
        }

        if (savedInstanceState==null) {
            // first run
            mCurrentIndex = (args==null) ? -1 : args.getInt(TopSongsFragment.Key.CURRENT_SONG, 0);
            mIsPlaying = false;
        } else {
            // restore
            mCurrentIndex = savedInstanceState.getInt(CURRENT_INDEX_KEY);
            mIsPlaying = savedInstanceState.getBoolean(IS_PLAYING_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putParcelableArrayList(SONG_PARCEL_KEY, mSongs);
        outState.putInt(CURRENT_INDEX_KEY, mCurrentIndex);
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
        modifyCurrentIndex(-1);
        setViewData();
        mPlayer.previous(getActivity());
    }

    private void onPlayButtonClick() {
        Util.showToast(getActivity(), "Play clicked");
        togglePlayState();
        setPlayButtonView();
    }

    private void togglePlayState() {
        if (mIsPlaying) {
            mPlayer.pause(getActivity());
        } else {
            mPlayer.play(getActivity());
        }
        mIsPlaying = !mIsPlaying;
    }

    private void onNextButtonClick() {
        Util.showToast(getActivity(), "Next clicked");
        modifyCurrentIndex(1);
        setViewData();
        mPlayer.next(getActivity());
        mIsPlaying = true;
        setPlayButtonView();
    }

    private void setViewData() {
        final SongInfo currentSong = mSongs.get(mCurrentIndex);
        mArtistTextView.setText(currentSong.primaryArtistName);
        mAlbumTextView.setText(currentSong.albumName);
        mSongTextView.setText(currentSong.name);
        final String imageUrl = currentSong.albumImageUrl;
        Util.loadImageInto(getActivity(), imageUrl, mAlbumImageView);
    }

    private void modifyCurrentIndex(int i) {
        final int newIndex = mCurrentIndex + i;
        // Loop from first to last and vice versa.
        mCurrentIndex = newIndex<0 ? mSongs.size()-1 : (newIndex>=mSongs.size() ? 0 : newIndex);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setViewData();

        // Don't start another player when the orientation etc changes.
        if (savedInstanceState!=null) {
            return;
        }
        mPlayer = Player.getSharedPlayer(getActivity());
        mPlayer.setNewPlaylist(mSongs);
        mPlayer.playAt(getActivity(), mCurrentIndex);
        mIsPlaying = true;
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
