/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.app.Fragment;
import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Help with safe image loading into ListView items.
 * TODO: Refactor to static method and remove this class.
 */
public class ImageListViewFragment extends Fragment {
    /** Load image at imageUrl into imageView and set background color appropriately. */
    void loadImageInto(@Nullable String imageUrl, ImageView imageView) {
        final int loadingColorId = getResources().getColor(R.color.loading);
        final int unavailableColorId = getResources().getColor(R.color.unavailable);
        final int questionMarkId = android.R.drawable.ic_menu_help;
        final Context context = getActivity();

        if (imageUrl!=null) {
            imageView.setBackgroundColor(loadingColorId);
            Picasso.with(context)
                    .load(imageUrl).centerCrop().resize(128,128).onlyScaleDown()
                    .error(unavailableColorId).into(imageView);
        } else {
            imageView.setBackgroundColor(unavailableColorId);
            Picasso.with(context).load(questionMarkId).into(imageView);
        }
    }
}
