package com.example.airqual;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PollenItemAdapter.OnPollenItemClickListener, PollutantItemAdapter.OnPollutantItemClickListener {

    private final String KISTA_ADDRESS = "Borgarfjordsgatan 12, 164 55 Kista";
    private final String KISTA_LOCATION = "59.40704825544182,17.94577779678242";
    private final double KISTA_LATITUDE = 59.40704825544182;
    private final double KISTA_LONGITUDE = 17.94577779678242;
    private static final int MIN_TIME = 1000; // Minimum time interval between location updates in milliseconds
    private static final int MIN_DISTANCE = 10; // Minimum distance between location updates in meters

    private DrawerLayout drawerLayout;
    private LinearLayout allergenSelectionDrawer;

    private ListView pollenListView;
    private ListView pollutantListView;
    private PollenItemAdapter pollenItemAdapter;
    private ArrayList<Pollen> pollens;

    private final HashMap<String, Boolean> checkBoxStates = new HashMap<>();
    private String jsonString;
    private ArrayList<AirQualityIndex> airQualityIndices;

    private double latitude;
    private double longitude;
    private TextView tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLocation = findViewById(R.id.location_text);

        checkLocationPermissions();

        drawerLayout = findViewById(R.id.drawer_layout);
        allergenSelectionDrawer = findViewById(R.id.allergen_drawer);
        LinearLayout allergenList = findViewById(R.id.allergen_list);

        addAllergiesToDrawer(getPollen(), allergenList);

        ImageButton buttonOpenDrawer = findViewById(R.id.btn_hamburger);
        buttonOpenDrawer.setOnClickListener(view -> drawerLayout.openDrawer(allergenSelectionDrawer));

        ImageButton buttonCloseDrawer = findViewById(R.id.btn_x_icon);
        buttonCloseDrawer.setOnClickListener(view -> drawerLayout.closeDrawer(allergenSelectionDrawer));

        Button saveButton = findViewById(R.id.btn_save);
        saveButton.setOnClickListener(view -> checkStates());

        pollenListView = findViewById(R.id.pollen_types);
        pollutantListView = findViewById(R.id.air_pollutants);

        Button airQualityRecsButton = findViewById(R.id.btn_pollutant_recommendation);
        airQualityRecsButton.setOnClickListener(view -> buildTotal());
    }

    private double addDecimals(double input) {
        int totalLength = 15;
        int decimalPlaces = totalLength - String.valueOf((int) input).length() - 1;

        // Create the format string
        String formatString = "%1$" + totalLength + "." + decimalPlaces + "f";

        // Format the double value
        String formattedValue = String.format(formatString, input);

        // Convert the formatted string back to a double
        double result = Double.parseDouble(formattedValue);
        return result;
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            startLocationUpdates();
            Log.d("Log", "location");
        }
    }

    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(MainActivity.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Handle new location updates here
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                //XDDDD
                latitude = addDecimals(latitude);
                longitude = addDecimals(longitude);

                fetchPollen(getString(R.string.api_key), latitude, longitude, MainActivity.this);
                fetchAirQuality(getString(R.string.api_key), latitude, longitude, MainActivity.this);
                geocodeAddress(getString(R.string.api_key), latitude + "," + longitude, MainActivity.this);
                Log.d("latlong", latitude + ", " + longitude);
                // Do something with the latitude and longitude
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Handle changes in location provider status
            }

            @Override
            public void onProviderEnabled(String provider) {
                // Handle when a location provider is enabled
            }

            @Override
            public void onProviderDisabled(String provider) {
                // Handle when a location provider is disabled
            }
        };


        // Check if location services are enabled
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Use the GPS provider for location updates
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Use the network provider for location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        } else {
            // Handle the case when no location provider is available
            Toast.makeText(this, "Location services are not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void buildTotal() {
        TextView tvUaqiTotal = findViewById(R.id.uaqi_total);
        AirQualityIndex aqi = airQualityIndices.get(0);
        String strTitle = aqi.getIndexDisplayName() + ": " + aqi.getAqiDisplay();
        tvUaqiTotal.setText(strTitle);

        CardView cardView = findViewById(R.id.recommendation_card);
        Log.d("showtotalcardview", "showtotalcardview");

        HashMap<String, String> healthMap = airQualityIndices.get(0).getHealthRecommendation();

        String strInfo = aqi.getIndexDisplayName() + ": " + aqi.getAqiDisplay() + "/100" + "\n\n" +
                "Quality: " + aqi.getCategory() + "\n\n" +
                "Dominant Pollutant: " + aqi.getDominantPollutant() + "\n\n" +
                //aqi.getHealthRecommendation() + "\n\n" +
                "General Population: " + healthMap.get("General Population") + "\n\n" +
                "Elderly: " + healthMap.get("Elderly");

        //Maybe add more recommendations

        TextView tvCardTitle = findViewById(R.id.card_title);
        tvCardTitle.setText("Summary of Air Quality");

        TextView tvInfoCard = findViewById(R.id.information_text);
        tvInfoCard.setText(strInfo);

        ImageButton buttonDismiss = findViewById(R.id.btn_dismiss_card);
        buttonDismiss.setOnClickListener(view -> {
            // Set the CardView's visibility to GONE or INVISIBLE
            cardView.setVisibility(View.GONE); // or View.INVISIBLE
        });

        cardView.setVisibility(View.VISIBLE);

    }


    private void addAllergiesToDrawer(String[] allergies, LinearLayout layout) {
        TextView tvTitle = new TextView(this);
        tvTitle.setText("Please select your allergies, friend");
        layout.addView(tvTitle);
        //TODO: add allergies not included in response, saying "none"

        for (String allergy : allergies) {
            final String finalAllergy = allergy;
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(allergy);

            // Initialize each checkbox state as false (unchecked)
            checkBoxStates.put(finalAllergy, false);

            // Set a listener to update the state when the checkbox is checked/unchecked
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> checkBoxStates.put(allergy, isChecked));

            layout.addView(checkBox);
        }
    }

    // TODO: Make to just a string array
    private static String[] getPollen() {
        return new String[]{
                "Hazel",
                "Ash",
                "Cottonwood",
                "Oak",
                "Pine",
                "Birch",
                "Olive",
                "Grasses",
                "Ragweed",
                "Alder",
                "Mugwort",
                "Elm",
                "Maple",
                "Juniper",
                "Cypress pine"
        };
    }

    private void checkStates() {

        ArrayList<Pollen> checkedPollen = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : checkBoxStates.entrySet()) {
            String allergy = entry.getKey();
            Boolean isChecked = entry.getValue();

            for (Pollen pollen : pollens) {
                if (pollen.getDisplayName().equals(allergy) && isChecked) {
                    checkedPollen.add(pollen);
                }
            }

        }

        pollenItemAdapter.clear();
        pollenItemAdapter.addAll(checkedPollen);
        pollenItemAdapter.notifyDataSetChanged();

        pollens = parseAllPollen();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode > 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Location permission not allowed", Toast.LENGTH_SHORT).show();
        }
    }

    private static void geocodeAddress(String apiKey, String latlng, MainActivity activity) {
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... voids) {
                try {
                    // Correctly set up the URL for the Geocoding API request
                    String urlString = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latlng + "&key=" + apiKey;

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

                        ArrayList<String> parsedCity = activity.parseCityGeolocation(response.toString());

                        String city = parsedCity.get(1);

                        return city;
                    } else {
                        Log.d("not successful", "Geocoding request failed with status code: " + responseCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(String result) {
                Log.d("Hello fgrom tthe other isde", result + " tihi");
                if (result != null && !result.isEmpty()) {
                    // Create and set the adapter
                    activity.runOnUiThread(() -> {
                        activity.tvLocation.setText(result);
                        Log.d("Result:", result);
                    });
                }
            }
        }.execute();
    }

    private static void fetchAirQuality(String apiKey, double lat, double lng, MainActivity activity) {
        new AsyncTask<Void, Void, ArrayList<Pollutant>>() {
            protected ArrayList<Pollutant> doInBackground(Void... voids) {

                try {
                    // Set up the URL for the Geocoding API request
                    final String urlString = "https://airquality.googleapis.com/v1/currentConditions:lookup?key=" + apiKey;

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");

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

                    // Send JSON data in the request body
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();

                    // Read the response from the API
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }

                            Log.d("API Response", response + "");
                            return parseExtendedAirQuality(response + "");
                        }
                    } else {
                        Log.d("API TRY: ", jsonInputString);
                        Log.d("API Request Failed", "Air Quality API request failed with status code: " + responseCode);
                    }

                    connection.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            private ArrayList<Pollutant> parseExtendedAirQuality(String extendedAirQualityInfo) {
                final String jsonResponse = extendedAirQualityInfo;
                ArrayList<Pollutant> pollutants = new ArrayList<>();
                activity.airQualityIndices = new ArrayList<>();
                try {
                    JSONObject json = new JSONObject(jsonResponse);

                    String dateTime = json.getString("dateTime");
                    String regionCode = json.getString("regionCode");

                    JSONArray indexesArray = json.getJSONArray("indexes");

                    String dominantPollutant = "";

                    for (int i = 0; i < indexesArray.length(); i++) {
                        JSONObject indexObject = indexesArray.getJSONObject(i);

                        String indexCode = indexObject.getString("code");
                        String indexDisplayName = indexObject.getString("displayName");
                        int aqi = indexObject.getInt("aqi");
                        String aqiDisplay = indexObject.getString("aqiDisplay");
                        String category = indexObject.getString("category");
                        dominantPollutant = indexObject.getString("dominantPollutant");


                        AirQualityIndex airQualityIndex = new AirQualityIndex(indexCode, indexDisplayName, aqi, aqiDisplay, category, dominantPollutant);
                        activity.airQualityIndices.add(airQualityIndex);
                        Log.d("Airquallist", activity.airQualityIndices.toString());

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


                        JSONObject healthRecommendationsObject = json.getJSONObject("healthRecommendations");

                        Pollutant pollutant = new Pollutant(pollutantDisplayName, concentrationValue, concentrationUnits, healthRecommendationsObject.getString("generalPopulation"));
                        pollutants.add(pollutant);
                        //activity.airQualityIndices.get(0).addHealthRecommendations(healthRecommendationsObject.getString(("generalPopulation")));
                    }

                    JSONObject healthRecommendationsObject = json.getJSONObject("healthRecommendations");
                    HashMap<String, String> healthMap = new HashMap<>();

                    String generalHealthRec = healthRecommendationsObject.getString("generalPopulation");
                    Log.d("gen", generalHealthRec);
                    String elderly = healthRecommendationsObject.getString("elderly");

                    healthMap.put("General Population", generalHealthRec);
                    healthMap.put("Elderly", elderly);

                    activity.airQualityIndices.get(0).setHealthRecommendation(healthMap);


                    /*
                    for (int i = 0; i < pollutantsArray.length(); i++) {
                        JSONObject pollutantObject = pollutantsArray.getJSONObject(i);

                        if (dominantPollutant.equals(pollutantObject.getString("code"))) {
                            activity.airQualityIndices.get(0).setHealthRecommendation(pollutantObject.getString("healthRecommendations"));

                            break;
                        }
                        Log.d("dominantPollutant: ", dominantPollutant);
                        Log.d("pollobjectcode: ", pollutantObject.getString("code"));
                        Log.d("pollobjecthr: ", pollutantObject.getString("healthRecommendations"));


                    }*/

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
                        //activity.buildTotal();
                    });
                }
            }
        }.execute();
    }

    public static void fetchPollen(String apiKey, double lat, double lng, MainActivity activity) {
        new AsyncTask<Void, Void, ArrayList<Pollen>>() {
            protected ArrayList<Pollen> doInBackground(Void... voids) {
                try {
                    // Correctly set up the URL for the Geocoding API request
                    String urlString = "https://pollen.googleapis.com/v1/forecast:lookup?key=" + apiKey + "&location.longitude=" + lng + "&location.latitude=" + lat + "&days=1";

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

                        activity.jsonString = response.toString();

                        return activity.parseAllPollen();

                    } else {
                        Log.d("Not successful, bad response code", "Pollen request failed with response code: " + responseCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(ArrayList<Pollen> result) {
                if (result != null && !result.isEmpty()) {
                    // Create and set the adapter
                    activity.runOnUiThread(() -> {
                        activity.pollens = result;
                        activity.pollenItemAdapter = new PollenItemAdapter(activity, result, activity);
                        activity.pollenListView.setAdapter(activity.pollenItemAdapter);
                    });
                }
            }

        }.execute();
    }

    private ArrayList<String> parseCityGeolocation(String jsonResponse) throws JSONException {

        ArrayList<String> str = new ArrayList<>();

        JSONObject responseObject = new JSONObject(jsonResponse);

        // Access the 'results' JSON array
        JSONArray resultsArray = responseObject.getJSONArray("results");

        // Loop through each result
        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject result = resultsArray.getJSONObject(i);

            // Extract 'address_components' JSON array
            JSONArray addressComponents = result.getJSONArray("address_components");

            // Check each address component for 'Warraq Al Arab'
            for (int j = 0; j < addressComponents.length(); j++) {
                JSONObject addressComponent = addressComponents.getJSONObject(j);
                String longName = addressComponent.getString("long_name");

                str.add(longName);
            }

        }


        return str;
    }

    private ArrayList<Pollen> parseAllPollen() {
        ArrayList<Pollen> pollenResponse = parsePollen(jsonString);
        final String[] pollenStringArray = getPollen();
        ArrayList<Pollen> newPollenList = new ArrayList<>();

        for (int i = 0; i < getPollen().length; i++) {

            Pollen newPollen = null;
            boolean found = false;

            for (int j = 0; j < Objects.requireNonNull(pollenResponse).size(); j++) {

                if (pollenResponse.get(j).getDisplayName().equals(pollenStringArray[i])) {
                    found = true;
                    newPollenList.add(pollenResponse.get(j)); //Adds existing pollen to new list
                    break;
                }

            }

            if (!found) {
                Log.d("f", "found");

                // adds nonexisting pollens to new list
                newPollen = new Pollen(pollenStringArray[i], "0", "", "", "", "", "");
            }


            if (newPollen != null) {
                newPollenList.add(newPollen);
            }

        }

        assert pollenResponse != null;
        pollenResponse.clear();
        pollenResponse.addAll(newPollenList);

        return pollenResponse;
    }


    private ArrayList<Pollen> parsePollen(String pollenInfo) {

        final String jsonResponse = pollenInfo;
        ArrayList<Pollen> firstPollens = new ArrayList<>();

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
                        firstPollens.add(pollen);
                    } else {
                        Log.d("parsePollen", "No index info for plant: " + displayName);

                        Pollen pollen = new Pollen(displayName, "", "", "", "", "", "");
                        firstPollens.add(pollen);
                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();

            Log.e("parsePollen", "Error parsing JSON: " + e.getMessage());

            return null;
        }

        Log.d("parsePollen", "Finished parsing. Total plants processed: " + firstPollens.size());

        return firstPollens;
    }

    public void showPollenCardView(Pollen pollen) {

        CardView cardView = findViewById(R.id.recommendation_card);

        TextView tvTitle = findViewById(R.id.card_title);
        tvTitle.setText(pollen.getDisplayName());

        TextView tvRecommendations = findViewById(R.id.information_text);

        String season = "";
        String cross = "";

        if (!pollen.getSeason().equals("")) {
            season = "Is in season during " + pollen.getSeason().toLowerCase() + ".";
        }

        if (!pollen.getCrossReaction().equals("")) {
            cross = "Some cross reactions include " + pollen.getCrossReaction();
        }

        final String str = pollen.getIndexDescription() + "." + "\n\n" + pollen.getHealthRecommendation() + "\n\n" + season + "\n\n" + cross;

        tvRecommendations.setText(str);


        cardView.setVisibility(View.VISIBLE);

        ImageButton buttonDismiss = findViewById(R.id.btn_dismiss_card);
        buttonDismiss.setOnClickListener(view -> cardView.setVisibility(View.GONE));
    }

    public void showPollutantCardView(Pollutant pollutant) {
        Log.d("display card view", "true");
        CardView cardView = findViewById(R.id.recommendation_card);

        TextView tvTitle = findViewById(R.id.card_title);
        tvTitle.setText(pollutant.getName());

        TextView tvInformation = findViewById(R.id.information_text);
        /*final String str = pollutant.getNonScientificName() + " - " + pollutant.getConcentration() +
                "\n\n" + "The Safe Cutoff for " + pollutant.getNonScientificName() + " is " +
                pollutant.getSafeAmountCutoff() + "\n\n" + pollutant.getRecommendations();
        tvInformation.setText(str);*/

        final String str = pollutant.getNonScientificName() + " - " + pollutant.getConcentration() +
                "\n\n" + pollutant.getRecommendations();
        tvInformation.setText(str);

        cardView.setVisibility(View.VISIBLE);

        ImageButton buttonDismiss = findViewById(R.id.btn_dismiss_card);

        buttonDismiss.setOnClickListener(view -> {
            // Set the CardView's visibility to GONE or INVISIBLE
            cardView.setVisibility(View.GONE); // or View.INVISIBLE
        });
    }

}
