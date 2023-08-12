package sheridan.aldahhas.androidtracker;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Pair;
import android.widget.ArrayAdapter;


import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class RealtimeService extends Service {

    private ArrayList<HashMap<String, String>> localStoredLocations = new ArrayList<>();

    private ArrayAdapter arrayAdapter;
    private ArrayList update = new ArrayList<String>();
    public static final int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "RealtimeServiceChannel";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private long interval = 30000;
    private long fastestInterval = 10000;
    private int priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY ;
    private int numberOfUpdates;
    private double Latitude = 0.0, Longitude = 0.0;
    private static final String TAG = "RealtimeService";

    private Handler handler;
    private Runnable saveLocationTask;
    private LocationManager locationManager;


    // Server url for location updates
    public static String dburl = "http://192.168.1.124/realtimelocation/addlocation.php",uniqueId;

    public RealtimeService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return null; // For simplicity, this is a non-binding service
    }

    @Override
    public void onCreate() {
        super.onCreate();
        uniqueId = UUID.randomUUID().toString();
        buildGoogleApiClient();

        // Register networkChangeReceiver
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);

        // Create a notification channel
        createNotificationChannel();

        // Build a notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Realtime Location Tracking")
                .setContentText("Tracking your location...")
                // .setSmallIcon(R.drawable.your_icon) // Add your icon here
                .build();

        // Start the service in the foreground
        startForeground(NOTIFICATION_ID, notification);
    }


    // All the methods from your activity go here, slightly adapted for a service
    // For example, this service won't interact with any UI components,
    // so anything related to views or user interaction should be removed or replaced with appropriate alternatives.

    /**
     * Function to start FusedLocation updates
     */
    public void requestLocationUpdate() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    /**
     * Build GoogleApiClient and connect
     */
    private synchronized void buildGoogleApiClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        // Creating a location request
                        mLocationRequest = LocationRequest.create();
                        mLocationRequest.setInterval(interval);
                        mLocationRequest.setFastestInterval(fastestInterval);
                        mLocationRequest.setPriority(priority);
                        mLocationRequest.setSmallestDisplacement(0);

                        // FusedLocation callback
                        mLocationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);

                                Latitude = locationResult.getLastLocation().getLatitude();
                                Longitude = locationResult.getLastLocation().getLongitude();

                                if (Latitude == 0.0 && Longitude == 0.0) {
                                    requestLocationUpdate();
                                } else {
                                    // Check internet permission and
                                    // Send Location to Database
                                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                                        saveLocation(uniqueId, Double.toString(Latitude), Double.toString(Longitude),getCurrentDate());
//                                        saveLocation(uniqueId, Double.toString(Latitude), Double.toString(Longitude));
                                    }
                                }
                            }
                        };

                        // Call location settings function to enable gps
                        locationSettingsRequest();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        mGoogleApiClient.connect();
                    }
                })
                .addOnConnectionFailedListener(connectionResult -> {

                })
                .addApi(LocationServices.API)
                .build();

        // Connect googleapiclient after build
        mGoogleApiClient.connect();
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = manager.getActiveNetworkInfo();


                if (activeNetwork != null && activeNetwork.isConnected()) {
                    // Internet connection available, get stored locations
                    LocationDBHelper dbHelper = new LocationDBHelper(RealtimeService.this);
                    ArrayList<HashMap<String, String>> storedLocations = dbHelper.getAllLocations();

                    for (HashMap<String, String> locationPair : storedLocations) {
                        saveLocation(uniqueId,locationPair.get("latitude"), locationPair.get("longitude"), locationPair.get("dateCreated")); // Pass date_created
//                        saveLocation(uniqueId,locationPair.get("latitude"), locationPair.get("longitude")); // Pass date_created
                    }


                    // Clear the locally stored locations in the database after successfully sending them to the server
                    dbHelper.clearStoredLocations();
                }
            }
        }
    };



    public void saveLocation(String uniqueId, String latitude, String longitude, String dateCreated){
//    public void saveLocation(String uniqueId, String latitude, String longitude){
        if(isConnectedToInternet()){
            if(dateCreated == "") dateCreated = getCurrentDate();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
            String finalDateCreated = dateCreated;
            StringRequest stringRequest = new StringRequest(Request.Method.POST, dburl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Post response from server in JSON
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if(error) {
                        update.add(Calendar.getInstance().getTime() + " - Location Updated");
                       // arrayAdapter.notifyDataSetChanged();
                    }else{
                        update.add(Calendar.getInstance().getTime() + " - Update Failed");
                        //arrayAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showLog("Volley error: "+error.toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                HashMap<String, String> locationData = new HashMap<>();
                locationData.put("uniqueId", uniqueId);
                locationData.put("latitude", Double.toString(Latitude));
                locationData.put("longitude", Double.toString(Longitude));
                locationData.put("dateCreated", finalDateCreated);
                localStoredLocations.add(locationData);
                return locationData;
            }
        };

        // Adding request to request queue
        requestQueue.add(stringRequest);
        }
        else {
            // If not connected, save data locally.
            LocationDBHelper dbHelper = new LocationDBHelper(this);
            dbHelper.insertLocation(latitude, longitude,getCurrentDate());
//              dbHelper.insertLocation(latitude, longitude);

                HashMap<String, String> locationData = new HashMap<>();
                locationData.put("uniqueId", uniqueId);
                locationData.put("latitude", Double.toString(Latitude));
                locationData.put("longitude", Double.toString(Longitude));
                locationData.put("dateCreated", getCurrentDate());
                localStoredLocations.add(locationData);

            }
        }



    // Get the current date in a suitable format
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date dateCreated = new Date();
        return dateFormat.format(dateCreated);
    }

    /**
     * Function to request Location Service Dialog
     */
    private void locationSettingsRequest(){
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(locationSettingsResponse -> {
                    // Start FusedLocation if GPS is enabled
                    requestLocationUpdate();
                })
                .addOnFailureListener(e -> {
                    // Show enable GPS Dialog and handle dialog buttons
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            //In a service, we can't interact with UI, so we can't show a dialog here.
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            showLog("Location Settings are Inadequate, and Cannot be fixed here. Fix in Settings");
                    }
                })
                .addOnCanceledListener(() -> {
                    showLog("Canceled No Thanks");
                });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("locations")) {
            ArrayList<HashMap<String, String>> locations = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("locations");

            // Iterate through stored locations and upload them
            for (HashMap<String, String> location : locations) {
                String latitude = location.get("latitude");
                String longitude = location.get("longitude");
                String dateCreated = getCurrentDate();

                HashMap<String, String> locationToPass = new HashMap<>();
                locationToPass.put("latitude", latitude);
                locationToPass.put("longitude", longitude);
                locationToPass.put("dateCreated", dateCreated);

                locations.add(locationToPass);
            }

            // Clear stored locations after successfully uploading them
            LocationDBHelper dbHelper = new LocationDBHelper(this);
            dbHelper.clearStoredLocations();
        }

        // Continue with other service logic
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    public void startSavingLocationsWhenDisconnected(final Context context) {
        if (handler == null) {
            handler = new Handler();
        }

        if (saveLocationTask != null) {
            handler.removeCallbacks(saveLocationTask);
        }

        // Initialize LocationManager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        saveLocationTask = new Runnable() {
            @Override
            public void run() {
                try {
                    // Request a single update of the device's location
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String dateCreated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                            // Save the location in the local database
                            LocationDBHelper dbHelper = new LocationDBHelper(context);
                            dbHelper.insertLocation(Double.toString(latitude), Double.toString(longitude),getCurrentDate());
                        }

                        // Implement other required methods for the LocationListener
                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                        }
                    }, null);
                } catch (SecurityException e) {
                    e.printStackTrace();
                    // Handle permission error if needed
                }

                // Schedule the next execution in 30 seconds
                handler.postDelayed(this, 30000);
            }
        };

        // Start the task immediately
        handler.post(saveLocationTask);
    }

    public void stopSavingLocationsWhenDisconnected() {
        if (handler != null && saveLocationTask != null) {
            handler.removeCallbacks(saveLocationTask);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            showLog("Canceled Location Thanks");
        } catch (Exception e) {
            e.printStackTrace();
        }
        unregisterReceiver(networkChangeReceiver);
    }

    // Use this method for logging
    private void showLog(String message) {
        Log.e(TAG, "" + message);
    }

    // And all other methods go here...

}
