package com.royal.chat.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class SystemPermissionHelper {
    public static final int PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST = 1010;
    public static final int PERMISSIONS_FOR_SAVE_FILE_AUDIO_REQUEST = 2020;
//    public static final int PERMISSIONS_FOR_SEND_SMS_REQUEST = 3030;
    public static final int PERMISSIONS_FOR_ALL = 4040;

    private Activity activity;
    private Fragment fragment;

    private String[] all_permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
//            Manifest.permission.READ_SMS,
//            Manifest.permission.SEND_SMS,
//            Manifest.permission.READ_CONTACTS
    };

    public SystemPermissionHelper(Activity activity) {
        this.activity = activity;
    }

    public SystemPermissionHelper(Fragment fragment) {
        this.fragment = fragment;
    }

    public boolean isSaveImagePermissionGranted() {
        return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) && isPermissionGranted(Manifest.permission.CAMERA);
    }

    public boolean isSaveAudioPermissionGranted() {
        return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) && isPermissionGranted(Manifest.permission.RECORD_AUDIO);
    }

//    public boolean isSendSmsPermissionGranted() {
//        return isPermissionGranted(Manifest.permission.READ_SMS) && isPermissionGranted(Manifest.permission.SEND_SMS) && isPermissionGranted(Manifest.permission.READ_CONTACTS);
//    }

    public boolean isAllPermissionGranted() {
        for (String permission : all_permissions) {
            if (!isPermissionGranted(permission)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPermissionGranted(String permission) {
        if (fragment != null) {
            return ContextCompat.checkSelfPermission(fragment.getContext(), permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void requestPermissionsForSaveFileImage() {
        checkAndRequestPermissions(PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
    }

    public void requestPermissionsForSaveFileAudio() {
        checkAndRequestPermissions(PERMISSIONS_FOR_SAVE_FILE_AUDIO_REQUEST, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO});
    }

//    public void requestPermissionsForSendSms() {
//        checkAndRequestPermissions(PERMISSIONS_FOR_SEND_SMS_REQUEST, new String[]{Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS});
//    }

    public void requestPermissionsForAll() {
        checkAndRequestPermissions(PERMISSIONS_FOR_ALL, all_permissions);
    }

    private void checkAndRequestPermissions(int requestCode, String... permissions) {
        if (collectDeniedPermissions(permissions).length > 0) {
            requestPermissions(requestCode, collectDeniedPermissions(permissions));
        }
    }

    private String[] collectDeniedPermissions(String... permissions) {
        ArrayList<String> deniedPermissionsList = new ArrayList<>();
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                deniedPermissionsList.add(permission);
            }
        }

        return deniedPermissionsList.toArray(new String[deniedPermissionsList.size()]);
    }

    private void requestPermissions(int requestCode, String... permissions) {
        if (fragment != null) {
            fragment.requestPermissions(permissions, requestCode);
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }
}