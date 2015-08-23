/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.github.bysy.spotifystreamer.PlayerActivity;
import com.github.bysy.spotifystreamer.R;
import com.github.bysy.spotifystreamer.TopSongsFragment;
import com.github.bysy.spotifystreamer.data.SongInfo;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Provide a service to stream songs.
 */
public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private static final String ACTION_NULL = "ACTION_NULL";
    private static final int NOTIFICATION_ID = 234500001;
    private static String TAG = PlayerService.class.getSimpleName();
    private OnStateChange mListener = null;
    private ArrayList<SongInfo> mSongs;
    private MediaPlayer mMediaPlayer;
    private int mCurrentIndex = 0;

    public static final String ACTION_NEW_PLAYLIST = "ACTION_NEW_PLAYLIST";
    public static final String ACTION_CHANGE_SONG = "ACTION_CHANGE_SONG";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RESUME = "ACTION_RESUME";

    public interface OnStateChange {
        void onStateChange(boolean isPlaying);
    }

    public boolean isPlaying() {
        return mMediaPlayer!=null && mMediaPlayer.isPlaying();
    }

    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
        public void registerListener(OnStateChange listener) {
            mListener = listener;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public void showForegroundNotification(SongInfo song) {
        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        final String artist = song.isCollaboration() ?
                song.primaryArtistName.concat(" & others") : song.primaryArtistName;
        builder.setSmallIcon(R.drawable.play_icon)
                .setContentTitle("Playing ".concat(song.name))
                .setContentText("by ".concat(artist))
                .setOngoing(true);
        TaskStackBuilder stack = TaskStackBuilder.create(this);
        stack.addParentStack(PlayerActivity.class);
        stack.addNextIntent(intent);
        PendingIntent pi = stack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        startForeground(NOTIFICATION_ID, builder.build());
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
                if (mMediaPlayer==null) success = initializeIfNecessary(intent);
                if (!success) break;
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
                if (mMediaPlayer==null) {
                    success = initializeIfNecessary(intent);
                }
                if (!success) break;
                try {
                    if (!mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                    }
                    if (mMediaPlayer.isPlaying()) break;
                    // Need to restart player completely
                    success = initializeIfNecessary(intent);
                    prepareAndStart();
                } catch (IllegalStateException e) {
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
        mCurrentIndex = intent.getIntExtra(TopSongsFragment.Key.CURRENT_SONG, 0);
        if (mSongs==null) {
            mSongs = intent.getParcelableArrayListExtra(TopSongsFragment.Key.SONGS_PARCEL);
            if (mSongs==null || mSongs.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Start playing a new list of songs. Returns whether media player was correctly set up. */
    private boolean handleNewPlaylist(Intent intent) {
        mSongs = intent.getParcelableArrayListExtra(TopSongsFragment.Key.SONGS_PARCEL);
        return initializeIfNecessary(intent) && prepareAndStart();
    }

    private boolean prepareAndStart() {
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
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
        // TODO: Consider acquiring wifi-lock if appropriate
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
        if (mListener!=null) mListener.onStateChange(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mListener!=null) mListener.onStateChange(false);
    }
}
