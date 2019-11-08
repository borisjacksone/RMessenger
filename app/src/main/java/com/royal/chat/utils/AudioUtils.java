package com.royal.chat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.royal.chat.App;
import com.royal.chat.R;
import com.royal.chat.utils.audiopick.AudioRecordDialog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AudioUtils {
    private static final String AUDIO_MIME = "audio/*";

    public static final int GALLERY_REQUEST_CODE = 183;
    public static final int RECORD_REQUEST_CODE = 212;

    private static final int BUFFER_SIZE_2_MB = 2048;

    private static final String AUDIO_FILE_NAME_PREFIX = "AUDIO_";

    private static String AUDIO_FILE_PATH;

    private AudioUtils() {
    }

    public static String saveUriToFile(Uri uri) throws Exception {
        ParcelFileDescriptor parcelFileDescriptor = App.getInstance().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        InputStream inputStream = new FileInputStream(fileDescriptor);
        BufferedInputStream bis = new BufferedInputStream(inputStream);

        File parentDir = StorageUtils.getAppExternalDataDirectoryFile();
        String fileName = String.valueOf(System.currentTimeMillis()) + ".mp3";
        File resultFile = new File(parentDir, fileName);

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(resultFile));

        byte[] buf = new byte[BUFFER_SIZE_2_MB];
        int length;

        try {
            while ((length = bis.read(buf)) > 0) {
                bos.write(buf, 0, length);
            }
        } catch (Exception e) {
            throw new IOException("Can\'t save Storage API audio to a file!", e);
        } finally {
            parcelFileDescriptor.close();
            bis.close();
            bos.close();
        }

        return resultFile.getAbsolutePath();
    }

    public static void startAudioPicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(AUDIO_MIME);
        activity.startActivityForResult(Intent.createChooser(intent,
                activity.getString(R.string.dialog_choose_audio_from)), GALLERY_REQUEST_CODE);
    }

    public static void startAudioPicker(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(AUDIO_MIME);
        fragment.startActivityForResult(Intent.createChooser(intent,
                fragment.getString(R.string.dialog_choose_audio_from)), GALLERY_REQUEST_CODE);
    }

    public static void startRecorderForResult(final Activity activity) {
        AudioRecordDialog audioRecordDialog = new AudioRecordDialog(activity);
        audioRecordDialog.setCancelable(false);
        audioRecordDialog.setCanceledOnTouchOutside(false);
        audioRecordDialog.setRecordDialogResult(new AudioRecordDialog.OnRecordResult() {
            @Override
            public void finish() {
//                activity.onActivityResult(RECORD_REQUEST_CODE, Activity.RESULT_OK, null);
            }
        });
        audioRecordDialog.show();

//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (intent.resolveActivity(activity.getPackageManager()) == null) {
//            return;
//        }
//
//        File audioFile = getTemporaryCameraFile();
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(audioFile));
//        activity.startActivityForResult(intent, RECORD_REQUEST_CODE);
    }

    public static void startRecorderForResult(final Fragment fragment) {
        AudioRecordDialog audioRecordDialog = new AudioRecordDialog(fragment.getActivity());
        audioRecordDialog.setCancelable(false);
        audioRecordDialog.setCanceledOnTouchOutside(false);
        audioRecordDialog.setRecordDialogResult(new AudioRecordDialog.OnRecordResult() {
            @Override
            public void finish() {
                fragment.onActivityResult(RECORD_REQUEST_CODE, Activity.RESULT_OK, null);
            }
        });
        audioRecordDialog.show();

//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (intent.resolveActivity(App.getInstance().getPackageManager()) == null) {
//            return;
//        }
//
//        File audioFile = getTemporaryCameraFile();
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, getValidUri(audioFile, fragment.getContext()));
//        fragment.startActivityForResult(intent, RECORD_REQUEST_CODE);
    }

    public static File getTemporaryCameraFile() {
        File storageDir = StorageUtils.getAppExternalDataDirectoryFile();
        File file = new File(storageDir, getTemporaryAudioFileName());
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File getLastUsedAudioFile() {
        File dataDir = StorageUtils.getAppExternalDataDirectoryFile();
        File[] files = dataDir.listFiles();
        List<File> filteredFiles = new ArrayList<>();
        for (File file : files) {
            if (file.getName().startsWith(AUDIO_FILE_NAME_PREFIX)) {
                filteredFiles.add(file);
            }
        }

        Collections.sort(filteredFiles);
        if (!filteredFiles.isEmpty()) {
            return filteredFiles.get(filteredFiles.size() - 1);
        } else {
            return null;
        }
    }

    private static Uri getValidUri(File file, Context context) {
        Uri outputUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String authority = context.getPackageName() + ".provider";
            outputUri = FileProvider.getUriForFile(context, authority, file);
        } else {
            outputUri = Uri.fromFile(file);
        }
        return outputUri;
    }

    private static String getTemporaryAudioFileName() {
        return AUDIO_FILE_NAME_PREFIX + System.currentTimeMillis() + ".mp3";
    }

    public static File getRecordFile() {
        if (AUDIO_FILE_PATH == null) {
            AUDIO_FILE_PATH = App.getInstance().getFilesDir().getAbsolutePath() + "/record_audio.mp3";
        }
        return new File(AUDIO_FILE_PATH);
    }

    public static String getRecordFilePath() {
        if (AUDIO_FILE_PATH == null) {
            AUDIO_FILE_PATH = App.getInstance().getFilesDir().getAbsolutePath() + "/record_audio.mp3";
        }
        return AUDIO_FILE_PATH;
    }

    public static boolean removeCachedAudios() {
        try {
            boolean cleaned = true;
            File folder = new File(App.getInstance().getFilesDir().getAbsolutePath());
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.contains(".mp3");
                    }
                });
                for (File file : files) {
                    if (!file.delete()) {
                        cleaned = false;
                    }
                }
                return cleaned;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
