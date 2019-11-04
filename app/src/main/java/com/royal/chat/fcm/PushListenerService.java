package com.royal.chat.fcm;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.royal.chat.App;
import com.royal.chat.R;
import com.royal.chat.ui.activity.SplashActivity;
import com.royal.chat.utils.NotificationUtils;

public class PushListenerService extends FirebaseMessagingService {

    private static final int NOTIFICATION_ID = 1;

    protected void showNotification(String message) {
        NotificationUtils.showNotification(
                App.getInstance(),
                SplashActivity.class,
                App.getInstance().getString(R.string.notification_title),
                message,
                R.drawable.app_icon,
                NOTIFICATION_ID);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        showNotification(remoteMessage.getNotification().getBody());
    }
}