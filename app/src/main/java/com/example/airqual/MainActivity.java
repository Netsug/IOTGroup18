package com.example.airqual;

import android.Manifest;
import android.annotation.SuppressLint;
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

    private static final int MIN_TIME = 1000; // Minimum time interval between location updates in milliseconds
    private static final int MIN_DISTANCE = 30; // Minimum distance between location updates in meters
    private static final String[] pollenTypes = {
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

    private DrawerLayout drawerLayout;
    private LinearLayout allergenSelectionDrawer;

    private ListView pollenListView;
    private ListView pollutantListView;
    private PollenItemAdapter pollenItemAdapter;
    private ArrayList<Pollen> pollens;

    private final HashMap<String, Boolean> checkBoxStates = new HashMap<>();
    private String pollenAPIResponse;
    private ArrayList<AirQualityIndex> airQualityIndices;

    private double latitude;
    private double longitude;
    private TextView tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLocationPermissions();

        makeUI();
    }

    private void makeUI() {
        drawerLayout = findViewById(R.id.drawer_layout);
        allergenSelectionDrawer = findViewById(R.id.allergen_drawer);
        LinearLayout allergenList = findViewById(R.id.allergen_list);
        addAllergiesToDrawer(allergenList);

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
        final int totalLength = 15;
        final int decimalPlaces = totalLength - String.valueOf((int) input).length() - 1;

        String formatString = "%1$" + totalLength + "." + decimalPlaces + "f";

        String formattedValue = String.format(formatString, input);

        return Double.parseDouble(formattedValue);
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        final LocationManager locationManager = (LocationManager) getSystemService(MainActivity.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                // API requires certain amount of decimal places
                latitude = addDecimals(latitude);
                longitude = addDecimals(longitude);
                tvLocation = findViewById(R.id.location_text);

                fetchPollen(getString(R.string.api_key), latitude, longitude, MainActivity.this);
                fetchAirQuality(getString(R.string.api_key), latitude, longitude, MainActivity.this);
                geocodeAddress(getString(R.string.api_key), latitude + "," + longitude, MainActivity.this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Use the GPS provider for location updates
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Use the network provider for location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        } else {
            Toast.makeText(this, "Location services are not available", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void buildTotal() {
        HashMap<String, String> healthMap = airQualityIndices.get(0).getHealthRecommendation();
        TextView tvUAQITotal = findViewById(R.id.uaqi_total);
        AirQualityIndex aqiObject = airQualityIndices.get(0);
        CardView cardView = findViewById(R.id.recommendation_card);
        String strTitle = aqiObject.getIndexDisplayName() + ": " + aqiObject.getAqiDisplay();
        tvUAQITotal.setText(strTitle);

        final String strInfo = aqiObject.getIndexDisplayName() + ": " + aqiObject.getAqiDisplay() + "/100" + "\n\n" +
                "Quality: " + aqiObject.getCategory() + "\n\n" +
                "Dominant Pollutant: " + aqiObject.getDominantPollutant() + "\n\n" +
                //aqi.getHealthRecommendation() + "\n\n" +
                "General Population: " + healthMap.get("General Population") + "\n\n" +
                "Elderly: " + healthMap.get("Elderly");

        TextView tvCardTitle = findViewById(R.id.card_title);
        tvCardTitle.setText("Summary of Air Quality");

        TextView tvInfoCard = findViewById(R.id.information_text);
        tvInfoCard.setText(strInfo);

        ImageButton buttonDismiss = findViewById(R.id.btn_dismiss_card);
        buttonDismiss.setOnClickListener(view -> cardView.setVisibility(View.GONE));

        cardView.setVisibility(View.VISIBLE);
    }

    private void addAllergiesToDrawer(LinearLayout layout) {
        TextView tvTitle = new TextView(this);
        tvTitle.setText("Please select your allergies");
        layout.addView(tvTitle);

        for (String allergy : MainActivity.pollenTypes) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(allergy);

            checkBoxStates.put(allergy, false);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> checkBoxStates.put(allergy, isChecked));

            layout.addView(checkBox);
        }
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
            // User permits location request
        } else {
            Toast.makeText(this, "Location permission not allowed", Toast.LENGTH_SHORT).show();
        }
    }

    private static void geocodeAddress(final String apiKey, final String latLong, MainActivity activity) {
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... voids) {
                try {
                    final String urlString = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latLong + "&key=" + apiKey;
                    final URL url = new URL(urlString);
                    final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    connection.disconnect();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        ArrayList<String> geolocationArray = activity.parseCityGeolocation(response.toString());

                        return geolocationArray.get(3) + ", " + geolocationArray.get(4);
                    } else {
                        Log.d("API Request Failed", "Geocode request failed with status code: " + responseCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            protected void onPostExecute(String result) {
                if (result != null && !result.isEmpty()) {
                    activity.runOnUiThread(() -> activity.tvLocation.setText(result));
                }
            }
        }.execute();
    }

    private static void fetchAirQuality(final String apiKey, final double lat, final double lng, MainActivity activity) {
        new AsyncTask<Void, Void, ArrayList<Pollutant>>() {
            protected ArrayList<Pollutant> doInBackground(Void... voids) {
                try {
                    final String urlString = "https://airquality.googleapis.com/v1/currentConditions:lookup?key=" + apiKey;

                    final URL url = new URL(urlString);
                    final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

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

                            return parseAirQuality(response.toString());
                        }
                    } else {
                        Log.d("API Request Failed", "Air Quality API request failed with status code: " + responseCode);
                    }

                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            private ArrayList<Pollutant> parseAirQuality(String extendedAirQualityInfo) {
                ArrayList<Pollutant> pollutants = new ArrayList<>();
                activity.airQualityIndices = new ArrayList<>();

                try {
                    JSONObject json = new JSONObject(extendedAirQualityInfo);
                    JSONArray indexesArray = json.getJSONArray("indexes");
                    String dominantPollutant = "";

                    for (int i = 0; i < indexesArray.length(); i++) {
                        JSONObject indexObject = indexesArray.getJSONObject(i);
                        String indexCode = indexObject.getString("code");
                        String indexDisplayName = indexObject.getString("displayName");
                        String aqiDisplay = "";
                        String category = "";

                        try {
                            aqiDisplay = indexObject.getString("aqiDisplay");
                            category = indexObject.getString("category");
                            dominantPollutant = indexObject.getString("dominantPollutant");
                        } catch (JSONException e) {
                            Log.d("JsonException", e.toString());
                        }

                        // So far we are only using the universal air quality index from the response, since the other air quality index is local,
                        // but we still store the local one for scalability options
                        activity.airQualityIndices.add(new AirQualityIndex(indexCode, indexDisplayName, aqiDisplay, category, dominantPollutant));
                    }

                    final JSONArray pollutantsArray = json.getJSONArray("pollutants");

                    for (int i = 0; i < pollutantsArray.length(); i++) {
                        final JSONObject pollutantObject = pollutantsArray.getJSONObject(i);

                        final String pollutantDisplayName = pollutantObject.getString("displayName");
                        final JSONObject concentrationObject = pollutantObject.getJSONObject("concentration");
                        final double concentrationValue = concentrationObject.getDouble("value");
                        final String concentrationUnits = concentrationObject.getString("units");
                        final JSONObject healthRecommendationsObject = json.getJSONObject("healthRecommendations");

                        pollutants.add(new Pollutant(pollutantDisplayName, concentrationValue, concentrationUnits, healthRecommendationsObject.getString("generalPopulation")));
                    }

                    final JSONObject healthRecommendationsObject = json.getJSONObject("healthRecommendations");
                    final HashMap<String, String> healthMap = new HashMap<>();
                    final String generalHealthRecommendation = healthRecommendationsObject.getString("generalPopulation");
                    final String elderly = healthRecommendationsObject.getString("elderly");

                    healthMap.put("General Population", generalHealthRecommendation);
                    healthMap.put("Elderly", elderly);

                    activity.airQualityIndices.get(0).setHealthRecommendation(healthMap);

                    return pollutants;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(ArrayList<Pollutant> result) {
                if (result != null && !result.isEmpty()) {
                    activity.runOnUiThread(() -> {
                        TextView tvUAQITotal = activity.findViewById(R.id.uaqi_total);
                        final AirQualityIndex aqiObject = activity.airQualityIndices.get(0);
                        final String strTitle = aqiObject.getIndexDisplayName() + ": " + aqiObject.getAqiDisplay();
                        tvUAQITotal.setText(strTitle);

                        activity.pollutantListView.setAdapter(new PollutantItemAdapter(activity, result, activity));
                    });
                }
            }
        }.execute();
    }

    public static void fetchPollen(final String apiKey, final double lat, final double lng, MainActivity activity) {
        new AsyncTask<Void, Void, ArrayList<Pollen>>() {
            protected ArrayList<Pollen> doInBackground(Void... voids) {
                try {
                    final String urlString = "https://pollen.googleapis.com/v1/forecast:lookup?key=" + apiKey + "&location.longitude=" + lng + "&location.latitude=" + lat + "&days=1";
                    final URL url = new URL(urlString);
                    final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    final int responseCode = connection.getResponseCode();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    connection.disconnect();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        activity.pollenAPIResponse = response.toString();

                        return activity.parseAllPollen();
                    } else {
                        Log.d("API Request Failed", "Pollen API request failed with status code: " + responseCode);                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            protected void onPostExecute(ArrayList<Pollen> result) {
                if (result != null && !result.isEmpty()) {
                    activity.runOnUiThread(() -> {
                        activity.pollens = result;
                        activity.pollenItemAdapter = new PollenItemAdapter(activity, result, activity);
                        activity.pollenListView.setAdapter(activity.pollenItemAdapter);
                    });
                }
            }
        }.execute();
    }

    private ArrayList<String> parseCityGeolocation(String jsonResponse) {
        ArrayList<String> str = new ArrayList<>();

        try {
            final JSONObject jsonObj = new JSONObject(jsonResponse);
            final JSONArray results = jsonObj.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                final JSONObject result = results.getJSONObject(i);
                final JSONArray addressComponents = result.getJSONArray("address_components");

                for (int j = 0; j < addressComponents.length(); j++) {
                    final JSONObject addressComponent = addressComponents.getJSONObject(j);
                    final String longName = addressComponent.getString("long_name");

                    str.add(longName);
                    // Code to change how we format addresses, might use later
                    /*
                    for (int k = 0; k < types.length(); k++) {
                        String type = types.getString(k);
                        if (type.equals("postal_town") || type.equals("political")) {
                            str.add(longName);
                        }
                    }
                    */
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return str;
    }

    private ArrayList<Pollen> parseAllPollen() {
        ArrayList<Pollen> pollenResponse = parsePollen(pollenAPIResponse);
        final String[] pollenStringArray = pollenTypes;
        ArrayList<Pollen> newPollenList = new ArrayList<>();

        for (int i = 0; i < pollenTypes.length; i++) {
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
                // The response does not include pollen which are measured at UPI value = 0, but we want
                // to display them anyway, so those pollen objects are created here
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

    private ArrayList<Pollen> parsePollen(final String pollenInfo) {
        ArrayList<Pollen> firstPollens = new ArrayList<>();

        try {
            final JSONObject json = new JSONObject(pollenInfo);
            final JSONArray dailyInfoArray = json.getJSONArray("dailyInfo");

            for (int i = 0; i < dailyInfoArray.length(); i++) {
                final JSONObject dailyInfoObject = dailyInfoArray.getJSONObject(i);
                final JSONArray plantInfoArray = dailyInfoObject.getJSONArray("plantInfo");

                for (int j = 0; j < plantInfoArray.length(); j++) {
                    final JSONObject plantInfoObject = plantInfoArray.getJSONObject(j);
                    final String displayName = plantInfoObject.getString("displayName");

                    if (plantInfoObject.has("indexInfo")) {
                        final JSONObject indexInfoObject = plantInfoObject.getJSONObject("indexInfo");
                        final String indexValue = indexInfoObject.getString("value");
                        final String indexCategory = indexInfoObject.getString("category");
                        final String indexDescription = indexInfoObject.getString("indexDescription");

                        String season = "", crossReaction = "", type = "";
                        if (plantInfoObject.has("plantDescription")) {
                            final JSONObject plantDescriptionObject = plantInfoObject.getJSONObject("plantDescription");
                            season = plantDescriptionObject.getString("season");
                            crossReaction = plantDescriptionObject.getString("crossReaction");
                            type = plantDescriptionObject.getString("type");
                        }

                        firstPollens.add(new Pollen(displayName, indexValue, indexCategory, indexDescription, season, crossReaction, type));
                    } else {
                        // The response does not include pollen which are measured at UPI value = 0, but we want
                        // to display them anyway, so those pollen objects are created here
                        firstPollens.add(new Pollen(displayName, "", "", "", "", "", ""));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }

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

        final String str = pollen.getIndexDescription() + "." + "\n\n"
                + pollen.getHealthRecommendation() + "\n\n"
                + season + "\n\n"
                + cross;

        tvRecommendations.setText(str);

        ImageButton buttonDismiss = findViewById(R.id.btn_dismiss_card);
        buttonDismiss.setOnClickListener(view -> cardView.setVisibility(View.GONE));

        cardView.setVisibility(View.VISIBLE);
    }

    public void showPollutantCardView(Pollutant pollutant) {
        CardView cardView = findViewById(R.id.recommendation_card);
        TextView tvTitle = findViewById(R.id.card_title);
        tvTitle.setText(pollutant.getName());
        TextView tvInformation = findViewById(R.id.information_text);

        final String str = pollutant.getNonScientificName() + " - " + pollutant.getConcentration() +
                "\n\n" + pollutant.getRecommendations();
        tvInformation.setText(str);

        ImageButton buttonDismiss = findViewById(R.id.btn_dismiss_card);
        buttonDismiss.setOnClickListener(view -> cardView.setVisibility(View.GONE));

        cardView.setVisibility(View.VISIBLE);
    }
}
