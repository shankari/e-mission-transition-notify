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
        // category and actions required 
        // https://github.com/e-mission/e-mission-phone/issues/191#issuecomment-265659578
        noteOpt.category = "TEST_CATEGORY";
        noteOpt.actions = [];
        console.log("About to dispatch "+JSON.stringify(noteOpt));
        // console.log("notification plugin = "+JSON.stringify(window.cordova.plugins.notification.local));
        window.cordova.plugins.notification.local.schedule(noteOpt);
    }
}

module.exports = TransitionNotification;
