package usertracking.vk.com.usertracking.userTracking;

import android.content.Context;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;

/**
 * Created by Vinay on 28/11/17.
 */

public class UserTrackingManager {

    private static final String TAG = UserTrackingManager.class.getSimpleName();

    private FirebaseJobDispatcher dispatcher;

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

    }

    public void stopTracking() {

    }
}

