package com.github.bysy.spotifystreamer;

import android.content.Context;
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
    /** Get the two character ISO code for user's preferred locale or "US" if not available. */
    static String getCountryCode() {
        String res = Locale.getDefault().getCountry();
        if (res.isEmpty()) {
            res = Locale.US.getCountry();
        }
        return res;
    }

    @Nullable
    static String getImageUrl(List<Image> images) {
        if (images.isEmpty()) return null;
        String url = images.get(0).url;
        // http://stackoverflow.com/questions/5617749/how-to-validate-a-url-website-name-in-edittext-in-android
        if (!Patterns.WEB_URL.matcher(url).matches()) return null;
        return url;
    }

    static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
