package com.royal.chat.utils.audiopick;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.royal.chat.R;
import com.royal.chat.utils.AnimationUtils;
import com.royal.chat.utils.AudioUtils;
import com.royal.chat.utils.RecordUtils;

public class AudioRecordDialog extends Dialog {

    private Activity parentActivity;
    private ImageButton startRecordButton;
    private ImageButton stopRecordButton;
    private ImageButton resultButton;
    private RecordUtils recordUtil;
    private TextView statusText;
    private int elapsedTime;
    private OnRecordResult onRecordResult;
    private ProgressBar progressBarReady, progressBarDoing, progressBarDone;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case RecordUtils.MESSAGE_STATUS_RECORD_FINISH:
                    alertRecordStopped();
                    break;
                case RecordUtils.MESSAGE_STATUS_ONE_SECOND_ELAPSED:
                    elapsedTime = msg.arg1;
                    showElapsedTime();
                    break;
            }
        }
    };

    public AudioRecordDialog(Activity parentActivity) {
        super(parentActivity);

        this.parentActivity = parentActivity;
        recordUtil = new RecordUtils(handler, AudioUtils.getRecordFilePath());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_recorder);

        startRecordButton = findViewById(R.id.startRecordButton);
        stopRecordButton = findViewById(R.id.stopRecordButton);
        resultButton = findViewById(R.id.resultButton);
        statusText = findViewById(R.id.textStatus);
        progressBarReady = findViewById(R.id.progressBarReady);
        progressBarDoing = findViewById(R.id.progressBarDoing);
        progressBarDone = findViewById(R.id.progressBarDone);
        AnimationUtils.getInstance(parentActivity).animate(startRecordButton);

        startRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordUtil.startRecordAudio();

                startRecordButton.setVisibility(View.GONE);
                stopRecordButton.setVisibility(View.VISIBLE);
                statusText.setVisibility(View.VISIBLE);
                resultButton.setVisibility(View.GONE);
                progressBarReady.setVisibility(View.GONE);
                progressBarDoing.setVisibility(View.VISIBLE);
                progressBarDone.setVisibility(View.GONE);
                AnimationUtils.getInstance(parentActivity).animate(stopRecordButton);
            }
        });

        stopRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordUtil.stopRecordAudio();

                startRecordButton.setVisibility(View.GONE);
                stopRecordButton.setVisibility(View.GONE);
                statusText.setVisibility(View.VISIBLE);
                resultButton.setVisibility(View.VISIBLE);
                progressBarReady.setVisibility(View.GONE);
                progressBarDoing.setVisibility(View.GONE);
                progressBarDone.setVisibility(View.VISIBLE);
                AnimationUtils.getInstance(parentActivity).animate(resultButton);

                String result = String.format(parentActivity.getString(R.string.record_result_format), elapsedTime);
                statusText.setText(result);
            }
        });

        resultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRecordResult != null) {
                    onRecordResult.finish();
                }
                dismiss();
            }
        });
    }

    private void alertRecordStopped() {
        startRecordButton.setVisibility(View.GONE);
        stopRecordButton.setVisibility(View.GONE);
        statusText.setVisibility(View.VISIBLE);
        resultButton.setVisibility(View.VISIBLE);
        progressBarReady.setVisibility(View.GONE);
        progressBarDoing.setVisibility(View.GONE);
        progressBarDone.setVisibility(View.VISIBLE);
        AnimationUtils.getInstance(parentActivity).animate(resultButton);

        String result = String.format(parentActivity.getString(R.string.record_result_format), elapsedTime);
        statusText.setText(result);
    }

    private void showElapsedTime() {
        String string = String.format("%02ds", elapsedTime);
        statusText.setText(string);
    }

    public void setRecordDialogResult(OnRecordResult onRecordResult) {
        this.onRecordResult = onRecordResult;
    }

    public interface OnRecordResult {
        void finish();
    }
}
