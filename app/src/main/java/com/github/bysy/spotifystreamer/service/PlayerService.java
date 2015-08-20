/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.bysy.spotifystreamer.TopSongsActivityFragment;
import com.github.bysy.spotifystreamer.data.SongInfo;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Provide a service to stream songs.
 */
public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private static final String ACTION_NULL = "ACTION_NULL";
    private static String TAG = PlayerService.class.getSimpleName();
    private ArrayList<SongInfo> mSongs;
    private MediaPlayer mMediaPlayer;
    private SongInfo mCurrentSong;
    private int mCurrentIndex = 0;

    public static final String ACTION_NEW_PLAYLIST = "ACTION_NEW_PLAYLIST";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "ACTION_NEXT";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int mode = START_STICKY;
        boolean success;
        final String action = intent==null ? ACTION_NULL : intent.getAction();
        switch (action) {
            case ACTION_NEW_PLAYLIST:
                success = handleNewPlaylist(intent);
                break;
            case ACTION_PREVIOUS:
                success = handleSkip(-1);
                break;
            case ACTION_NEXT:
                success = handleSkip(1);
                break;
            case ACTION_NULL:
                Log.d(TAG, "Intent is null. Nothing to do.");
                success = true;
                break;
            default:
                success = false;
        }
        if (success) {
            Log.d(TAG, action.concat(": Success"));
        } else {
            Log.d(TAG, action.concat(": Failed"));
        }
        return mode;
    }

    private boolean handleSkip(int i) {
        final int newIndex = mCurrentIndex + i;
        // Loop from first to last and vice versa.
        mCurrentIndex = newIndex<0 ? mSongs.size()-1 : (newIndex>=mSongs.size() ? 0 : newIndex);
        mMediaPlayer.reset();
        return prepareAndStart();
    }

    /** Start playing a new list of songs. Returns whether media player was correctly set up. */
    private boolean handleNewPlaylist(Intent intent) {
        mSongs = intent.getParcelableArrayListExtra(TopSongsActivityFragment.Key.SONGS_PARCEL);
        if (mSongs == null || mSongs.isEmpty()) {
            return false;
        }
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        } else {
            mMediaPlayer.reset();
        }
        mCurrentIndex = intent.getIntExtra(TopSongsActivityFragment.Key.CURRENT_SONG, 0);
        return prepareAndStart();
    }

    private boolean prepareAndStart() {
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (mCurrentIndex>=mSongs.size()) mCurrentIndex = 0;
        mCurrentSong = mSongs.get(mCurrentIndex);
        final String previewUrl = mCurrentSong.previewUrl;
        Log.d(TAG, "Trying to play ".concat(previewUrl));
        try {
            mMediaPlayer.setDataSource(previewUrl);
        } catch (IOException e) {
            Log.e(TAG, "Setting of preview url failed.");
            e.printStackTrace();
            mMediaPlayer.reset();
            return false;
        }
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.prepareAsync();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }
}
