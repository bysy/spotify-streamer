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
import android.util.Log;
import android.widget.RemoteViews;

import com.github.bysy.spotifystreamer.Player;
import com.github.bysy.spotifystreamer.R;
import com.github.bysy.spotifystreamer.data.SongInfo;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Provide a service to stream songs.
 */
public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
    private static final String ACTION_NULL = "ACTION_NULL";
    private static final int NOTIFICATION_ID = 234500001;
    private static String TAG = PlayerService.class.getSimpleName();
    private OnStateChange mListener = null;
    private SongInfo mSong;
    private MediaPlayer mMediaPlayer;
    private Player mPlaylistController;
    private boolean mIsForeground = false;

    public static final String SONG_KEY = "SONG_KEY";

    public static final String ACTION_NEW_SONG = "ACTION_NEW_SONG";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_STOP = "ACTION_STOP";
    private AudioManager mAudioManager;
    private PendingIntent mPrevIntent;
    private PendingIntent mPauseIntent;
    private PendingIntent mPlayIntent;
    private PendingIntent mNextIntent;
    private PendingIntent mStopIntent;

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange!=AudioManager.AUDIOFOCUS_GAIN) {
            mPlaylistController.pause();
        }
    }

    public interface OnStateChange {
        void onStateChange();
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
        if (!mIsForeground) {
            startForeground(NOTIFICATION_ID, createNotification(song, true));
            mIsForeground = true;
        }
    }

    private Notification createNotification(SongInfo song, boolean isPlaying) {
        Log.d(TAG, "setting notification with isPlaying = " + isPlaying);

        final PendingIntent playPauseIntent = isPlaying ? mPauseIntent : mPlayIntent;
        final int playPauseRes = isPlaying ? R.drawable.pause_icon : R.drawable.play_icon;
        final String artist = song.getArtistSummary();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.play_icon)
                .setContentTitle("Playing ".concat(song.name))
                .setContentText("by ".concat(artist))
                .addAction(R.drawable.previous_icon, "Previous", mPrevIntent);
        if (isPlaying) {
            builder.addAction(R.drawable.pause_icon, "Pause", mPauseIntent);
        } else {
            builder.addAction(R.drawable.play_icon, "Play", mPlayIntent);
        }

        builder.addAction(R.drawable.next_icon, "Next", mNextIntent);
        builder.addAction(R.drawable.close_icon, "Stop", mStopIntent);
        // Set lockscreen visibility
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean showOnLockScreen =
                prefs.getBoolean(getString(R.string.pref_notification_key), false);
        if (showOnLockScreen) {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        }
        // Don't assign an intent for the whole notification for now.
        // It's not part of the optional rubric item, so I can get some zzz. =)
        // TODO: Figure out how to go back to original activity
        // FLAG_ACTIVITY_CLEAR_TOP doesn't do the trick. Nor the combo with new task.

        //Intent intent = new Intent(this.getBaseContext(), MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //TaskStackBuilder stack = TaskStackBuilder.create(this);
        //stack.addNextIntent(intent);
        //PendingIntent pi = stack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        //builder.setContentIntent(pi);

        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
            // Display custom layout
            // Thank you http://stackoverflow.com/a/16168704
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.player_notification);
            views.setOnClickPendingIntent(R.id.previousButton, mPrevIntent);
            views.setOnClickPendingIntent(R.id.playPauseButton, playPauseIntent);
            views.setOnClickPendingIntent(R.id.nextButton, mNextIntent);
            views.setOnClickPendingIntent(R.id.stopButton, mStopIntent);
            views.setImageViewResource(R.id.playPauseButton, playPauseRes);
            views.setTextViewText(R.id.songNameView, song.name);
            views.setTextViewText(R.id.albumNameView, song.albumName);
            views.setTextViewText(R.id.artistNameView, artist);
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
    public void onCreate() {
        super.onCreate();
        mPrevIntent = newPendingIntent(ACTION_PREVIOUS);
        mPauseIntent = newPendingIntent(ACTION_PAUSE);
        mPlayIntent = newPendingIntent(ACTION_RESUME);
        mNextIntent = newPendingIntent(ACTION_NEXT);
        mStopIntent = newPendingIntent(ACTION_STOP);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int mode = START_STICKY;
        boolean success;
        final String action = intent==null ? ACTION_NULL : intent.getAction();
        switch (action) {
            case ACTION_NEW_SONG:
                success = requestAudioFocus() && handleNewSong(intent);
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
                final boolean haveFocus = requestAudioFocus();
                success = true;
                if (mMediaPlayer==null) {
                    success = haveFocus && initializeIfNecessary(intent);
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
                if (mMediaPlayer!=null) mMediaPlayer.reset();
                abandonAudioFocus();
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
        if (mListener!=null) mListener.onStateChange();
        if (success) {
            Log.d(TAG, action.concat(": Success"));
        } else {
            Log.d(TAG, action.concat(": Failed"));
        }
        return mode;
    }

    private boolean requestAudioFocus() {
        int ret = mAudioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        Log.v(TAG, "AudioFocus: " + (ret==AudioManager.AUDIOFOCUS_GAIN ? "Yes" : "No"));
        return ret==AudioManager.AUDIOFOCUS_GAIN;
    }

    private void abandonAudioFocus() {
        mAudioManager.abandonAudioFocus(this);
        Log.v(TAG, "Abandoned audio focus");
    }

    private boolean initializeIfNecessary(Intent intent) {
        if (mMediaPlayer==null) {
            mMediaPlayer = new MediaPlayer();
        } else {
            mMediaPlayer.reset();
        }
        if (mSong==null) {
            mSong = intent.getParcelableExtra(SONG_KEY);
            if (mSong==null) {
                return false;
            }
        }
        return true;
    }

    /** Start playing a new song. Returns whether media player was correctly set up. */
    private boolean handleNewSong(Intent intent) {
        mSong = intent.getParcelableExtra(SONG_KEY);
        return initializeIfNecessary(intent) && prepareAndStart();
    }

    private boolean prepareAndStart() {
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        final String previewUrl = mSong.previewUrl;
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
        abandonAudioFocus();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        if (mListener!=null) mListener.onStateChange();
        updateNotification(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mListener!=null) mListener.onStateChange();
        updateNotification(false);
    }
}
