package sheridan.aldahhas.androidtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectivityReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (isConnectedToInternet(context)) {
            LocationDBHelper dbHelper = new LocationDBHelper(context);
            ArrayList<HashMap<String, String>> locations = dbHelper.getAllLocations();

            for (HashMap<String, String> location : locations) {
                // Send each location to the server...
            }

            // Clear the stored locations after sending.
            dbHelper.clearStoredLocations();
        }
    }

    private boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
