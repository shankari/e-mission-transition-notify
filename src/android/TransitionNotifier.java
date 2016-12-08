package edu.berkeley.eecs.emission.cordova.transitionnotify;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.webkit.ValueCallback;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class TransitionNotifier extends CordovaPlugin {

    public static final String USERDATA = "userdata";
    private static String TAG =  TransitionNotifier.class.getSimpleName();

    public static final String EVENTNAME_ERROR = "event name null or empty.";

    java.util.Map<String,BroadcastReceiver> receiverMap =
                    new java.util.HashMap<String,BroadcastReceiver>(10);

    @Override
    public Object onMessage(String id, Object data) {
/*
        try {
            fireEvent( id, data );
        } catch (JSONException e) {
            Log.e(TAG, String.format("userdata [%s] for event [%s] is not a valid json object!", data, id));
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
        */
        return Boolean.TRUE;
    }

    /**
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return
     * @throws JSONException
     */
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
                    // fireNativeEvent(eventName, userData);
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
                            // fireEvent(eventName, userData);
                            throw new JSONException("Get this to compile now");

                        } catch (JSONException e) {
                            Log.e(TAG, "'userdata' is not a valid json object!");
                        }

                    }
                };

                // registerReceiver(r, new IntentFilter(eventName));

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

                // unregisterReceiver(r);


            }
            callbackContext.success();
            return true;
        }
        return false;
    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        // deregister receiver
        for( BroadcastReceiver r : receiverMap.values() ) {
                    // unregisterReceiver(r);
        }

        receiverMap.clear();

        super.onDestroy();

    }
}
