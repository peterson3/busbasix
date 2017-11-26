package com.example.tiago.busbasix;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tiago.busbasix.API.SoundMeter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Button inputDataBtn;
    private Double lat;
    private Double lng;
    private static final int REQUEST_LOCATION = 2;
    private final int REQUEST_PERMISSION_LOCATION=1;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView enderecosText;
    private ProgressDialog dialog;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference rootRef = database.getReference();
    private SoundMeter mSensor;
    private Onibus onibusAbordo = null; //Onibus que o usuário está abordo
    private Circle me = null;
    private Address origin = null;
    private Address destination = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        enderecosText = (TextView) findViewById(R.id.enderecos_text);
        Toast.makeText(getApplicationContext(), "Inicializando...", Toast.LENGTH_SHORT).show();


        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("AUDIO PERMISSION", "NOT GRANTED");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            1);
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                Log.d("AUDIO PERMISSION", "GRANTED");
            }
                }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }


        LatLng initialPos;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("GPS_PROVIDER", "IS ENABLED");
            Location lastKnowLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d("GPS_PROVIDER", "GOT LAST KNOWN LOCATION");

            if (lastKnowLoc != null) {
                double new_lat = lastKnowLoc.getLatitude();
                double new_long = lastKnowLoc.getLongitude();
                initialPos = new LatLng(new_lat, new_long);

            }


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
                    //Location.distanceBetween();
                    LatLng latLng = new LatLng(new_lat, new_long);
                    //mMap.addPolyline(new PolylineOptions().add(curPoint, latLng).width(10).color(Color.BLUE));
                    // Instantiates a new CircleOptions object and defines the center and radius
                    //CircleOptions circleOptions = new CircleOptions()
                     //       .center(curPoint)
                    //       .radius(1000); // In meters

                    // Get back the mutable Circle
