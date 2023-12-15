package com.example.airqual;

import java.util.HashMap;

public class Pollutant {
    private final String name;
    private final double concentrationValue;
    private final String concentrationUnit;
    private String concentration;
    private String nonScientificName;

    private String safeAmountCutoff;
    private final String recommendations;

    final char micro = '\u00B5';
    final char cubed = '\u00B3';


    public Pollutant(String name, double concentrationValue, String concentrationUnit, String rec) {

        HashMap<String, String> unitConverterMap = new HashMap<>();
        HashMap<String, String> nonScientificNameMap = new HashMap<>();
        HashMap<String, String> safeAmountCutoffMap = new HashMap<>();

        this.name = name;
        this.concentrationValue = concentrationValue;
        this.concentrationUnit = concentrationUnit;

        this.recommendations = rec;

        ////////////////////////////////

        safeAmountCutoffMap.put("CO", "≈9000ppb");
        safeAmountCutoffMap.put("NO2", "21.31ppb");
        safeAmountCutoffMap.put("O3", "35.74ppb");
        safeAmountCutoffMap.put("PM10","50" + micro + "g/m" + cubed);
        safeAmountCutoffMap.put("PM2.5","12" + micro+ "g/m" + cubed);
        safeAmountCutoffMap.put("SO2", "7.67ppb");

        for (String key : safeAmountCutoffMap.keySet()) {
            if (key.equals(name)) {
                safeAmountCutoff = safeAmountCutoffMap.get(key);
                break;
            }
        }

        ////////////////////////////////

        nonScientificNameMap.put("CO", "Carbon Monoxide");
        nonScientificNameMap.put("NO2", "Nitrogen Dioxide");
        nonScientificNameMap.put("O3", "Ozone");
        nonScientificNameMap.put("PM10","Coarse Particulate Matter");
        nonScientificNameMap.put("PM2.5","Fine Particulate Matter");
        nonScientificNameMap.put("SO2", "Sulfur Dioxide");

        for (String key : nonScientificNameMap.keySet()) {
            if (key.equals(name)) {
                nonScientificName = nonScientificNameMap.get(key);
                break;
            }
        }

        ////////////////////////////////

        unitConverterMap.put("PARTS_PER_BILLION", "ppb");
        unitConverterMap.put("MICROGRAMS_PER_CUBIC_METER", micro + "g/m" + cubed);

        for (String key : unitConverterMap.keySet()) {
            if (key.equals(concentrationUnit)) {
                concentration = concentrationValue + unitConverterMap.get(key);
                break;
            }
        }

        ////////////////////////////////
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

    public String getConcentration(){
        return concentration;
    }
    public String getNonScientificName(){
        return nonScientificName;
    }

    public String getSafeAmountCutoff() {
        return safeAmountCutoff;
    }
}