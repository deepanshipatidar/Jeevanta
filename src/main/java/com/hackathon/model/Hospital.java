package com.hackathon.model;

/**
 * Domain model representing a hospital entity.
 *
 * Maps to the {@code hospitals} table in {@code hackathon_db}:
 * <pre>
 *   id             INT AUTO_INCREMENT PRIMARY KEY
 *   name           VARCHAR(255)
 *   lat            DOUBLE
 *   lng            DOUBLE
 *   available_beds INT
 * </pre>
 */
public class Hospital {

    private int    id;
    private String name;
    private double lat;
    private double lng;
    private int    availableBeds;

    // ─── Constructors ──────────────────────────────────────────────────

    public Hospital() { }

    public Hospital(int id, String name, double lat, double lng, int availableBeds) {
        this.id             = id;
        this.name           = name;
        this.lat            = lat;
        this.lng            = lng;
        this.availableBeds  = availableBeds;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getAvailableBeds() {
        return availableBeds;
    }

    public void setAvailableBeds(int availableBeds) {
        this.availableBeds = availableBeds;
    }

    // ─── Object Overrides ──────────────────────────────────────────────

    @Override
    public String toString() {
        return "Hospital{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", availableBeds=" + availableBeds +
                '}';
    }
}
