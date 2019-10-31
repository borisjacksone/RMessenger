package com.royal.chat.fcm;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.quickblox.messages.services.fcm.QBFcmPushListenerService;
import com.royal.chat.App;
import com.royal.chat.R;
import com.royal.chat.ui.activity.SplashActivity;
import com.royal.chat.utils.ActivityLifecycle;
import com.royal.chat.utils.NotificationUtils;

import java.util.Map;

public class PushListenerService extends QBFcmPushListenerService {
    private static final String TAG = PushListenerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    protected void showNotification(String message) {
        NotificationUtils.showNotification(App.getInstance(), SplashActivity.class,
                App.getInstance().getString(R.string.notification_title), message,
                R.mipmap.ic_notification, NOTIFICATION_ID);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    @Override
    protected void sendPushMessage(Map data, String from, String message) {
        super.sendPushMessage(data, from, message);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);
        if (ActivityLifecycle.getInstance().isBackground()) {
            showNotification(message);
        }
    }
}