package gpx.trip.tracker.dto;

import java.time.LocalDateTime;

public class MarkerRoutePoint {
    private boolean isReached = false;
    private LocalDateTime plannedDateTimeOfArrival;
    private LocalDateTime predictedDateTimeOfArrival;
    private LocalDateTime actualDateTimeOfArrival;
    private RoutePoint refPoint;
    public MarkerRoutePoint(RoutePoint refPoint) {
        this.refPoint=refPoint;
    }

    public RoutePoint getRefPoint() {
        return refPoint;
    }

    public void setRefPoint(RoutePoint refPoint) {
        this.refPoint = refPoint;
    }

    public boolean isReached() {
        return isReached;
    }

    public void setReached(boolean reached) {
        isReached = reached;
    }

    public LocalDateTime getPlannedDateTimeOfArrival() {
        return plannedDateTimeOfArrival;
    }

    public void setPlannedDateTimeOfArrival(LocalDateTime plannedDateTimeOfArrival) {
        this.plannedDateTimeOfArrival = plannedDateTimeOfArrival;
    }

    public LocalDateTime getPredictedDateTimeOfArrival() {
        return predictedDateTimeOfArrival;
    }

    public void setPredictedDateTimeOfArrival(LocalDateTime predictedDateTimeOfArrival) {
        this.predictedDateTimeOfArrival = predictedDateTimeOfArrival;
    }

    public LocalDateTime getActualDateTimeOfArrival() {
        return actualDateTimeOfArrival;
    }

    public void setActualDateTimeOfArrival(LocalDateTime actualDateTimeOfArrival) {
        this.actualDateTimeOfArrival = actualDateTimeOfArrival;
    }

    @Override
    public String toString() {
        return "MarkerRoutePoint{" +
                "isReached=" + isReached +
                ", plannedDateTimeOfArrival=" + plannedDateTimeOfArrival +
                ", predictedDateTimeOfArrival=" + predictedDateTimeOfArrival +
                ", actualDateTimeOfArrival=" + actualDateTimeOfArrival +
                ", refPoint=" + refPoint +
                '}';
    }
}
