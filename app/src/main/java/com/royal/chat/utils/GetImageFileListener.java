package com.royal.chat.utils;

import java.io.File;

public interface GetImageFileListener {

    void onImageFileShowReady(File file);

    void onImageFileUploadReady(File file);

    void onImageFileUpdateReady(File file);
}
