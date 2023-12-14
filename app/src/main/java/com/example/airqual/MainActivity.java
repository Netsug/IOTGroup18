package com.example.airqual;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;


import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements PollenItemAdapter.OnPollenItemClickListener, PollutantItemAdapter.OnPollutantItemClickListener {

    private final String KISTA_ADDRESS = "Borgarfjordsgatan 12, 164 55 Kista";
    private final String KISTA_LOCATION = "59.40704825544182,17.94577779678242";
    private final double KISTA_LATITUDE = 59.40704825544182;
    private final double KISTA_LONGITUDE = 17.94577779678242;

    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DrawerLayout drawerLayout;
    private LinearLayout allergenSelectionDrawer;

    private ListView pollenTypesListView;
    private ListView pollutantListView;
    private PollenItemAdapter pollenItemAdapter;
    private HashMap<String, Boolean> checkBoxStates = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String apiKey = getString(R.string.api_key);

        drawerLayout = findViewById(R.id.drawer_layout);
        allergenSelectionDrawer = findViewById(R.id.allergen_drawer);
        LinearLayout allergenList = findViewById(R.id.allergen_list);

        addAllergiesToDrawer("Please select your pollen allergies", getPollen(), allergenList);

        ImageButton buttonOpenDrawer = findViewById(R.id.btn_hamburger);
        buttonOpenDrawer.setOnClickListener(view -> {
            drawerLayout.openDrawer(allergenSelectionDrawer);
        });

        ImageButton buttonCloseDrawer = findViewById(R.id.btn_x_icon);
        buttonCloseDrawer.setOnClickListener(view -> {
            drawerLayout.closeDrawer(allergenSelectionDrawer);
        });

        Button saveButton = findViewById(R.id.btn_save);
        saveButton.setOnClickListener(view -> {
            checkStates();
        });

        pollenTypesListView = findViewById(R.id.pollen_types);
        pollutantListView = findViewById(R.id.air_pollutants);

        //geocodeAddress(apiKey, KISTA_LOCATION);
        fetchAirQuality(apiKey, 51.500000, 0.120000, this);
        //When pollen value is 0, there is nothing in the respective part of the response
        double LONGITUDE = 31.06549440597758;
        double LATITUDE = 31.204389882873883;
        fetchPollen(apiKey, LATITUDE, LONGITUDE, this);

    }

    private void addAllergiesToDrawer(String title, String[] allergies, LinearLayout layout) {
        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        layout.addView(tvTitle);

        for (String allergy : allergies) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(allergy);

            // Initialize each checkbox state as false (unchecked)
            checkBoxStates.put(allergy, false);

            // Set a listener to update the state when the checkbox is checked/unchecked
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkBoxStates.put(allergy, isChecked);
            });

            layout.addView(checkBox);
        }
    }

    private static String[] getPollen() {
        String[] pollen = {
                "Hazel",
                "Ash",
                "Cottonwood",
                "Oak",
                "Pine",
                "Birch",
                "Olive",
                "Graminales",
                "Ragweed",
                "Alder",
                "Mugwort",
                "Elm",
                "Maple",
                "Juniper",
                "Cypress pine"
        };

        return pollen;
    }

    public void checkStates() {
        for (Map.Entry<String, Boolean> entry : checkBoxStates.entrySet()) {
            String allergy = entry.getKey();
            Boolean isChecked = entry.getValue();
            // Process the checked state as needed
            if (isChecked) {

            }
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final int FINE_PERMISSION_CODE = 1;
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

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

    private static void geocodeAddress(String apiKey, String latlng) {
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

    private static void fetchAirQuality (String apiKey, double lat, double lng, MainActivity activity) {
        new AsyncTask<Void, Void, ArrayList<Pollutant>>() {
            protected ArrayList<Pollutant> doInBackground(Void... voids) {

                try {
                    // Set up the URL for the Geocoding API request
                    final String urlString = "https://airquality.googleapis.com/v1/currentConditions:lookup?key=" + apiKey;

                    // Create a URL object
                    URL url = new URL(urlString);

                    // Open a connection to the URL
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // Set the request method to POST
                    connection.setRequestMethod("POST");

                    // Set request headers
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");

                    // Enable input/output streams
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    // Prepare JSON input for the request
                    final String jsonInputString = "{" +
                            "\"location\": {" +
                            "\"latitude\":" + lat + "," +
                            "\"longitude\":" + lng +
                            "}," +
                            "\"extraComputations\": [" +
                            "\"LOCAL_AQI\"," +
                            "\"HEALTH_RECOMMENDATIONS\"," +
                            "\"POLLUTANT_ADDITIONAL_INFO\"," +
                            "\"DOMINANT_POLLUTANT_CONCENTRATION\"," +
                            "\"POLLUTANT_CONCENTRATION\"," + 
                            "\"EXTRA_COMPUTATION_UNSPECIFIED\"," +
                            "]" +
                            "}";

                    // The JSON-request
                    // Log.d("Request JSON", jsonInputString);

                    // Send JSON data in the request body
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    // Get the response code
                    int responseCode = connection.getResponseCode();

                    // Read the response from the API
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }

                            Log.d("API Response", response + "");
                            return parseExtendedAirQuality(response+"");
                        }
                    } else {
                        Log.d("API TRY: ", jsonInputString);
                        Log.d("API Request Failed", "Air Quality API request failed with status code: " + responseCode);
                    }

                    // Disconnect the connection
                    connection.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

            }

            private ArrayList<Pollutant> parseExtendedAirQuality(String extendedAirQualityInfo) {
                final String jsonResponse = extendedAirQualityInfo;
                ArrayList<Pollutant> pollutants = new ArrayList<>();

                try {
                    JSONObject json = new JSONObject(jsonResponse);

                    String dateTime = json.getString("dateTime");
                    String regionCode = json.getString("regionCode");

                    Log.d("DateTime", dateTime);
                    Log.d("RegionCode", regionCode);

                    JSONArray indexesArray = json.getJSONArray("indexes");

                    for (int i = 0; i < indexesArray.length(); i++) {
                        JSONObject indexObject = indexesArray.getJSONObject(i);

                        String indexCode = indexObject.getString("code");
                        String indexDisplayName = indexObject.getString("displayName");
                        int aqi = indexObject.getInt("aqi");
                        String aqiDisplay = indexObject.getString("aqiDisplay");
                        String category = indexObject.getString("category");
                        String dominantPollutant = indexObject.getString("dominantPollutant");

                        Log.d("IndexCode", indexCode);
                        Log.d("IndexDisplayName", indexDisplayName);
                        Log.d("AQI", String.valueOf(aqi));
                        Log.d("AQIDisplay", aqiDisplay);
                        Log.d("Category", category);
                        Log.d("DominantPollutant", dominantPollutant);

                        // TODO: PASS INT TO TITLE: AQI ONLY

                    }

                    JSONArray pollutantsArray = json.getJSONArray("pollutants");

                    for (int i = 0; i < pollutantsArray.length(); i++) {
                        JSONObject pollutantObject = pollutantsArray.getJSONObject(i);

                        String pollutantCode = pollutantObject.getString("code");
                        String pollutantDisplayName = pollutantObject.getString("displayName");
                        String pollutantFullName = pollutantObject.getString("fullName");

                        JSONObject concentrationObject = pollutantObject.getJSONObject("concentration");
                        double concentrationValue = concentrationObject.getDouble("value");
                        String concentrationUnits = concentrationObject.getString("units");

                        Log.d("PollutantCode", pollutantCode);
                        Log.d("PollutantDisplayName", pollutantDisplayName);
                        Log.d("PollutantFullName", pollutantFullName);
                        Log.d("ConcentrationValue", String.valueOf(concentrationValue));
                        Log.d("ConcentrationUnits", concentrationUnits);

                        JSONObject healthRecommendationsObject = json.getJSONObject("healthRecommendations");
                        Log.d("GeneralPopulation", healthRecommendationsObject.getString("generalPopulation"));

                        Pollutant pollutant = new Pollutant(pollutantDisplayName, concentrationValue, concentrationUnits, healthRecommendationsObject.getString("generalPopulation"));
                        pollutants.add(pollutant);

                    }

                    // TODO: Add logging for other health recommendations.
                    return pollutants;

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(ArrayList<Pollutant> result) {
                if (result != null && !result.isEmpty()) {
                    // Create and set the adapter
                    activity.runOnUiThread(() -> {
                        PollutantItemAdapter adapter = new PollutantItemAdapter(activity, result, activity);
                        activity.pollutantListView.setAdapter(adapter);
                    });
                }
            }
        }.execute();
    }

    private static void fetchPollen(String apiKey, double lat, double lng, MainActivity activity) {
        new AsyncTask<Void, Void, ArrayList<Pollen>>() {
            protected ArrayList<Pollen> doInBackground(Void... voids) {
                try {
                    ArrayList<Pollen> pollens;
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

                        Log.d("jsonString response", jsonString);
                        return parsePollen(jsonString);
                    } else {
                        Log.d("Not successful, bad response code", "Pollen request failed with response code: " + responseCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            private ArrayList<Pollen> parsePollen(String pollenInfo) {

                final String jsonResponse = pollenInfo;
                ArrayList<Pollen> pollens = new ArrayList<>();

                try {
                    Log.d("parsePollen", "Starting JSON parsing.");
                    JSONObject json = new JSONObject(jsonResponse);
                    JSONArray dailyInfoArray = json.getJSONArray("dailyInfo");

                    for (int i = 0; i < dailyInfoArray.length(); i++) {
                        JSONObject dailyInfoObject = dailyInfoArray.getJSONObject(i);
                        JSONArray plantInfoArray = dailyInfoObject.getJSONArray("plantInfo");

                        for (int j = 0; j < plantInfoArray.length(); j++) {

                            JSONObject plantInfoObject = plantInfoArray.getJSONObject(j);
                            String displayName = plantInfoObject.getString("displayName");
                            Log.d("parsePollen", "Processing plant: " + displayName);

                            if (plantInfoObject.has("indexInfo")) {
                                JSONObject indexInfoObject = plantInfoObject.getJSONObject("indexInfo");
                                String indexValue = indexInfoObject.getString("value");
                                String indexCategory = indexInfoObject.getString("category");
                                String indexDescription = indexInfoObject.getString("indexDescription");

                                Log.d("parsePollen", "Index Info - Value: " + indexValue + ", Category: " + indexCategory + ", Description: " + indexDescription);

                                String season = "", crossReaction = "", type = "";
                                if (plantInfoObject.has("plantDescription")) {
                                    JSONObject plantDescriptionObject = plantInfoObject.getJSONObject("plantDescription");
                                    season = plantDescriptionObject.getString("season");
                                    crossReaction = plantDescriptionObject.getString("crossReaction");
                                    type = plantDescriptionObject.getString("type");

                                    Log.d("parsePollen", "Plant Description - Season: " + season + ", Cross Reaction: " + crossReaction + ", Type: " + type);
                                }

                                Pollen pollen = new Pollen(displayName, indexValue, indexCategory, indexDescription, season, crossReaction, type);
                                pollens.add(pollen);
                            } else {
                                Log.d("parsePollen", "No index info for plant: " + displayName);

                                Pollen pollen = new Pollen(displayName, "", "", "", "", "", "");
                                pollens.add(pollen);
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("parsePollen", "Error parsing JSON: " + e.getMessage());
                    return null;
                }
                Log.d("parsePollen", "Finished parsing. Total plants processed: " + pollens.size());
                return pollens;
            }

            protected void onPostExecute(ArrayList<Pollen> result) {
                if (result != null && !result.isEmpty()) {
                    // Create and set the adapter
                    activity.runOnUiThread(() -> {
                        PollenItemAdapter adapter = new PollenItemAdapter(activity, result, activity);
                        activity.pollenTypesListView.setAdapter(adapter);



                    });
                }
            }

        }.execute();
    }

    public void showCardView(Pollen pollen) {
        Log.d("display card view", "true");
        CardView cardView = findViewById(R.id.recommendation_card);
        TextView tvTitle = findViewById(R.id.card_title);
        tvTitle.setText(pollen.getDisplayName());
        TextView tvRecommendations = findViewById(R.id.information_text);
        tvRecommendations.setText(pollen.getIndexDescription());

        cardView.setVisibility(View.VISIBLE);

        ImageButton buttonDismiss = findViewById(R.id.btn_dismiss_card);

        buttonDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set the CardView's visibility to GONE or INVISIBLE
                cardView.setVisibility(View.GONE); // or View.INVISIBLE
            }
        });
    }

    public void showPollutantCardView(Pollutant pollutant) {
        Log.d("display card view", "true");
        CardView cardView = findViewById(R.id.recommendation_card);

        TextView tvTitle = findViewById(R.id.card_title);
        tvTitle.setText(pollutant.getName());

        TextView tvInformation = findViewById(R.id.information_text);
        final String str = pollutant.getNonScientificName() + " - " + pollutant.getConcentration() +
                "\n\n" + "The Safe Cutoff for " + pollutant.getNonScientificName() + " is " +
                pollutant.getSafeAmountCutoff() + "\n\n" + pollutant.getRecommendations();
        tvInformation.setText(str);

        cardView.setVisibility(View.VISIBLE);
        //CardView recommendationsCard = new CardView(getContext());

        ImageButton buttonDismiss = findViewById(R.id.btn_dismiss_card);

        buttonDismiss.setOnClickListener(view -> {
            // Set the CardView's visibility to GONE or INVISIBLE
            cardView.setVisibility(View.GONE); // or View.INVISIBLE
            Log.d("Click" , pollutant.getName()+ "");
        });
    }

}
