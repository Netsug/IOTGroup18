package com.example.airqual;

import java.util.HashMap;

public class AirQualityIndex {
    private final String indexCode;
    private final String indexDisplayName;
    private final String aqiDisplay;
    private final String category;
    private final String dominantPollutant;
    private HashMap<String, String> healthRecommendations;

    public AirQualityIndex(String indexCode, String indexDisplayName,
                           String aqiDisplay, String category, String dominantPollutant) {
        this.indexCode = indexCode;
        this.indexDisplayName = indexDisplayName;
        this.aqiDisplay = aqiDisplay;
        this.category = category;
        this.dominantPollutant = dominantPollutant;
        this.healthRecommendations = null;
    }

    public String getIndexCode() { return indexCode; }

    public String getIndexDisplayName() {
        return indexDisplayName;
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