//                    Circle me = mMap.addCircle(circleOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));


                    //For Testing
                    try {
                        Log.d("URL", "url/buses/myLat="+String.valueOf(new_lat)
                                +"&myLong="+String.valueOf(new_long)
                                +"&maxDistance="+String.valueOf(300));
                        new BusSearchTask().execute("https://iksfznseee.localtunnel.me/buses/myLat="+String.valueOf(new_lat)
                                +"&myLong="+String.valueOf(new_long)
                                +"&maxDistance="+String.valueOf(300)).get();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    //TODO:
                    //Se o usuário está abordo de algum ônibus - Então alimenta informação em tempo real do ônibus com informação do usuário
                    if (onibusAbordo != null){
                        onibusAbordo.setLatLong(latLng);
                        if (location.hasSpeed()){
                            onibusAbordo.setVelocidade(location.getSpeed());
                        }

                        // O Marcador desse ônibus também deve mudar
                    }
                    //TODO:
                    //Mover o Marcador do Usuário
                    //Se passou de um minuto desde a última atualização, Então requisitar novamente os ônibus próximos.
                    //Se não, Considere apenas a lista de ônibus previamente recuperada

                    lat = new_lat;
                    lng = new_long;


                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> enderecos = geocoder.getFromLocation(lat, lng, 1);
                        enderecosText.setText(enderecos.get(0).getThoroughfare());
                        Log.d("ENDERECO HASH", String.valueOf(enderecos.get(0).hashCode()));
                        Log.d("ENDERECO URL", String.valueOf(enderecos.get(0).getUrl()));
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
            Toast.makeText(getApplicationContext(), "GPS PROVIDER INDISPONIVEL", Toast.LENGTH_SHORT).show();
        }


    }

    public void onSearch(View view) {

        //Cake Recipe
        //1- Check if From Location and To Location are not empty
        EditText from_editText = (EditText) findViewById(R.id.from_location);
        //from_editText.clearFocus();
        String from_text = from_editText.getText().toString();
        EditText to_editText = (EditText) findViewById(R.id.to_location);
        //to_editText.clearFocus();


        // Check if no view has focus:
        View cview = this.getCurrentFocus();
        if (cview != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(cview.getWindowToken(), 0);
        }


        String to_Text = from_editText.getText().toString();
        if (from_text.isEmpty() || to_Text.isEmpty()){
            Toast.makeText(this, "Falta informações de Localização", Toast.LENGTH_SHORT).show();
        }
        //2- Get Buses That Has This Locations on there WAY TO
            //TODO

        //Get all lines from database
        //Log.d("FIREBASE", "Trying to Retreive Data");

       // DatabaseReference linhasRef = rootRef.child("Linhas");

        //Log.d("FIREBASE", "Fine");

        //3- Turn available the List of Buses by Classifications
            //TODO
        //4- Turn available the Travel Info to User
            //TODO

        List<Address> addressList = null;
        if (to_editText != null || !to_editText.equals("")) {
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                addressList = geocoder.getFromLocationName(to_Text, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.destination = addressList.get(0);
            try {
                new DirectionSearchTask()
                        .execute("https://maps.googleapis.com/maps/api/directions/json?" +
                                "alternatives=true" +
                                "&origin=" + origin.getLatitude() +"," + origin.getLongitude() +
                                "&destination="  + destination.getLatitude() +"," + destination.getLongitude() +
                                "&mode=transit" +
                                "&key=AIzaSyCgL70drGeVpDEz40qIiYoqdwP6gKmnXl8").get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            //  mMap.addMarker(new MarkerOptions().position(lat_long_search).title("Marker on search"));
          //  mMap.moveCamera(CameraUpdateFactory.newLatLng(lat_long_search));
          //  mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
        }

    }

    public void onClickFromMyCurrentLocation(View view) {

        Log.d("CLICKTEST", "CLICK TESTE OK");
        //Check if GPS is TurnedOn
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("TEST", "provider enabled test OK");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("TEST", "provider permission test OK");
                Location lastKnowLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //Get Current Lat Long
                LatLng myLatLng = new LatLng(lastKnowLoc.getLatitude(), lastKnowLoc.getLongitude());
                //Get, from Google API, Street Name and ETC
                Geocoder geocoder = new Geocoder(getApplicationContext());
                List<Address> addressList = null;
                try {
                    addressList = geocoder.getFromLocation(myLatLng.latitude, myLatLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Set info in TextField
                EditText from_textField = (EditText) findViewById(R.id.from_location);
                if (addressList != null){
                    Log.d("TEST", "adress list not null OK");
                    from_textField.setText(addressList.get(0).getThoroughfare());
                }
                else{
                    Log.d("TEST", "adress list null");
                }

            }
            else{
                Log.d("TEST", "GPS PERMISSION FAILED");

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "No permission for GPS", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d("TEST", "PERMISSION FOR FINE LOCATION OK");

                    Location lastKnowLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    //Get Current Lat Long
                    LatLng myLatLng = new LatLng(lastKnowLoc.getLatitude(), lastKnowLoc.getLongitude());
                    //Get, from Google API, Street Name and ETC
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    List<Address> addressList = null;
                    try {
                        addressList = geocoder.getFromLocation(myLatLng.latitude, myLatLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Set info in TextField
                    EditText from_textField = (EditText) findViewById(R.id.from_location);
                    if (addressList != null){
                        Log.d("TEST", "adress list not null OK");
                        Address endereco = addressList.get(0);
                        this.origin = endereco;
                        Log.d("ENDERECO HASH", String.valueOf(endereco.hashCode()));
                        Log.d("ENDERECO URL", String.valueOf(endereco.getUrl()));
                        from_textField.setText(endereco.getThoroughfare() + ", " + endereco.getLocality() );
                    }
                    else{
                        Log.d("TEST", "adress list null");
                    }


                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "No permission for Location", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d("TEST", "PERMISSION FOR COARSE LOCATION OK");

                }


                }
        }else{
            showGPSDisabledAlertToUser();
        }

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
        boolean GPS_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        Location location;
        LatLng initialPos;
        if(GPS_enabled){
            Log.d("GPS_PROVIDER", "IS ENABLED");

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d("GPS_PROVIDER", "GOT LAST KNOWN LOCATION");

            if(location!=null){
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                initialPos = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(initialPos));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
            }
        }
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        //Toast.makeText(this, "Teste", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Linha " + marker.getTitle());
        builder.setMessage("Você está abordo deste ônibus?");

        builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                //AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                //builder.setMessage("Você está abordo deste ônibus?");
                //dialog
                Toast.makeText(MapsActivity.this, "Ótimo! Colabore avaliando seu ônibus!", Toast.LENGTH_LONG).show();
                //TODO:
                //Teste de Ruído:
                //Pedir Permissão para utilizar o MICROFONE
                //Verificar Volume do Som Ambiente
                //Com base no "Emperismo", ou "Heurísticas" definir um valor aceitável
                /*if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(getParent(),
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            1);

                    MediaRecorder mediaRecorder = null;

                }
                */
                //TODO:
                //Setar Onibus Abotdo
                //onibusAbordor = onibus?;
                //Escutar Microfone
                new ListenMicSensorTask().execute();
                //mSensor.stop();

                //Passar informações de GPS do Usuário para o ônibus
                //Mostrar no Painel do Usuário - Linhas e Onibus
                //Começar a avaliar via sensores
                //Pedir feedback diretamente
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing


                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        return false;
    }

    public class BusSearchTask extends AsyncTask<String, String, List<Onibus>> {
        private final ProgressDialog dialog = new ProgressDialog(MapsActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDialog dialog;
            dialog = new ProgressDialog(getApplicationContext());
           // dialog.setMessage("Recebendo Dados de Ônibus...");
            //dialog.show();
            //Log.d("Teste Json", "PRE - 03");

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

                InputStream stream = null;
                try{
                    stream = connection.getInputStream();
                }
                catch (FileNotFoundException ex){
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run(){
                            Toast.makeText(MapsActivity.this, "Não foi possível recuperar as linhas", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();

                Log.d("Teste Json", "3");

                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                //JSONObject parentObject = new JSONObject(finalJson);
                Log.d("Final JSON", finalJson);
                //JSONArray data  = parentObject.getJSONArray("");
                JSONArray data = new JSONArray(finalJson);
                Log.d("JSON DATA", data.toString());
                Log.d("QtdInfo no JSON", String.valueOf(data.length()));

                List<Onibus> onibusLista = new ArrayList<>();

                for (int i=0; i<data.length(); i++){
                    //JSONArray interData = data.getJSONArray(i);
                    JSONObject interData = data.getJSONObject(i);
                //    Log.d("JSON DATA", interData.toString());
                //    Log.d("GET Json - DATAHORA", interData.getString(0));
                //    Log.d("GET Json - ORDEM", interData.getString(1));
                //    Log.d("GET Json - LINHA", interData.getString(2));
                //    Log.d("GET Json - LATITUDE", interData.getString(3));
                //    Log.d("GET Json - LONGITUDE", interData.getString(4));
                //    Log.d("GET Json - VELOCIDADE", interData.getString(5));
                //    Log.d("GET Json - DIRECAO", interData.getString(6));
                    Onibus onibus = new Onibus();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");


                    Date convertedDate = new Date();

                    /*try {
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
                    */

                    try {
                        convertedDate= dateFormat.parse(interData.getString("DataHora"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    onibus.setDataHora(convertedDate);
                    onibus.setOrdem(interData.getString("Ordem"));
                    onibus.setLinha(interData.getString("Linha"));
                    onibus.setLatLong(new LatLng(Double.parseDouble(interData.getString("Latitude")), Double.parseDouble(interData.getString("Longitude"))));
                    onibus.setVelocidade(Float.parseFloat(interData.getString("Velocidade")));
                    onibus.setDirecao(Integer.parseInt(interData.getString("Direcao")));



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
                    ArrayList<ArrayList<Onibus>> onibusPorLinha = new ArrayList<>();

                    //Pega a primeira linha

                do {
                    Onibus first = onibusLista.get(0);
                    String linha = first.getLinha();
                    ArrayList<Onibus> primeiraLinha = new ArrayList<Onibus>();
                    primeiraLinha.add(first);
                    onibusPorLinha.add(primeiraLinha);
                    //Pega todos os onibus dessa Linha
                    for (int i = 1; i < onibusLista.size(); i++) {
                        Onibus contender = onibusLista.get(i);
                        if (contender.getLinha().equals(linha)) {
                            primeiraLinha.add(contender);
                        }
                    }
                    onibusLista.removeAll(primeiraLinha);
                }while(onibusLista.size() != 0);


                for (int i=0; i<onibusPorLinha.size(); i++){
                    //Verificando a lista da linha
                    String logPrint = onibusPorLinha.get(i).get(0).getOrdem();
                    for (int j=1; j<onibusPorLinha.get(i).size(); j++){
                        logPrint += ", " + onibusPorLinha.get(i).get(j).getOrdem();
                    }

                    String numeroLinha = "";
                    if (onibusPorLinha.get(i).get(0).getLinha().isEmpty()){
                        numeroLinha = "S/LINHA";
                    }
                    else{
                        numeroLinha = onibusPorLinha.get(i).get(0).getLinha();
                    }
                    Log.d("LINHA " + numeroLinha, logPrint);
                }

                Log.d("Quantidade de Linhas", String.valueOf(onibusPorLinha.size()));

                Location myLoc = new Location("meu local");
                myLoc.setLatitude(-22.906847);
                myLoc.setLongitude(-43.172896);

                ArrayList<Onibus> onibusPerto = new ArrayList<>();
                for (int i=0; i<onibusPorLinha.size(); i++){
                    for (int j=0; j<onibusPorLinha.get(i).size(); j++){

                        LatLng posOnibus = onibusPorLinha.get(i).get(j).getLatLong();
                        Location busLoc = new Location("busLoc");
                        busLoc.setLatitude(posOnibus.latitude);
                        busLoc.setLongitude(posOnibus.longitude);
                        double distance = myLoc.distanceTo(busLoc);
                        //Log.i("DISTANCE", String.valueOf(distance));
                        //Distância Menor que 300m
                        if (distance < 300){
                            Log.d("ONIBUS PERTO", "DISTANCIA " + String.valueOf(distance));
                            onibusPerto.add(onibusPorLinha.get(i).get(j));
                        }
                    }
                }

               return onibusPerto;

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
            dialog.dismiss();
            //TODO: Adaptar Resultado para exibição
            if (result != null){
                //Remove all Markers
                mMap.clear();
                //Toast.makeText(getApplicationContext(), "Linha: " + result.get(0).getLinha(), Toast.LENGTH_SHORT).show();
                for (int i=0; i<result.size(); i++){
                    mMap.addMarker(new MarkerOptions()
                            .position(result.get(i).getLatLong())
                            .title("Linha " + result.get(i).getLinha())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_marker))
                            .snippet(result.get(i).getOrdem())
                    );
                }
                Log.d("QtdOnibusNaTela", String.valueOf(result.size()));


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

        double distance_between(Location l1, Location l2)
        {
            double lat1=l1.getLatitude();
            double lon1=l1.getLongitude();
            double lat2=l2.getLatitude();
            double lon2=l2.getLongitude();
            double R = 6371; // km
            double dLat = (lat2-lat1)*Math.PI/180;
            double dLon = (lon2-lon1)*Math.PI/180;
            lat1 = lat1*Math.PI/180;
            lat2 = lat2*Math.PI/180;

            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            double d = R * c * 1000;

            return d;
        }
    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public class ListenMicSensorTask extends AsyncTask<String, Void, Object>{
        private boolean done = false;

        public void quit() {
            done = true;
        }

        @Override
        protected Void doInBackground(String... params) {
            Log.d("Listen on MIC", "Starting MIC.." );
            mSensor = new SoundMeter();

            try {
                mSensor.start();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Log.d("Listen on MIC", "Starting SoundMeter.." );

            Log.d("Listen on MIC", "On Current Thread " + Thread.currentThread().getName());
            double amplitudeNoise = 0;
            while (!done){

                /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        double amplitudeNoise =  mSensor.getAmplitude();
                        while (amplitudeNoise < 30000){
                            amplitudeNoise =  mSensor.getAmplitude();
                            Log.d("NOISE AMPLITUDE", String.valueOf(amplitudeNoise));
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                        mSensor.stop();
                        Log.d("MIC SENSOR", "STOPPED");
                    }
                }, 1000);*/

                amplitudeNoise =  mSensor.getAmplitude();
                Log.d("NOISE AMPLITUDE", String.valueOf(amplitudeNoise));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }
    }

    public class DirectionSearchTask extends AsyncTask<String, String, JSONObject> {
        private final ProgressDialog dialog = new ProgressDialog(MapsActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDialog dialog;
            dialog = new ProgressDialog(getApplicationContext());
            // dialog.setMessage("Recebendo Dados de Ônibus...");
            //dialog.show();
            //Log.d("Teste Json", "PRE - 03");

        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject response = null;
            //HTTP request google
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                Log.d("INTERNET CONNECTION", "Param: " + params[0]);
                connection = (HttpURLConnection) url.openConnection();
                Log.d("INTERNET CONNECTION", "Open Con OK");
                connection.connect();
                Log.d("INTERNET CONNECTION", "CONNECTED");
                InputStream stream = null;
                try {
                    stream = connection.getInputStream();
                } catch (FileNotFoundException ex) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MapsActivity.this, "Não foi possível conectar", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();

                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJsonString = buffer.toString();

                //JSONObject parentObject = new JSONObject(finalJson);
                Log.d("Final JSON", finalJsonString);
                response = new JSONObject(finalJsonString);
                Log.d("JSON DATA", response.toString());

            } catch (IOException e) {
                e.printStackTrace();
                return response;
            } catch (JSONException e) {
                e.printStackTrace();
                return response;
            }

            return response;
        }

            @Override
        protected void onPostExecute(final JSONObject result) {
            super.onPostExecute(result);
            dialog.dismiss();
            //TODO: Adaptar Resultado para exibição
        }

        double distance_between(Location l1, Location l2)
        {
            double lat1=l1.getLatitude();
            double lon1=l1.getLongitude();
            double lat2=l2.getLatitude();
            double lon2=l2.getLongitude();
            double R = 6371; // km
            double dLat = (lat2-lat1)*Math.PI/180;
            double dLon = (lon2-lon1)*Math.PI/180;
            lat1 = lat1*Math.PI/180;
            lat2 = lat2*Math.PI/180;

            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            double d = R * c * 1000;

            return d;
        }
    }

}

