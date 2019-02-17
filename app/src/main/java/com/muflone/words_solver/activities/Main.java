package com.muflone.words_solver.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.base.Joiner;
import com.muflone.words_solver.Itertools;
import com.muflone.words_solver.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends AppCompatActivity {
    EditText text_letters;
    EditText text_length;
    ListView list_solutions;
    final ArrayList<String> list_items = new ArrayList<String>();
    ArrayAdapter adapter_list;
    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize_ui();

        adapter_list = new ArrayAdapter(this,
            android.R.layout.simple_list_item_1, list_items );
        list_solutions.setAdapter(adapter_list);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        /* Allow blocking calls from network in the UI thread */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void initialize_ui()
    {
        text_letters = findViewById(R.id.text_letters);
        text_length = findViewById(R.id.text_length);
        list_solutions = findViewById(R.id.list_solutions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }


    public void button_solve_onClick(View view)
    {
        boolean online_backend = sharedPrefs.getBoolean("online_backend", true);
        String letters = text_letters.getText().toString().toUpperCase();
        int minimal_length = Integer.parseInt(text_length.getText().toString());

        /* To see the TimingLogger results you have to enable the debug using:
           adb -s EMULATOR-xxxx shell setprop words_solver VERBOSE
         */
        TimingLogger timings = new TimingLogger("words_solver",
                                                "button_solve_onClick");

        /* Hide virtual keyboard */
        InputMethodManager inputManager = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                             InputMethodManager.HIDE_NOT_ALWAYS);
        /* Clear results list */
        list_items.clear();
        adapter_list.notifyDataSetChanged();

        /* Too few letters */
        if (letters.length() < minimal_length | minimal_length < 3)
            return;

        timings.addSplit("Starting computation");
        if (online_backend)
        {
            URLConnection connection = null;
            BufferedReader bufferedReader = null;
            StringBuffer jsonStringBuffer = new StringBuffer();
            try {
                URL url = new URL("http://words-solver.apps.muflone.com/matches/" +
                                  "1/222/" +
                                  "italian" + "/" +
                                  letters.toUpperCase() + "/" +
                                  String.format("%02d", minimal_length) + "/");
                connection = url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                /* Save all the received text in jsonStringBuffer */
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    jsonStringBuffer.append(line);
                }

                Log.d("words_solver", jsonStringBuffer.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            timings.addSplit("Online backend: network transfer");

            try {
                /* Convert the jsonStringBuffer in a JSON array to extract items */
                JSONObject jsonObject = new JSONObject(jsonStringBuffer.toString());
                JSONArray jsonResults = jsonObject.getJSONArray("results");
                /* Extract items from the JSON array */
                for (int i = 0; i < jsonResults.length(); i++) {
                    list_items.add(jsonResults.getString(i));
                }
            }
            catch(Exception e)
            {
                Log.i("App", "Error parsing data" +e.getMessage());
            }
            timings.addSplit("Online backend: data process");
        }
        else
        {
            // Use offline solver (slow)
            String[] letters_array = new String[letters.length()];
            List<String> permutations = new ArrayList<>();

            for (int j = 0; j < letters.length(); j++) {
                letters_array[j] = letters.substring(j, j + 1);
            }
            for (int j = minimal_length; j <= letters.length(); j++)
                for (List<String> products : Itertools.permutations(Arrays.asList(letters_array), j)) {
                    permutations.add(Joiner.on("").join(products));
                }
            timings.addSplit("Offline backend: permutations process");

            ArrayList<String> dictionary = new ArrayList<>();
            try {
                AssetManager assetManager = getAssets();
                InputStreamReader inputStream = new InputStreamReader(assetManager.open("italian.dict"));
                BufferedReader inputReader = new BufferedReader(inputStream);
                for (String line; (line = inputReader.readLine()) != null; ) {
                    Log.d("words_solver", line);
                    if (permutations.contains(line)) {
                        list_items.add(line);
                    }
                }
                inputReader.close();
                inputStream.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            timings.addSplit("Offline backend: dictionary compare");
        }
        timings.dumpToLog();
        adapter_list.notifyDataSetChanged();

        Toast.makeText(this,  list_items.size() + " solutions found", Toast.LENGTH_SHORT).show();
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Start Preferemces acitivity
        if (id == R.id.action_preferences) {
            Intent intent = new Intent(this, Preferences.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
