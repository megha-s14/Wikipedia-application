package com.example.wikipedia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private Button btnFetchData;
    private TextView txtWikiData;
    private ProgressBar progressBar;
    private EditText etxSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        etxSearch = (EditText) findViewById(R.id.etxSearch);
        txtWikiData = (TextView) findViewById(R.id.txtWikiData);
        btnFetchData = (Button) findViewById(R.id.btnFetchData);
        etxSearch.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        btnFetchData.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String keyword = etxSearch.getText().toString();

                String WIKIPEDIA_URL = "https://en.wikipedia.org/w/api.php?action=query&titles=" + keyword + "&prop=revisions&rvprop=content&format=json&prop=extracts";

                // Start AsyncTask
                FetchWikiDataAsync fetchWikiDataAsync = new FetchWikiDataAsync();
                fetchWikiDataAsync.execute(WIKIPEDIA_URL);
            }
        });
    }

    private class FetchWikiDataAsync extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this, "Fetching Data. Please wait.", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String[] params)
        {
            try
            {
                String sURL = params[0];

                URL url = new URL(sURL);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"UTF-8");



                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(line);
                }

                String wikiData = stringBuilder.toString();


                String formattedData = parseJSONData(wikiData);

                return formattedData;

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String formattedData)
        {
            super.onPostExecute(formattedData);
            progressBar.setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {

                txtWikiData.setText(Html.fromHtml(formattedData,Html.FROM_HTML_MODE_LEGACY));
            }
            else
            {

                txtWikiData.setText(Html.fromHtml(formattedData));
            }
        }
    }

    private String parseJSONData(String wikiData)
    {
        try
        {

            JSONObject rootJSON = new JSONObject(wikiData);
            JSONObject query = rootJSON.getJSONObject("query");
            JSONObject pages = query.getJSONObject("pages");
            JSONObject number = pages.getJSONObject(pages.keys().next());
            String formattedData = number.getString("extract");

            return formattedData;
        }
        catch (JSONException json)
        {
            json.printStackTrace();
        }

        return null;
    }

}

