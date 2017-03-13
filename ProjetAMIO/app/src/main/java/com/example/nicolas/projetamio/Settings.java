package com.example.nicolas.projetamio;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Date;

public class Settings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_preference);
        //noinspection deprecation
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
//
//        StringBuilder builder = new StringBuilder();
//
//        builder.append("\n Email"
//        + prefs.getString(MainActivity.prefEmail,"null"));

        prefs.edit().putString(MainActivity.prefEmail,"").commit();

        Log.d("Settings","ça marche ?");

    }


}
