package com.hackathon.model;

public class Doctor {
    private int id;
    private String name;
    private String specialization;
    private String hospital;
    private int experience;
    private double rating;
    private String phone;
    private double fee;
    private boolean available;

    public Doctor() {}

    public Doctor(int id, String name, String specialization, String hospital, int experience, double rating, String phone, double fee, boolean available) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.hospital = hospital;
        this.experience = experience;
        this.rating = rating;
        this.phone = phone;
        this.fee = fee;
        this.available = available;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
    
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
