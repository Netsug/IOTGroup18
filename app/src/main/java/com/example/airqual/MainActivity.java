package com.example.airqual;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.Manifest;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;


import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
        private String apiKey = "AIzaSyCgqT25H7Vqusyow_eiqdBQPBt1Kw5PxA8";
    //private String address = "Borgarfjordsgatan 12, 164 55 Kista";
    private String latlng = "59.40704825544182,17.94577779678242";
    private double latitude = 59.40704825544182;
    private double longitude = 17.94577779678242;
    private final int FINE_PERMISSION_CODE = 1;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        geocodeAddress(apiKey, latlng);*/
        fetchPollen(apiKey, latitude, longitude);

    }


    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    Log.d("123", "123");
                    Log.d("Lat1", "" + currentLocation.getLatitude());
                    Log.d("Long1", "" + currentLocation.getLongitude());

                }
                else{
                    Log.d("1455423", "1453");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            Toast.makeText(this, "Location permission not allowed", Toast.LENGTH_SHORT).show();
        }
    }

    public static void geocodeAddress(String apiKey, String latlng) {
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... voids) {
                try {
                    // Correctly set up the URL for the Geocoding API request
                    String urlString = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+latlng+"&key="+apiKey;

                    // Create a URL object
                    URL url = new URL(urlString);

                    // Open a connection to the URL
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // Set the request method to GET
                    connection.setRequestMethod("GET");

                    // Get the response code
                    int responseCode = connection.getResponseCode();

                    // Read the response from the API
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Close the reader and the connection
                    reader.close();
                    connection.disconnect();

                    // Check if the request was successful (status code 200)
                    if (responseCode == 200) {
                        String jsonString = response.toString();
                        Log.d("jsonString", jsonString);
                    } else {
                        Log.d("not successful", "Geocoding request failed with status code: " + responseCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(String result) {
                Log.d("MainActivity", "Geocoding result: " + result);
            }
        }.execute();
    }

    public static void fetchPollen(String apiKey, double lat, double lng) {
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... voids) {
                try {
                    // Correctly set up the URL for the Geocoding API request
                    String urlString = "https://pollen.googleapis.com/v1/forecast:lookup?key="+apiKey+"&location.longitude="+lng+"&location.latitude="+lat+"&days=1";

                    // Create a URL object
                    URL url = new URL(urlString);

                    // Open a connection to the URL
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // Set the request method to GET
                    connection.setRequestMethod("GET");

                    // Get the response code
                    int responseCode = connection.getResponseCode();

                    // Read the response from the API
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Close the reader and the connection
                    reader.close();
                    connection.disconnect();

                    // Check if the request was successful (status code 200)
                    if (responseCode == 200) {
                        String jsonString = response.toString();
                        parsePollen(jsonString);
                        JSONObject json = new JSONObject(jsonString);

                        Log.d("jsonString", jsonString);
                    } else {
                        Log.d("not successful", "Geocoding request failed with status code: " + responseCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            public void parsePollen(String pollenInfo) {
                String jsonResponse = pollenInfo;

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(jsonResponse);

                    ArrayList<String> desiredPollenTypes = new ArrayList<>();
                    desiredPollenTypes.add("GRASS");
                    desiredPollenTypes.add("WEED");

                    String desiredPollenType = "GRASS";  // Replace this with the type of pollen you want to select

                    JsonNode dailyInfoNode = jsonNode.get("dailyInfo");
                    for (JsonNode dailyInfo : dailyInfoNode) {
                        JsonNode pollenTypeInfoNode = dailyInfo.get("pollenTypeInfo");
                        for (JsonNode pollenType : pollenTypeInfoNode) {
                            String pollenCode = pollenType.get("code").asText();
                            if (desiredPollenTypes.contains(pollenCode)){
                                // Found the desired pollen type
                                String displayName = pollenType.get("displayName").asText();
                                boolean inSeason = pollenType.get("inSeason").asBoolean();

                                // Access other attributes as needed
                                // ...

                                System.out.println("Pollen Type: " + displayName);
                                //System.out.println("In Season: " + inSeason);
                                Log.d("Pollen Type: ", displayName);
                                Log.d("In Season: ", ""+inSeason);

                                // Print additional attributes
                                // ...

                                break;  // Exit the loop once the desired pollen type is found
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            /*protected void onPostExecute(String result) {
                Log.d("MainActivity", "Pollen result: " + result);
            }*/
        }.execute();
    }

}
