package com.royal.chat.utils.audiopick.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.royal.chat.R;
import com.royal.chat.utils.AudioUtils;

public class AudioSourcePickDialogFragment extends DialogFragment {

    private static final int POSITION_GALLERY = 0;
    private static final int POSITION_RECORDER = 1;

    private OnAudioSourcePickedListener onAudioSourcePickedListener;

    public static void show(FragmentManager fm, OnAudioSourcePickedListener onAudioSourcePickedListener) {
        AudioSourcePickDialogFragment fragment = new AudioSourcePickDialogFragment();
        fragment.setOnAudioSourcePickedListener(onAudioSourcePickedListener);
        fragment.show(fm, AudioSourcePickDialogFragment.class.getSimpleName());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_choose_audio_from);
        builder.setItems(R.array.dlg_audio_pick, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case POSITION_GALLERY:
                        onAudioSourcePickedListener.onAudioSourcePicked(AudioSourcePickDialogFragment.AudioSource.GALLERY);
                        break;
                    case POSITION_RECORDER:
                        onAudioSourcePickedListener.onAudioSourcePicked(AudioSource.RECORDER);
                        break;
                }
            }
        });

        return builder.create();
    }

    private void setOnAudioSourcePickedListener(AudioSourcePickDialogFragment.OnAudioSourcePickedListener onAudioSourcePickedListener) {
        this.onAudioSourcePickedListener = onAudioSourcePickedListener;
    }

    public interface OnAudioSourcePickedListener {

        void onAudioSourcePicked(AudioSourcePickDialogFragment.AudioSource source);
    }

    public enum AudioSource {
        GALLERY,
        RECORDER
    }

    public static class LoggableActivityAudioSourcePickedListener implements AudioSourcePickDialogFragment.OnAudioSourcePickedListener {

        private Activity activity;
        private Fragment fragment;

        public LoggableActivityAudioSourcePickedListener(Activity activity) {
            this.activity = activity;
        }

        public LoggableActivityAudioSourcePickedListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onAudioSourcePicked(AudioSourcePickDialogFragment.AudioSource source) {
            switch (source) {
                case GALLERY:
                    if (fragment != null) {
                        AudioUtils.startAudioPicker(fragment);
                    } else {
                        AudioUtils.startAudioPicker(activity);
                    }
                    break;
                case RECORDER:
                    if (fragment != null) {
                        AudioUtils.startRecorderForResult(fragment);
                    } else {
                        AudioUtils.startRecorderForResult(activity);
                    }
                    break;
            }
        }
    }
}
