package com.royal.chat.utils.audiopick;

import java.io.File;

public interface OnAudioPickedListener {

    void onAudioPicked(int requestCode, File file);

    void onAudioPickError(int requestCode, Exception e);

    void onAudioPickClosed(int requestCode);
}
