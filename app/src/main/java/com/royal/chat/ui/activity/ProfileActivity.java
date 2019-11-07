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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;

import com.bumptech.glide.Glide;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringUtils;
import com.quickblox.users.model.QBUser;
import com.royal.chat.R;
import com.royal.chat.utils.ImageUtils;
import com.royal.chat.utils.ResourceUtils;
import com.royal.chat.utils.SharedPrefsHelper;
import com.royal.chat.utils.ToastUtils;
import com.royal.chat.utils.UiUtils;
import com.royal.chat.utils.chat.ChatHelper;

import java.io.File;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends BaseActivity {

    private CircleImageView imageView;
    private TextView nameAbbr;
    private EditText editFirstName;
    private EditText editLastName;
    private Button buttonOK;
    private Uri imageUri;

    private static final int REQUEST_CODE_TAKE_PICTURE = 8182;
    private static final int REQUEST_CODE_PICK_IMAGE_SINGLE = 8283;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        QBUser oldUser = ChatHelper.getCurrentUser();

        setActionBar();
        initViews(oldUser);
    }

    private void initViews(final QBUser oldUser) {
        imageView = findViewById(R.id.profileImage);
        nameAbbr = findViewById(R.id.nameAbbr);
        editFirstName = findViewById(R.id.edittext_user_first_name);
        editLastName = findViewById(R.id.edittext_user_last_name);
        buttonOK = findViewById(R.id.button_ok);

        editFirstName.addTextChangedListener(new TextWatcherListener(editFirstName));
        editLastName.addTextChangedListener(new TextWatcherListener(editLastName));

        String fullName = oldUser.getFullName();
        String[] names = fullName.split(" ");

        if (oldUser.getFileId() == null) {
            imageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(0));
            nameAbbr.setVisibility(View.VISIBLE);
            nameAbbr.setText(UiUtils.getFirstTwoCharacters(fullName));
        } else {
            nameAbbr.setVisibility(View.GONE);

            File imageFile = ImageUtils.getImageFileContent(String.valueOf(oldUser.getId()));
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
                        try {
                            File imageFile = ImageUtils.getImageFileContent(inputStream, String.valueOf(oldUser.getId()));
                            Glide.with(ProfileActivity.this)
                                    .load(imageFile)
                                    .override(100, 100)
                                    .dontTransform()
                                    .error(R.drawable.ic_error)
                                    .into(imageView);
                            inputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        hideProgressDialog();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        hideProgressDialog();
                    }
                });
            } else {
                Glide.with(ProfileActivity.this)
                        .load(imageFile)
                        .override(100, 100)
                        .dontTransform()
                        .error(R.drawable.ic_error)
                        .into(imageView);
            }
        }

        editFirstName.setText(names[0]);
        editLastName.setText(names[1]);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectImageMenu(v);
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

    private void updateUser(final QBUser user) {
        showProgressDialog(R.string.wait);
        ChatHelper.getInstance().updateUser(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                SharedPrefsHelper.getInstance().saveQbUser(user);
                hideProgressDialog();
                buttonOK.setEnabled(true);
                ToastUtils.longToast(R.string.profile_updated);
            }

            @Override
            public void onError(QBResponseException e) {
                e.printStackTrace();
                hideProgressDialog();
                buttonOK.setEnabled(true);
                ToastUtils.longToast(R.string.error);
            }
        });
    }

    private void uploadImageFile(QBUser oldUser) {
        buttonOK.setEnabled(false);
        showProgressDialog(R.string.wait);
        ToastUtils.longToast(R.string.uploading_profile_image);
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            File imageFile = ImageUtils.getImageFileContent(inputStream, String.valueOf(oldUser.getId()));
            if (imageFile == null) {
                hideProgressDialog();
                ToastUtils.longToast(R.string.error);
                imageUri = null;
                buttonOK.setEnabled(true);
                return;
            }
            QBContent.uploadFileTask(imageFile, false, String.valueOf(imageFile.hashCode()), new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {

                }
            }).performAsync( new QBEntityCallback<QBFile>() {
                @Override
                public void onSuccess(QBFile qbFile, Bundle params) {
                    hideProgressDialog();
                    ToastUtils.longToast(R.string.uploaded_profile_image);
                    QBUser updatedUser = ChatHelper.getCurrentUser();
                    updatedUser.setFileId(qbFile.getId());
                    imageUri = null;
                    updatedUser.setPassword(null);
                    updateUser(updatedUser);
                }

                @Override
                public void onError(QBResponseException error) {
                    error.printStackTrace();
                    hideProgressDialog();
                    ToastUtils.longToast(R.string.error);
                    imageUri = null;
                    buttonOK.setEnabled(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            hideProgressDialog();
            ToastUtils.longToast(R.string.error);
            imageUri = null;
            buttonOK.setEnabled(true);
        }
    }

    private void updateImageFile(QBUser oldUser) {
        buttonOK.setEnabled(false);
        showProgressDialog(R.string.wait);
        ToastUtils.longToast(R.string.updating_profile_image);
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            File imageFile = ImageUtils.getImageFileContent(inputStream, String.valueOf(oldUser.getId()));
            if (imageFile == null) {
                ToastUtils.longToast(R.string.error);
                hideProgressDialog();
                imageUri = null;
                buttonOK.setEnabled(true);
                return;
            }
            QBContent.updateFileTask(imageFile, oldUser.getFileId(), String.valueOf(imageFile.hashCode()), new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {

                }
            }).performAsync( new QBEntityCallback<QBFile>() {
                @Override
                public void onSuccess(QBFile qbFile, Bundle params) {
                    hideProgressDialog();
                    ToastUtils.longToast(R.string.updated_profile_image);
                    QBUser updatedUser = ChatHelper.getCurrentUser();
                    updatedUser.setFileId(qbFile.getId());
                    imageUri = null;
                    updatedUser.setPassword(null);
                    updateUser(updatedUser);
                }

                @Override
                public void onError(QBResponseException errors) {
                    errors.printStackTrace();
                    ToastUtils.longToast(R.string.error);
                    hideProgressDialog();
                    imageUri = null;
                    buttonOK.setEnabled(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.longToast(R.string.error);
            hideProgressDialog();
            imageUri = null;
            buttonOK.setEnabled(true);
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
                    ToastUtils.longToast(R.string.saved_picture);
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
                } else {
                    ToastUtils.longToast(R.string.no_image_selected);
                }
            } catch (Exception e) {
                ToastUtils.longToast(R.string.error);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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
