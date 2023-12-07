package gpx.trip.tracker.dto;

import java.time.LocalDateTime;

public class RegisteredRoutePoint {

    private LocalDateTime dateTimeOfRegistration;
    private RoutePoint refPoint;
    public RegisteredRoutePoint(RoutePoint refPoint) {
        this.refPoint=refPoint;
    }

    public RoutePoint getRefPoint() {
        return refPoint;
    }

    public void setRefPoint(RoutePoint refPoint) {
        this.refPoint = refPoint;
    }

    public LocalDateTime getDateTimeOfRegistration() {
        return dateTimeOfRegistration;
    }

    public void setDateTimeOfRegistration(LocalDateTime dateTimeOfRegistration) {
        this.dateTimeOfRegistration = dateTimeOfRegistration;
    }

}
