package usertracking.vk.com.usertracking.userTracking;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Vinay on 28/11/17.
 */

public class UserTrackingManager {

    private static final String TAG = UserTrackingManager.class.getSimpleName();

    private FirebaseJobDispatcher dispatcher;
    private static final long UPDATE_INTERVAL = 1000;
    private static final long FASTEST_UPDATE_INTERVAL = 300;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5;

    private static UserTrackingManager mInstance;
    private static Context mContext;

    private UserTrackingManager() {
    }

    public static synchronized UserTrackingManager getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new UserTrackingManager();
            mContext = context;

        }

        return mInstance;
    }

    public void startTracking() {
        Log.e(TAG, "Start Tracking..");

        if (!isDeviceLocationEnabled())
            return;

        // prepare location request
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);

        // check for permission
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // request updates
        LocationServices.getFusedLocationProviderClient(mContext).requestLocationUpdates(mLocationRequest, getPendingIntent());
    }

    public void stopTracking() {
        Log.e(TAG, "stopUserTracking..");

        // stop updates
        LocationServices.getFusedLocationProviderClient(mContext).removeLocationUpdates(getPendingIntent());

        if (isDeviceLocationEnabled())
            reScheduleTracking();
    }

    public void reScheduleTracking() {

    }

    public PendingIntent getPendingIntent() {
        Intent intent = new Intent(mContext, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_LOCATION_UPDATES);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    public boolean isDeviceLocationEnabled() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}

