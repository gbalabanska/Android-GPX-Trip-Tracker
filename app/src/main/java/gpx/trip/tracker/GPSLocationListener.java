package gpx.trip.tracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import gpx.trip.tracker.dto.MarkerRoutePoint;
import gpx.trip.tracker.dto.PredictedRoutePoint;
import gpx.trip.tracker.dto.RegisteredRoutePoint;
import gpx.trip.tracker.dto.RoutePoint;
import gpx.trip.tracker.management.DataManager;
import gpx.trip.tracker.utilities.DistanceCalculator;

public class GPSLocationListener implements LocationListener {
    private static final String MY_TAG = "myTag";
    private Location lastLocation;
    private int currentClosestRoutePointIndex = 0;
    private int startSearchIndex;
    private int stopSearchIndex;
    private final DataManager dataManager;
    private Context context;
    private boolean isFirstLocationUpdate = true;

    public GPSLocationListener(Context c) {
        context = c;
        dataManager = DataManager.getInstance();
    }

  int i = 0;
    @Override
    public void onLocationChanged(Location loc) {
        Log.i(MY_TAG, "onLocationChanged()");
        //service stops working when the last marker point is reached
        if (dataManager.getMarkerRoutePoints().get(dataManager.getMarkerRoutePoints().size() - 1).isReached()) {
            Intent serviceIntent = new Intent(context, LocationService.class);
            context.stopService(serviceIntent);
        }

        //acceleration testing---------------
        if (i < dataManager.getPlannedRoutePoints().size()) {
            RoutePoint testRoutePoint = dataManager.getPlannedRoutePoints().get(i);
            loc = new Location("test gps") ;
            loc.setLatitude(testRoutePoint.getLat());
            loc.setLongitude(testRoutePoint.getLon());
        }
        i++;
        //-----------------------------------

        if (loc != null) {
            LocalDateTime timeOfRegistration = LocalDateTime.now();
            if (isFirstLocationUpdate) {
                //initialize trip
                isFirstLocationUpdate = false;
                MarkerRoutePoint firstPoint = dataManager.getMarkerRoutePoints().get(0);
                firstPoint.setReached(true);
                firstPoint.setPlannedDateTimeOfArrival(timeOfRegistration);
                firstPoint.setActualDateTimeOfArrival(timeOfRegistration);
                firstPoint.setPredictedDateTimeOfArrival(timeOfRegistration);

                dataManager.setPlannedTimesOfArrivalAtMarkers(timeOfRegistration);
            }

            lastLocation = loc;
            double lat = loc.getLatitude();
            double lng = loc.getLongitude();
            Log.d(MY_TAG, "Current location found -> latitude:" + lat + ", longitude:" + lng);
            registerClosestRoutePoint(timeOfRegistration);
            calculatePredictedArrivals();
            updatePredictedDateTime(dataManager.getPredictedRoutePoints());
            dataManager.getAdapter().notifyDataSetChanged();
        }
        Log.i(MY_TAG, "end onLocationChanged()");
    }

    public void updatePredictedDateTime(ArrayList<PredictedRoutePoint> predictedRoutePoints) {
        for (MarkerRoutePoint markerPoint : dataManager.getMarkerRoutePoints()) {
            if (!markerPoint.isReached()) {
                int routePointId = markerPoint.getRefPoint().getId();
                for (PredictedRoutePoint predictedPoint : predictedRoutePoints) {
                    if (predictedPoint.getRefPoint().getId() == routePointId) {
                        double predictedReachingTime = predictedPoint.getPredictedReachingTime();
                        LocalDateTime predictedDateTimeOfArrival = LocalDateTime.now()
                                .plus((long) predictedReachingTime, ChronoUnit.SECONDS);
                        markerPoint.setPredictedDateTimeOfArrival(predictedDateTimeOfArrival);
                    }
                }
            }
        }
    }

