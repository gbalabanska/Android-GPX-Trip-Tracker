package gpx.trip.tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class RoutePointsVisualizerActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean shouldFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_points_visualizer);

        shouldFinish = getIntent().getBooleanExtra("shouldFinish", false);
        finishIfRequired();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RoutePointAdapter adapter = new RoutePointAdapter(RoutePointsVisualizerActivity.this);
        recyclerView.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            String msgText = getString(R.string.service_notify);
            Intent serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.putExtra("msgText", msgText);
            if(!shouldFinish) {
                startService(serviceIntent);
            }
        }
    }

    private void finishIfRequired() {
        if (shouldFinish) {
            finish();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.location_granted, Toast.LENGTH_SHORT).show();
                Intent serviceIntent = new Intent(this, LocationService.class);
                startService(serviceIntent);
            } else {
                Toast.makeText(this, R.string.location_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }
}


