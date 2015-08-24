/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.bysy.spotifystreamer.data.SongInfo;
import com.github.bysy.spotifystreamer.service.PlayerService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Manage player state.
 */
public class Player implements ServiceConnection, PlayerService.OnStateChange {
    private static final String TAG = Player.class.getSimpleName();

    // Keep playlist state in static fields. That works nicely because when our app
    // is restored after it was killed completely, we don't really want to recreate
    // these fields to their old values.
    // If desired however, we could easily use GSON with a SharedPreference and
    // restore appropriately. Alternatively, we could build a ContentProvider.
    // Originally, I favored the ContentProvider approach because it would also
    // let us cache queries. But since we're not supposed to save the preview
    // audio files, we'd have to fire up the radio in any event.
    private static boolean sNowPlaying = false;
    private static ArrayList<SongInfo> sSongs = null;
    private static int sCurrentIdx = -1;

    private PlayerService mService;
    private boolean mAutoPlay = false;
    private Set<OnPlayStateChange> mPlayListeners = new HashSet<>(2);

    public interface OnPlayStateChange {
        void onPlayStateChange(boolean isPlaying, @Nullable SongInfo maybeSong);
    }

    public void registerPlayChangeListener(OnPlayStateChange listener) {
        mPlayListeners.add(listener);
    }

    public void unregisterPlayChangeListener(OnPlayStateChange listener) {
        mPlayListeners.remove(listener);
    }

    static boolean nowPlaying() {
        return sNowPlaying;
    }

    /** Bind to the service. Call this before invoking play state methods (playAt() etc). */
    void initialize(Context context) {
        Context appContext = context.getApplicationContext();
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
        sNowPlaying = true;
        sSongs = songs;
        sCurrentIdx = 0;
    }

    public void setCurrentIndex(int currentIndex) {
        sCurrentIdx = currentIndex;
    }

    public void setAutoPlay(boolean autoPlay) {
        mAutoPlay = autoPlay;
        if (mAutoPlay && mService!=null && !sSongs.isEmpty()) {
            playAt(sCurrentIdx);  // otherwise plays when service is connected
        }
    }

    /** Return current song. Playlist must have been set prior to calling this method.*/
    public @Nullable SongInfo getCurrentSong() {
        if (sSongs==null) return null;
        return sSongs.get(sCurrentIdx);
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
    }

    /**
     * Play a song from the playlist.
     *
     * @param index The index of the song to be played.
     */
    void playAt(int index) {
        if (!checkSongs("playAt")) return;
        sCurrentIdx = Math.max(0, Math.min(index, sSongs.size()-1));
        sendPlayerCommand(PlayerService.ACTION_NEW_SONG);
        mService.showForegroundNotification(getCurrentSong());
    }

    void pause() {
        if (!checkSongs("pause")) return;
        sendPlayerCommand(PlayerService.ACTION_PAUSE);
    }

    public void previous() {
        if (!checkSongs("previous")) return;
        int prev = sCurrentIdx - 1;
        sCurrentIdx = (prev>=0) ? prev : sSongs.size()-1;
        sendPlayerCommand(PlayerService.ACTION_NEW_SONG);
        mService.showForegroundNotification(getCurrentSong());
    }

    public void next() {
        if (!checkSongs("next")) return;
        int next = sCurrentIdx + 1;
        sCurrentIdx = (next< sSongs.size()) ? next : 0;
        sendPlayerCommand(PlayerService.ACTION_NEW_SONG);
        mService.showForegroundNotification(getCurrentSong());
    }

    private boolean checkSongs(@NonNull String forMethod) {
        final boolean haveSongs = sSongs !=null && !sSongs.isEmpty();
        if (!haveSongs) {
            Log.e(TAG, forMethod.concat(" called but playlist is empty"));
        }
        return haveSongs;
    }

    private void sendPlayerCommand(@NonNull String action) {
        // This method sends the full playlist each time. That's inefficient
        // but it means we are in control of the position in the playlist
        // no matter if the service has to be recreated without having
        // to bind to it. As a half-way optimization, PlayerService only
        // copies in the playlist when it has to be recreated. Otherwise,
        // it retains the previous playlist.
        Context appContext = mService.getApplicationContext();
        Intent playerIntent = new Intent(appContext, PlayerService.class);
        playerIntent.setAction(action);
        playerIntent.putExtra(PlayerService.SONG_KEY, sSongs.get(sCurrentIdx));
        mService.onStartCommand(playerIntent, 0, 0);
    }

    @Override
    public void onStateChange(boolean isPlaying) {
        for (OnPlayStateChange listener : mPlayListeners) {
            listener.onPlayStateChange(isPlaying, getCurrentSong());
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
        mService = binder.getService();
        mService.setPlaylistController(this);
        binder.registerListener(this);
        if (mAutoPlay && !sSongs.isEmpty()) {
            playAt(sCurrentIdx);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }
}
