package gpx.trip.tracker.dto;

import java.io.Serializable;
import java.util.Arrays;

public class RoutePoint implements Serializable {
    private int id;
    private static int nextId = 0;
    private double lat;
    private double lon;
    private double term; //time to pass previous segment in seconds
    private int[] siblings; //array of siblings of the point corresponds to tag <ref>
    private String name; //only marker points have name
    private int rest; //time in seconds for rest in this point
    private double reachingTime; //time in seconds from the start of the trip that is needed to reach this point = term of all previous point + rest in all previous points

    public RoutePoint() {
        this.id = nextId; // assign a unique ID to each object
        nextId++;
    }

    @Override
    public String toString() {

        return "RoutePoint{" +
                "id=" + id +
                ", lat=" + lat +
                ", lon=" + lon +
                ", term=" + term +
                ", siblings=" + Arrays.toString(siblings) +
                ", name='" + name + '\'' +
                ", rest=" + rest +
                ", reachingTime=" + reachingTime +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static int getNextId() {
        return nextId;
    }

    public static void setNextId(int nextId) {
        RoutePoint.nextId = nextId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getTerm() {
        return term;
    }

    public void setTerm(double term) {
        this.term = term;
    }

    public int[] getSiblings() {
        return siblings;
    }

    public void setSiblings(int[] siblings) {
        this.siblings = siblings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRest() {
        return rest;
    }

    public void setRest(int rest) {
        this.rest = rest;
    }

    public double getReachingTime() {
        return reachingTime;
    }

    public void setReachingTime(double reachingTime) {
        this.reachingTime = reachingTime;
    }
}
