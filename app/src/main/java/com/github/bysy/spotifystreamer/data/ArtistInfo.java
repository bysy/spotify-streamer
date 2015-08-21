/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.bysy.spotifystreamer.Util;

import java.util.ArrayList;
import java.util.Collection;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Capture information about an artist.
 */
public class ArtistInfo {
    @NonNull public String id;
    @NonNull public final String name;
    @NonNull public final String smallImageUrl;  // guaranteed to be pattern-valid if non-empty

    @NonNull
    public static ArrayList<ArtistInfo> listOf(@Nullable Collection<Artist> spotifyArtists) {
        if (spotifyArtists==null) return new ArrayList<>();
        ArrayList<ArtistInfo> artists = new ArrayList<>(spotifyArtists.size());
        for (Artist x : spotifyArtists) {
            artists.add(valueOf(x));
        }
        return artists;
    }

    @NonNull
    public static ArtistInfo valueOf(@NonNull Artist spotifyArtist) {
        return new ArtistInfo(spotifyArtist);
    }

    private ArtistInfo(Artist spotifyArtist) {
        id = spotifyArtist.id;
        name = spotifyArtist.name;
        final String url = Util.getImageUrl(spotifyArtist.images);
        smallImageUrl = (url==null) ? "" : url;
    }
}
