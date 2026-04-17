package com.example.athleticare;

public class InjuryModel {

    private String name;
    private String schoolId;
    private String sport;
    private String injuryType;
    private String injuryArea;
    private String injuryDate;

    public InjuryModel(String name, String schoolId, String sport,
                       String injuryType, String injuryArea, String injuryDate) {
        this.name = name;
        this.schoolId = schoolId;
        this.sport = sport;
        this.injuryType = injuryType;
        this.injuryArea = injuryArea;
        this.injuryDate = injuryDate;
    }

    public String getName() { return name; }
    public String getSchoolId() { return schoolId; }
    public String getSport() { return sport; }
    public String getInjuryType() { return injuryType; }
    public String getInjuryArea() { return injuryArea; }
    public String getInjuryDate() { return injuryDate; }
}