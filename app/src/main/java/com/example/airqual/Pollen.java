package com.example.airqual;

import android.util.Log;

public class Pollen {

    private final String displayName;
    private final String indexValue;
    private String indexCategory;
    private String indexDescription;
    private String season;
    private String crossReaction;
    private String type;
    private String healthRecommendation;

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
                this.healthRecommendation = "Low risk for allergies.";
                break;
            case "2":
                this.healthRecommendation = "Moderate risk, take precautions if sensitive.";
                break;
            case "3":
                this.healthRecommendation = "High risk, advisable to stay indoors.";
                break;
            case "4":
                this.healthRecommendation = "Very high risk, necessary to stay indoors.";
                break;
            case "5":
                this.healthRecommendation = "Extremely high risk, take all precautions.";

                break;
            default:
                this.healthRecommendation = "Enjoy the outdoors!";
                this.indexCategory = "None";
                this.indexDescription = "There is no measurable amount of this pollen in the air";
                this.season = "";
                this.crossReaction = "";
                this.type = "";

                break;
        }

    }

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

    public String getCrossReaction() { return crossReaction; }

    public String getType() {
        return type;
    }

    public String getHealthRecommendation() { return this.healthRecommendation; }
}
