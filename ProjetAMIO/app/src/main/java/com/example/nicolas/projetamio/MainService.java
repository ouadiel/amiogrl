package com.example.nicolas.projetamio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {
    static int count_notif = 1;
    Timer timer;
    TimerTask timerTask;
    int timerTime = 10000;
    static NotificationCompat.Builder mBuilder;
    static NotificationManager mNotificationManager;
    ArrayList<HashMap<String, String>> datalist = new ArrayList<>();
    String stringWEFinEmail = "";
    String stringSemaineDebutNotif = "";
    String stringSemaineFinNotif = "";
    String stringWEDebutEmail = "";
    String stringSemaineDebutEmail="";
    String stringSemaineFinEmail="";
    String stringMinuit = "00:00:00";
    Date timeMinuit;
    Date timeWEFinEmail;
    Date timeSemaineDebutNotif;
    Date timeSemaineFinNotif;
    Date timeWEDebutEmail;
    Date timeSemaineDebutEmail;
    Date timeSemaineFinEmail;
    Date currentTime;
    Calendar calendarMinuit;
    Calendar calendarSemaineDebutNotif;
    Calendar calendarSemaineFinNotif;
    Calendar calendarWEFinEmail;
    Calendar calendarWEDebutEmail;
    Calendar calendarSemaineDebutEmail;
    Calendar calendarSemaineFinEmail;
    Calendar calendarNow;

    final String m1 = "Salle 2.10";
    final String m2 ="Salle 2.08";
    final String m3 ="Salle 2.09";
    final String m4="Salle 2.06";
    final String m5="Salle 2.05 ";
    String bufferAll = "";
    String lastAlert="Pas d'alerte récente \nenregistrée";

    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MainService", "Service started");
        try {
            // TODO : Prendre en compte les parametres (email, horaires et timer)
            /*
            Valeurs par défauts
             */
            stringSemaineDebutNotif = "19:00:00";
            stringSemaineFinNotif = "23:00:00";
            stringSemaineDebutEmail=stringSemaineFinNotif;
            stringSemaineFinEmail="06:00:00";
            stringWEDebutEmail =stringSemaineDebutNotif;
            stringWEFinEmail =stringSemaineFinNotif;

            timeSemaineDebutNotif = new SimpleDateFormat("HH:mm:ss").parse(stringSemaineDebutNotif);
            calendarSemaineDebutNotif = Calendar.getInstance();
            calendarSemaineDebutNotif.setTime(timeSemaineDebutNotif);

            timeSemaineFinNotif = new SimpleDateFormat("HH:mm:ss").parse(stringSemaineFinNotif);
            calendarSemaineFinNotif = Calendar.getInstance();
            calendarSemaineFinNotif.setTime(timeSemaineFinNotif);
            calendarSemaineFinNotif.add(Calendar.DATE, 1);

            timeSemaineDebutEmail = new SimpleDateFormat("HH:mm:ss").parse(stringSemaineDebutEmail);
            calendarSemaineDebutEmail = Calendar.getInstance();
            calendarSemaineDebutEmail.setTime(timeSemaineDebutEmail);
            calendarSemaineDebutEmail.add(Calendar.DATE,1);

            timeSemaineFinEmail = new SimpleDateFormat("HH:mm:ss").parse(stringSemaineFinEmail);
            calendarSemaineFinEmail = Calendar.getInstance();
            calendarSemaineFinEmail.setTime(timeSemaineFinEmail);
            calendarSemaineFinEmail.add(Calendar.DATE,1);

            timeMinuit = new SimpleDateFormat("HH:mm:ss").parse(stringMinuit);
            calendarMinuit = Calendar.getInstance();
            calendarMinuit.setTime(timeMinuit);
            calendarMinuit.add(Calendar.DATE,1);

            timeWEFinEmail = new SimpleDateFormat("HH:mm:ss").parse(stringWEFinEmail);
            calendarWEFinEmail = Calendar.getInstance();
            calendarWEFinEmail.setTime(timeWEFinEmail);
            calendarWEFinEmail.add(Calendar.DATE,1);

            timeWEDebutEmail = new SimpleDateFormat("HH:mm:ss").parse(stringWEDebutEmail);
            calendarWEDebutEmail = Calendar.getInstance();
            calendarWEDebutEmail.setTime(timeWEFinEmail);
            calendarWEDebutEmail.add(Calendar.DATE,1);

        } catch (ParseException e) {
            Log.e("MainService", "Error parsing the date parameters, using default params...");
            e.printStackTrace();
        }
        timer = new Timer();
        startTimer();
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("Alerte lumiere");
        mBuilder.setContentText("Une salle est allumée");
        mBuilder.setVibrate(new long[] { 100,1000,100,1000 });
        mBuilder.setLights(Color.CYAN, 100, 100);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);

        mBuilder.setAutoCancel(true);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
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
        timer.schedule(timerTask, 1000, timerTime);
    }

    public void stopTimertask() {
        if (timer != null) {

            timer.cancel();
            timer.purge();

        }

    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("MainService", "Timer ticked");
                new AsyncConnectTask().execute(); // remplis le result
            }
        };
    }

    private class CheckChangementBrusque extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            if (datalist.size() > 20) {
                Log.d("MainService", "CheckChgmntBrusque : Datalist size > 20, entrée dans le if");
                Float buf1;
                Float buf2;
                Boolean[] changes = new Boolean[5];
                Arrays.fill(changes, false);    // fill changes avec des false
                String buffer="";
                bufferAll="";

                for (int i = datalist.size() - 5; i < datalist.size(); i++) {
                    int j = 0;

                    bufferAll = datalist.get(i - 5).get("value"); // check la valeur i-5
                    buf1 = Float.parseFloat(bufferAll);
                    bufferAll = datalist.get(i).get("value");
                    buf2 = Float.parseFloat(bufferAll); // check la valeur i
                    bufferAll = "";
                    if (Math.abs(buf1 - buf2) > 150) {  // check for changes between state n and n-1
                        changes[j] = true;
                    }
                    j++;
                }

                for (int j = 0; j < changes.length; j++) {  // check if one or more changes happened
                    if (changes[j]) {
                        buffer = datalist.get(datalist.size() - j).get("mote");
                        if (buffer.equals("9.138")) {
                            bufferAll+=m1+", ";
                        }
                        else if (buffer.equals("81.77")) {
                            bufferAll+=m2+", ";
                        }
                        else if (buffer.equals("153.111")) {
                            bufferAll+=m3+", ";
                        }else if (buffer.equals("53.105")) {
                            bufferAll+=m4+", ";
                        }else if (buffer.equals("77.106")) {
                            bufferAll+=m5+", ";
                        } else {
                            Log.e("MainService","CheckChangementBrusque - Mote "+buffer+" non reconnu");
                            return null;
                        }
                    }
                }
                if (!bufferAll.isEmpty() && bufferAll.length() > 2) {    // bufferAll is not empty so a change has occured
                    if (timeNotif()) { // check si on est entre 19 et 23h et en semaine
                        bufferAll = bufferAll.substring(0, bufferAll.length() - 2); // enleve le dernier ", " (2 char)
                        // modif de la notif pour correspondre et afficher les motes
                        Log.d("MainService","CheckChgmntBrusque : bufferAll not empty, changements pour les motes "+ bufferAll+ "et notification");
                        mBuilder.setContentTitle("Alerte lumiere");
                        mBuilder.setContentText("Le(s) salle(s) " + bufferAll + " ont notifiees un changement brusque de luminisote.");
                        mNotificationManager.notify(count_notif, mBuilder.build()); // send notif
                        lastAlertRefresh();
                        count_notif++;
                    }
                    else {
                        timeEmail();
                    }
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Log.d("MainService", "Sortie de CheckChgmntBrusque");
        }

    }

    private boolean timeNotif() { // check if time is between 19 and 23h and day is in weekdays.
        calendarNow = Calendar.getInstance();
        if (calendarNow.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendarNow.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return false;
        }
        Date x = calendarNow.getTime();
        if (x.after(calendarSemaineDebutNotif.getTime()) && x.before(calendarSemaineFinNotif.getTime())) {
                //checks whether the current time is between 19:00:00 and 23:00:00.
                return true;
            }
        return false;
    }

    private boolean timeEmail() { // check if time is between 19 and 23h and day is in weekend OR if time is between 23 and 6h in week.
            calendarNow = Calendar.getInstance();
            calendarNow.add(Calendar.DATE, 1);
            currentTime = calendarNow.getTime();

            if (calendarNow.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendarNow.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) { // week days
                if (currentTime.after(calendarSemaineDebutEmail.getTime()) && currentTime.before(calendarSemaineFinEmail.getTime())) {
                    sendMail("Le(s) salle(s) " + bufferAll + " ont notifiees un changement brusque de luminisote.");
                    lastAlertRefresh();
                    return true;
                }
            }
            else { // weekend
                if ((currentTime.after(calendarWEDebutEmail.getTime()) && currentTime.before(calendarMinuit.getTime())) ||
                        (currentTime.after(calendarMinuit.getTime()) && currentTime.before(calendarWEFinEmail.getTime()))) { //checks whether the current time is between 23 and 6
                    sendMail("Le(s) salle(s) " + bufferAll + " ont notifiees un changement brusque de luminisote.");
                    lastAlertRefresh();
                    return true;
                }
            }
            return false;
    }

    private void lastAlertRefresh() {
        calendarNow.getInstance();
        lastAlert=calendarNow.getTime().toString();
        MainActivity.textViewLastA.setText(lastAlert);
    }
    
    private void sendMail(String content) {
        // TODO : utiliser le string global
        /*
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE); // Accede aux sharedprefs
        String str = sharedPref.getString("email","test@gmail.com");
        */
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","nicolas.rigal@telecomnancy.net", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[LUMIO] Notification Lumieres Ecole");
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
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
                Log.e("MainService","Error : Malformed URL");
            }
            HttpURLConnection urlConnection = null;
            int responseCode = 0;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                responseCode = urlConnection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("MainService", "Error : Connection failed");
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
                Log.e("MainService","Error : Reading failed");
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


                    datalist.add(datum);
                    Log.d("MainService", "Datum added to datalist");

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
