package com.example.tiago.busbasix;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Region;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.util.view.MenuView;
import com.example.tiago.busbasix.API.Rate;
import com.example.tiago.busbasix.API.RoutesOptionActivity;
import com.example.tiago.busbasix.API.busBasix.Onibus;
import com.example.tiago.busbasix.API.busBasix.SoundMeter;
import com.example.tiago.busbasix.API.googleDirection.Direction;
import com.example.tiago.busbasix.API.googleDirection.Polyline;
import com.example.tiago.busbasix.API.googleDirection.Route;
import com.example.tiago.busbasix.API.rioBus.Bus;
import com.example.tiago.busbasix.API.rioBus.Line;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, SensorEventListener {

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
    private static final int SHOW_ROUTES_RESULT_CODE = 0;
    private int retorno_opcao_rota;
    private Direction direction = null;
    TextView current_status;
    EditText from_editText;
    EditText to_editText;
    Button rate_btn;
    Sensor temperatureSensor;
    SensorManager sensor;


    private com.google.android.gms.maps.model.Polyline drawedPolyline = null;

    private List<Marker> bus_markers;
    private Bus bus_i_am_in;
    public List<Bus> buses_in_screen;

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

        TextView current_status = (TextView) findViewById(R.id.current_status);
        EditText from_editText = (EditText) findViewById(R.id.from_location);
        EditText to_editText = (EditText) findViewById(R.id.to_location);


        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        //Add your action onClick
                        break;
                    case R.id.menu_mapa:

                        break;

                    case R.id.menu_linhas:
                        Intent busInfoActivity = new Intent(MapsActivity.this, BusInfoActivity.class);
                        startActivity(busInfoActivity);
                        break;
                }
                return false;
            }
        });

        current_status.setText("DESCONHECIDO");
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

        sensor = (SensorManager)getSystemService(SENSOR_SERVICE);
        temperatureSensor = sensor.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if (temperatureSensor != null) {
    /* register listener and do other magic... */
            Log.d("TEMPERATURA","Sensor de Temperatura Ligado");
            sensor.registerListener(this, temperatureSensor, 1000000);

        }
        else{
            Log.d("TEMPERATURA","Sensor de Temperatura Nulo");
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

                    //mMap.animateCamera(mMap.animateCamera(new ););


                    LatLng curPoint = new LatLng(lat, lng);
                    double new_lat = location.getLatitude();
                    double new_long = location.getLongitude();
                    //Location.distanceBetween();
                    LatLng latLng = new LatLng(new_lat, new_long);

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    if (bus_i_am_in != null){
                        Log.d ("GPS Listener LATLNG",location.getLatitude() + ", " + location.getLongitude());
                        //TODO sendo to API
                    }
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
                    //mMap.addPolyline(new PolylineOptions().add(curPoint, latLng).width(10).color(Color.BLUE));
                    // Instantiates a new CircleOptions object and defines the center and radius
                    //CircleOptions circleOptions = new CircleOptions()
                     //       .center(curPoint)
                    //       .radius(1000); // In meters

                    // Get back the mutable Circle
//                    Circle me = mMap.addCircle(circleOptions);
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));


                    //For Testing
//                    try {
//                        Log.d("URL", "url/buses/myLat="+String.valueOf(new_lat)
//                                +"&myLong="+String.valueOf(new_long)
//                                +"&maxDistance="+String.valueOf(300));
//                        new BusSearchTask().execute("https://iksfznseee.localtunnel.me/buses/myLat="+String.valueOf(new_lat)
//                                +"&myLong="+String.valueOf(new_long)
//                                +"&maxDistance="+String.valueOf(300)).get();
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    }

                    //TODO:
