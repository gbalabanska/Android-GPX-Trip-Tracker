package gpx.trip.tracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


public class LocationService extends Service {
    private static final String MY_TAG = "myTag";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int NOTIFICATION_ID = 123;
    private static final int REQUEST_INTERVAL = 1000;
    private static final int MIN_DISTANCE = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Context context = getApplicationContext();
        locationListener = new GPSLocationListener(context);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String msgText = "Service running...";
        if (intent != null) {
            msgText = intent.getStringExtra("msgText");
        }
        startForegroundService(msgText);
        return START_STICKY;
    }

    private void startForegroundService(String msgText) {
        String channelId = "location_service_channel";
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "GPX Trip Tracker",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Location Service")
                .setContentText(msgText)
                .setSmallIcon(R.drawable.andr)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, REQUEST_INTERVAL, MIN_DISTANCE, locationListener);
            } else {
                Toast.makeText(this, R.string.location_perm_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        Log.d(MY_TAG, "LocationService locationManager.removeUpdates(locationListener)");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}