package com.example.vikrach.simplespeedometer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Created by vikrach on 4/3/17.
 */

public class Preferences extends PreferenceActivity {

    EditTextPreference setSpeed;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        addPreferencesFromResource(R.xml.preferences);


        setSpeed = (EditTextPreference) findPreference("set_speed_preference");


        @Override
        protected void onResume() {
            super.onResume();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            // Set up a listener whenever a key changes
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }

    }


}
