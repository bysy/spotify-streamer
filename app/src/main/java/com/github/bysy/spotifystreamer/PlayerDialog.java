/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.bysy.spotifystreamer.data.SongInfo;

/**
 * Display the player controls. Containing Activity needs to implement interface GetPlayer.
 */
public class PlayerDialog extends DialogFragment implements Player.OnStateChange {
    private static final String TAG = PlayerDialog.class.getSimpleName();
    private ImageView mAlbumImageView;
    private TextView mArtistTextView;
    private TextView mAlbumTextView;
    private TextView mSongTextView;
    private ImageButton mPlayButton;
    private Player mPlayer;  // handles player state
    private ShareActionProvider mShareActionProvider;

    public PlayerDialog() {
    }

    public interface GetPlayer {
        @NonNull Player getPlayer();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
                mPlayer.previous();
            }
        });
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.togglePlayState();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.next();
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_player_dialog, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        final SongInfo song = mPlayer.getCurrentSong();
        if (mShareActionProvider!=null && song!=null) {
            mShareActionProvider.setShareIntent(getShareIntent(song));
        }
    }

    private void setShareIntent(SongInfo song) {
        if (mShareActionProvider!=null) {
            mShareActionProvider.setShareIntent(getShareIntent(song));
        }
    }

    private Intent getShareIntent(SongInfo song) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //noinspection deprecation since we target older API
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        final String shareText =
                getString(R.string.app_name_hashtag) + ": Loving this track\n" +
                        song.externalSpotifyUrl;
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        return shareIntent;
    }

    @Override
    public void onPause() {
        mPlayer.unregisterPlayChangeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        mPlayer.registerPlayChangeListener(this);
        super.onResume();
    }

    private void setPlayButtonView(boolean isPlaying) {
        if (isPlaying) {
            mPlayButton.setImageResource(R.drawable.pause_icon);
        } else {
            mPlayButton.setImageResource(R.drawable.play_icon);
        }
    }

    private void setViewData(SongInfo currentSong) {
        if (currentSong==null) return;
        mArtistTextView.setText(currentSong.getArtistSummary());
        mAlbumTextView.setText(currentSong.albumName);
        mSongTextView.setText(currentSong.name);
        final String imageUrl = currentSong.albumImageUrl;
        Util.loadImageInto(getActivity(), imageUrl, mAlbumImageView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Activity parent = getActivity();
        if (!(parent instanceof GetPlayer)) {
            throw new IllegalStateException(
                    TAG.concat(": Containing Activity doesn't implement GetPlayer"));
        }
        mPlayer = ((GetPlayer) parent).getPlayer();
        mPlayer.registerPlayChangeListener(this);
    }

    // Update views and share intent in response to play state changes

    @Override
    public void onSongChange(@NonNull SongInfo song) {
        setViewData(song);
        setShareIntent(song);
    }

    @Override
    public void onPlayStateChange(boolean isPlaying) {
        if (mPlayButton==null) return;
        setPlayButtonView(isPlaying);
    }
}
