package edu.berkeley.eecs.emission.cordova.transitionnotify;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.ValueCallback;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Importing dependencies from the notification plugin
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.appplant.cordova.plugin.localnotification.TriggerReceiver;
import de.appplant.cordova.plugin.notification.Manager;

/*
 * Importing dependencies from the logger plugin
 */
import edu.berkeley.eecs.emission.R;
import edu.berkeley.eecs.emission.cordova.unifiedlogger.Log;
import edu.berkeley.eecs.emission.cordova.usercache.UserCache;
import edu.berkeley.eecs.emission.cordova.usercache.UserCacheFactory;

public class TransitionNotificationReceiver extends BroadcastReceiver {

    public static final String USERDATA = "userdata";
    private static String TAG =  TransitionNotificationReceiver.class.getSimpleName();

    public static final String EVENTNAME_ERROR = "event name null or empty.";

    private static final String TRIP_STARTED = "trip_started";
    private static final String TRIP_ENDED = "trip_ended";
    private static final String TRACKING_STARTED = "tracking_started";
    private static final String TRACKING_STOPPED = "tracking_stopped";

    private static final String CONFIG_LIST_KEY = "config_list";

    public TransitionNotificationReceiver() {
        // The automatically created receiver needs a default constructor
        android.util.Log.i(TAG, "noarg constructor called");
    }

    public TransitionNotificationReceiver(Context context) {
        android.util.Log.i(TAG, "constructor called with arg "+context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(context, TAG, "TripDiaryStateMachineReciever onReceive(" + context + ", " + intent + ") called");

        // Next two calls copied over from the constructor, figure out if this is the best place to
        // put them
        Set<String> validTransitions = new HashSet<String>(Arrays.asList(new String[]{
                context.getString(R.string.transition_initialize),
                context.getString(R.string.transition_exited_geofence),
                context.getString(R.string.transition_stopped_moving),
                context.getString(R.string.transition_stop_tracking),
                context.getString(R.string.transition_start_tracking),
                context.getString(R.string.transition_tracking_error)
        }));

        if (!validTransitions.contains(intent.getAction())) {
            Log.e(context, TAG, "Received unknown action "+intent.getAction()+" ignoring");
            return;
        }
        fireGenericTransition(context, intent.getAction(), new JSONObject());
    }

    /**
     * @param context
     * @param eventName
     * @param userInfo
     * @throws JSONException
     */
    protected void fireGenericTransition( final Context context, final String eventName,
                                          final JSONObject userInfo) {
        Log.d(context, TAG, "Received platform-specification notification "+eventName);
        if (eventName.equals(context.getString(R.string.transition_exited_geofence))) {
            postNativeAndNotify(context, TRIP_STARTED);
    }

        if (eventName.equals(context.getString(R.string.transition_stopped_moving))) {
            postNativeAndNotify(context, TRIP_ENDED);
        }

        if (eventName.equals(context.getString(R.string.transition_stop_tracking))) {
            postNativeAndNotify(context, TRACKING_STOPPED);
        }

        if (eventName.equals(context.getString(R.string.transition_start_tracking))) {
            postNativeAndNotify(context, TRACKING_STARTED);
    }
            }

    public void postNativeAndNotify(Context context, String genericTransition) {
        Log.d(context, TAG, "Broadcasting generic transition "+genericTransition
                +" and generating notification");
        Intent genericTransitionIntent = new Intent();
        genericTransitionIntent.setAction(genericTransition);
        context.sendBroadcast(genericTransitionIntent);
        notifyEvent(context, genericTransition, null);
            }

    public void notifyEvent(Context context, String eventName, JSONObject jsonData) {
        Log.d(context, TAG, "Generating all notifications for generic "+eventName);
                        try {
            JSONObject notifyConfigWrapper = UserCacheFactory.getUserCache(context).getLocalStorage(eventName, false);
            if (notifyConfigWrapper == null) {
                Log.d(context, TAG, "no configuration found for event "+eventName+", skipping notification");
                return;
        }
            JSONArray notifyConfigs = notifyConfigWrapper.getJSONArray(CONFIG_LIST_KEY);
            for(int i = 0; i < notifyConfigs.length(); i++) {
               try {
                   JSONObject currNotifyConfig = notifyConfigs.getJSONObject(i);
                   Log.d(context, TAG, "generating notification for event "+eventName
                           +" and id = "+currNotifyConfig.getLong("id"));
                   Manager.getInstance(context).schedule(currNotifyConfig, TriggerReceiver.class);
               } catch (Exception e) {
                   Log.e(context, TAG, "Got error "+e.getMessage()+" while processing object "
                           + notifyConfigs.getJSONObject(i) + " at index "+i);
    }
                    }
        } catch(JSONException e) {
            Log.e(context, TAG, e.getMessage());
            Log.e(context, TAG, e.toString());
               }
    }
}
