package com.example.airqual;

public class PollenType {

    private String name;
    private String category;
    private String healthRecommendations;


    public PollenType (String name, String category, String healthRecommendations) {
        this.name = name;
        this.category = category;
        this.healthRecommendations = healthRecommendations;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getHealthRecommendations() {
        return healthRecommendations;
    }

    public String toString() {
        return name + ", " + category + ", " + healthRecommendations;
    }

}
