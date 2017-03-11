package com.example.nicolas.projetamio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {
    Timer timer;
    TimerTask timerTask;
    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MainService","Service started");
        timer = new Timer();
        startTimer();
        return START_STICKY;
    }

    public void startTimer() {
        //set a new Timer
        Timer timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first 10s the TimerTask will run every 20min
        timer.schedule(timerTask, 10000, 1200000);
    }
    public void stopTimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        new AsyncConnectTask().execute(); // remplis le result
                        try {
                            this.wait(100); // evite de lancer le parseur JSON avant que le String soit rempli
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            parseJSON(MainActivity.result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("MainService", "Data retrieved (Timer ticked)");
                    }
                };
            }
        };
    }

    /*
    Connecte a l'URL et retourne les infos en String
     */
    private class AsyncConnectTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            URL url = null;
            MainActivity.result = "";
            try {
                url = new URL("http://iotlab.telecomnancy.eu/rest/data/1/light1/last");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error : Malformed URL";
            }
            HttpURLConnection urlConnection = null;
            int responseCode;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                responseCode = urlConnection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
                return "Error : Connection failed";
            }
            Log.d("MainActivity-AsyncCo", "HTML Code = " + responseCode);
            if (responseCode != 200) {
                Context context = getApplicationContext();
                CharSequence text = "HTML response code = " + responseCode;
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                if (in != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null)
                        MainActivity.result += line;
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                return "Error : Reading failed";
            } finally {
                urlConnection.disconnect();
                return "Done";
            }
        }
    }

    protected void parseJSON(String r) throws IOException {
        try {
            MainActivity.jsonObject = new JSONObject(r);
            JSONArray data =MainActivity.jsonObject.getJSONArray("data");

            for (int i=0;i<data.length()-1;i++) {
                JSONObject m = data.getJSONObject(i);
                String timestamp = m.getString("timestamp");
                String label = m.getString("label");
                String value = m.getString("value");
                String mote = m.getString("mote");


                HashMap<String, String> datum = new HashMap<>();

                datum.put("timestamp", timestamp);
                datum.put("label", label);

                datum.put("value", value);
                datum.put("mote", mote);


                MainActivity.datalist.add(datum);


            }

        } catch (final JSONException e) {
            Log.e("jsonparser", "json parsing error: " + e.getMessage());
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stop the timer, if it's not already null
        stopTimertask();
        Log.d("MainService","Timer destroyed");
        Log.d("MainService","Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
