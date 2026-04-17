package com.example.athleticare;

public class Player {

    private String name;
    private String schoolId;
    private Long age;

    // Required empty constructor for Firebase
    public Player() {
    }

    public Player(String name, String schoolId, Long age) {
        this.name = name;
        this.schoolId = schoolId;
        this.age = age;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public Long getAge() {
        return age;
    }

    // Setters (optional but useful for Firebase)
    public void setName(String name) {
        this.name = name;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public void setAge(Long age) {
        this.age = age;
    }
}