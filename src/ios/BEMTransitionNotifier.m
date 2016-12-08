/********* CDVbroadcaster.m Cordova Plugin Implementation *******/

#import "BEMTransitionNotifier.h"
#import "TripDiaryStateMachine.h"
#import "APPLocalNotification.h"

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

- (void)fireEvent:(NSString *)eventName data:(NSDictionary*)data;
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
                                                      [strongSelf fireEvent:(NSString*)note.object data:note.userInfo];
                                                  }];
}

- (void)dealloc
{
    
    for ( id observer in self.observerMap) {
    
        [[NSNotificationCenter defaultCenter] removeObserver:observer];
        
    }
    
    [_observerMap removeAllObjects];
    
    _observerMap = nil;
    
}

-(NSMutableDictionary *)observerMap
{
    if (!_observerMap) {
        _observerMap = [[NSMutableDictionary alloc] initWithCapacity:100];
    }
    
    return _observerMap;
}

- (void)fireEvent:(NSString *)eventName data:(NSDictionary*)data
{
    NSMutableDictionary* testObject = [NSMutableDictionary new];
    testObject[@"id"] = @737679;
    testObject[@"title"] = @"Direct through plugin";
    testObject[@"text"] = @"Incident to report";
    testObject[@"data"] = data;
    NSError *error;
    NSData *testObjectData = [NSJSONSerialization dataWithJSONObject:testObject
                                                       options:(NSJSONWritingOptions)0
                                                         error:&error];
    
    NSString* testObjectString = [[NSString alloc] initWithData:testObjectData encoding:NSUTF8StringEncoding];
    NSString *func = [NSString stringWithFormat:@"window.cordova.plugins.BEMTransitionNotification.dispatchIOSLocalNotification(%@);", testObjectString];
    
    [self.commandDelegate evalJs:func];
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

    id observer = self.observerMap[eventName];
    
    if (!observer) {
        __typeof(self) __weak weakSelf = self;
        
        observer = [[NSNotificationCenter defaultCenter] addObserverForName:eventName
                                                                     object:nil
                                                                      queue:[NSOperationQueue mainQueue]
                                                                 usingBlock:^(NSNotification *note) {
                                                                     
                                                                     __typeof(self) __strong strongSelf = weakSelf;
                                                                     
                                                                     [strongSelf fireEvent:eventName data:note.userInfo];
                                                                     
                                                                 }];
        [self.observerMap setObject:observer forKey:eventName];
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
    
    id observer = self.observerMap[ eventName ];
    
    if (observer) {
        
        [[NSNotificationCenter defaultCenter] removeObserver:observer
                                                        name:eventName
                                                      object:self];
    }
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}


@end
