package usertracking.vk.com.usertracking.userTracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by cropinvinay on 12/12/17.
 */

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = LocationUpdatesBroadcastReceiver.class.getSimpleName();
    public static final String ACTION_LOCATION_UPDATES = "com.vk.usertracking.LocationUpdatesBroadcastReceiver.ACTION_LOCATION_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
