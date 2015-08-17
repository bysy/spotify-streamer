/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Patterns;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import kaaes.spotify.webapi.android.models.Image;


/**
 * Utilities.
 */
class Util {
    /** Return the two character ISO code for user's preferred locale or "US" if not available. */
    static String getCountryCode() {
        String res = Locale.getDefault().getCountry();
        if (res.isEmpty()) {
            res = Locale.US.getCountry();
        }
        return res;
    }

    /** Return the Url of the smallest usable image. */
    @Nullable
    static String getImageUrl(@NonNull List<Image> images) {
        if (images.isEmpty()) return null;
        final String url = findSmallImage(images).url;
        // http://stackoverflow.com/questions/5617749/how-to-validate-a-url-website-name-in-edittext-in-android
        if (!Patterns.WEB_URL.matcher(url).matches()) return null;  // rare
        return url;
    }

    /** Return the smallest usable image. */
    private static Image findSmallImage(@NonNull List<Image> images) {
        // NOTE: Spotify's docs say the first image is always the widest but don't specify
        // any further ordering. But in practice, they're sorted by descending size and the
        // last image is still plenty big enough.
        return images.get(images.size()-1);
    }

    static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
