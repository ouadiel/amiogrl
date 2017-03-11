package com.example.nicolas.projetamio;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    TextView textView;
    ToggleButton button;
    CheckBox checkBox;
    static String result = "";
    static JSONObject jsonObject = null;
    static ArrayList<HashMap<String, String>> datalist = new ArrayList<>();
    String buffer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("MainActivity", "Création de l'activité");


        new AsyncLogTask().execute();// AsyncLog

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


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
                    e.printStackTrace();
                }
                textView = (TextView) findViewById(R.id.TV4);
                textView.setText("");
                if (datalist != null) {
                    if (datalist.size() > 6) {


                      for (int i = datalist.size() - 5; i < datalist.size(); i++) {
                           textView.setText(textView.getText() + datalist.get(datalist.size() - i).get("mote") + " " + datalist.get(datalist.size() - i).get("value") + "\n");
                          //   textView.setText(textView.getText()+datalist.get(datalist.size()-1).get("value")+"\n");
                        }
                    }
                }

                //   try {
                //   parseJSON(result);
                // textView = (TextView) findViewById(R.id.TV4);
                //  textView.setText(jsonObject.getString("value"));
                //  } catch (IOException e) {
                //    e.printStackTrace();
                //} catch (JSONException e) {
                //    e.printStackTrace();
                //  }
            }
        });
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

        // CheckBox du Start at boot
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

    private class AsyncLogTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            Log.d("MainActivity", "AsyncLog");
            return 0;
        }
    }

    /*
    Connecte a l'URL et retourne les infos en String
     */
    private class AsyncConnectTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            URL url = null;
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


            }

        } catch (final JSONException e) {
            Log.e("jsonparser", "json parsing error: " + e.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(getApplicationContext(),
                            "Json parsing error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
