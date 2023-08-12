package sheridan.aldahhas.androidtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Start service or other actions when the device boots up
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (isConnected) {
                // Handle network connected actions (e.g., upload stored locations)

                // Retrieve stored locations
                LocationDBHelper dbHelper = new LocationDBHelper(context);
                ArrayList<HashMap<String, String>> storedLocations = dbHelper.getAllLocations();

                // Create an intent to start RealtimeService
                Intent startServiceIntent = new Intent(context, RealtimeService.class);

                // Convert stored locations into a serializable form (e.g., ArrayList of Pairs)
                ArrayList<HashMap<String, String>> locationsToPass = new ArrayList<>();
                for (HashMap<String, String> location : storedLocations) {
                    String latitude = location.get("latitude");
                    String longitude = location.get("longitude");
//                    String dateCreated = getCurrentDate();

                    HashMap<String, String> locationToPass = new HashMap<>();
                    locationToPass.put("latitude", latitude);
                    locationToPass.put("longitude", longitude);
//                    locationToPass.put("dateCreated", dateCreated);

                    locationsToPass.add(locationToPass);
                }




                startServiceIntent.putExtra("locations", locationsToPass);

                // Start the service to send locations to the server
                context.startService(startServiceIntent);

                // Clear local storage
                dbHelper.clearStoredLocations();
            } else {
                // Handle network disconnected actions (e.g., store locations)
                RealtimeService realtimeService = new RealtimeService();
                realtimeService.startSavingLocationsWhenDisconnected(context);
            }
        }
    }
    // Get the current date in a suitable format
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date dateCreated = new Date();
        return dateFormat.format(dateCreated);
    }


}

