package com.example.vikrach.simplespeedometer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Created by vikrach on 4/3/17.
 */

public class Preferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    EditTextPreference setSpeed;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        addPreferencesFromResource(R.xml.preferences);


        setSpeed = (EditTextPreference) findPreference("set_speed_preference");
    }
        @Override
        protected void onResume() {
            super.onResume();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            // Set up a listener whenever a key changes
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if("set_speed_preference".equals(key)) {
            if((sharedPreferences.getString("set_speed_preference", "")).equals("") ){
                sharedPreferences.edit().putString(key, "5").commit();
            }
            int val = Integer.valueOf(sharedPreferences.getString("set_speed_preference", ""));
            if(val > 20)
                sharedPreferences.edit().putString(key,"20").commit();
            else if(val < 1)
                sharedPreferences.edit().putString(key,"1").commit();
        }
    }
}
