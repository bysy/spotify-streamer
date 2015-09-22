package com.github.bysy.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.bysy.spotifystreamer.data.ArtistInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A retained {@link Fragment} subclass to hold state associated with artist search.
 */
public class ArtistSearchStateFragment extends Fragment {
    private static final String FRAGMENT_TAG = "ARTIST_SEARCH_STATE_FRAGMENT_TAG";
    private String mLastSearch;
    private List<ArtistInfo> mArtists = new ArrayList<>();

    static ArtistSearchStateFragment getAssociatedInstance(FragmentActivity activity) {
        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (fragment==null) {
            fragment = new ArtistSearchStateFragment();
            fragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
        }
        return (ArtistSearchStateFragment) fragment;
    }

    public ArtistSearchStateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    public String getLastSearch() {
        return mLastSearch;
    }

    public void setLastSearch(String lastSearch) {
        mLastSearch = lastSearch;
    }

    public List<ArtistInfo> getArtists() {
        return mArtists;
    }

    public void setArtists(ArrayList<ArtistInfo> artists) {
        mArtists = artists;
    }

    public void addArtists(ArrayList<ArtistInfo> newArtists) {
        mArtists.addAll(newArtists);
    }
}
