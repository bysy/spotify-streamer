/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.bysy.spotifystreamer.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Capture information about a song.
 */
public class SongInfo {
    public final String id;
    /** The first artist listed. */
    public final String primaryArtistName;
    /** The number of artists credited with creating this track. */
    public final int numberOfArtists;
    public final String albumName;
    public final String name;
    public final String albumImageUrl;
    public final String previewUrl;

    /** Create a list from Spotify Web Api tracks. */
    @NonNull
    public static List<SongInfo> listOf(@Nullable Collection<Track> spotifyTracks) {
        if (spotifyTracks==null) return Collections.emptyList();
        List<SongInfo> songs = new ArrayList<>(spotifyTracks.size());
        for (Track t : spotifyTracks) {
            songs.add(valueOf(t));
        }
        return songs;
    }

    /** Create an instance from a Spotify Web Api track. */
    @NonNull
    public static SongInfo valueOf(@NonNull Track spotifyTrack) {
        return new SongInfo(spotifyTrack);
    }

    public boolean isCollaboration() {
        return numberOfArtists > 1;
    }

    // Enforce instantiation via factory methods
    private SongInfo(Track spotifyTrack) {
        id = spotifyTrack.id;
        primaryArtistName = spotifyTrack.artists.get(0).name;
        numberOfArtists = spotifyTrack.artists.size();
        albumName = spotifyTrack.album.name;
        name = spotifyTrack.name;
        albumImageUrl = Util.getLargeImageUrl(spotifyTrack.album.images);
        previewUrl = spotifyTrack.preview_url;
    }
}
