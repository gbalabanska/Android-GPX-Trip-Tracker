package gpx.trip.tracker.management;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import gpx.trip.tracker.dto.MarkerRoutePoint;
import gpx.trip.tracker.dto.PredictedRoutePoint;
import gpx.trip.tracker.dto.RegisteredRoutePoint;
import gpx.trip.tracker.dto.RoutePoint;
import gpx.trip.tracker.RoutePointAdapter;

//Singleton class
public class DataManager {
    private static DataManager instance;
    private ArrayList<RoutePoint> plannedRoutePoints;
    private ArrayList<MarkerRoutePoint> markerRoutePoints;
    private ArrayList<RegisteredRoutePoint> registeredRoutePoints;
    private ArrayList<PredictedRoutePoint> predictedRoutePoints;
    private RoutePointAdapter adapter;
    public ArrayList<MarkerRoutePoint> getMarkerRoutePoints() {
        return markerRoutePoints;
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void initializeMarkerRoutePoints() {
        for (int i = 0; i < plannedRoutePoints.size(); i++) {
            if (plannedRoutePoints.get(i).getName() != null && !plannedRoutePoints.get(i).getName().equals("")) {
                MarkerRoutePoint mrp = new MarkerRoutePoint(plannedRoutePoints.get(i));
                markerRoutePoints.add(mrp);
            }
        }
    }

    public void setPlannedRoutePoints(ArrayList<RoutePoint> plannedRoutePoints) {
        this.plannedRoutePoints = plannedRoutePoints;
    }

    public RoutePoint getRoutePointByIdFromPlanned(int id) {
        if (id >= 0 && id < plannedRoutePoints.size()) {
            return plannedRoutePoints.get(id);
        } else {
            return null;
        }
    }

    public void clearAllData() {
        registeredRoutePoints = new ArrayList<>();
        plannedRoutePoints = new ArrayList<>();
        markerRoutePoints = new ArrayList<>();
        predictedRoutePoints = new ArrayList<>();
    }

    private DataManager() {
        registeredRoutePoints = new ArrayList<>();
        plannedRoutePoints = new ArrayList<>();
        markerRoutePoints = new ArrayList<>();
        predictedRoutePoints = new ArrayList<>();
    }

    public int registeredRoutePointsContains(int routePointId) {
        //looking for the last occurrence because one route point may be registered many times - GPS inaccuracy
        for (int i = registeredRoutePoints.size() - 1; i >= 0; i--) {
            if (registeredRoutePoints.get(i).getRefPoint().getId() == routePointId) {
                return i;
            }
        }
        return -1; //return -1 if no match is found
    }

    public void clearPredictedRoutePoints() {
        predictedRoutePoints.clear();
    }

    public void setPlannedTimesOfArrivalAtMarkers(LocalDateTime timeOfRegistration) {
        for (MarkerRoutePoint mrp : markerRoutePoints) {
            int markerId = mrp.getRefPoint().getId();
            double timeIntervalToPoint = plannedRoutePoints.get(markerId).getReachingTime();
            LocalDateTime plannedTime = timeOfRegistration.plusSeconds((long) timeIntervalToPoint);
            mrp.setPlannedDateTimeOfArrival(plannedTime);
        }
    }

    public long calculateActualTimeToPassSegment(int segmentIndex) {
        int startPointIndex = registeredRoutePointsContains(segmentIndex);
        int endPointIndex = registeredRoutePointsContains(segmentIndex + 1);

        if ((endPointIndex != -1) && (startPointIndex != -1) && (endPointIndex - startPointIndex > 0)) {
            //the segment was correctly registered
            LocalDateTime enterTime = registeredRoutePoints.get(startPointIndex).getDateTimeOfRegistration();
            LocalDateTime exitTime = registeredRoutePoints.get(endPointIndex).getDateTimeOfRegistration();
            Duration duration = Duration.between(enterTime, exitTime);
            long seconds = duration.getSeconds();
            return seconds;
        }
        return 0;
    }

    public void addPredictedRoutePoint(PredictedRoutePoint predictedRoutePoint) {
        predictedRoutePoints.add(predictedRoutePoint);
    }

    public ArrayList<RegisteredRoutePoint> getRegisteredRoutePoints() {
        return registeredRoutePoints;
    }

    public ArrayList<RoutePoint> getPlannedRoutePoints() {
        return plannedRoutePoints;
    }

    public void setMarkerRoutePoints(ArrayList<MarkerRoutePoint> markerRoutePoints) {
        this.markerRoutePoints = markerRoutePoints;
    }

    public void setRegisteredRoutePoints(ArrayList<RegisteredRoutePoint> registeredRoutePoints) {
        this.registeredRoutePoints = registeredRoutePoints;
    }

    public ArrayList<PredictedRoutePoint> getPredictedRoutePoints() {
        return predictedRoutePoints;
    }

    public void setPredictedRoutePoints(ArrayList<PredictedRoutePoint> predictedRoutePoints) {
        this.predictedRoutePoints = predictedRoutePoints;
    }

    public RoutePointAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(RoutePointAdapter adapter) {
        this.adapter = adapter;
    }
}

