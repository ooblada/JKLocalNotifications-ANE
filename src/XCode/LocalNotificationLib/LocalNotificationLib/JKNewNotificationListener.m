//
//  JKNewNotificationListener.m
//  LocalNotificationLib
//
//  Created by Juan Carlos Pazmino on 12/1/17.
//  Copyright © 2017 Juank. All rights reserved.
//

#import "JKNewNotificationListener.h"
#import "JKNotificationDispatcher.h"
#import "JKNotificationFactory.h"
#import "Constants.h"

static NSString *const JKNotificationDismissActionIdentifier = @"_JKNotificationDismissAction_";

@interface JKNewNotificationListener ()<UNUserNotificationCenterDelegate>

@end

@implementation JKNewNotificationListener

+ (void)load {
    UNUserNotificationCenter *center = JKNotificationFactory.factory.notificationCenter;
    if (!center) return;
    id<UNUserNotificationCenterDelegate> originalDelegate = center.delegate;
    center.delegate = [[self sharedListener] setupWithOriginalDelegate:originalDelegate];
}

@dynamic originalDelegate;

- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    if ([self.originalDelegate respondsToSelector:@selector(userNotificationCenter:willPresentNotification:withCompletionHandler:)]) {
        [self.originalDelegate userNotificationCenter:center willPresentNotification:notification withCompletionHandler:^(UNNotificationPresentationOptions options) {
            [self handleNotification:notification withCompletionHandler:completionHandler];
        }];
        return;
    }

    [self handleNotification:notification withCompletionHandler:completionHandler];
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler {
    if ([self.originalDelegate respondsToSelector:@selector(userNotificationCenter:didReceiveNotificationResponse:withCompletionHandler:)]) {
        [self.originalDelegate userNotificationCenter:center didReceiveNotificationResponse:response withCompletionHandler:^{
            [self handleResponse:response withCompletionHandler:completionHandler];
        }];
        return;
    }

    [self handleResponse:response withCompletionHandler:completionHandler];
}

- (void)handleNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    NSDictionary *userInfo = notification.request.content.userInfo;
    if ([userInfo[JK_NOTIFICATION_SHOW_IN_FOREGROUND] boolValue]) {
        completionHandler(UNNotificationPresentationOptionAlert | UNNotificationPresentationOptionBadge | UNNotificationPresentationOptionSound);
        return;
    }
    [self.dispatcher dispatchDidReceiveNotificationWithUserInfo:userInfo completionHandler:^{
        completionHandler(UNNotificationPresentationOptionNone);
    }];
}

- (void)handleResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler {
    NSDictionary *userInfo = response.notification.request.content.userInfo;
    [self.dispatcher dispatchDidReceiveNotificationWithActionId:[self actionIdentifierFromResponse:response]
                                                       userInfo:userInfo
                                                       response:[self userResponseFromResponse:response]
                                              completionHandler:completionHandler];
}

- (NSString *)userResponseFromResponse:(UNNotificationResponse *)response {
    if (![response isKindOfClass:[UNTextInputNotificationResponse class]]) return nil;
    return ((UNTextInputNotificationResponse *)response).userText;
}

- (NSString *)actionIdentifierFromResponse:(UNNotificationResponse *)response {
    if([response.actionIdentifier isEqualToString:UNNotificationDefaultActionIdentifier]) return nil;

    if([response.actionIdentifier isEqualToString:UNNotificationDismissActionIdentifier]) {
        return JKNotificationDismissActionIdentifier;
    }
    return response.actionIdentifier;
}

@end
