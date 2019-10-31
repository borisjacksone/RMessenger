package com.royal.chat.utils.audiopick;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.royal.chat.utils.audiopick.fragment.AudioPickHelperFragment;
import com.royal.chat.utils.audiopick.fragment.AudioSourcePickDialogFragment;

public class AudioPickHelper {
    public void pickAnAudio(Fragment fragment, int requestCode) {
        AudioPickHelperFragment audioPickHelperFragment = AudioPickHelperFragment.start(fragment, requestCode);
        showAudioSourcePickerDialog(fragment.getChildFragmentManager(), audioPickHelperFragment);
    }

    public void pickAnAudio(FragmentActivity activity, int requestCode) {
        AudioPickHelperFragment audioPickHelperFragment = AudioPickHelperFragment.start(activity, requestCode);
        showAudioSourcePickerDialog(activity.getSupportFragmentManager(), audioPickHelperFragment);
    }

    private void showAudioSourcePickerDialog(FragmentManager fm, AudioPickHelperFragment fragment) {
        AudioSourcePickDialogFragment.show(fm,
                new AudioSourcePickDialogFragment.LoggableActivityAudioSourcePickedListener(fragment));
    }
}
