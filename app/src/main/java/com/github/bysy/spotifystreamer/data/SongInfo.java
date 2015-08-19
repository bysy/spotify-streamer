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
    @NonNull public final String id;
    /** The first artist listed. */
    @NonNull public final String primaryArtistName;
    /** The number of artists credited with creating this track. */
    public final int numberOfArtists;
    @NonNull public final String albumName;
    @NonNull public final String name;
    /** Url for the album image. Guaranteed to hold a pattern-valid Url if non-empty. */
    @NonNull public final String albumImageUrl;
    @NonNull public final String previewUrl;

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
        final String imageUrl = Util.getLargeImageUrl(spotifyTrack.album.images);
        albumImageUrl = imageUrl==null ? "" : imageUrl;
        previewUrl = spotifyTrack.preview_url;
    }

    // Implement equals() and hashCode(). Thanks AndroidStudio!

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SongInfo songInfo = (SongInfo) o;

        if (numberOfArtists != songInfo.numberOfArtists) return false;
        if (!id.equals(songInfo.id)) return false;
        if (!primaryArtistName.equals(songInfo.primaryArtistName)) return false;
        if (!albumName.equals(songInfo.albumName)) return false;
        if (!name.equals(songInfo.name)) return false;
        if (!albumImageUrl.equals(songInfo.albumImageUrl)) return false;
        return previewUrl.equals(songInfo.previewUrl);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + primaryArtistName.hashCode();
        result = 31 * result + numberOfArtists;
        result = 31 * result + albumName.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + albumImageUrl.hashCode();
        result = 31 * result + previewUrl.hashCode();
        return result;
    }
}
