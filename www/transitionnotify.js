/*global cordova, module*/

var exec = require("cordova/exec")

/*
 * Format of the returned value:
 * {
 *    "isDutyCycling": true/false,
 *    "accuracy": "high/balanced/hundredmeters/..",
 *    "geofenceRadius": 1234,
 *    "accuracyThreshold": 1234,
 *    "filter": "time/distance",
 *    "filterValue": 1234,
 *    "tripEndStationaryMins": 1234
 * }
 */

var TransitionNotification = {
    TRIP_END: 'trip_ended',

    addEventListener: function(eventName, notifyOptions) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, "TransitionNotification", "addEventListener", [eventName, notifyOptions]);
        });
    },
    removeEventListener: function(eventName, notifyOptions) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, "TransitionNotification", "removeEventListener", [eventName, notifyOptions]);
        });
    },
    /*
     * The iOS local notification code.
     * See 
     * https://github.com/e-mission/e-mission-phone/issues/191#issuecomment-265574206
     * Invoked only from the iOS native code.
     */
    dispatchIOSLocalNotification: function(noteOpt) {
        console.log("About to dispatch "+JSON.stringify(noteOpt));
        window.cordova.plugins.notification.local.schedule(noteOpt);
    }
}

module.exports = TransitionNotification;