    private void registerClosestRoutePoint(LocalDateTime timeOfRegistration) {
        Log.i(MY_TAG, "registerClosestRoutePoint()");
        setStartStopIndexes();
        double minDistance = DistanceCalculator.calculateDistance(lastLocation.getLatitude(), lastLocation.getLongitude(),
                dataManager.getPlannedRoutePoints().get(currentClosestRoutePointIndex).getLat(), dataManager.getPlannedRoutePoints().get(currentClosestRoutePointIndex).getLon());

        Log.d(MY_TAG, "startSearchIndex = " + startSearchIndex + ", stopSearchIndex = " + stopSearchIndex);

        for (int i = startSearchIndex; i <= stopSearchIndex; i++) {
            double currentDistance = DistanceCalculator.calculateDistance(lastLocation.getLatitude(), lastLocation.getLongitude(),
                    dataManager.getPlannedRoutePoints().get(i).getLat(), dataManager.getPlannedRoutePoints().get(i).getLon());
            if (currentDistance <= minDistance) {
                minDistance = currentDistance;
                currentClosestRoutePointIndex = i;
            }
        }
        RegisteredRoutePoint registeredRoutePoint = new RegisteredRoutePoint(dataManager.getPlannedRoutePoints().get(currentClosestRoutePointIndex));
        registeredRoutePoint.setDateTimeOfRegistration(timeOfRegistration);
        dataManager.getRegisteredRoutePoints().add(registeredRoutePoint);

        handleRegistrationOfMarkers();

        Log.d(MY_TAG, "Closest point index = " + currentClosestRoutePointIndex + ", Lat:" + dataManager.getPlannedRoutePoints().get(currentClosestRoutePointIndex).getLat()
                + ", Lon:" + dataManager.getPlannedRoutePoints().get(currentClosestRoutePointIndex).getLon()
                + ", Distance: " + minDistance
                + "km, Registered at: " + timeOfRegistration);
    }

    private void setStartStopIndexes() {
        startSearchIndex = (currentClosestRoutePointIndex - 10) > 0 ? (currentClosestRoutePointIndex - 10) : 0;
        stopSearchIndex = (currentClosestRoutePointIndex + 10) < dataManager.getPlannedRoutePoints().size() - 1 ? (currentClosestRoutePointIndex + 10) : dataManager.getPlannedRoutePoints().size() - 1;
    }

    private void calculatePredictedArrivals() {
        Log.d(MY_TAG, "calculatePredictedArrivals");
        dataManager.clearPredictedRoutePoints();
        RegisteredRoutePoint lastRegisteredPoint = dataManager.getRegisteredRoutePoints().get(dataManager.getRegisteredRoutePoints().size() - 1);
        //iterate over all future route points and calculate their predicted time to reach
        double previousReachingTime = 0;
        for (int i = lastRegisteredPoint.getRefPoint().getId() + 1; i < dataManager.getPlannedRoutePoints().size(); i++) {
            RoutePoint currentRoutePoint = dataManager.getPlannedRoutePoints().get(i);
            //iterate over all siblings of the current route point
            int plannedRest = currentRoutePoint.getRest();
            double plannedTerm = currentRoutePoint.getTerm();
            double predictedTerm;
            int relevantSiblingsCount = 0;
            double sumOfAdjustmentFactors = 0;
            double avgAdjustmentFactor = 0;

            for (int j = 0; j < currentRoutePoint.getSiblings().length; j++) {
                int segmentIndex = currentRoutePoint.getSiblings()[j];
                //check if siblings is relevant - if yes - actual time to pass - if no - time to pass = null
                long actualTimeToPassSegment = dataManager.calculateActualTimeToPassSegment(segmentIndex);
                if (actualTimeToPassSegment > 0) {
                    relevantSiblingsCount++;
                    double plannedTimeToPassSegment = dataManager.getRoutePointByIdFromPlanned(segmentIndex + 1).getTerm();
                    sumOfAdjustmentFactors += actualTimeToPassSegment / plannedTimeToPassSegment;
                }
            }
            if (relevantSiblingsCount != 0 && sumOfAdjustmentFactors != 0) {
                avgAdjustmentFactor = sumOfAdjustmentFactors / relevantSiblingsCount;
            }
            if (avgAdjustmentFactor != 0) {
                predictedTerm = plannedTerm * avgAdjustmentFactor;
            } else {
                //case where no relevant siblings were found
                predictedTerm = plannedTerm;
            }
            PredictedRoutePoint predictedRoutePoint = new PredictedRoutePoint(currentRoutePoint);
            predictedRoutePoint.setPredictedTerm(predictedTerm);
            //calculate predictedReachingTime for the current point
            double reachingTime = previousReachingTime + predictedTerm + currentRoutePoint.getRest();
            predictedRoutePoint.setPredictedReachingTime(reachingTime);
            previousReachingTime = reachingTime;

            dataManager.addPredictedRoutePoint(predictedRoutePoint);
        }
    }

