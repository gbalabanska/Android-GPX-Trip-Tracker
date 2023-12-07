package gpx.trip.tracker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

import gpx.trip.tracker.dto.RoutePoint;
import gpx.trip.tracker.management.DataManager;
import gpx.trip.tracker.utilities.GPXDeserializer;
import gpx.trip.tracker.utilities.GpxValidator;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    private DataManager dataManager;
    private Button btnLoadGPXFile;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Button btnLang;
    private Switch switchNotifyPassedPoints;
    private Switch switchDisplayCoordinates;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initializations
        dataManager = DataManager.getInstance();

        btnLoadGPXFile = findViewById(R.id.btnLoadGPXFile);
        setBtnLoadGPXFile();

        spinner = findViewById(R.id.spinner);
        setSpinner();

        btnLang = findViewById(R.id.btnLang);
        setbtnLang();

        switchNotifyPassedPoints = findViewById(R.id.switchNotifyPassedPoints);
        setSwitchNotifyPassedPoints();

        switchDisplayCoordinates = findViewById(R.id.switchDisplayCoordinates);
        setSwitchDisplayCoordinates();

    }

    private void setSwitchDisplayCoordinates() {
        SharedPreferences preferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean displayCoordinates = preferences.getBoolean("displayCoordinates", false);
        switchDisplayCoordinates.setChecked(displayCoordinates);
        switchDisplayCoordinates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("displayCoordinates", isChecked);
                editor.apply();
            }
        });
    }

    private void setSwitchNotifyPassedPoints() {
        SharedPreferences preferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean notifyPassedPoints = preferences.getBoolean("notifyPassedPoints", false);
        switchNotifyPassedPoints.setChecked(notifyPassedPoints);

        switchNotifyPassedPoints.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("notifyPassedPoints", isChecked);
                editor.apply();
            }
        });
    }

    private void setbtnLang() {
        btnLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LangChangeFragment fragment = new LangChangeFragment();
                fragment.show(getSupportFragmentManager(), "langChangeDialog");
            }
        });
    }

    private void setSpinner() {
        String[] items = {"English", "Български"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setEnabled(false);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    setLocale("en");
                } else {
                    setLocale("bg");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void setBtnLoadGPXFile() {
        btnLoadGPXFile.setOnClickListener(v -> checkReadExternalStoragePermission());
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null && result.getData().getData() != null) {
                            handleSelectedFile(result.getData().getData());
                        }
                    }
                });
    }

    private void checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            openFilePicker();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    private void handleSelectedFile(@NonNull android.net.Uri uri) {
        String fileName = getFileNameFromUri(uri);
        if (fileName != null && fileName.endsWith(".gpx")) {
            Context context = getApplicationContext();
            boolean isValid = GpxValidator.validateGpx(uri, context);
            if (isValid) {
                RoutePoint.setNextId(0);
                ArrayList<RoutePoint> routePoints = GPXDeserializer.deserializeGPX(context, uri);
                dataManager.setPlannedRoutePoints(routePoints);
                dataManager.initializeMarkerRoutePoints();
                setTextViewsForLoadedData(fileName);
                setTripBtns();
            } else {
                Toast.makeText(this, R.string.invalid_gpx, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.invalid_extension, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                Toast.makeText(this, R.string.file_access_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }


    private void setTripBtns() {
        Button startTripButton = findViewById(R.id.btnStartTrip);
        //enable start
        int color = ContextCompat.getColor(this, R.color.orange);
        startTripButton.setBackgroundTintList(ColorStateList.valueOf(color));

        Button checkProgressButton = findViewById(R.id.checkProgressButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        checkProgressButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        startTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RoutePointsVisualizerActivity.class);
                startActivity(intent);

                startTripButton.setVisibility(View.GONE);
                checkProgressButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
            }
        });

        checkProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RoutePointsVisualizerActivity.class);
                startActivity(intent);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTripButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
                startTripButton.setVisibility(View.VISIBLE);
                checkProgressButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                resetTextViews();
                clearResources();
            }
        });
    }

    private void resetTextViews() {
        TextView tvFileName = findViewById(R.id.tvFileName);
        TextView tvStartPoint = findViewById(R.id.tvStartPoint);
        TextView tvEndPoint = findViewById(R.id.tvEndPoint);
        TextView tvPlannedTime = findViewById(R.id.tvPlannedTime);

        tvFileName.setText(R.string.file_name);
        tvStartPoint.setText(R.string.start_point);
        tvEndPoint.setText(R.string.end_point);
        tvPlannedTime.setText(R.string.planned_time);
    }

    private void setTextViewsForLoadedData(String fileName) {
        TextView textView = findViewById(R.id.tvFileName);
        String currentText = textView.getText().toString();
        String newText = currentText + " " + fileName;
        textView.setText(newText);

        textView = findViewById(R.id.tvStartPoint);
        currentText = textView.getText().toString();
        newText = currentText + " " + dataManager.getPlannedRoutePoints().get(0).getName();
        textView.setText(newText);

        textView = findViewById(R.id.tvEndPoint);
        currentText = textView.getText().toString();
        newText = currentText + " " + dataManager.getPlannedRoutePoints().get(dataManager.getPlannedRoutePoints().size() - 1).getName();
        textView.setText(newText);

        textView = findViewById(R.id.tvPlannedTime);
        currentText = textView.getText().toString();
        double timeToReachInSeconds = dataManager.getPlannedRoutePoints().get(dataManager.getPlannedRoutePoints().size() - 1).getReachingTime();
        newText = currentText + " " + formatSecondsToTimeString(timeToReachInSeconds);
        textView.setText(newText);
    }

    public static String formatSecondsToTimeString(double timeInSeconds) {
        long roundedTimeInSeconds = Math.round(timeInSeconds);

        long hours = roundedTimeInSeconds / 3600;
        long minutes = (roundedTimeInSeconds % 3600) / 60;
        long seconds = roundedTimeInSeconds % 60;

        StringBuilder formattedTime = new StringBuilder();

        if (hours > 0) {
            formattedTime.append(hours).append("h ");
        }

        if (minutes > 0) {
            formattedTime.append(minutes).append("min ");
        }

        if (seconds > 0) {
            formattedTime.append(seconds).append("s");
        }

        return formattedTime.toString().trim();
    }


    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                fileName = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName;
    }

    private void setLocale(String languageCode) {
        clearResources();

        Locale newLocale = new Locale(languageCode);

        if (!getCurrentLocale().equals(newLocale)) {
            Locale.setDefault(newLocale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(newLocale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            recreate();
        }
    }

    private Locale getCurrentLocale() {
        return getResources().getConfiguration().getLocales().get(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearResources();
    }

    private void clearResources() {
        Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
        stopService(serviceIntent);
        Intent intent = new Intent(MainActivity.this, RoutePointsVisualizerActivity.class);
        intent.putExtra("shouldFinish", true);
        startActivity(intent);
        dataManager.clearAllData();
    }
}