//                    //Se o usuário está abordo de algum ônibus - Então alimenta informação em tempo real do ônibus com informação do usuário
//                    if (onibusAbordo != null){
//                        onibusAbordo.setLatLong(latLng);
//                        if (location.hasSpeed()){
//                            onibusAbordo.setVelocidade(location.getSpeed());
//                        }
//
//                        // O Marcador desse ônibus também deve mudar
//                    }
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

        //1- Check if From Location and To Location are not empty
        current_status = (TextView) findViewById(R.id.current_status);
        from_editText = (EditText) findViewById(R.id.from_location);
        to_editText = (EditText) findViewById(R.id.to_location);

        String from_text = from_editText.getText().toString();
        String to_Text = to_editText.getText().toString();


        // Check if no view has focus:
        View cview = this.getCurrentFocus();
        if (cview != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(cview.getWindowToken(), 0);
        }


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
                String requestString = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "alternatives=true" +
                        "&origin=" + origin.getLatitude() +"," + origin.getLongitude() +
                        "&destination="  + destination.getLatitude() +"," + destination.getLongitude() +
                        "&mode=transit" +
                        "&language=pt-BR" +
                        "&key=AIzaSyCgL70drGeVpDEz40qIiYoqdwP6gKmnXl8";
                Log.d("Request String" , requestString);
                direction = new DirectionSearchTask().execute(requestString).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (direction != null){
                if (direction.routes.size()>0){
                    //Buscar Informações Lista das Linhas p/ cada Rota

                    //Exibir Lista de Rotas para o usuário (nova atividade)
                    Intent showRoutesOptionsToUser = new Intent(MapsActivity.this, RoutesOptionActivity.class);
                   // Bundle extras = new Bundle();
                    Gson gson = new Gson();
                    String teste = gson.toJson(direction.routes);
                   // extras.putSerializable("routesList", (Serializable)geson );
                    //showRoutesOptionsToUser.putExtras(extras);
                    showRoutesOptionsToUser.putExtra("json", teste);
                    startActivityForResult(showRoutesOptionsToUser, SHOW_ROUTES_RESULT_CODE);
                }
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check that it is the SecondActivity with an OK result
        if (requestCode == SHOW_ROUTES_RESULT_CODE) {
            if (resultCode == RESULT_OK) {

                // get String data from Intent
                int returnValue = data.getIntExtra("listposition", -1);

                if (returnValue == -1){
                    Toast.makeText(this, "Erro. Cod: 2000", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "Opção de Rota Escolhida: " + String.valueOf(returnValue+1), Toast.LENGTH_SHORT).show();
                    Log.d("Escolha", "Usuario escolheu a opção de rota " + String.valueOf(returnValue));
                    retorno_opcao_rota = returnValue;
                    //Com base nesse retorno, procurar informações dos onibus e jogar na tela.

                    String requestString=  "http://rest.riob.us/v3/search/";
                    ArrayList<String> nomeOnibus = direction.routes.get(retorno_opcao_rota).getNomeOnibus();

                    //tratando nome dos ônibus (retirando caracteres alfa)
                    for (int i=0; i<nomeOnibus.size(); i++){
                        String nomeOnibus_tratado  = nomeOnibus.get(i);
                        nomeOnibus_tratado = nomeOnibus_tratado.replaceAll("\\D+","");
                        requestString += nomeOnibus_tratado;
                        if (i!=nomeOnibus.size()-1) // Enquanto não for a Ultima Iteração adicionar a virgula
                        {
                            requestString += ",";
                        }
                    }

                    //Desenhando Polyline da Rota Escolhida
                    DrawRoutePolyline(direction.routes.get(retorno_opcao_rota));

                    try {
                        buses_in_screen = new BusRioInfoSearchTask().execute(requestString).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
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
        mMap.setMyLocationEnabled(true);
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
    public boolean onMarkerClick(final Marker marker) {
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
                Toast.makeText(MapsActivity.this, "Colabore avaliando seu ônibus!", Toast.LENGTH_LONG).show();
                current_status.setText("ABORDO DO ÔNIBUS");
                int index = bus_markers.indexOf(marker);
                bus_i_am_in = buses_in_screen.get(index);
                TextView current_status = (TextView) findViewById(R.id.current_status);
                current_status.setText(bus_i_am_in.line + "-" + bus_i_am_in.order);
                View btn = (FloatingActionButton)findViewById(R.id.rate_fl_btn);
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Iniciar Atividade de Avaliação do ônibus
                        Intent showRatingActivity = new Intent(MapsActivity.this, RatingActivity.class);
                        showRatingActivity.putExtra("lineNumber", bus_i_am_in.line);
                        showRatingActivity.putExtra("lineOrder", bus_i_am_in.order);
                        startActivity(showRatingActivity);
                    }
                });

                View exit_btn = (FloatingActionButton)findViewById(R.id.exit_fl_btn);
                exit_btn.setVisibility(View.VISIBLE);
                exit_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Sair do Onibus
                        bus_i_am_in = null;
                        //TODO
                        //Para de escutar os sensores
                        //TODO:
                        //Processo inverso do momento de ingresso ao onibus
                        //Tornar os botões Invisíveis
                    }
                });
                new ListenMicSensorTask().execute();
                //mSensor.stop();
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

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public void DrawRoutePolyline(Route route) {
        if (drawedPolyline !=null){
            drawedPolyline.remove();
        }
        List<LatLng> list = decodePoly(route.overview_polyline.points);

        PolylineOptions options = new PolylineOptions().width(15).color(Color.BLUE).geodesic(true);
        for (int z = 0; z < list.size(); z++) {
            LatLng point = list.get(z);
            options.add(point);
        }
        drawedPolyline = mMap.addPolyline(options);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        super.onResume();

        if (event.sensor.getType() != Sensor.TYPE_AMBIENT_TEMPERATURE) return;

        float temperature;
        if (event.values.length > 0) {
            temperature = event.values[0];
            Log.d("TEMPERATURA", String.valueOf(temperature));
        }
        else{
            Log.d("TEMPERATURA", "FAIL");

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //Async Tasks Region

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
                try {
                    stream = connection.getInputStream();
                } catch (FileNotFoundException ex) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
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
                JSONArray data = new JSONArray(finalJson);
                List<Onibus> onibusLista = new ArrayList<>();

                for (int i = 0; i < data.length(); i++) {
                    JSONObject interData = data.getJSONObject(i);
                    Onibus onibus = new Onibus(interData);
                    Log.d("Onibus Instanciado", onibus.toString());
                    onibusLista.add(onibus);
                }
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
                } while (onibusLista.size() != 0);


                for (int i = 0; i < onibusPorLinha.size(); i++) {
                    //Verificando a lista da linha
                    String logPrint = onibusPorLinha.get(i).get(0).getOrdem();
                    for (int j = 1; j < onibusPorLinha.get(i).size(); j++) {
                        logPrint += ", " + onibusPorLinha.get(i).get(j).getOrdem();
                    }

                    String numeroLinha = "";
                    if (onibusPorLinha.get(i).get(0).getLinha().isEmpty()) {
                        numeroLinha = "S/LINHA";
                    } else {
                        numeroLinha = onibusPorLinha.get(i).get(0).getLinha();
                    }
                    Log.d("LINHA " + numeroLinha, logPrint);
                }

                Log.d("Quantidade de Linhas", String.valueOf(onibusPorLinha.size()));

                Location myLoc = new Location("meu local");
                myLoc.setLatitude(-22.906847);
                myLoc.setLongitude(-43.172896);

                ArrayList<Onibus> onibusPerto = new ArrayList<>();
                for (int i = 0; i < onibusPorLinha.size(); i++) {
                    for (int j = 0; j < onibusPorLinha.get(i).size(); j++) {

                        LatLng posOnibus = onibusPorLinha.get(i).get(j).getLatLong();
                        Location busLoc = new Location("busLoc");
                        busLoc.setLatitude(posOnibus.latitude);
                        busLoc.setLongitude(posOnibus.longitude);
                        double distance = myLoc.distanceTo(busLoc);
                        //Log.i("DISTANCE", String.valueOf(distance));
                        //Distância Menor que 300m
                        if (distance < 300) {
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
            } catch (ParseException e) {
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
            if (result != null) {
                //Remove all Markers
                mMap.clear();
                //Toast.makeText(getApplicationContext(), "Linha: " + result.get(0).getLinha(), Toast.LENGTH_SHORT).show();
                for (int i = 0; i < result.size(); i++) {
                    mMap.addMarker(new MarkerOptions()
                            .position(result.get(i).getLatLong())
                            .title("Linha " + result.get(i).getLinha())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_marker))
                            .snippet(result.get(i).getOrdem())
                    );
                }
                Log.d("QtdOnibusNaTela", String.valueOf(result.size()));
            }
        }
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
                if (amplitudeNoise > 3000){
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run(){
                            //update ui here
                            // display toast here
                            //Toast.makeText(MapsActivity.this, "Nível de Ruído Muito Alto, Reportando", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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

    public class DirectionSearchTask extends AsyncTask<String, String, Direction> {
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
        protected Direction doInBackground(String... params) {
            JSONObject response = null;
            Direction direction = null;
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
                            Toast.makeText(MapsActivity.this, "Erro ao recuperar rotas", Toast.LENGTH_SHORT).show();
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
                //Garanto que a resposta será apenas um Objeto "Direction"
                response = new JSONObject(finalJsonString);
                direction = new Direction(response);



            } catch (IOException e) {
                e.printStackTrace();
                return direction;
            } catch (JSONException e) {
                e.printStackTrace();
                return direction;
            }

            return direction
                    ;
        }

            @Override
        protected void onPostExecute(final Direction result) {
                super.onPostExecute(result);
                Log.d("REQUEST STATUS",result.status);
                for (int i=0; i<result.routes.size(); i++){
                    Log.d("DIRECTION " + String.valueOf(i) +" SUMMARY", result.routes.get(i).summary);
                    Log.d("DIRECTION " + String.valueOf(i) +" FARE", result.routes.get(i).fare.text);
                    for (int j=0; j<result.routes.get(i).warnings.size(); j++){
                        //Log.d("DIRECTION " + String.valueOf(i) +" WARNING", result.routes.get(i).warnings.get(j));
                    }
                    for (int j=0; j<result.routes.get(i).legs.size(); j++){
                       // Log.d("DIRECTION " + String.valueOf(i) +" LEG", result.routes.get(i).legs.get(j));
                        for (int k=0; k<result.routes.get(i).legs.get(j).steps.size(); k++){

                            if ( result.routes.get(i).legs.get(j).steps.get(k).transit_details!=null){
                                 Log.d("DIRECTION " + String.valueOf(i) +" LINE",
                                         result.routes.get(i).legs.get(j).steps.get(k).transit_details.line.short_name + "-" + result.routes.get(i).legs.get(j).steps.get(k).transit_details.line.name);
                            }
                            else{
                                Log.d("DIRECTION " + String.valueOf(i) +" LINE", "Andar " + result.routes.get(i).legs.get(j).steps.get(k).duration.text);
                            }

                        }
                    }
                }

                DrawRoutePolyline(result.routes.get(0));
                dialog.dismiss();

        }
    }

    public class BusRioInfoSearchTask extends AsyncTask<String, String, List<Bus>> {

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
        protected List<Bus> doInBackground(String... params) {
            JSONArray response = null;
            ArrayList<Bus> buses = null;
            try {
                //HTTP request rioBus
                HttpURLConnection connection = null;
                BufferedReader reader = null;
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
                            Toast.makeText(MapsActivity.this, "Erro ao recuperar informações de ônibus", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();

                String stringLine = "";
                while ((stringLine = reader.readLine()) != null) {
                    buffer.append(stringLine);
                }

                String finalJsonString = buffer.toString();
                response = new JSONArray (finalJsonString);
                buses = new ArrayList<>();
                for (int i=0; i<response.length(); i++){
                    JSONObject bus_object = response.getJSONObject(i);
                    Bus bus = new Bus();
                    Type BusType = new TypeToken<Bus>(){}.getType();
                    bus = new Gson().fromJson(bus_object.toString(), BusType);
                    buses.add(bus);
                }
                //TODO
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return buses;
        }

        @Override
        protected void onPostExecute(final List<Bus> result) {
            super.onPostExecute(result);
            //Imprimir cada Onibus no mapa
            if (result != null){
                for (int i=0; i<result.size(); i++){
                    Log.d("RESULT", result.get(i).line + " " + result.get(i).order+ " "  + result.get(i).latitude + " " + result.get(i).longitude );
                }
                if (bus_markers != null){
                    for (int i=0; i<bus_markers.size(); i++){
                        bus_markers.get(i).remove();
                    }
                    bus_markers.clear();
                }

                bus_markers = new ArrayList<>();
                //mMap.clear();

                //instantiates a calendar using the current time in the specified timezone
                //change the timezone
                TimeZone tz = TimeZone.getTimeZone("America/Sao_Paulo");
                TimeZone.setDefault(tz);
                Calendar cSchedStartCal = GregorianCalendar.getInstance(tz);
                SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                Log.d("HOUR", format1.format(cSchedStartCal.getTime()));

                long compare1 = cSchedStartCal.getTime().getTime();

                for (int i = 0; i < result.size(); i++) {
                    LatLng pos = new LatLng(result.get(i).latitude, result.get(i).longitude);

                    long compare2 = result.get(i).timeStamp.getTime();

                    long duration  = compare2 - compare1;

                  //  long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
                    long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
                  //  long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);

  //                  Log.d("TIME DIFFERENCE seg", String.valueOf(diffInSeconds));
    //                Log.d("TIME DIFFERENCE hour", String.valueOf(diffInHours));

                    diffInMinutes = Math.abs(diffInMinutes);

                    Log.d("TIME DIFFERENCE min", String.valueOf(diffInMinutes));

                    Marker busMarker = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title("Linha " + result.get(i).line)
                            .snippet(result.get(i).order)
                    );



                    if (diffInMinutes < 5){
                        busMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_green));
                    }
                    if (diffInMinutes >= 5 && diffInMinutes < 10){
                        busMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_yellow));
                    }
                    if (diffInMinutes >= 10){
                        busMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_red));
                    }
                    bus_markers.add(busMarker);
                }
            }


            dialog.dismiss();

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



    //Async Tasks Region End


}