    private void handleRegistrationOfMarkers() {
        ArrayList<RegisteredRoutePoint> registeredRoutePoints = dataManager.getRegisteredRoutePoints();
        ArrayList<MarkerRoutePoint> markerRoutePoints = dataManager.getMarkerRoutePoints();

        int lastIndex = registeredRoutePoints.size() - 1;

        if (lastIndex >= 0) {
            RegisteredRoutePoint lastRegisteredPoint = registeredRoutePoints.get(lastIndex);

            if (isMarker(lastRegisteredPoint)) {
                int id = lastRegisteredPoint.getRefPoint().getId();
                MarkerRoutePoint markerRoutePoint = findMarkerById(id);
                if (markerRoutePoint != null) {
                    markerRoutePoint.setReached(true);
                    markerRoutePoint.setActualDateTimeOfArrival(lastRegisteredPoint.getDateTimeOfRegistration());

                    handleNotification(markerRoutePoint.getRefPoint().getName());
                }
            } else {
                //the last registered point was not a marker
                RegisteredRoutePoint previousRegisteredPoint = findPreviousRegisteredPoint(lastIndex);

                if (previousRegisteredPoint != null && !isMarker(previousRegisteredPoint)) {
                    //there was a previous registered point that is not marker, check for markers in between
                    int markerId = getMarkerInRange(previousRegisteredPoint.getRefPoint().getId(), lastRegisteredPoint.getRefPoint().getId(), markerRoutePoints);

                    if (markerId != -1) {
                        LocalDateTime middleTime = calculateMiddleTime(previousRegisteredPoint.getDateTimeOfRegistration(), lastRegisteredPoint.getDateTimeOfRegistration());
                        MarkerRoutePoint markerRoutePoint = findMarkerById(markerId);
                        if (markerRoutePoint != null) {
                            markerRoutePoint.setReached(true);
                            markerRoutePoint.setActualDateTimeOfArrival(middleTime);
                            Log.d(MY_TAG, "Missed marker point is now registered id=: " + markerId);
                            handleNotification(markerRoutePoint.getRefPoint().getName());
                        }
                    }
                }
            }
        }
    }

    private void handleNotification(String name) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean notifyPassedPoints = preferences.getBoolean("notifyPassedPoints", false);

        if (notifyPassedPoints) {
            sendNotification(context.getString(R.string.notify_title), name);
        }
        Log.d(MY_TAG, "Last registered point was a Marker.");
    }

    private final String CHANNEL_ID = "my_channel_id";
    private final int notificationId = 0; // override previous notification

    private void sendNotification(String title, String message) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bike)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(context, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    private void createNotificationChannel() {
        CharSequence name = "MyNotificationChannel";
        String description = "Channel for My App Notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private MarkerRoutePoint findMarkerById(int id) {
        for (int i = 0; i < dataManager.getMarkerRoutePoints().size(); i++) {
            if (id == dataManager.getMarkerRoutePoints().get(i).getRefPoint().getId()) {
                return dataManager.getMarkerRoutePoints().get(i);
            }
        }
        return null;
    }

    private LocalDateTime calculateMiddleTime(LocalDateTime time1, LocalDateTime time2) {
        long millis1 = time1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long millis2 = time2.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long middleMillis = (millis1 + millis2) / 2;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(middleMillis), ZoneId.systemDefault());
    }

    private boolean isMarker(RegisteredRoutePoint routePoint) {
        int id = routePoint.getRefPoint().getId();
        if (dataManager.getPlannedRoutePoints().get(id).getName() != null) {
            return true;
        }
        return false;
    }

    private RegisteredRoutePoint findPreviousRegisteredPoint(int currentIndex) {
        //find the previous registered point by iterating backward in the list
        for (int i = currentIndex - 1; i >= 0; i--) {
            RegisteredRoutePoint previousPoint = dataManager.getRegisteredRoutePoints().get(i);
            if (isMarker(previousPoint)) {
                //return the previous point if it's a marker
                return previousPoint;
            }
        }
        return null; //no previous registered point found
    }

    private int getMarkerInRange(int start, int end, ArrayList<MarkerRoutePoint> markerRoutePoints) {
        //check if there is a marker route point in the range [start, end]
        for (int i = start + 1; i < end; i++) {
            for (MarkerRoutePoint marker : markerRoutePoints) {
                if (marker.getRefPoint().getId() == i) {
                    return i; // Found a marker in the range
                }
            }
        }
        return -1; //no marker found in the range
    }

}
