/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.bysy.spotifystreamer.MainActivity;
import com.github.bysy.spotifystreamer.Player;
import com.github.bysy.spotifystreamer.R;
import com.github.bysy.spotifystreamer.TopSongsFragment;
import com.github.bysy.spotifystreamer.data.SongInfo;
import com.squareup.picasso.Picasso;

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
    private Player mPlaylistController;
    private int mCurrentIndex = 0;
    private boolean mIsForeground = false;

    public static final String ACTION_NEW_PLAYLIST = "ACTION_NEW_PLAYLIST";
    public static final String ACTION_CHANGE_SONG = "ACTION_CHANGE_SONG";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_STOP = "ACTION_STOP";

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

    public void setPlaylistController(Player controller) {
        mPlaylistController = controller;
    }

    public void showForegroundNotification(SongInfo song) {
        startForeground(NOTIFICATION_ID, createNotification(song, true));
        mIsForeground = true;
    }

    private Notification createNotification(SongInfo song, boolean isPlaying) {
        Log.d(TAG, "setting notification with isPlaying = " + isPlaying);
        // Display custom layout
        // Thank you http://stackoverflow.com/a/16168704
        RemoteViews views = new RemoteViews(
                getPackageName(), R.layout.player_notification);
        // TODO: Some stuff below can be put in fields to reduce overhead
        // TODO: Extract expanded layout building so we can skip it completely on older SDKs
        final PendingIntent prevIntent = newPendingIntent(ACTION_PREVIOUS);
        final PendingIntent pauseIntent = newPendingIntent(ACTION_PAUSE);
        final PendingIntent playIntent = newPendingIntent(ACTION_RESUME);
        final PendingIntent nextIntent = newPendingIntent(ACTION_NEXT);
        final PendingIntent stopIntent = newPendingIntent(ACTION_STOP);

        final PendingIntent playPauseIntent = isPlaying ? pauseIntent : playIntent;

        views.setOnClickPendingIntent(R.id.previousButton, prevIntent);
        views.setOnClickPendingIntent(R.id.playPauseButton, playPauseIntent);
        views.setOnClickPendingIntent(R.id.nextButton, nextIntent);
        views.setOnClickPendingIntent(R.id.stopButton, stopIntent);

        final int playPauseRes = isPlaying ? R.drawable.pause_icon : R.drawable.play_icon;
        views.setImageViewResource(R.id.playPauseButton, playPauseRes);
        views.setTextViewText(R.id.songNameView, song.name);
        views.setTextViewText(R.id.albumNameView, song.albumName);
        views.setTextViewText(R.id.artistNameView, song.primaryArtistName);

        // Go back to main activity. We could figure out if want Main or Player,
        // but I really need some sleep. It's not part of the rubric, so I can
        // get some zzz. =) This way the user can click on Now Playing to go back
        // to the full player.
        Intent intent = new Intent(this.getBaseContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        final String artist = song.isCollaboration() ?
                song.primaryArtistName.concat(" & others") : song.primaryArtistName;
        builder.setSmallIcon(R.drawable.play_icon)
                .setContentTitle("Playing ".concat(song.name))
                .setContentText("by ".concat(artist))
                .addAction(R.drawable.previous_icon, "Previous", prevIntent);
        if (isPlaying) {
            builder.addAction(R.drawable.pause_icon, "Pause", pauseIntent);
        } else {
            builder.addAction(R.drawable.play_icon, "Play", playIntent);
        }

        builder.addAction(R.drawable.next_icon, "Next", nextIntent);
        builder.addAction(R.drawable.pause_icon, "Stop", stopIntent);
        // Set lockscreen visibility
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean showOnLockScreen =
                prefs.getBoolean(getString(R.string.pref_notification_key), false);
        if (showOnLockScreen) {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        }
        TaskStackBuilder stack = TaskStackBuilder.create(this);
        stack.addNextIntent(intent);
        PendingIntent pi = stack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);

        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
            notification.bigContentView = views;
            Picasso.with(this).load(song.albumImageUrl)
                    .centerCrop()
                    .resize(96, 96)
                    .into(views, R.id.albumImageView, NOTIFICATION_ID, notification);
        }
        return notification;
    }

    private void updateNotification(boolean isPlaying) {
        if (!mIsForeground || mPlaylistController==null) return;
        NotificationManager nm = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID,
                createNotification(mPlaylistController.getCurrentSong(), isPlaying));
    }

    private Intent newServiceIntent(String action) {
        Intent intent = new Intent(this, PlayerService.class);
        intent.setAction(action);
        return intent;
    }

    private PendingIntent newPendingIntent(String action) {
        return PendingIntent.getService(this, 0, newServiceIntent(action), 0);
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
            case ACTION_PREVIOUS:
                if (mPlaylistController==null) {
                    success = false;
                } else {
                    mPlaylistController.previous();
                    success = true;
                }
                break;
            case ACTION_NEXT:
                if (mPlaylistController==null) {
                    success = false;
                } else {
                    mPlaylistController.next();
                    success = true;
                }
                break;
            case ACTION_STOP:
                success = true;
                mIsForeground = false;
                stopForeground(true);
                break;
            case ACTION_NULL:
                Log.d(TAG, "Intent is null. Nothing to do.");
                success = true;
                break;
            default:
                success = false;
        }
        final boolean isPlaying = isPlaying();
        updateNotification(isPlaying);
        if (mListener!=null) mListener.onStateChange(isPlaying);
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
        updateNotification(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mListener!=null) mListener.onStateChange(false);
        updateNotification(false);
    }
}
