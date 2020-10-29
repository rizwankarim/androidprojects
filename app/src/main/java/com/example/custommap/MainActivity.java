package com.example.custommap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button CurrentLoc, Tracking;
    TextView Longitude, Latitude, Address, Country, City, Province;
    LocationManager locationManager;
    FusedLocationProviderClient fusedLocation;
    Geocoder gcd;
    Location location;
    double lat, lon;
    String provider, myAddress, country, city, province;
    final int MY_PERMISSION_REQUEST_CODE = 7171;
    private static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getCurrenLocation();

        }
        CurrentLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions( MainActivity.this,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    OnGPS();
                } else {
                    getCurrenLocation();
                }
            }
        });

        Tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, MapsActivity.class);
                Bundle b= new Bundle();
                b.putDouble("Longitude",lon);
                b.putDouble("Latitude",lat);
                b.putString("Address",myAddress);
                b.putString("Country",country);
                b.putString("City",city);
                b.putString("Province",province);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });
    }

    public void initComponents() {
        CurrentLoc = findViewById(R.id.currentLoc);
        Tracking = findViewById(R.id.trackRoute);
        Longitude = findViewById(R.id.longitude);
        Latitude = findViewById(R.id.latitude);
        Address = findViewById(R.id.address);
        Country = findViewById(R.id.country);
        City = findViewById(R.id.city);
        Province = findViewById(R.id.province);
    }

    public void getCurrenLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        getDetails();

    }

    private void getDetails(){
        Latitude.setText("");
        Longitude.setText("");
        Address.setText("");
        Country.setText("");
        City.setText("");
        Province.setText("");
        fusedLocation = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else {
            fusedLocation.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lat=location.getLatitude();
                                lon=location.getLongitude();
                                Latitude.setText(String.valueOf(lat));
                                Longitude.setText(String.valueOf(lon));
                                gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                                List<android.location.Address> address = null;
                                try {
                                    address = gcd.getFromLocation(lat, lon, 1);
                                    if (address.size() > 0) {
                                        myAddress = address.get(0).getAddressLine(0);
                                        country= address.get(0).getCountryName();
                                        city= address.get(0).getLocality();
                                        province= address.get(0).getAdminArea();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            Address.setText(myAddress);
                            Country.setText(country);
                            City.setText(city);
                            Province.setText(province);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MapDemoActivity", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getCurrenLocation();
                break;
        }
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}