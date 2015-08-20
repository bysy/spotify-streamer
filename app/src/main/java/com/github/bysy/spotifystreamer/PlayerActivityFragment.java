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

import com.github.bysy.spotifystreamer.data.SongInfo;
import com.github.bysy.spotifystreamer.service.PlayerService;

import java.util.ArrayList;

/**
 * Play a song.
 */
public class PlayerActivityFragment extends Fragment {

    private static final String TAG = PlayerActivityFragment.class.getSimpleName();
    private static final String SONG_PARCEL_KEY = "SONG_PARCEL_KEY";
    private static final String CURRENT_INDEX_KEY = "CURRENT_INDEX_KEY";
    private ImageView mAlbumImageView;
    private TextView mArtistTextView;
    private TextView mAlbumTextView;
    private TextView mSongTextView;
    private ArrayList<SongInfo> mSongs;
    private int mCurrentIndex;

    public PlayerActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent in = getActivity().getIntent();
        if (in==null) {
            return;
        }
        // TODO: Check if intent is retained
        mSongs = in.getParcelableArrayListExtra(TopSongsActivityFragment.Key.SONGS_PARCEL);
        if (savedInstanceState==null) {
            // first run
            mCurrentIndex = in.getIntExtra(TopSongsActivityFragment.Key.CURRENT_SONG, 0);
        } else {
            // restore
            mCurrentIndex = savedInstanceState.getInt(CURRENT_INDEX_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putParcelableArrayList(SONG_PARCEL_KEY, mSongs);
        outState.putInt(CURRENT_INDEX_KEY, mCurrentIndex);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        mAlbumImageView = (ImageView) view.findViewById(R.id.playerAlbumImage);

        mArtistTextView = (TextView) view.findViewById(R.id.artistNameView);
        mAlbumTextView = (TextView) view.findViewById(R.id.albumNameView);
        mSongTextView = (TextView) view.findViewById(R.id.songNameView);

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
        modifyCurrentIndex(-1);
        sendPlayerCommand(PlayerService.ACTION_CHANGE_SONG);
    }

    private void onPlayButtonClick() {
        Util.showToast(getActivity(), "Play clicked");
    }

    private void onNextButtonClick() {
        Util.showToast(getActivity(), "Next clicked");
        modifyCurrentIndex(1);
        sendPlayerCommand(PlayerService.ACTION_CHANGE_SONG);
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

    private void sendPlayerCommand(String action) {
        final Context appContext = getActivity().getApplicationContext();
        Intent playerIntent = new Intent(appContext, PlayerService.class);
        playerIntent.setAction(action);
        playerIntent.putExtra(TopSongsActivityFragment.Key.CURRENT_SONG, mCurrentIndex);
        playerIntent.putExtra(TopSongsActivityFragment.Key.SONGS_PARCEL, mSongs);
        appContext.startService(playerIntent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setViewData();

        // Don't start another player when the orientation etc changes.
        if (savedInstanceState!=null) {
            return;
        }
        sendPlayerCommand(PlayerService.ACTION_NEW_PLAYLIST);
    }
}
