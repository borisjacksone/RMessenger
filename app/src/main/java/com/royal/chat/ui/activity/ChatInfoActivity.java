package com.royal.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.royal.chat.R;
import com.royal.chat.ui.adapter.UsersAdapter;
import com.royal.chat.utils.GetImageFileListener;
import com.royal.chat.utils.chat.ChatHelper;
import com.royal.chat.utils.qb.QbUsersHolder;
import com.quickblox.users.model.QBUser;

import java.io.File;
import java.util.List;

public class ChatInfoActivity extends BaseActivity implements GetImageFileListener {
    private static final String EXTRA_DIALOG = "dialog";

    private ListView usersListView;
    private UsersAdapter adapter;
    private QBChatDialog qbDialog;

    private Runnable notifyDataSetChangedThread = new Runnable() {
        @Override
        public void run() {
            if (adapter == null) {
                return;
            }
            adapter.notifyDataSetChanged();
        }
    };

    public static void start(Context context, QBChatDialog qbDialog) {
        Intent intent = new Intent(context, ChatInfoActivity.class);
        intent.putExtra(EXTRA_DIALOG, qbDialog);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_info);

        actionBar.setDisplayHomeAsUpEnabled(true);
        usersListView = findViewById(R.id.list_chat_info_users);
        qbDialog = (QBChatDialog) getIntent().getSerializableExtra(EXTRA_DIALOG);

        getDialog();
    }

    private void getDialog() {
        String dialogID = qbDialog.getDialogId();
        ChatHelper.getInstance().getDialogById(dialogID, new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                qbDialog = qbChatDialog;
                buildUserList();
            }

            @Override
            public void onError(QBResponseException e) {
                finish();
            }
        });
    }

    private void buildUserList() {
        List<Integer> userIds = qbDialog.getOccupants();
        List<QBUser> users = QbUsersHolder.getInstance().getUsersByIds(userIds);
        adapter = new UsersAdapter(this, users, this);
        usersListView.setAdapter(adapter);
    }

    @Override
    public void onImageFileShowReady(File file) {
        if (file == null) {
            return;
        }
        runOnUiThread(notifyDataSetChangedThread);
    }

    @Override
    public void onImageFileUploadReady(File file) {

    }

    @Override
    public void onImageFileUpdateReady(File file) {

    }
}