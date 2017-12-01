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
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Vinay on 28/11/17.
 */

public class UserTrackingManager {

    private static final String TAG = UserTrackingManager.class.getSimpleName();

    private FirebaseJobDispatcher dispatcher;
    private static final long UPDATE_INTERVAL = 1000;
    private static final long FASTEST_UPDATE_INTERVAL = 300;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5;
    public final static String TRACKING_TIME_FORMAT = "HH:mm:ss";

    private static UserTrackingManager mInstance;
    private static Context mContext;

    public boolean isUserTrackingEnabled;
    private int trackingInterval = 30;
    private String trackingStartTime = "09:00:00";
    private String trackingEndTime = "20:00:00";

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
        if (!isUserTrackingEnabled)
            return;

        Log.e(TAG, "Reschedule tracking in " + getNextInterval() + " seconds");

        // Create a new dispatcher using the Google Play driver.
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(mContext));

        Job userTrackingJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(LocationJobService.class)
                // uniquely identifies the job
                .setTag(LocationJobService.TAG)
                // one-off job
                .setRecurring(false)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // start at exact intervals configured
                .setTrigger(Trigger.executionWindow(getNextInterval(), getNextInterval()))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
                .build();

        dispatcher.mustSchedule(userTrackingJob);
    }

    public int getNextInterval() {
        if (isTrackingWindow()) {
            return trackingInterval;
        } else {
            return getNextDaysInterval();
        }
    }

    public String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(TRACKING_TIME_FORMAT);
        return sdf.format(new Date());
    }

    public boolean isTrackingWindow() {
        Log.e(TAG, "isTrackingWindow..");

        String currentTime = getCurrentTime();

        boolean valid = false;

        try {
            String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
            //
            if (trackingStartTime.matches(reg) && trackingEndTime.matches(reg) && currentTime.matches(reg)) {

                // Start Time
                Date sTime = new SimpleDateFormat(TRACKING_TIME_FORMAT).parse(trackingStartTime);

                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(sTime);

                // Current Time
                Date cTime = new SimpleDateFormat(TRACKING_TIME_FORMAT).parse(currentTime);
                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(cTime);

                // End Time
                Date eTime = new SimpleDateFormat(TRACKING_TIME_FORMAT).parse(trackingEndTime);
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(eTime);

                //
                if (currentTime.compareTo(trackingEndTime) < 0) {
                    currentCalendar.add(Calendar.DATE, 1);
                    cTime = currentCalendar.getTime();
                }

                if (trackingStartTime.compareTo(trackingEndTime) < 0) {
                    startCalendar.add(Calendar.DATE, 1);
                    sTime = startCalendar.getTime();
                }

                //
                if (cTime.before(sTime)) {
                    valid = false;
                } else {

                    if (cTime.after(eTime)) {
                        endCalendar.add(Calendar.DATE, 1);
                        eTime = endCalendar.getTime();
                    }

                    if (cTime.before(eTime)) {
                        valid = true;
                    } else {
                        valid = false;
                    }
                }

                Log.e(TAG, "" + valid);
                return valid;

            }
        } catch (Exception e) {
            Log.e(TAG, "Exception : " + e);
        }
        Log.e(TAG, "" + valid);
        return valid;
    }

    public int getNextDaysInterval() {

        Log.e(TAG, "getNextDaysInterval..");

        // startTime - currentTime

        try {
            Date cTime = new SimpleDateFormat(TRACKING_TIME_FORMAT).parse(getCurrentTime());
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(cTime);

            Date sTime = new SimpleDateFormat(TRACKING_TIME_FORMAT).parse(trackingStartTime);
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.add(Calendar.DATE, 1);
            startCalendar.setTime(sTime);

            long timeDifferenceInSeconds = ((startCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis()) / (60 * 1000)) * 60;

            return (int) Math.abs(timeDifferenceInSeconds);
        } catch (Exception e) {
            Log.e(TAG, "Exception : " + e);
        }

        return trackingInterval; // should never flow here if tracking properly configured for the company
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

