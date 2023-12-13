package com.example.airqual;

public class Pollutant {
    private String name;
    private double concentrationValue;
    private String concentrationUnit;
    private String recommendations;

    public Pollutant(String name, double concentrationValue, String concentrationUnit, String rec) {
        this.name = name;
        this.concentrationValue = concentrationValue;
        this.concentrationUnit = concentrationUnit;

        this.recommendations = rec.toString();
    }

    public String getName() {
        return name;
    }

    public double getConcentrationValue() {
        return concentrationValue;
    }

    public String getConcentrationUnit() {
        return concentrationUnit;
    }

    public String getRecommendations(){
        return recommendations;
    }

}
