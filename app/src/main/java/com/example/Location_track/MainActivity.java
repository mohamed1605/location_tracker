package com.example.Location_track;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.Location_track.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_REQUEST_CODE = 100;
    private LocationManager locationManager;
    private MapView mapView;
    private IMapController mapController;
    private Marker userMarker;
    private TextView speedTextView;
    private Location previousLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        mapView = findViewById(R.id.mapView);
        speedTextView = findViewById(R.id.speedTextView);

        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(15.0);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates from GPS provider
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        if (userMarker == null) {
            userMarker = new Marker(mapView);
            userMarker.setTitle("You are here");
            mapView.getOverlays().add(userMarker);
        }

        userMarker.setPosition(geoPoint);
        mapController.setCenter(geoPoint);

        if (previousLocation != null) {
            float speed = previousLocation.distanceTo(location) / ((location.getTime() - previousLocation.getTime()) / 1000.0f);
            // Convert speed to km/h
            speed = speed * 3.6f;
            speedTextView.setText("Speed: " + speed + " km/h");
        } else {
            speedTextView.setText("Speed: 0 km/h");
        }

        previousLocation = location;
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(this, "GPS Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "GPS Disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Not used in modern Android versions
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
