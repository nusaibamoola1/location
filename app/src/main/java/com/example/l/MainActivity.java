package com.example.l;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView AddressText;
    private Button LocationButton;
    private Button registerButton;
    private LocationRequest locationRequest;

    String fullAddress;
    String username;
    int x;
    String type;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AddressText = findViewById(R.id.addressText);
        LocationButton = findViewById(R.id.locationButton);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getCurrentLocation();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRegister(v);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (isGPSEnabled()) {

                    getCurrentLocation();

                }else {

                    turnOnGPS();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() >0){

                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        AddressText.setText("Latitude: "+ latitude + "\n" + "Longitude: "+ longitude);
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }

    /////////////////////////////////////////////////////////////////////////////////////

    public void doRegister(View v) {
        EditText Username = (EditText)findViewById(R.id.Username);
        EditText Password = (EditText)findViewById(R.id.Password);

        String username = Username.getText().toString();
        String password = Password.getText().toString();

        OkHttpClient client = new OkHttpClient();
        String url = "https://lamp.ms.wits.ac.za/~s2430972/users.php";

        RequestBody formBody = new FormBody.Builder()
                .add("Username", username)
                .add("Password", password)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    final TextView reg = (TextView)findViewById(R.id.isRegistered);
                    final String resp = response.body().string();

                    System.out.println(resp);

                    if(resp.contains("False")){
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                reg.setVisibility(View.VISIBLE);
                                reg.setText("Existing Account");
                            }
                        });
                    }
                    else if(resp.contains("True")){
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(MainActivity.this, a.class);
                                startActivity(i);
                            }
                        });

                    }
                }
            }
        });
    }

    public void confirm(View v){
        CheckBox vol =(CheckBox)findViewById(R.id.Volunteer);
        CheckBox atrisk =(CheckBox)findViewById(R.id.AtRisk);

        if(vol.isChecked() && atrisk.isChecked()){
            Toast.makeText(this, "Only choose one",Toast.LENGTH_LONG).show();

        }
        else if(vol.isChecked()==false && atrisk.isChecked()==false){
            Toast.makeText(this, "Select user type",Toast.LENGTH_LONG).show();
        }
        else if(vol.isChecked()){
            x = 1;
            type ="Volunteer";
            url ="https://lamp.ms.wits.ac.za/~s2430972/volunteers.php";
            OkHttpClient client = new OkHttpClient();
            RequestBody Formbody = new FormBody.Builder()
                    .add("username", username)
                    .add("type", type)
                    .add("address", fullAddress)
                    .build();

            Request request = new Request.Builder().url(url).post(Formbody).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String result = response.body().string();
                    System.out.println(result);
                    if (result.contains("Updated")) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MainActivity.this, a.class);
                                startActivity(intent);
                            }
                        });
                    } else if (result.trim().equalsIgnoreCase("Failed")) {
                        System.out.println("Cannot insert");
                    }
                }
            });
        }
        else{
            x = 0;
            type = "At Risk";
            url ="https://lamp.ms.wits.ac.za/~s2430972/atrisk.php";
            OkHttpClient client = new OkHttpClient();
            RequestBody Formbody = new FormBody.Builder()
                    .add("email", username)
                    .add("type", type)
                    .add("address", fullAddress)
                    .build();

            Request request = new Request.Builder().url(url).post(Formbody).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String result = response.body().string();
                    System.out.println(result);
                    if (result.contains("Updated")) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MainActivity.this, a.class);
                                startActivity(intent);
                            }
                        });
                    } else if (result.contains("Failed")) {
                        System.out.println("Failed to insert");
                    }
                }
            });
        }
    }
}