package com.example.nicolas.projetamio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by nicolas on 08/02/2017.
 */

public class MyBootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MyBootBroadcastReceiver","Checking the preferences");
        SharedPreferences sharedPref = context.getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
        // Check shared pref and act accordingly
        Boolean daPref = sharedPref.getBoolean("startAtBoot",false);
        if (daPref==true) {
            Log.d("MainActivity","Création et démarrage du MainService");
            Intent i = new Intent("com.example.nicolas.projetamio.MainService");
            i.setClass(context, MainService.class);
            context.startService(i);
        }
    }
}
