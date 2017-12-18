package com.example.tiago.busbasix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tiago.busbasix.API.rioBus.Bus;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RatingActivity extends AppCompatActivity {

    private Button submit;
    private TextView title;
    private RatingBar rateBar;
    private String lineNumber;
    private String lineOrder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        Random random = new Random();
        int randomRate = random.nextInt(6);

        Intent callingIntent = getIntent();
        lineNumber = callingIntent.getStringExtra("lineNumber");
        lineOrder = callingIntent.getStringExtra("lineOrder");


        rateBar = (RatingBar)findViewById(R.id.ratingBar);
        submit = (Button)findViewById(R.id.rate_submit);
        title = (TextView)findViewById(R.id.rate_title);

        switch (randomRate){
                case 0:
                title.setText("Confiança");
                break;
                case 1:
                title.setText("Conforto");
                break;
                case 2:
                title.setText("Conveniência");
                break;
                case 3:
                title.setText("Comunicação");
                break;
                 case 4:
                title.setText("Acessibilidade");
                break;
                 case 5:
                title.setText("Segurança");
                break;

        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 //new PutInfo().executeOnExecutor(String.valueOf(rateBar.getNumStars()));
                new PutInfo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,String.valueOf(rateBar.getRating()), lineNumber);
                RatingActivity.this.finish();
            }
        });

}

public class PutInfo extends AsyncTask<String, String, String> {

    private final ProgressDialog dialog = new ProgressDialog(RatingActivity.this);

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ProgressDialog dialog;
        Log.d("PRE EXECUTE", "1");
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            Log.d("DO IN BACKGROUND ", "1");
            String urlString = "http://busbasix.ddns.net:3000/buses/" + params[1];
            URL url = new URL(urlString);
            Log.d("URI", urlString);
            Log.d("DO IN BACKGROUND ", "2");
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            Log.d("DO IN BACKGROUND ", "3");
            httpCon.setRequestMethod("PUT");
            Log.d("DO IN BACKGROUND ", "4");
            httpCon.setDoOutput(true);
            httpCon.setDoInput(true);

            Log.d("DO IN BACKGROUND ", "4.1");

            httpCon.setDoInput(true);
            Log.d("DO IN BACKGROUND ", "4.2");
            httpCon.setRequestProperty("Content-Type", "application/json" );
            httpCon.setRequestProperty("Accept", "application/json");

            OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
            Log.d("DO IN BACKGROUND ", "5");

            Log.d("DO IN BACKGROUND ", "6");

            String s = "{" +
                    "\"linha\": \"455\"," +
                    "\"avaliacao\":" +
                    "{" +
                    "\"qtd\" : \"1\"," +
                    "\"confianca\": \"" + params[0] + "\"," +
                    "\"conveniencia\": \"0\"," +
                    "\"comunicacao\": \"0\"," +
                    "\"acessibilidade\": \"0\"," +
                    "\"seguranca\": \"0\"" +
                    "}" +
                    "}";
            Log.d("DO IN BACKGROUND ", "7");

            out.write(s);
            Log.d("DO IN BACKGROUND ", "8");

            Log.d("INPUT PUT", s);
            out.flush();
            out.close();
            Log.d("DO IN BACKGROUND ", "9");
            Log.d("RESPONSE", httpCon.getResponseMessage());;
            BufferedReader br;
            if (200 <= httpCon.getResponseCode() && httpCon.getResponseCode() <= 299) {
                br = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(httpCon.getErrorStream()));
            }


            String line = br.readLine();
            String lineConcat = line;
            while( (line != null) && (!line.isEmpty()) ){
                line = br.readLine();
                lineConcat += line;
            }

            Log.d("RESPONSE-BODY", lineConcat);;


            return "OK";
        } catch (ProtocolException e) {
            e.printStackTrace();
            Log.e("ProtocolException", e.getMessage());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("MalformedURLException", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("IOException", e.getMessage());

        }
        return "ok";
    }

    @Override
    protected void onPostExecute(final String result) {
        super.onPostExecute(result);
        }


    }

}

