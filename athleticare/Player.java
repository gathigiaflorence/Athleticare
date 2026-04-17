package com.example.athleticare;

public class Player {

    private String name;
    private String schoolId;
    private Long age;

   
    public Player() {
    }

    public Player(String name, String schoolId, Long age) {
        this.name = name;
        this.schoolId = schoolId;
        this.age = age;
    }

    
    public String getName() {
        return name;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public Long getAge() {
        return age;
    }

   
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
