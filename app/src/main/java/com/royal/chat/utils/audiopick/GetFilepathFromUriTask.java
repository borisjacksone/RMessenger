package com.royal.chat.utils.audiopick;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.FragmentManager;

import com.royal.chat.App;
import com.royal.chat.async.BaseAsyncTask;
import com.royal.chat.ui.dialog.ProgressDialogFragment;
import com.royal.chat.utils.AudioUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class GetFilepathFromUriTask extends BaseAsyncTask<Intent, Void, File> {

    private static final String SCHEME_CONTENT = "content";
    private static final String SCHEME_CONTENT_GOOGLE = "content://com.google.android";
    private static final String SCHEME_FILE = "file";

    private WeakReference<FragmentManager> fmWeakReference;
    private OnAudioPickedListener listener;
    private int requestCode;

    public GetFilepathFromUriTask(FragmentManager fm, OnAudioPickedListener listener, int requestCode) {
        this.fmWeakReference = new WeakReference<>(fm);
        this.listener = listener;
        this.requestCode = requestCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showProgress();
    }

    @Override
    public File performInBackground(Intent... params) throws Exception {
        Intent data = params[0];

        String audioFilePath = null;
        Uri uri = data.getData();
        String uriScheme = uri.getScheme();

        boolean isFromGoogleApp = uri.toString().startsWith(SCHEME_CONTENT_GOOGLE);
        boolean isKitKatAndUpper = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (SCHEME_CONTENT.equalsIgnoreCase(uriScheme) && !isFromGoogleApp && !isKitKatAndUpper) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = App.getInstance().getContentResolver().query(uri, filePathColumn, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    audioFilePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        } else if (SCHEME_FILE.equalsIgnoreCase(uriScheme)) {
            audioFilePath = uri.getPath();
        } else {
            audioFilePath = AudioUtils.saveUriToFile(uri);
        }

        if (TextUtils.isEmpty(audioFilePath)) {
            throw new IOException("Can't find a filepath for URI " + uri.toString());
        }

        return new File(audioFilePath);
    }

    @Override
    public void onResult(File file) {
        hideProgress();
        Log.w(GetFilepathFromUriTask.class.getSimpleName(), "onResult listener = " + listener);
        if (listener != null) {
            listener.onAudioPicked(requestCode, file);
        }
    }

    @Override
    public void onException(Exception e) {
        hideProgress();
        Log.w(GetFilepathFromUriTask.class.getSimpleName(), "onException listener = " + listener);
        if (listener != null) {
            listener.onAudioPickError(requestCode, e);
        }
    }

    private void showProgress() {
        FragmentManager fm = fmWeakReference.get();
        if (fm != null) {
            ProgressDialogFragment.show(fm);
        }
    }

    private void hideProgress() {
        FragmentManager fm = fmWeakReference.get();
        if (fm != null) {
            ProgressDialogFragment.hide(fm);
        }
    }
}
