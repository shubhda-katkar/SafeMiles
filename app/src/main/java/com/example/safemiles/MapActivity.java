package com.example.safemiles;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQ_LOCATION = 100;
    private GoogleMap mMap;

    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private List<Geofence> geofenceList = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;

    private FirebaseFirestore firestore;   // ✅ Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        geofencingClient = LocationServices.getGeofencingClient(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        firestore = FirebaseFirestore.getInstance();   // ✅ Initialize Firestore

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setTrafficEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                    REQ_LOCATION);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
            }
        });

        // ✅ Load from Firestore
        loadBlackspotsFromFirestore();
    }

    private void loadBlackspotsFromFirestore() {
        firestore.collection("blackspots")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("MapActivity", "Firestore error: " + error.getMessage());
                        return;
                    }

                    if (value == null) return;

                    // ✅ Clear old markers/geofences before updating
                    mMap.clear();
                    geofenceList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {

                        String id = doc.getId();
                        String name = doc.getString("name");
                        String reason = doc.getString("reason");
                        double lat = doc.getDouble("latitude");
                        double lng = doc.getDouble("longitude");
                        long radiusLong = doc.getLong("radius");
                        float radius = (float) radiusLong;

                        LatLng pos = new LatLng(lat, lng);

                        // ✅ Marker
                        mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(name)
                                .snippet(reason)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_grey_pin))
                                .anchor(0.5f, 1.0f));

                        // ✅ Circle
                        mMap.addCircle(new CircleOptions()
                                .center(pos)
                                .radius(radius)
                                .strokeColor(Color.parseColor("#606060"))
                                .fillColor(Color.parseColor("#80606060"))
                                .strokeWidth(5));


                        // ✅ Geofence
                        geofenceList.add(
                                new Geofence.Builder()
                                        .setRequestId(id)
                                        .setCircularRegion(lat, lng, radius)
                                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                        .build()
                        );
                    }

                    // ✅ Add all geofences after loading
                    addGeofencesToClient();
                });
    }

    private void addGeofencesToClient() {
        if (geofenceList.isEmpty()) return;

        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(request, geofencePendingIntent)
                    .addOnSuccessListener(aVoid -> Log.d("MapActivity", "✅ Geofences added"))
                    .addOnFailureListener(e -> Log.e("MapActivity", "❌ Failed: " + e.getMessage()));
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(@NonNull Context context, @DrawableRes int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null)
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32,
                context.getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                context.getResources().getDisplayMetrics());

        vectorDrawable.setBounds(0, 0, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
