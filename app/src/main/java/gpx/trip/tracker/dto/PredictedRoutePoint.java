package gpx.trip.tracker.dto;

public class PredictedRoutePoint {
    private double predictedTerm;
    private double predictedReachingTime;
    private RoutePoint refPoint;
    public PredictedRoutePoint(RoutePoint refPoint) {
        this.refPoint=refPoint;
    }

    public RoutePoint getRefPoint() {
        return refPoint;
    }

    public void setRefPoint(RoutePoint refPoint) {
        this.refPoint = refPoint;
    }

    public double getPredictedTerm() {
        return predictedTerm;
    }

    public void setPredictedTerm(double predictedTerm) {
        this.predictedTerm = predictedTerm;
    }

    public double getPredictedReachingTime() {
        return predictedReachingTime;
    }

    public void setPredictedReachingTime(double predictedReachingTime) {
        this.predictedReachingTime = predictedReachingTime;
    }

    @Override
    public String toString() {
        return "PredictedRoutePoint{" +
                ", predictedTerm=" + predictedTerm +
                ", predictedReachingTime=" + predictedReachingTime +
                '}';
    }
}
