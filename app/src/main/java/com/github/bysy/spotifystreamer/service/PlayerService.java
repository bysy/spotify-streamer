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
    private int mCurrentIndex = 0;

    public static final String ACTION_NEW_PLAYLIST = "ACTION_NEW_PLAYLIST";
    public static final String ACTION_CHANGE_SONG = "ACTION_CHANGE_SONG";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RESUME = "ACTION_RESUME";

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
            case ACTION_CHANGE_SONG:
                success = initializeIfNecessary(intent) && prepareAndStart();
                break;
            case ACTION_NEW_PLAYLIST:
                success = handleNewPlaylist(intent);
                break;
            case ACTION_PAUSE:
                success = true;
                try {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    }
                } catch (IllegalStateException e) {
                    success = false;
                    mMediaPlayer.reset();
                }
                break;
            case ACTION_RESUME:
                success = true;
                try {
                    if (!mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                    }
                } catch (IllegalStateException e) {
                    success = false;
                    mMediaPlayer.reset();
                }
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

    private boolean initializeIfNecessary(Intent intent) {
        if (mMediaPlayer==null) {
            mMediaPlayer = new MediaPlayer();
        } else {
            mMediaPlayer.reset();
        }
        mCurrentIndex = intent.getIntExtra(TopSongsActivityFragment.Key.CURRENT_SONG, 0);
        if (mSongs==null) {
            mSongs = intent.getParcelableArrayListExtra(TopSongsActivityFragment.Key.SONGS_PARCEL);
            if (mSongs==null || mSongs.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Start playing a new list of songs. Returns whether media player was correctly set up. */
    private boolean handleNewPlaylist(Intent intent) {
        mSongs = intent.getParcelableArrayListExtra(TopSongsActivityFragment.Key.SONGS_PARCEL);
        return initializeIfNecessary(intent) && prepareAndStart();
    }

    private boolean prepareAndStart() {
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (mCurrentIndex>=mSongs.size()) mCurrentIndex = 0;
        final String previewUrl = mSongs.get(mCurrentIndex).previewUrl;
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
