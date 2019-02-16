package com.muflone.wordsolver;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.common.base.Joiner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    EditText text_letters;
    EditText text_length;
    ListView list_solutions;
    final ArrayList<String> list_items = new ArrayList<String>();
    ArrayAdapter adapter_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize_ui();

        adapter_list = new ArrayAdapter(this,
            android.R.layout.simple_list_item_1, list_items );
        list_solutions.setAdapter(adapter_list);
    }

    private void initialize_ui()
    {
        text_letters = (EditText) findViewById(R.id.text_letters);
        text_length = (EditText) findViewById(R.id.text_length);
        list_solutions = (ListView) findViewById(R.id.list_solutions);
    }


    public void button_solve_onClick(View view)
    {
        String letters = text_letters.getText().toString().toUpperCase();
        String[] letters_array = new String[letters.length()];
        List<String> permutations = new ArrayList<>();
        int minimal_length = Integer.parseInt(text_length.getText().toString());

        for (int j = 0; j < letters.length(); j++)
        {
            letters_array[j] = letters.substring(j, j + 1);
        }
        for (int j = minimal_length; j <= letters.length(); j++)
            for (List<String> products : Itertools.permutations(Arrays.asList(letters_array), j))
            {
                permutations.add(Joiner.on("").join(products));
            }

        list_items.clear();
        AssetManager assetManager = getAssets();
        InputStreamReader inputStream;
        BufferedReader inputReader;
        try
        {
            inputStream = new InputStreamReader(assetManager.open("italian.dict"));
            inputReader = new BufferedReader(inputStream);
            for (String line; (line = inputReader.readLine()) != null;)
            {
                line = line.toUpperCase();
                if (permutations.contains(line))
                {
                    System.out.println(line);
                    list_items.add(line);
                }
            }
            inputReader.close();
            inputStream.close();
            adapter_list.notifyDataSetChanged();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
}
