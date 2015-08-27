/*
 * Copyright (c) 2015. bysy@users.noreply.github.com
 */

package com.github.bysy.spotifystreamer;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.android.supportv7.app.AppCompatPreferenceActivity;


/**
 * Settings.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    @SuppressWarnings("deprecation")  // use deprecated methods because we are targeting API 10
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // I really didn't like the black checkbox on an already
        // dark background. Where was my accent color? Bumping the min Sdk
        // just for a nicer settings screen didn't seem worth it. It turns
        // out we can display any activity with material-ish styling thanks
        // to AppCompatDelegate. The sample AppCompatPreferenceActivity
        // implementation is just what we need.
        // As an added complication, the most recent compat library
        // will not allow a theme with ActionBar in conjunction with
        // AppCompatDelegate. So if we want an action bar, nice styling,
        // and backwards compatibility, we need to add our own toolbar.
        // Sources consulted: Chris Bane's blog and SO answers
        // provided the important ideas.
        setContentView(R.layout.activity_settings);
        addPreferencesFromResource(R.xml.pref_general);
        View actionBarView = findViewById(R.id.action_bar);
        if (actionBarView!=null && actionBarView instanceof Toolbar) {
            setSupportActionBar((Toolbar) actionBarView);
        }
        Preference localeList = findPreference(getString(R.string.pref_locale_list_preference_key));
        localeList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String stringValue = newValue.toString();
                updateCustomEnabled(stringValue);
                return sUpdateSummaryListener.onPreferenceChange(preference, newValue);
            }
        });
        updateSummary(localeList);
        String customValue = PreferenceManager.getDefaultSharedPreferences(this).getString(
                localeList.getKey(),
                getString(R.string.pref_locale_default_value));
        updateCustomEnabled(customValue);
        bindGenericSummaryToValue(findPreference(getString(R.string.pref_locale_key)));
    }

    @SuppressWarnings("deprecation")
    private void updateCustomEnabled(String stringValue) {
        final Preference customLocale = findPreference(getString(R.string.pref_locale_key));
        if (stringValue.equals(getString(R.string.pref_locale_custom_value))) {
            customLocale.setEnabled(true);
        } else {
            customLocale.setEnabled(false);
        }
    }

    // Rest of file based on trimmed-down AndroidStudio-generated settings activity.

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sUpdateSummaryListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sUpdateSummaryListener
     */
    private static void bindGenericSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sUpdateSummaryListener);

        // Trigger the listener immediately with the preference's
        // current value.
        updateSummary(preference);
    }

    private static void updateSummary(Preference preference) {
        sUpdateSummaryListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}
