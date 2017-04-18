package com.example.vikrach.simplespeedometer;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

/**
 * Created by vikrach on 4/3/17.
 */

public class Preferences extends PreferenceActivity  {

    EditTextPreference setSpeed;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        addPreferencesFromResource(R.xml.preferences);

    }
}
