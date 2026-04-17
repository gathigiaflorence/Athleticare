package com.example.athleticare;

public class AppointmentModel {

    private String docId;

    private String schoolId;
    private String date;
    private String time;
    private String appointmentType;
    private String injuryType;
    private String athleteName;
    private String status;

    public AppointmentModel() {}

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public String getInjuryType() {
        return injuryType;
    }

    public String getAthleteName() {
        return athleteName;
    }

    public String getStatus() {
        return status;
    }
}