package com.example.tiago.busbasix;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.zxing.client.result.AddressBookAUResultParser;


import java.io.IOException;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button inputDataBtn;
    private Double lat;
    private Double lng;
    private static final int REQUEST_LOCATION = 2;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView enderecosText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        enderecosText = (TextView)findViewById(R.id.enderecos_text);
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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (lat == null){
                            lat=location.getLatitude();
                        }
                        if(lng == null)
                        {
                            lng =location.getLongitude();
                        }
                        LatLng curPoint = new LatLng(lat, lng);
                        double new_lat = location.getLatitude();
                        double new_long = location.getLongitude();
                        LatLng latLng = new LatLng(new_lat, new_long);
                        mMap.addPolyline(new PolylineOptions().add(curPoint, latLng).width(10).color(Color.BLUE));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(  18.0f));

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

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });

            }
            else {
                System.out.println("GPS PROVIDER INDISPONIVEL");
            }


        }


    public void onSearch(View view){
        EditText location_tf = (EditText)findViewById(R.id.destText);
        String location = location_tf.getText().toString();
        List<Address> addressList =null;

        if (location != null || !location.equals("")){
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try{
                addressList = geocoder.getFromLocationName(location, 1);
            }catch (IOException e){
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng lat_long_search = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(lat_long_search).title("Marker on search"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lat_long_search));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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
            System.out.println("LOCATION DISABLED");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        mMap.setMyLocationEnabled(true);


    }

}
