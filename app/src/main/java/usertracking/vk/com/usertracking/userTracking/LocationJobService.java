package usertracking.vk.com.usertracking.userTracking;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by Vinay on 27/11/17.
 */

public class LocationJobService extends JobService {

    public static final String TAG = LocationJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.e("JobService", "onStartJob..");
        UserTrackingManager.getInstance(this).startTracking(); // TO DO : need to fix, running on main thread
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.e("JobService", "onStopJob..");
        // jobFinished(job, false);
        return false;
    }
}
