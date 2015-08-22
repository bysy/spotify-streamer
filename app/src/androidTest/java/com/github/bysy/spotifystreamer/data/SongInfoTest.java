/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer.data;

import android.os.Parcel;
import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Test SongInfo.
 */
public class SongInfoTest extends AndroidTestCase {
    private static ArtistSimple exampleArtist;
    private static Track exampleTrack;
    private static AlbumSimple exampleAlbum;
    static {
        exampleArtist = new ArtistSimple();
        exampleArtist.name = "Elvis Presley";
        exampleArtist.external_urls = Collections.emptyMap();
        exampleArtist.href = "https://en.wikipedia.org/wiki/Elvis_Presley";
        exampleArtist.id = "artist1234567";
        exampleArtist.type = "type";
        exampleArtist.uri = "uri";

        exampleAlbum = new AlbumSimple();
        exampleAlbum.name = "Album name";
        exampleAlbum.images = Collections.emptyList();

        exampleTrack = new Track();
        exampleTrack.name = "Song name";
        exampleTrack.album = exampleAlbum;
        List<ArtistSimple> artists = new ArrayList<>();
        artists.add(exampleArtist);
        exampleTrack.artists = artists;
        exampleTrack.preview_url = "http://preview_url";
        exampleTrack.external_ids = Collections.emptyMap();
        exampleTrack.popularity = 100;
        exampleTrack.id = "track1234567";
        exampleTrack.external_urls = new HashMap<>();
        exampleTrack.external_urls.put("spotify", "http://external_url");
    }

    public void testParcelable() {
        Parcel parcel = Parcel.obtain();
        final SongInfo inSong = SongInfo.valueOf(exampleTrack);
        inSong.writeToParcel(parcel, 0);
        // Reset the data position so that reading begins from the start when recreating.
        // https://developer.android.com/training/testing/unit-testing/instrumented-unit-tests.html#build
        parcel.setDataPosition(0);
        SongInfo outSong = SongInfo.CREATOR.createFromParcel(parcel);

        assertTrue("Instance recreated from parcel differs from original", inSong.equals(outSong));
    }
}
