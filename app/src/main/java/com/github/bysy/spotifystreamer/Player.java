/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.bysy.spotifystreamer.data.SongInfo;
import com.github.bysy.spotifystreamer.service.PlayerService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Manage player state via a singleton.
 */
public class Player implements ServiceConnection, PlayerService.OnStateChange {
    // The idea for this class is to shield clients from the need to bind to
    // the service. The service itself only knows how to play a single song;
    // we manage the playlist here. Lastly, the service delegates back to us
    // if it receives a command to play the previous or next song.
    private static final String TAG = Player.class.getSimpleName();

    // The singleton works well here because when our app is restored after it
    // was killed completely, we don't really want to recreate the playlist with
    // its old state.
    // If desired however, we could easily use GSON with a SharedPreference and
    // restore appropriately. Alternatively, we could build a ContentProvider.
    // Originally, I favored the ContentProvider approach because it would also
    // let us cache queries. But since we're not supposed to save the preview
    // audio files, we'd have to fire up the radio anyway.
    private static Player sInstance;

    private boolean mHasPlaylist = false;
    private ArrayList<SongInfo> mSongs = null;
    private int mCurrentIdx = -1;
    private SongInfo mLastSong;

    private PlayerService mService;
    private boolean mAutoPlay = false;
    private Set<OnStateChange> mPlayListeners = new HashSet<>(2);
    private boolean mShowNotification;  // Updated from preference in initialize()
    private int mDuration = 0;

    public interface OnStateChange {
        void onPlayStateChange(boolean isPlaying);
        void onSongChange(@NonNull SongInfo song);
    }

    /**
     * Register a play change listener.
     * The listener will receive callbacks when the play state changes
     * between play and pause and when the current song changes. The
     * listener is also updated once upon registering.
     */
    public void registerPlayChangeListener(OnStateChange listener) {
        mPlayListeners.add(listener);
        updateListener(listener, true);
    }

    public void unregisterPlayChangeListener(OnStateChange listener) {
        mPlayListeners.remove(listener);
    }

    public static Player getInstance() {
        if (sInstance==null) {
            sInstance = new Player();
        }
        return sInstance;
    }

    public boolean hasPlaylist() {
        return mHasPlaylist;
    }

    /** Bind to the service. Call this before invoking play state methods (playAt() etc). */
    void initialize(Context context) {
        final Context appContext = context.getApplicationContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        mShowNotification = prefs.getBoolean(context.getString(R.string.pref_notification_key), false);
        if (mService==null) {
            Intent bindIntent = new Intent(appContext, PlayerService.class);
            appContext.bindService(bindIntent, this, Context.BIND_AUTO_CREATE);
        }
    }

    void setNewPlaylist(@NonNull ArrayList<SongInfo> songs) {
        if (songs.isEmpty()) {
            Log.w(TAG, "Cannot play empty playlist");
            return;
        }
        mHasPlaylist = true;
        mSongs = songs;
        mCurrentIdx = 0;
    }

    public void setCurrentIndex(int currentIndex) {
        mCurrentIdx = currentIndex;
    }

    public void setAutoPlay(boolean autoPlay) {
        mAutoPlay = autoPlay;
        if (mAutoPlay && mService!=null && !mSongs.isEmpty()) {
            playAt(mCurrentIdx);  // otherwise plays when service is connected
        }
    }

    /** Return current song. Playlist must have been set prior to calling this method.*/
    public @Nullable SongInfo getCurrentSong() {
        if (mSongs ==null) return null;
        return mSongs.get(mCurrentIdx);
    }

    public int getPosition() {
        return mService!=null ? (int) mService.getPosition() : -1;
    }

    public int getDuration() {
        return mDuration;
    }

    public boolean isPlaying() {
        return mService!=null && mService.isPlaying();
    }

    public void togglePlayState() {
        if (mService==null) return;
        if (mService.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    /**
     * Start playing. If paused, resumes play.
     */
    void play() {
        if (!checkSongs("play")) return;
        sendPlayerCommand(PlayerService.ACTION_RESUME);
        mService.showForegroundNotification(getCurrentSong());
    }

    /**
     * Play a song from the playlist.
     *
     * @param index The index of the song to be played.
     */
    void playAt(int index) {
        if (!checkSongs("playAt")) return;
        mCurrentIdx = Math.max(0, Math.min(index, mSongs.size()-1));
        sendPlayerCommand(PlayerService.ACTION_NEW_SONG);
        mService.showForegroundNotification(getCurrentSong());
    }

    public void pause() {
        if (!checkSongs("pause")) return;
        sendPlayerCommand(PlayerService.ACTION_PAUSE);
    }

    public void previous() {
        if (!checkSongs("previous")) return;
        int prev = mCurrentIdx - 1;
        mCurrentIdx = (prev>=0) ? prev : mSongs.size()-1;
        sendPlayerCommand(PlayerService.ACTION_NEW_SONG);
        mService.showForegroundNotification(getCurrentSong());
    }

    public void next() {
        if (!checkSongs("next")) return;
        int next = mCurrentIdx + 1;
        mCurrentIdx = (next< mSongs.size()) ? next : 0;
        sendPlayerCommand(PlayerService.ACTION_NEW_SONG);
        mService.showForegroundNotification(getCurrentSong());
    }

    public void seekTo(int milliseconds) {
        if (mService==null) return;
        mService.seekTo(milliseconds);
        if (!isPlaying()) {
            play();
        }
    }

    private boolean checkSongs(@NonNull String forMethod) {
        final boolean haveSongs = mSongs !=null && !mSongs.isEmpty();
        if (!haveSongs) {
            Log.e(TAG, forMethod.concat(" called but playlist is empty"));
        }
        return haveSongs;
    }

    private void sendPlayerCommand(@NonNull String action) {
        // Send the given action along with the current song.
        Context appContext = mService.getApplicationContext();
        Intent playerIntent = new Intent(appContext, PlayerService.class);
        playerIntent.setAction(action);
        playerIntent.putExtra(PlayerService.SONG_KEY, mSongs.get(mCurrentIdx));
        mService.onStartCommand(playerIntent, 0, 0);
    }

    @Override
    public void onStateChange() {
        for (OnStateChange listener : mPlayListeners) {
            updateListener(listener, false);
        }
    }

    private void updateListener(OnStateChange listener, boolean doFullUpdate) {
        if (isPlaying()) {
            mDuration = (int) mService.getDuration();
        }
        listener.onPlayStateChange(isPlaying());
        final SongInfo song = getCurrentSong();
        if (song!=null) {
            if (song!=mLastSong || doFullUpdate) {
                listener.onSongChange(getCurrentSong());
            }
        }
        mLastSong = song;
    }

    public boolean shouldShowNotification() {
        return mShowNotification;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
        mService = binder.getService();
        mService.setPlaylistController(this);
        binder.registerListener(this);
        if (mAutoPlay && !mSongs.isEmpty()) {
            playAt(mCurrentIdx);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    // Enforce singleton pattern
    private Player() { }
}
