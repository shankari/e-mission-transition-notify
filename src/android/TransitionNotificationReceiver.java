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

public class TransitionNotificationReceiver extends BroadcastReceiver {

    public static final String USERDATA = "userdata";
    private static String TAG =  TransitionNotificationReceiver.class.getSimpleName();

    public static final String EVENTNAME_ERROR = "event name null or empty.";

    java.util.Map<String,BroadcastReceiver> receiverMap =
                    new java.util.HashMap<String,BroadcastReceiver>(10);

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
        fireEvent(context, intent.getAction(), new JSONObject());
    }

    /**
     * @param context
     * @param eventName
     * @param jsonData
     * @throws JSONException
     */
    protected void fireEvent( final Context context, final String eventName, final JSONObject jsonData) {
        try {
        JSONObject testObject = new JSONObject("{'id': 737679," +
                "'title': 'Direct through plugin'," +
                "'text': 'Incident to report?'}");
        testObject.put("data", jsonData);
            Manager.getInstance(context).schedule(testObject, TriggerReceiver.class);
        } catch(JSONException e) {
            Log.e(context, TAG, e.getMessage());
            Log.e(context, TAG, e.toString());
        }
    }

    /*
    protected void registerReceiver(android.content.BroadcastReceiver receiver, android.content.IntentFilter filter) {
        LocalBroadcastManager.getInstance(super.webView.getContext()).registerReceiver(receiver,filter);
    }

    protected void unregisterReceiver(android.content.BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(super.webView.getContext()).unregisterReceiver(receiver);
    }

    protected boolean sendBroadcast(android.content.Intent intent) {
        return LocalBroadcastManager.getInstance(super.webView.getContext()).sendBroadcast(intent);
    }

    @Override
    public Object onMessage(String id, Object data) {

        try {
            fireEvent( id, data );
        } catch (JSONException e) {
            Log.e(TAG, String.format("userdata [%s] for event [%s] is not a valid json object!", data, id));
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private void fireNativeEvent( final String eventName, JSONObject userData ) {
        if( eventName == null ) {
            throw new IllegalArgumentException("eventName parameter is null!");
        }

        final Intent intent = new Intent(eventName);

        if( userData != null ) {
            Bundle b = new Bundle();
            b.putString(USERDATA, userData.toString());
            intent.putExtras(b);
        }

        sendBroadcast( intent );
    }
    */

    /**
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return
     * @throws JSONException
     */
    /*
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if( action.equals("fireNativeEvent")) {

            final String eventName = args.getString(0);
            if( eventName==null || eventName.isEmpty() ) {
                callbackContext.error(EVENTNAME_ERROR);

            }
            final JSONObject userData = args.getJSONObject(1);


            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    fireNativeEvent(eventName, userData);
                }
            });

            callbackContext.success();
            return true;
        }
        else if (action.equals("addEventListener")) {

            final String eventName = args.getString(0);
            if (eventName == null || eventName.isEmpty()) {
                callbackContext.error(EVENTNAME_ERROR);
                return false;
            }
            if (!receiverMap.containsKey(eventName)) {

                final BroadcastReceiver r = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, final Intent intent) {

                        final Bundle b = intent.getExtras();

                        // parse the JSON passed as a string.
                        try {

                            String userData = "{}";
                            if (b != null) {//  in some broadcast there might be no extra info
                                userData = b.getString(USERDATA, "{}");
                            } else {
                                Log.v(TAG, "No extra information in intent bundle");
                            }
                            fireEvent(eventName, userData);

                        } catch (JSONException e) {
                            Log.e(TAG, "'userdata' is not a valid json object!");
                        }

                    }
                };

                registerReceiver(r, new IntentFilter(eventName));

                receiverMap.put(eventName, r);
            }
            callbackContext.success();

            return true;
        } else if (action.equals("removeEventListener")) {

            final String eventName = args.getString(0);
            if (eventName == null || eventName.isEmpty()) {
                callbackContext.error(EVENTNAME_ERROR);
                return false;
            }

            BroadcastReceiver r = receiverMap.remove(eventName);

            if (r != null) {

                unregisterReceiver(r);


            }
            callbackContext.success();
            return true;
        }
        return false;
    }
    */

    /**
     *
     */
    /*
    @Override
    public void onDestroy() {
        // deregister receiver
        for( BroadcastReceiver r : receiverMap.values() ) {
                    unregisterReceiver(r);
        }

        receiverMap.clear();

        super.onDestroy();

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void sendJavascript(final String javascript) {
        webView.getView().post(new Runnable() {
           @Override
           public void run() {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.sendJavascript(javascript);
                   } else {
                    webView.loadUrl("javascript:".concat(javascript));
                    }
               }
            });
    }
    */
}
