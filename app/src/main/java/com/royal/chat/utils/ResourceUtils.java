package com.royal.chat.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import com.royal.chat.App;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class ResourceUtils {

    public static String getString(@StringRes int stringId) {
        return App.getInstance().getString(stringId);
    }

    public static Drawable getDrawable(@DrawableRes int drawableId) {
        return App.getInstance().getResources().getDrawable(drawableId);
    }

    public static int getColor(@ColorRes int colorId) {
        return App.getInstance().getResources().getColor(colorId);
    }

    public static int getDimen(@DimenRes int dimenId) {
        return (int) App.getInstance().getResources().getDimension(dimenId);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, generateUUID(), null);
        return Uri.parse(path);
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}