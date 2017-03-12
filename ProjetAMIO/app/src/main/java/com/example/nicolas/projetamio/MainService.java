package com.example.nicolas.projetamio;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {
    Timer timer;
    TimerTask timerTask;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;


    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MainService","Service started");
        timer = new Timer();
        startTimer();
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("Alerte lumiere");
        mBuilder.setContentText("Une salle est allumée");

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        return START_STICKY;
    }

    public void startTimer() {
        //set a new Timer
        Timer timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first 10s the TimerTask will run every 20min
        timer.schedule(timerTask, 1000, 10000);
    }
    public void stopTimertask() {
        if (timer != null) {

            timer.cancel();
            timer.purge();

        }

    }
   // public interface VariableChangeListener {
      //   void onVariableChanged(JSONObject data);
    //}

    //public void setVariableChangeListener(VariableChangeListener variableChangeListener) {
      //  m = variableChangeListener;
    //}

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("MainService", "Timer ticked");



                        new AsyncConnectTask().execute(); // remplis le result

                       // try {
                        //    new ParseJSON().execute(MainActivity.result);
                          //  new CheckChangementBrusque().execute(); // check si une notif doit etre envoyée et l'envoie
                        //} catch (IOException e) {
                          //  e.printStackTrace();
                       // }
                        //Log.d("MainService", "Data retrieved");
                    }


        };
    }

    private class CheckChangementBrusque extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {

        if (MainActivity.datalist.size() > 20) {
            Log.d("MainService","CheckChgmntBrusque : Datalist size > 20, entrée dans le if");
            Float buf1;
            Float buf2;
            Boolean[] changes = new Boolean[5];
            Arrays.fill(changes, false);    // fill changes avec des false
            String buffer = "";

            for (int i = MainActivity.datalist.size() - 5; i < MainActivity.datalist.size(); i++) {
                int j=0;

                buffer = MainActivity.datalist.get(i - 5).get("value"); // check la valeur i-5
                buf1 = Float.parseFloat(buffer);
                buffer = MainActivity.datalist.get(i).get("value");
                buf2 = Float.parseFloat(buffer); // check la valeur i
                buffer="";
                if (Math.abs(buf1 - buf2) > 150) {  // check for changes between state n and n-1
                    changes[j] = true;
                }
                j++;
            }

            for (int j = 0; j < changes.length; j++) {  // check if one or more changes happened
                if (changes[j]) {
                    buffer += MainActivity.datalist.get(MainActivity.datalist.size() - j).get("mote") + ", ";
                }
            }
            if (!buffer.isEmpty() && buffer.length()>2) {    // buffer is not empty so a change has occured
                Log.d("MainService","CheckChgmntBrusque : buffer not empty, changements");
                if (timeOK()) { // check si on est entre 19 et 23h
                    buffer = buffer.substring(0,buffer.length()-2); // enleve le dernier ", " (2 char)
                    // modif de la notif pour correspondre et afficher les motes
                    mBuilder.setContentTitle("Alerte lumiere");
                    mBuilder.setContentText("Le(s) mote(s) "+buffer+" ont notifies un changement brusque");
                    mNotificationManager.notify(1, mBuilder.build()); // send notif
                }
            }
        }


        return null;}

        @Override
        protected void onPostExecute (Void v) {
            super.onPostExecute(v);
            Log.d("MainService","Sortie de CheckChgmntBrusque");
        }

    }

    private boolean timeOK() {
        try {
            String string1 = "19:00:00";
            Date time1 = new SimpleDateFormat("HH:mm:ss").parse(string1);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);

            String string2 = "23:00:00";
            Date time2 = new SimpleDateFormat("HH:mm:ss").parse(string2);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(time2);
            calendar2.add(Calendar.DATE, 1);

            Calendar calendar3 = Calendar.getInstance();
            calendar3.add(Calendar.DATE, 1);

            Date x = calendar3.getTime();
            if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
                //checks whether the current time is between 19:00:00 and 23:00:00.
                return true;
            }
            else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
    Connecte a l'URL et retourne les infos en String
     */
    private class AsyncConnectTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute (Void v) {
            super.onPostExecute(v);

            new ParseJSON().execute(MainActivity.result);

            Log.d("MainService", "Sortie de AsyncConnect");

        }

        @Override
        protected Void doInBackground(Void... params) {
            URL url = null;
            MainActivity.result = "";
            try {
                url = new URL("http://iotlab.telecomnancy.eu/rest/data/1/light1/last");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("MainService","Error : Malformed URL");
            }
            HttpURLConnection urlConnection = null;
            int responseCode = 0;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                responseCode = urlConnection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("MainService", "Error : Connection failed");
            }
            Log.d("MainService", "HTML Code = " + responseCode);
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
                Log.d("MainService","Error : Reading failed");
            } finally {
                urlConnection.disconnect();
                Log.d("MainService", "Done");
            }
            return null;
        }





    }

    private class ParseJSON extends AsyncTask<String, Void, Void> {


        @Override
        protected void onPostExecute (Void v) {
            super.onPostExecute(v);
            new CheckChangementBrusque().execute(); // check si une notif doit etre envoyée et l'envoie
            Log.d("MainService","Sortie de ParseJSON");

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                MainActivity.jsonObject = new JSONObject(params[0]);
                JSONArray data = MainActivity.jsonObject.getJSONArray("data");

                for (int j = 0; j < data.length(); j++) {
                    JSONObject m = data.getJSONObject(j);
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
            return null;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stop the timer, if it's not already null
        stopTimertask();

        timer.cancel();
        timer.purge();

        Log.d("MainService","Timer destroyed");
        Log.d("MainService","Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
