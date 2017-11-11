package com.example.tiago.busbasix;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.zxing.client.result.AddressBookAUResultParser;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLOutput;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button inputDataBtn;
    private Double lat;
    private Double lng;
    private static final int REQUEST_LOCATION = 2;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView enderecosText;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        enderecosText = (TextView) findViewById(R.id.enderecos_text);
        Toast.makeText(getApplicationContext(), "Procurando Onibus próximos...", Toast.LENGTH_SHORT).show();

        /*inputDataBtn = (Button) findViewById(R.id.button2);
        inputDataBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Código
                //Random rand = new Random();

                LatLng curPoint = new LatLng(lat, lng);

                lat += 0.00001;
                lng += 0.00001;

                System.out.println("Selecting Another Point");
                LatLng otherPoint = new LatLng(lat, lng);
                mMap.addPolyline(new PolylineOptions().add(curPoint, otherPoint).width(20).color(Color.RED));
                //mMap.addMarker(new MarkerOptions().position(otherPoint).title("other marker"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(otherPoint));
                //mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
            }
        });
        */


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }



        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

          /*  Location lastKnowLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnowLoc != null){
                double new_lat = lastKnowLoc.getLatitude();
                double new_long = lastKnowLoc.getLongitude();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(new_lat, new_long)));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
            }*/



            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (lat == null) {
                        lat = location.getLatitude();
                    }
                    if (lng == null) {
                        lng = location.getLongitude();
                    }
                    LatLng curPoint = new LatLng(lat, lng);
                    double new_lat = location.getLatitude();
                    double new_long = location.getLongitude();
                    LatLng latLng = new LatLng(new_lat, new_long);
                    mMap.addPolyline(new PolylineOptions().add(curPoint, latLng).width(10).color(Color.BLUE));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));

                    lat = new_lat;
                    lng = new_long;

                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> enderecos = geocoder.getFromLocation(lat, lng, 1);
                        enderecosText.setText(enderecos.get(0).getThoroughfare());
                        //enderecosText.setText(enderecos.get(0).getLocality());
                        //enderecosText.append(enderecos.get(0).getPostalCode());
                        //enderecosText.append(enderecos.get(0).getPremises());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {
                    Toast.makeText(getApplicationContext(), "Provider Enabled", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onProviderDisabled(String provider) {
                    Toast.makeText(getApplicationContext(), "Provider Enabled", Toast.LENGTH_SHORT).show();

                }
            });

        } else {
            Toast.makeText(getApplicationContext(),"GPS PROVIDER INDISPONIVEL", Toast.LENGTH_SHORT).show();
        }


    }

    public void onSearch(View view) {
        EditText location_tf = (EditText) findViewById(R.id.destText);
        String location = location_tf.getText().toString();
        List<Address> addressList = null;
        Log.d("Teste Json", "0");

        try {
            new BusSearchTask().execute("http://10.0.2.2:8888/linhas/841").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng lat_long_search = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(lat_long_search).title("Marker on search"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lat_long_search));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Federal Fluminense University Computing Institute and move the camera
        //LatLng ic_uff = new LatLng(lat, lng);
        //mMap.addMarker(new MarkerOptions().position(ic_uff).title("Marker in IC-UFF"));

        // mMap.moveCamera(CameraUpdateFactory.newLatLng(ic_uff));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
            Toast.makeText(getApplicationContext(), "Location Disabled", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        mMap.setMyLocationEnabled(true);
    }


    public class BusSearchTask extends AsyncTask<String, String, List<Onibus>> {
        private final ProgressDialog dialog = new ProgressDialog(MapsActivity.this);

        @Override
        protected void onPreExecute() {
            Log.d("Teste Json", "PRE - 01");

            super.onPreExecute();
            Log.d("Teste Json", "PRE - 02");

            this.dialog.setMessage("Recebendo Dados de Ônibus...");
            this.dialog.show();
            Log.d("Teste Json", "PRE - 03");

        }

        @Override
        protected List<Onibus> doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            Log.d("Teste Json", "1");
            try {
                URL url = new URL(params[0]);
                Log.d("INTERNET CONNECTION", "Param: " + params[0]);

                connection = (HttpURLConnection) url.openConnection();
                Log.d("INTERNET CONNECTION", "Open Con OK");
                connection.connect();
                Log.d("INTERNET CONNECTION", "CONNECTED");


                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                Log.d("Teste Json", "3");

                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                JSONObject parentObject = new JSONObject(finalJson);
                Log.d("Final JSON", finalJson);
                JSONArray data  = parentObject.getJSONArray("DATA");
                Log.d("JSON DATA", data.toString());
                Log.d("QtdInfo no JSON", String.valueOf(data.length()));

                List<Onibus> onibusLista = new ArrayList<>();

                for (int i=0; i<data.length(); i++){
                    JSONArray interData = data.getJSONArray(i);
                    Log.d("JSON DATA", interData.toString());
                    Log.d("GET Json - DATAHORA", interData.getString(0));
                    Log.d("GET Json - ORDEM", interData.getString(1));
                    Log.d("GET Json - LINHA", interData.getString(2));
                    Log.d("GET Json - LATITUDE", interData.getString(3));
                    Log.d("GET Json - LONGITUDE", interData.getString(4));
                    Log.d("GET Json - VELOCIDADE", interData.getString(5));
                    Log.d("GET Json - DIRECAO", interData.getString(6));
                    Onibus onibus = new Onibus();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
                    Date convertedDate = new Date();
                    try {
                        convertedDate= dateFormat.parse(interData.getString(0));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    onibus.setDataHora(convertedDate);
                    onibus.setOrdem(interData.getString(1));
                    onibus.setLinha(interData.getString(2));
                    onibus.setLatLong(new LatLng(Double.parseDouble(interData.getString(3)), Double.parseDouble(interData.getString(4))));
                    onibus.setVelocidade(Float.parseFloat(interData.getString(5)));
                    onibus.setDirecao(Integer.parseInt(interData.getString(6)));
                    Log.d("Onibus Instanciado", onibus.toString());
                    onibusLista.add(onibus);
                }





                    /*
                //JSONObject parentObject = new JSONObject(finalJson);
                //JSONArray parentArray = parentObject.getJSONArray("data");

                List<Onibus> onibusLista = new ArrayList<>();

                //Gson gson = new Gson();
                //for (int i = 0; i < parentArray.length(); i++) {
                    //JSONObject finalObject = parentArray.getJSONObject(i);
                    /**
                     * below single line of code from Gson saves you from writing the json parsing yourself
                     * which is commented below
                     */

                    //Onibus onibus = new Onibus();
                    /*
                    onibus.setLinha(finalObject.getString("DATA[0][2]"));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
                    Date convertedDate = new Date();
                    try {
                       convertedDate= dateFormat.parse(finalObject.getString("DATA[0][0]"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    onibus.setDataHora(convertedDate);
                    onibus.setLatLong(new LatLng(finalObject.getDouble("DATA[0][3]"), finalObject.getDouble("DATA[0][4]")));
                    onibus.setOrdem(finalObject.getString("DATA[0][1]"));
                    onibusLista.add(onibus);
                       }
                    */


               return onibusLista;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<Onibus> result) {
            super.onPostExecute(result);
            this.dialog.dismiss();
            //TODO: Adaptar Resultado para exibição
            if (result != null){
                //Toast.makeText(getApplicationContext(), "Linha: " + result.get(0).getLinha(), Toast.LENGTH_SHORT).show();
                mMap.addMarker(new MarkerOptions().position(result.get(0).getLatLong()).title(result.get(0).getLinha() + " "+ result.get(0).getOrdem()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(result.get(0).getLatLong()));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
            }
            /*
            if (result != null) {
                MovieAdapter adapter = new MovieAdapter(getApplicationContext(), R.layout.row, result);
                lvMovies.setAdapter(adapter);
                lvMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // list item click opens a new detailed activity
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        MovieModel movieModel = result.get(position); // getting the model
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putExtra("movieModel", new Gson().toJson(movieModel)); // converting model json into string type and sending it via intent
                        startActivity(intent);
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Not able to fetch data from server, please check url.", Toast.LENGTH_SHORT).show();
            }
            */
        }
    }

}

