package com.royal.chat.ui.activity;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringUtils;
import com.quickblox.users.model.QBUser;
import com.royal.chat.R;
import com.royal.chat.utils.GetImageFileListener;
import com.royal.chat.utils.GetImageFileTask;
import com.royal.chat.utils.ImageUtils;
import com.royal.chat.utils.ResourceUtils;
import com.royal.chat.utils.SharedPrefsHelper;
import com.royal.chat.utils.SystemPermissionHelper;
import com.royal.chat.utils.ToastUtils;
import com.royal.chat.utils.UiUtils;
import com.royal.chat.utils.chat.ChatHelper;

import java.io.File;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends BaseActivity implements GetImageFileListener {

    private CircleImageView imageView;
    private CircleImageView editImageView;
    private TextView nameAbbr;
    private EditText editFirstName;
    private EditText editLastName;
    private Button buttonOK;
    private Uri imageUri;
    private SystemPermissionHelper systemPermissionHelper;

    private static final int REQUEST_CODE_TAKE_PICTURE = 8182;
    private static final int REQUEST_CODE_PICK_IMAGE_SINGLE = 8283;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        QBUser oldUser = ChatHelper.getCurrentUser();
        systemPermissionHelper = new SystemPermissionHelper(ProfileActivity.this);

        setActionBar();
        initViews(oldUser);
    }

    private void initViews(final QBUser oldUser) {
        imageView = findViewById(R.id.profileImage);
        editImageView = findViewById(R.id.profileEditImage);
        nameAbbr = findViewById(R.id.nameAbbr);
        editFirstName = findViewById(R.id.edittext_user_first_name);
        editLastName = findViewById(R.id.edittext_user_last_name);
        buttonOK = findViewById(R.id.button_ok);

        editImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_profile));

        editFirstName.addTextChangedListener(new TextWatcherListener(editFirstName));
        editLastName.addTextChangedListener(new TextWatcherListener(editLastName));

        String fullName = oldUser.getFullName();
        String[] names = fullName.split(" ");

        if (oldUser.getFileId() == null) {
            showDefaultAvatar(fullName);
        } else {
            showDefaultAvatar(fullName);

            File imageFile = ImageUtils.getExistImageFile(String.valueOf(oldUser.getId()));
            if (imageFile == null) {
                int fileId = oldUser.getFileId();
                Bundle params = new Bundle();

                showProgressDialog(R.string.wait);

                QBContent.downloadFileById(fileId, params, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int i) {

                    }
                }).performAsync(new QBEntityCallback<InputStream>() {
                    @Override
                    public void onSuccess(InputStream inputStream, Bundle bundle) {
                        GetImageFileTask task = new GetImageFileTask(ProfileActivity.this, GetImageFileTask.SHOW_IMAGE);
                        task.execute(inputStream, String.valueOf(oldUser.getId()));
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        hideProgressDialog();
                    }
                });
            } else {
                ImageUtils.showImageFile(imageFile, imageView);
                nameAbbr.setVisibility(View.GONE);
            }
        }

        editFirstName.setText(names[0]);
        editLastName.setText(names[1]);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (systemPermissionHelper.isSaveImagePermissionGranted()) {
                    showSelectImageMenu(v);
                } else {
                    systemPermissionHelper.requestPermissionsForSaveFileImage();
                }
            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = editFirstName.getText().toString().trim();
                String lastName = editLastName.getText().toString().trim();
                if (StringUtils.isEmpty(firstName)) {
                    editFirstName.setError(getString(R.string.text_required));
                    return;
                }

                if (StringUtils.isEmpty(lastName)) {
                    editLastName.setError(getString(R.string.text_required));
                    return;
                }

                String fullName = firstName + " " + lastName;

                if (fullName.equals(oldUser.getFullName())) {
                    if (imageUri != null) {
                        if (oldUser.getFileId() == null) {
                            uploadImageFile(oldUser);
                        } else {
                            updateImageFile(oldUser);
                        }
                    }
                } else {
                    QBUser updatedUser = ChatHelper.getCurrentUser();
                    updatedUser.setFullName(fullName);
                    updatedUser.setPassword(null);
                    updateUser(updatedUser);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SystemPermissionHelper.PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST && grantResults[0] != -1) {
            if (systemPermissionHelper.isSaveImagePermissionGranted()) {
                showSelectImageMenu(imageView);
            } else {
                systemPermissionHelper.requestPermissionsForSaveFileImage();
            }
        } else {
            systemPermissionHelper.requestPermissionsForSaveFileImage();
        }
    }

    private void showDefaultAvatar(String fullName) {
        imageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(0));
        imageView.setImageDrawable(null);
        nameAbbr.setVisibility(View.VISIBLE);
        nameAbbr.setText(UiUtils.getFirstTwoCharacters(fullName));
    }

    private void showWaitStatus(int stringID) {
        buttonOK.setEnabled(false);
        showProgressDialog(R.string.wait);
        ToastUtils.longToast(stringID);
    }

    private void showErrorStatus(Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        hideProgressDialog();
        buttonOK.setEnabled(true);
        imageUri = null;
        ToastUtils.longToast(R.string.error);
    }

    private void showSuccessStatus(int stringID) {
        hideProgressDialog();
        buttonOK.setEnabled(true);
        imageUri = null;
        ToastUtils.longToast(stringID);
    }

    private void updateUser(final QBUser user) {
        showProgressDialog(R.string.wait);
        ChatHelper.getInstance().updateUser(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                SharedPrefsHelper.getInstance().saveQbUser(user);
                showSuccessStatus(R.string.profile_updated);
            }

            @Override
            public void onError(QBResponseException e) {
                showErrorStatus(e);
            }
        });
    }

    private void uploadImageFile(QBUser oldUser) {
        showWaitStatus(R.string.uploading_profile_image);
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            GetImageFileTask task = new GetImageFileTask(ProfileActivity.this, GetImageFileTask.UPLOAD_IMAGE);
            task.execute(inputStream, String.valueOf(oldUser.getId()));
        } catch (Exception e) {
            showErrorStatus(e);
        }
    }

    private void updateImageFile(QBUser oldUser) {
        showWaitStatus(R.string.updating_profile_image);
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            GetImageFileTask task = new GetImageFileTask(ProfileActivity.this, GetImageFileTask.UPDATE_IMAGE);
            task.execute(inputStream, String.valueOf(oldUser.getId()));
        } catch (Exception e) {
            showErrorStatus(e);
        }
    }

    private void showSelectImageMenu(View view) {
        imageUri = null;
        PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.activity_profile_image, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu_select_camera:

                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PICTURE);
                        }

                        break;
                    case R.id.menu_select_gallery:

                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);

                        startActivityForResult(Intent.createChooser(intent, getString(R.string.menu_select_gallery)), REQUEST_CODE_PICK_IMAGE_SINGLE);

                        break;
                }

                return true;
            }
        });
        popupMenu.show();
    }

    private void setActionBar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.menu_dialogs_profile);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_TAKE_PICTURE && resultCode == RESULT_OK) {

            imageUri = null;

            Bundle extras = data.getExtras();
            if (extras == null) {
                ToastUtils.longToast(R.string.error);
            } else {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap == null) {
                    ToastUtils.longToast(R.string.error);
                } else {
                    imageView.setImageBitmap(imageBitmap);
                    nameAbbr.setVisibility(View.GONE);
                    imageUri = ResourceUtils.getImageUri(getApplicationContext(), imageBitmap);
                    ToastUtils.longToast(R.string.selected_picture);
                }
            }
        }

        if (requestCode == REQUEST_CODE_PICK_IMAGE_SINGLE) {
            try {
                if (resultCode == RESULT_OK && null != data) {

                    imageUri = null;

                    if (data.getData() != null) {
                        imageUri = data.getData();
                    } else {
                        if (data.getClipData() != null) {
                            ClipData mClipData = data.getClipData();
                            ClipData.Item item = mClipData.getItemAt(0);
                            imageUri = item.getUri();
                        }
                    }

                    imageView.setImageURI(imageUri);
                    nameAbbr.setVisibility(View.GONE);
                    ToastUtils.longToast(R.string.selected_picture);
                } else {
                    ToastUtils.longToast(R.string.no_image_selected);
                }
            } catch (Exception e) {
                ToastUtils.longToast(R.string.error);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onImageFileShowReady(File imageFile) {
        hideProgressDialog();
        if (imageFile == null) {
            showDefaultAvatar(ChatHelper.getCurrentUser().getFullName());
            return;
        }
        ImageUtils.showImageFile(imageFile, imageView);
        nameAbbr.setVisibility(View.GONE);
    }

    @Override
    public void onImageFileUploadReady(File imageFile) {
        if (imageFile == null) {
            showErrorStatus(null);
            return;
        }
        QBContent.uploadFileTask(imageFile, false, String.valueOf(imageFile.hashCode()), new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {

            }
        }).performAsync( new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle params) {
                showSuccessStatus(R.string.uploaded_profile_image);

                QBUser updatedUser = ChatHelper.getCurrentUser();
                updatedUser.setFileId(qbFile.getId());
                updatedUser.setPassword(null);
                updateUser(updatedUser);
            }

            @Override
            public void onError(QBResponseException error) {
                showErrorStatus(error);
            }
        });
    }

    @Override
    public void onImageFileUpdateReady(File imageFile) {
        if (imageFile == null) {
            showErrorStatus(null);
            return;
        }
        QBContent.updateFileTask(imageFile, ChatHelper.getCurrentUser().getFileId(), String.valueOf(imageFile.hashCode()), new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {

            }
        }).performAsync( new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle params) {
                showSuccessStatus(R.string.updated_profile_image);

                QBUser updatedUser = ChatHelper.getCurrentUser();
                updatedUser.setFileId(qbFile.getId());
                updatedUser.setPassword(null);
                updateUser(updatedUser);
            }

            @Override
            public void onError(QBResponseException error) {
                showErrorStatus(error);
            }
        });
    }

    private class TextWatcherListener implements TextWatcher {
        private EditText editText;

        private TextWatcherListener(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            editText.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
