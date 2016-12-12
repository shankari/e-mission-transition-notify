/********* CDVbroadcaster.m Cordova Plugin Implementation *******/

#import "BEMTransitionNotifier.h"
#import "TripDiaryStateMachine.h"
#import "APPLocalNotification.h"
#import "BEMBuiltinUserCache.h"
#import "DataUtils.h"
#import "LocalNotificationManager.h"

#define TRIP_STARTED @"trip_started"
#define TRIP_ENDED @"trip_ended"
#define TRACKING_STARTED @"tracking_started"
#define TRACKING_STOPPED @"tracking_stopped"

#define CONFIG_LIST_KEY @"config_list"

static inline void throwWithName( NSError *error, NSString* name )
{
    if (error) {
        @throw [NSException exceptionWithName:name
                                       reason:error.debugDescription
                                     userInfo:@{ @"NSError" : error }];
    }
}

@interface BEMTransitionNotifier () {
  // Member variables go here.
}

@property (nonatomic,strong) NSMutableDictionary *observerMap;
- (void)pluginInitialize;
- (void)fireGenericTransitionFor:(NSString*) transition withUserInfo:(NSDictionary*) userInfo;
- (void)notifyEvent:(NSString *)eventName data:(NSDictionary*)data;
- (void)addEventListener:(CDVInvokedUrlCommand*)command;
- (void)removeEventListener:(CDVInvokedUrlCommand*)command;

@end


@implementation BEMTransitionNotifier

- (void)pluginInitialize
{
    // TODO: We should consider adding a create statement to the init, similar
    // to android - then it doesn't matter if the pre-populated database is not
    // copied over.
    NSLog(@"BEMTransitionNotifier:pluginInitialize singleton -> initialize statemachine and delegate");
    __typeof(self) __weak weakSelf = self;
    [[NSNotificationCenter defaultCenter] addObserverForName:CFCTransitionNotificationName object:nil queue:nil
                                                  usingBlock:^(NSNotification* note) {
                                                      __typeof(self) __strong strongSelf = weakSelf;
                                                      [strongSelf fireGenericTransitionFor:(NSString*)note.object withUserInfo:note.userInfo];
                                                  }];
}

- (void)fireGenericTransitionFor:(NSString*) transition withUserInfo:(NSDictionary*) userInfo {
    [LocalNotificationManager addNotification:[NSString stringWithFormat:@"Received platform-specific notification %@", transition] showUI:FALSE];

    if ([TripDiaryStateMachine instance].currState == kWaitingForTripStartState &&
            ([transition isEqualToString:CFCTransitionExitedGeofence] ||
             [transition isEqualToString:CFCTransitionVisitEnded])) {
                return [self postNativeAndNotify:TRIP_STARTED];
    }
    
    if ([transition isEqualToString:CFCTransitionTripEndDetected] ||
        [transition isEqualToString:CFCTransitionTripEnded]) {
        return [self postNativeAndNotify:TRIP_ENDED];
    }
    
    if ([transition isEqualToString:CFCTransitionTrackingStopped]) {
        return [self postNativeAndNotify:TRIP_ENDED];
}

    if ([transition isEqualToString:CFCTransitionStartTracking]) {
        return [self postNativeAndNotify:TRIP_ENDED];
    }
    }
    
- (void)postNativeAndNotify:(NSString*)genericTransition
{
    [LocalNotificationManager addNotification:[NSString stringWithFormat:@"Broadcasting generic transition %@ and generating notification", genericTransition] showUI:FALSE];

    [[NSNotificationCenter defaultCenter] postNotificationName:genericTransition
                                                        object:self];
    [self notifyEvent:genericTransition data:NULL];
    return;
}

- (void)notifyEvent:(NSString *)eventName data:(NSDictionary*)data
{
    [LocalNotificationManager addNotification:[NSString stringWithFormat:@"Generating all notifications for generic %@", eventName] showUI:FALSE];
    
    NSDictionary* notifyConfigWrapper = [[BuiltinUserCache database] getLocalStorage:eventName
                                                                        withMetadata:NO];
    if (notifyConfigWrapper == NULL) {
        [LocalNotificationManager addNotification:[NSString stringWithFormat:@"no configurations found for event %@, skipping notification", eventName] showUI:FALSE];
        return;
    }
    
    NSArray* notifyConfigs = notifyConfigWrapper[CONFIG_LIST_KEY];
    for(int i=0; i < [notifyConfigs count]; i++) {
        NSDictionary* currNotifyConfig = [notifyConfigs objectAtIndex:i];
        NSString* currNotifyString = [DataUtils saveToJSONString:currNotifyConfig];
        NSString *func = [NSString stringWithFormat:@"window.cordova.plugins.BEMTransitionNotification.dispatchIOSLocalNotification(%@);", currNotifyString];
        [LocalNotificationManager addNotification:[NSString stringWithFormat:@"generating notification for event %@ and id %@", eventName, currNotifyConfig[@"id"]] showUI:FALSE];
    
    [self.commandDelegate evalJs:func];
}
}

