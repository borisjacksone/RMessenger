package com.royal.chat.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringUtils;
import com.royal.chat.App;
import com.royal.chat.R;
import com.royal.chat.utils.SharedPrefsHelper;
import com.royal.chat.utils.chat.ChatHelper;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class LoginActivity extends BaseActivity {
    private static final int UNAUTHORIZED = 401;

    private String loginID;
    private EditText usernameEditText;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.user_name);
        usernameEditText.addTextChangedListener(new TextWatcherListener(usernameEditText));

        loginID = Settings.Secure.getString(LoginActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);

        TextView textLogin = findViewById(R.id.login_method_view);
        Button buttonLogin = findViewById(R.id.button_login);

        final String lastUsedName = SharedPrefsHelper.getInstance().getSavedUserName();
        if (lastUsedName == null || StringUtils.isEmpty(lastUsedName)) {
            textLogin.setText(R.string.login_new_name);
            usernameEditText.setVisibility(View.VISIBLE);
            usernameEditText.setText("");
            buttonLogin.setVisibility(View.GONE);
        } else {
            textLogin.setText(R.string.login_last_name);
            usernameEditText.setVisibility(View.GONE);
            usernameEditText.setText(lastUsedName);
            buttonLogin.setVisibility(View.VISIBLE);

            buttonLogin.setText(String.format(getString(R.string.login_button_text_format), lastUsedName));
            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    QBUser qbUser = new QBUser();
                    qbUser.setLogin(loginID.trim());
                    qbUser.setFullName(lastUsedName.trim());
                    qbUser.setPassword(App.USER_DEFAULT_PASSWORD);
                    signIn(qbUser);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login_user_done:
                String userName = usernameEditText.getText().toString();
                if (StringUtils.isEmpty(userName)) {
                    return false;
                }
                QBUser qbUser = new QBUser();
                qbUser.setLogin(loginID.trim());
                qbUser.setFullName(usernameEditText.getText().toString().trim());
                qbUser.setPassword(App.USER_DEFAULT_PASSWORD);
                signIn(qbUser);
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signIn(final QBUser user) {
        showProgressDialog(R.string.dlg_login);
        ChatHelper.getInstance().login(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser userFromRest, Bundle bundle) {
                if (userFromRest.getFullName().equals(user.getFullName())) {
                    loginToChat(user);
                } else {
                    //Need to set password NULL, because server will update user only with NULL password
                    user.setPassword(null);
                    updateUser(user);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                if (e.getHttpStatusCode() == UNAUTHORIZED) {
                    signUp(user);
                } else {
                    hideProgressDialog();
                    showErrorSnackbar(R.string.login_chat_login_error, e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signIn(user);
                        }
                    });
                }
            }
        });
    }

    private void updateUser(final QBUser user) {
        ChatHelper.getInstance().updateUser(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                loginToChat(user);
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                showErrorSnackbar(R.string.login_chat_login_error, e, null);
            }
        });
    }

    private void loginToChat(final QBUser user) {
        //Need to set password, because the server will not register to chat without password
        user.setPassword(App.USER_DEFAULT_PASSWORD);
        ChatHelper.getInstance().loginToChat(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                SharedPrefsHelper.getInstance().saveQbUser(user);
                DialogsActivity.start(LoginActivity.this);
                finish();
                hideProgressDialog();
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                showErrorSnackbar(R.string.login_chat_login_error, e, null);
            }
        });
    }

    private void signUp(final QBUser newUser) {
        SharedPrefsHelper.getInstance().removeQbUser();
        QBUsers.signUp(newUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                hideProgressDialog();
                signIn(newUser);
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                showErrorSnackbar(R.string.login_sign_up_error, e, null);
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