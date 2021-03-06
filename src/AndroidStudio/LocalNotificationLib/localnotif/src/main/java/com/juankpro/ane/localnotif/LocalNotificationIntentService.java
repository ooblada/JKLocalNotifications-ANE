package com.juankpro.ane.localnotif;

import android.app.IntentService;
import android.content.Intent;

import com.juankpro.ane.localnotif.util.ApplicationStatus;
import com.juankpro.ane.localnotif.util.Logger;

import java.util.Objects;

/**
 * Created by Juank on 10/21/17.
 */

public class LocalNotificationIntentService extends IntentService {
    public LocalNotificationIntentService() {
        super(Constants.NOTIFICATION_INTENT_SERVICE);
    }

    @Override
    public final void onHandleIntent(Intent intent) {
        boolean backgroundMode = intent.getBooleanExtra(Constants.BACKGROUND_MODE_KEY, false);

        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        tryEventDispatch(intent);

        handleResponse(intent);

        logStatus(intent);
        Logger.log("Background mode: " + backgroundMode);
        if(backgroundMode) {
            if (!ApplicationStatus.getActive()) openActivityInBackground(intent);
        }
        else if (!ApplicationStatus.getInForeground()) {
            openActivityInForeground(intent);
        }
    }

    private void tryEventDispatch(Intent intent) {
        String code = intent.getStringExtra(Constants.NOTIFICATION_CODE_KEY);
        byte[] data = intent.getByteArrayExtra(Constants.ACTION_DATA_KEY);
        String actionId = intent.getStringExtra(Constants.ACTION_ID_KEY);
        String userResponse = intent.getStringExtra(Constants.USER_RESPONSE_KEY);
        Logger.log("LocalNotificationIntentService trying event dispatch");
        new LocalNotificationEventDispatcher(code, data, actionId, userResponse).dispatchWhenActive();
    }

    private void handleResponse(Intent intent) {
        String userResponse = intent.getStringExtra(Constants.USER_RESPONSE_KEY);
        if (userResponse == null) return;
        new NotificationDispatcher(getApplicationContext(), Objects.requireNonNull(intent.getExtras())).dispatch();
    }

    private void openActivityInBackground(Intent intent) {
        openActivity(intent, true);
    }

    private void openActivityInForeground(Intent intent) {
        openActivity(intent, false);
    }

    private void openActivity(Intent intent, boolean backgroundMode) {
        Intent newIntent = new Intent();
        newIntent.setClassName(getApplicationContext(), Objects.requireNonNull(intent.getStringExtra(Constants.MAIN_ACTIVITY_CLASS_NAME_KEY)));
        newIntent.putExtra(Constants.BACKGROUND_MODE_KEY, backgroundMode);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(newIntent);
    }

    private void logStatus(Intent intent) {
        if(ApplicationStatus.getActive()) {
            Logger.log("LocalNotificationIntentService::onHandleIntent App is running in foreground");
        } else {
            Logger.log("LocalNotificationIntentService::onHandleIntent App is running in background or not running");
        }
        Logger.log("LocalNotificationIntentService::onHandleIntent Intent: " + intent.toString());
    }
}