package com.craigcleveland.popularmovies;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences from preferences XML
        addPreferencesFromResource(R.xml.preferences);
    }

}
