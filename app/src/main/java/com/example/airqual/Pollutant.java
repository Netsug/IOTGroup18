package com.example.airqual;

public class Pollutant {

    String name;
    double concentrationValue;
    String concentrationUnit;


    public Pollutant(String name, double concentrationValue, String concentrationUnit) {
        this.name = name;
        this. concentrationValue = concentrationValue;
        this.concentrationUnit = concentrationUnit;

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

}
