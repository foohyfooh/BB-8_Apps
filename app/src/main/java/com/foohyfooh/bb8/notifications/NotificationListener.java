package com.foohyfooh.bb8.notifications;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.foohyfooh.bb8.BB8CommandService;
import com.foohyfooh.bb8.utils.ColourUtils;

public class NotificationListener extends NotificationListenerService {
        
    private static final String TAG = "NotificationListener";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationPosted: ID :" + sbn.getId() + "\t"
                + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

        Config config = ConfigDatabase.getInstance(getApplicationContext()).dao().get(sbn.getPackageName());
        if(config != null){
            Log.d(TAG, "onNotificationPosted: Changing Colour for " + sbn.getPackageName());
            Intent serviceIntent = new Intent(this, BB8CommandService.class);
            serviceIntent.setAction(config.getPattern());
            serviceIntent.putExtra(BB8CommandService.EXTRA_COLOUR, ColourUtils.extractColoursToArray(config.getHexColour()));
            getApplicationContext().startService(serviceIntent);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationRemoved ID :" + sbn.getId() + "\t"
                + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
    }

}
