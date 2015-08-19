/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import kaaes.spotify.webapi.android.models.Image;


/**
 * Utilities.
 */
public class Util {
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

    @Nullable
    public static String getLargeImageUrl(@NonNull List<Image> images) {
        if (images.isEmpty()) return null;
        final String url = images.get(0).url;
        if (!Patterns.WEB_URL.matcher(url).matches()) return null;
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

    /** Load image at imageUrl into imageView and set background color appropriately. */
    static void loadImageInto(@NonNull Context context,
                              @Nullable String imageUrl, @NonNull ImageView imageView) {
        final Resources resources = context.getResources();
        final int loadingColorId = resources.getColor(R.color.loading);
        final int unavailableColorId = resources.getColor(R.color.unavailable);
        final int questionMarkId = android.R.drawable.ic_menu_help;

        if (imageUrl!=null) {
            imageView.setBackgroundColor(loadingColorId);
            Picasso.with(context)
                    .load(imageUrl) //.centerCrop().resize(128,128).onlyScaleDown()
                    .error(unavailableColorId).into(imageView);
        } else {
            imageView.setBackgroundColor(unavailableColorId);
            Picasso.with(context).load(questionMarkId).into(imageView);
        }
    }
}
