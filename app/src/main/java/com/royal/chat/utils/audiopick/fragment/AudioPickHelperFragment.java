package com.royal.chat.utils.audiopick.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.royal.chat.utils.AudioUtils;
import com.royal.chat.utils.audiopick.OnAudioPickedListener;
import com.royal.chat.utils.audiopick.GetFilepathFromUriTask;

public class AudioPickHelperFragment extends Fragment {

    private static final String ARG_REQUEST_CODE = "requestCode";
    private static final String ARG_PARENT_FRAGMENT = "parentFragment";

    private static final String TAG = AudioPickHelperFragment.class.getSimpleName();

    private OnAudioPickedListener listener;

    public static AudioPickHelperFragment start(Fragment fragment, int requestCode) {
        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_CODE, requestCode);
        args.putString(ARG_PARENT_FRAGMENT, fragment.getClass().getSimpleName());

        return start(fragment.getActivity().getSupportFragmentManager(), args);
    }

    public static AudioPickHelperFragment start(FragmentActivity activity, int requestCode) {
        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_CODE, requestCode);

        return start(activity.getSupportFragmentManager(), args);
    }

    private static AudioPickHelperFragment start(FragmentManager fm, Bundle args) {
        AudioPickHelperFragment fragment = (AudioPickHelperFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new AudioPickHelperFragment();
            fm.beginTransaction().add(fragment, TAG).commitAllowingStateLoss();
            fragment.setArguments(args);
        }
        return fragment;
    }

    public static void stop(FragmentManager fm) {
        Fragment fragment = fm.findFragmentByTag(TAG);
        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    public AudioPickHelperFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment fragment = ((AppCompatActivity) context).getSupportFragmentManager()
                .findFragmentByTag(getArguments().getString(ARG_PARENT_FRAGMENT));
        if (fragment != null) {
            if (fragment instanceof OnAudioPickedListener) {
                listener = (OnAudioPickedListener) fragment;
            }
        } else {
            if (context instanceof OnAudioPickedListener) {
                listener = (OnAudioPickedListener) context;
            }
        }

        if (listener == null) {
            throw new IllegalStateException("Either activity or fragment should implement OnAudioPickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (isResultFromAudioPick(requestCode, resultCode, data)) {
            if (requestCode == AudioUtils.RECORD_REQUEST_CODE && (data == null || data.getData() == null)) {
                data = new Intent();
                data.setData(Uri.fromFile(AudioUtils.getRecordFile()));
            }
            new GetFilepathFromUriTask(getChildFragmentManager(), listener, getArguments().getInt(ARG_REQUEST_CODE)).execute(data);
        } else {
            stop(getChildFragmentManager());
            if (listener != null) {
                listener.onAudioPickClosed(getArguments().getInt(ARG_REQUEST_CODE));
            }
        }
    }

    public void setListener(OnAudioPickedListener listener) {
        this.listener = listener;
    }

    private boolean isResultFromAudioPick(int requestCode, int resultCode, Intent data) {
        return resultCode == Activity.RESULT_OK
                && ((requestCode == AudioUtils.RECORD_REQUEST_CODE)
                || (requestCode == AudioUtils.GALLERY_REQUEST_CODE && data != null));
    }
}
