package com.example.airqual;

import java.util.HashMap;

public class AirQualityIndex {

    // Fields
    private String indexCode;
    private String indexDisplayName;
    private int aqi;
    private String aqiDisplay;
    private String category;
    private String dominantPollutant;

    private HashMap<String, String> healthRecommendations;

    // Constructor
    public AirQualityIndex(String indexCode, String indexDisplayName, int aqi,
                           String aqiDisplay, String category, String dominantPollutant) {
        this.indexCode = indexCode;
        this.indexDisplayName = indexDisplayName;
        this.aqi = aqi;
        this.aqiDisplay = aqiDisplay;
        this.category = category;
        this.dominantPollutant = dominantPollutant;
        this.healthRecommendations = null;
    }

    public String getIndexCode() {
        return indexCode;
    }

    public String getIndexDisplayName() {
        return indexDisplayName;
    }

    public int getAqi() {
        return aqi;
    }

    public String getAqiDisplay() {
        return aqiDisplay;
    }

    public String getCategory() {
        return category;
    }

    public String getDominantPollutant() {
        return dominantPollutant;
    }

    public HashMap<String, String> getHealthRecommendation() {
        return this.healthRecommendations;
    }

    public void setHealthRecommendation(HashMap<String, String> hr) {
        this.healthRecommendations = hr;
    }

}

