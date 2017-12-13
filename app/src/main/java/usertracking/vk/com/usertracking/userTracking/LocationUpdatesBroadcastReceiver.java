package usertracking.vk.com.usertracking.userTracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;

/**
 * Created by Vinay on 27/11/17.
 */

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = LocationUpdatesBroadcastReceiver.class.getSimpleName();
    public static final String ACTION_LOCATION_UPDATES = "com.vk.usertracking.LocationUpdatesBroadcastReceiver.ACTION_LOCATION_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive..");
        if (intent != null) {
            final String action = intent.getAction();
            UserTrackingManager trackingManager = UserTrackingManager.getInstance(context);

            // Manage user location updates received
            if (ACTION_LOCATION_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    trackingManager.persistUserLocation(locations);
                    if (locations.size() > 0) {
                        trackingManager.stopTracking();
                    }
                }
            }
            // Manage tracking when location provider turned off / on
            else if (action.equals("android.location.PROVIDERS_CHANGED")) {
                Log.d(TAG, "Location turned on / off");

                if (trackingManager.isDeviceLocationEnabled()) {
                    // resume tracking
                    trackingManager.reScheduleTracking();
                } else {
                    //stop cancel scheduled jobs
                    trackingManager.cancelScheduledJob();
                }
            }
        }
    }
}
