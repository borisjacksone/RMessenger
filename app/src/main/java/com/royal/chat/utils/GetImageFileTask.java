package com.royal.chat.utils;

import android.os.AsyncTask;

import java.io.File;
import java.io.InputStream;

public class GetImageFileTask extends AsyncTask<Object, Void, File> {

    private GetImageFileListener listener;
    private int method;

    public static final int SHOW_IMAGE = 1;
    public static final int UPLOAD_IMAGE = 2;
    public static final int UPDATE_IMAGE = 3;

    public GetImageFileTask(GetImageFileListener listener, int method) {
        this.listener = listener;
        this.method = method;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected File doInBackground(Object... objects) {
        InputStream inputStream = (InputStream) objects[0];
        String fileName = (String) objects[1];
        return ImageUtils.getImageFileContent(inputStream, fileName);
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);

        switch (method) {
            case SHOW_IMAGE:
                listener.onImageFileShowReady(file);
                break;
            case UPLOAD_IMAGE:
                listener.onImageFileUploadReady(file);
                break;
            case UPDATE_IMAGE:
                listener.onImageFileUpdateReady(file);
                break;
            default:
                break;
        }
    }
}