- (void)addEventListener:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult;
    
    __block NSString* eventName = command.arguments[0];
    
    if (eventName == nil || [eventName length] == 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"eventName is null or empty"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    __block NSDictionary* localNotifyConfig = command.arguments[1];
        
    if (localNotifyConfig == nil || [localNotifyConfig count] == 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"localNotifyConfig is null or empty"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
                                                                     
    NSMutableDictionary* configWrapper = [[BuiltinUserCache database] getLocalStorage:eventName
                                                                      withMetadata:NO];
    NSMutableArray* currList;
    
    if (configWrapper == NULL) {
        configWrapper = [NSMutableDictionary new];
        currList = [NSMutableArray new];
        configWrapper[CONFIG_LIST_KEY] = currList;
    } else {
        currList = configWrapper[CONFIG_LIST_KEY];
    }
                                                                     
    // Checking for the invariant
    assert(configWrapper != NULL && currList != NULL && configWrapper[CONFIG_LIST_KEY] == currList);
                                                                     
    NSUInteger existingIndex = [currList indexOfObjectPassingTest:^BOOL(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        return obj[@"id"] == localNotifyConfig[@"id"];
                                                                 }];
    
    BOOL modified = YES;
    if (existingIndex == NSNotFound) {
        [LocalNotificationManager addNotification:[NSString stringWithFormat:@"new configuration, adding object with id %@", localNotifyConfig[@"id"]]];
        [currList addObject:localNotifyConfig];
    } else {
        if ([localNotifyConfig isEqualToDictionary:currList[existingIndex]]) {
            [LocalNotificationManager addNotification:[NSString stringWithFormat:@"configuration unchanged, skipping list modify"]];
            modified = NO;
        } else {
            [LocalNotificationManager addNotification:[NSString stringWithFormat:@"configuration changed, changing object at index %lu", existingIndex]];
            [currList setObject:localNotifyConfig atIndexedSubscript:existingIndex];
        }
    }
    
    if (modified) {
        [[BuiltinUserCache database] putLocalStorage:eventName jsonValue:configWrapper];
    }
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)removeEventListener:(CDVInvokedUrlCommand*)command
{

    CDVPluginResult* pluginResult;

    __block NSString* eventName = command.arguments[0];
    
    if (eventName == nil || [eventName length] == 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"eventName is null or empty"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    __block NSDictionary* localNotifyConfig = command.arguments[1];
    
    if (localNotifyConfig == nil || [localNotifyConfig count] == 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"localNotifyConfig is null or empty"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
        
    NSMutableDictionary* configWrapper = [[BuiltinUserCache database] getLocalStorage:eventName
                                                                         withMetadata:NO];
    if (configWrapper != NULL) { // There is an existing entry for this event
        NSMutableArray* currList = configWrapper[CONFIG_LIST_KEY];
        NSUInteger existingIndex = [currList indexOfObjectPassingTest:^BOOL(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            return obj[@"id"] == localNotifyConfig[@"id"];
        }];
        if (existingIndex != -1) { // There is an existing entry for this ID
            [LocalNotificationManager addNotification:[NSString stringWithFormat:@"removed obsolete notification at %lu", existingIndex]];
            [currList removeObjectAtIndex:existingIndex];
            if ([currList count] == 0) { // list size is now zero, can remove the entry
                [LocalNotificationManager addNotification:[NSString stringWithFormat:@"list size is now 0, removing entry for event %@", eventName]];
                [[BuiltinUserCache database] removeLocalStorage:eventName];
            } else {
                [LocalNotificationManager addNotification:[NSString stringWithFormat:@"saving list with size %lu", [currList count]];
                [[BuiltinUserCache database] putLocalStorage:eventName jsonValue:configWrapper];
            }
        }
    }
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}


@end
