package com.example.airqual;

import android.util.Log;

public class Pollen {

    // Fields
    private String displayName;
    private String indexValue;
    private String indexCategory;
    private String indexDescription;
    private String season;
    private String crossReaction;
    private String type;
    private String healthRecommendation;

    // Constructor
    public Pollen(String displayName, String indexValue, String indexCategory, String indexDescription, String season, String crossReaction, String type) {
        this.displayName = displayName;
        this.indexValue = indexValue;
        this.indexCategory = indexCategory;
        this.indexDescription = indexDescription;
        this.season = season;
        this.crossReaction = crossReaction;
        this.type = type;
        switch (indexValue) {
            case "1":
                healthRecommendation = "Low risk for allergies.";
                break;
            case "2":
                healthRecommendation = "Moderate risk, take precautions if sensitive.";
                break;
            case "3":
                healthRecommendation = "High risk, advisable to stay indoors.";
                break;
            case "4":
                healthRecommendation = "Very high risk, necessary to stay indoors.";
                break;
            case "5":
                healthRecommendation = "Extremely high risk, take all precautions.";

                break;
            default:
                healthRecommendation = "Very very low";
                this.indexCategory = "None";
                break;
        }

    }

    // Getter methods
    public String getDisplayName() {
        return displayName;
    }

    public String getIndexValue() {
        return indexValue;
    }

    public String getIndexCategory() {
        Log.d("Hello", "From me");
        return indexCategory;
    }

    public String getIndexDescription() {
        return indexDescription;
    }

    public String getSeason() {
        return season;
    }

    public String getCrossReaction() {
        return crossReaction;
    }

    public String getType() {
        return type;
    }
}
