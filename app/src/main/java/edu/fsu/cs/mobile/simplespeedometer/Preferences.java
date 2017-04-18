package edu.fsu.cs.mobile.simplespeedometer;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;



public class Preferences extends PreferenceActivity  {

    EditTextPreference setSpeed;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        addPreferencesFromResource(R.xml.preferences);

    }
}