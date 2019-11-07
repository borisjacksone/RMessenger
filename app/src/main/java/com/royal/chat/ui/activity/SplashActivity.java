package com.royal.chat.ui.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.royal.chat.App;
import com.royal.chat.R;
import com.royal.chat.ui.dialog.ProgressDialogFragment;
import com.royal.chat.utils.SharedPrefsHelper;
import com.royal.chat.utils.SystemPermissionHelper;
import com.royal.chat.utils.ToastUtils;
import com.royal.chat.utils.audiopick.AudioPickHelper;
import com.royal.chat.utils.chat.ChatHelper;
import com.quickblox.users.model.QBUser;

import java.util.Locale;

public class SplashActivity extends BaseActivity {
    private static final int SPLASH_DELAY = 2000;

    private static final String TAG = SplashActivity.class.getSimpleName();
    private SystemPermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        String locale = sharedPrefsHelper.getLocale();
        sharedPrefsHelper.putLocale(locale);
        setAppLocale(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelIfNotExist();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        fillVersion();

        permissionHelper = new SystemPermissionHelper(this);
        if (permissionHelper.isAllPermissionGranted()) {
            startHomeActivity();
        } else {
            permissionHelper.requestPermissionsForAll();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SystemPermissionHelper.PERMISSIONS_FOR_ALL && grantResults[0] != -1) {
            if (permissionHelper.isAllPermissionGranted()) {
                startHomeActivity();
            } else {
                permissionHelper.requestPermissionsForAll();
            }
        } else {
            permissionHelper.requestPermissionsForAll();
        }
    }

    private void startHomeActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SharedPrefsHelper.getInstance().hasQbUser()) {
                    restoreChatSession();
                } else {
                    LoginActivity.start(SplashActivity.this);
                    finish();
                }
            }
        }, SPLASH_DELAY);
    }

    private void setAppLocale(String localeCode) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(new Locale(localeCode.toLowerCase()));
        }
        res.updateConfiguration(conf, dm);
    }

    @Override
    public void onBackPressed() {

    }

    private void fillVersion() {
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            ((TextView) findViewById(R.id.text_splash_app_version)).setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            showErrorSnackbar(R.string.error, e, null);
        }
    }

    private void restoreChatSession() {
        if (ChatHelper.getInstance().isLogged()) {
            DialogsActivity.start(this);
            finish();
        } else {
            QBUser currentUser = getUserFromSession();
            if (currentUser == null) {
                LoginActivity.start(this);
                finish();
            } else {
                loginToChat(currentUser);
            }
        }
    }

    private QBUser getUserFromSession() {
        QBUser user = SharedPrefsHelper.getInstance().getQbUser();
        QBSessionManager qbSessionManager = QBSessionManager.getInstance();
        if (qbSessionManager.getSessionParameters() == null) {
            ChatHelper.getInstance().destroy();
            return null;
        }
        int userId = qbSessionManager.getSessionParameters().getUserId();
        user.setId(userId);
        return user;
    }

    private void loginToChat(final QBUser user) {
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_restoring_chat_session);

        ChatHelper.getInstance().loginToChat(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                Log.v(TAG, "Chat login onSuccess()");
                ProgressDialogFragment.hide(getSupportFragmentManager());
                DialogsActivity.start(SplashActivity.this);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                if (e.getMessage().equals("You have already logged in chat")) {
                    loginToChat(user);
                } else {
                    ProgressDialogFragment.hide(getSupportFragmentManager());
                    Log.w(TAG, "Chat login onError(): " + e);
                    showErrorSnackbar(R.string.error_recreate_session, e,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    loginToChat(user);
                                }
                            });
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannelIfNotExist() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager.getNotificationChannel(App.CHANNEL_ONE_ID) == null) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(App.CHANNEL_ONE_ID, App.CHANNEL_ONE_NAME, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}