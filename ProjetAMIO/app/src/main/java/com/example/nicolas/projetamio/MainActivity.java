package com.example.nicolas.projetamio;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
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
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    static String prefEmail = "";
    TextView textView;
    ToggleButton button;
    CheckBox checkBox;
    static String result = "";
    static JSONObject jsonObject = null;
    ArrayList<HashMap<String, String>> datalist = new ArrayList<>();
    String allume = "";
    static String stringLastAlert="Pas d'alerte récente \nenregistrée";
    static TextView textViewLastA;

    final String m1 = "Salle 2.10";
    final String m2 ="Salle 2.08";
    final String m3 ="Salle 2.09";
    final String m4="Salle 2.06";
    final String m5="Salle 2.05 ";
    String bufferAll = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("MainActivity", "Création de l'activité");
        new AsyncConnectTask().execute(); // Connexion au service pour avoir une valeur a traiter



        /*
        Floating action button
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",prefEmail, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bug/Aide sur l'application");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Bonjour,");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
//                Snackbar.make(view, "fais ton action", Snackbar.LENGTH_LONG)
//                        .setAction("close", null).show();

            }
        });

        /*
        Get values button
         */
        Button button1 = (Button) findViewById(R.id.GV1);
        button1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Envoi de la requete au WebService");
                new AsyncConnectTask().execute(); // remplis le result
                //try {
                //    this.wait(100); // evite de lancer le parseur JSON avant que le String soit rempli
                // } catch (InterruptedException e) {
                //   e.printStackTrace();
                //}
                try {
                    parseJSON(result);
                } catch (IOException e) {
                    Log.e("MainActivity","Error parsing the JSON File");
                    e.printStackTrace();
                }
                bufferAll="";
                String buffer ="";

                // textView associé au get values
                textView = (TextView) findViewById(R.id.TV4);
                textView.setText("");
                if (datalist != null) {
                    if (datalist.size() > 6) {
                        SpannableStringBuilder builder = new SpannableStringBuilder(); // string formatting

                        for (int i = datalist.size() - 5; i < datalist.size(); i++) {
                          if (Float.parseFloat(datalist.get(datalist.size() - i).get("value"))>250) {
                              allume="ALLUME";
                          }
                          else  {
                              allume="ETEINT";
                          }
                          /*
                            Lie chaque mote a sa salle
                           */
                            buffer = datalist.get(datalist.size() - i).get("mote");
                            if (buffer.equals("9.138")) {
                                bufferAll=m1;
                            }else if (buffer.equals("81.77")) {
                                bufferAll=m2;
                            }else if (buffer.equals("153.111")) {
                                bufferAll=m3;
                            }else if (buffer.equals("53.105")) {
                                bufferAll=m4;
                            }else if (buffer.equals("77.106")) {
                                bufferAll=m5;
                            } else {
                                Log.e("MainService", "CheckChangementBrusque - Mote " + buffer + " non reconnu");
                                return;
                            }
                            // remplis le TextView pour afficher les values
                            textView.setText(textView.getText() + bufferAll +"\t"+ allume + "\n");
                            textView.setTextSize(16);
                      }
                    }
                }
            }
        });

        /*
        Bouton de lancement du service
         */
        button = (ToggleButton) findViewById(R.id.Btn1);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    Log.d("MainActivity", "Création et démarrage du MainService");
                    startService(new Intent(MainActivity.this, MainService.class));
                    textView = (TextView) findViewById(R.id.TV2);
                    textView.setText("En cours");
                } else {
                    stopService(new Intent(MainActivity.this, MainService.class));
                    Log.d("MainActivity", "Service stopped");
                    textView = (TextView) findViewById(R.id.TV2);
                    textView.setText("Arrete");
                }
            }
        });

        /*
        TextView du Last Alert
         */
        textViewLastA = (TextView) findViewById(R.id.TV6);
        textViewLastA.setText(stringLastAlert);


        /*
        CheckBox du Start at boot
         */
        checkBox = (CheckBox) findViewById(R.id.startAtBootChkb);
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE); // Accede aux sharedprefs
        Boolean bool = sharedPref.getBoolean("startAtBoot", false);
        if (bool == true) {
            checkBox.setChecked(true); // syncronise l'etat du Checkbox avec les prefs actuelles
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("MainActivity", "Start at boot checked");
                    SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("startAtBoot", true);
                    editor.commit();
                } else {
                    Log.d("MainActivity", "Start at boot unchecked");
                    SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("startAtBoot", false);
                    editor.commit();
                }
            }
        });

    }

    /*
    Connecte a l'URL et retourne les infos en String
     */
    private class AsyncConnectTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            URL url;
            result = "";
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
                        result += line;
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
            jsonObject = new JSONObject(r);
            JSONArray data = jsonObject.getJSONArray("data");

            for (int j = 0; j < data.length() ; j++) {
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
                Log.d("MainActivity","Datum added to datalist");

            }

        } catch (final JSONException e) {
            Log.e("jsonparser", "json parsing error: " + e.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(getApplicationContext(),
//                            "Json parsing error: " + e.getMessage(),
                          "Veuillez recliquer dans 3 secondes svp", Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MainService.class));// A enlever ?
        Log.d("MainActivity", "MainService destroyed");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, Settings.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }
}
