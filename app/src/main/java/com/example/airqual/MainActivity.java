package com.example.airqual;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;



import android.content.pm.PackageManager;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    // Replace 'YOUR_API_KEY' with your actual Google Maps API key
    String apiKey = "AIzaSyCgqT25H7Vqusyow_eiqdBQPBt1Kw5PxA8";

    // Example address for geocoding
    String address = "Borgarfjordsgatan 12, 164 55 Kista";
    String latlng = "59.40704825544182,17.94577779678242";
    String latitude = "59.40704825544182";
    String longitude = "17.94577779678242";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "hello");
        geocodeAddress(apiKey, latlng);
        Log.d("MainActivity", "hello2");

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

}
