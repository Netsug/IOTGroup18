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

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private final String KISTA_ADDRESS = "Borgarfjordsgatan 12, 164 55 Kista";
    private final String KISTA_LOCATION = "59.40704825544182,17.94577779678242";

    private double KISTA_LATITUDE = 59.40704825544182;

    private double KISTA_LONGITUDE = 17.94577779678242;
    private double WESTBANK_LONGITUDE = 35.32704825544182;
    private double WESTBANK_LATITUDE = 32.32577779678242;
    private final int FINE_PERMISSION_CODE = 1;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String apiKey = getString(R.string.api_key);

        //geocodeAddress(apiKey, KISTA_LOCATION);
        fetchPollen(apiKey, KISTA_LATITUDE, KISTA_LONGITUDE);

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
                    String urlString = "https://pollen.googleapis.com/v1/forecast:lookup?key="+apiKey+"&location.longitude="+lng+"&location.latitude="+lat+"&days=1&plantsDescription=0";

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
                final String jsonResponse = pollenInfo;

                try {
                    JSONObject json = new JSONObject(jsonResponse);

                    String regionCode = json.getString("regionCode");           // Region Code / eller i vilket land lat/longituderna är
                    JSONArray dailyInfoArray = json.getJSONArray("dailyInfo");

                    ///////////////////////////////////////////////////////
                    // TODO: Göra något med desiredPollenTypes såsmåningom.
                    ArrayList<String> desiredPollenTypes = new ArrayList<>();
                    desiredPollenTypes.add("GRASS");
                    desiredPollenTypes.add("WEED");
                    ////////////////////////////////////////////////////////

                    for (int i = 0; i < dailyInfoArray.length(); i++) {
                        JSONObject dailyInfoObject = dailyInfoArray.getJSONObject(i);

                        JSONObject dateObject = dailyInfoObject.getJSONObject("date");      // Dagens datum.
                        int year = dateObject.getInt("year");                               // 2023
                        int month = dateObject.getInt("month");                             // 12
                        int day = dateObject.getInt("day");                                 // 5

                        JSONArray pollenTypeInfoArray = dailyInfoObject.getJSONArray("pollenTypeInfo");

                        /////////
                        Log.d("Year", year + "");
                        Log.d("Month", month + "");
                        Log.d("Day", day + "");
                        /////////

                        for (int j = 0; j < pollenTypeInfoArray.length(); j++) {
                            JSONObject pollenTypeInfoObject = pollenTypeInfoArray.getJSONObject(j);
                            String pollenTypeCode = pollenTypeInfoObject.getString("code");                 // Kod, I.E GRASS / TREE etc
                            String pollenTypeDisplayName = pollenTypeInfoObject.getString("displayName");   // Samma som Code men med Stor Versal och små gemen, Grass, Tree etc...

                            ////////////
                            Log.d("PollenTypeCode", pollenTypeCode + "");
                            Log.d("PollenTypeDisplayName", pollenTypeDisplayName + "");
                            ////////////

                            // TODO: Göra vad vi behöver med den här infon.
                        }

                        JSONArray plantInfoArray = dailyInfoObject.getJSONArray("plantInfo");

                        for (int j = 0; j < plantInfoArray.length(); j++) {
                            JSONObject plantInfoObject = plantInfoArray.getJSONObject(j);

                            String plantCode = plantInfoObject.getString("code");               // Plantans Kod I.E ASH, HAZEL, OAK
                            String plantDisplayName = plantInfoObject.getString("displayName"); // Samma men stor bla bla blah i.e Ash, Hazel, Oak

                            //////////////
                            Log.d("PlantCode", plantCode + "");
                            Log.d("PlantDisplayName",plantDisplayName + "");
                            //////////////
                        }

                        /*
                        //////////////////////////////////////////////////////////////

                        Av någon anledning får min respons ingen fakta om nivåerna av varje pollen.
                        Men det spelare ingen roll för principen är detsamma för alla attributer.
                        Nytt object inuti ett annat blir som en ny for-loop att loopa igenom.
                        Till exempel innehåller daily info pollentypeinfo som innehåller code



                        Yttre Loop (for-loopen över dailyInfoArray):
                        Syfte: Itererar över arrayen med daglig information.
                        Vad den gör: Extraherar information för varje dag, inklusive datum, pollenTypeInfo och plantInfo arrayer.

                        Inre Loop (for-loopen över pollenTypeInfoArray):
                        Syfte: Itererar över arrayen med pollen typer för varje dag.
                        Vad den gör: Extraherar information om varje pollen typ, som kod och displayName.

                        Inre Loop (for-loopen över plantInfoArray):
                        Syfte: Itererar över arrayen med växttyper för varje dag.
                        Vad den gör: Extraherar information om varje växttyp, som kod och displayName.


                        //////////////////////////////////////////////////////////////
                         */

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
