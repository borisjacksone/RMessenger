package com.royal.chat.utils;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class RecordUtils {

    private Timer recordTimer = null;
    private int elapsedTime = 0;

    private Handler handler;
    private MediaRecorder mediaRecorder;
    private boolean recording = false;
    private String filePath;

    public static final int MESSAGE_STATUS_RECORD_FINISH = 1;
    public static final int MESSAGE_STATUS_ONE_SECOND_ELAPSED = 2;
    public static final int RECORD_AUDIO_MILLIS = 31000;

    private Runnable callBack = new Runnable() {
        @Override
        public void run() {
            if (!recording) {
                return;
            }

            recording = false;
            stopRecord();
            stopTimer();

            handler.sendEmptyMessage(MESSAGE_STATUS_RECORD_FINISH);
        }
    };

    public RecordUtils(Handler handler, String filePath) {
        this.handler = handler;
        this.filePath = filePath;
    }

    private boolean initMediaRecorder(){
        try{
            if(mediaRecorder != null) {
                mediaRecorder.release();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        try{
            File file = new File(filePath);
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mediaRecorder.setOutputFile(file.getAbsolutePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.prepare();
        }catch (Exception ex){
            ex.printStackTrace();
            return  false;
        }

        return true;
    }

    private void startRecord(){
        try{
            mediaRecorder.start();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void stopRecord() {
        try{
            mediaRecorder.stop();
            mediaRecorder.release();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void startTimer() {
        recordTimer = new Timer();
        recordTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                elapsedTime ++;
                if (elapsedTime >= RECORD_AUDIO_MILLIS / 1000) {
                    return;
                }
                Message message = handler.obtainMessage(MESSAGE_STATUS_ONE_SECOND_ELAPSED, elapsedTime, -1);
                handler.sendMessage(message);
            }
        }, 1000L, 1000L);
    }

    private void stopTimer() {
        if (recordTimer == null) {
            return;
        }

        recordTimer.cancel();
        recordTimer = null;
        elapsedTime = 0;
    }

    public boolean startRecordAudio(){
        if(initMediaRecorder() && !recording) {
            recording = true;

            startRecord();
            startTimer();

            handler.postDelayed(callBack, RECORD_AUDIO_MILLIS);
            return true;
        }

        return false;
    }

    public void stopRecordAudio() {
        recording = false;
        stopRecord();
        stopTimer();
        handler.removeCallbacks(callBack);
    }
}
