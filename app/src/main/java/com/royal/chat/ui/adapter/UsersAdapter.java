package com.royal.chat.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.content.QBContent;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.royal.chat.R;
import com.royal.chat.utils.GetImageFileListener;
import com.royal.chat.utils.GetImageFileTask;
import com.royal.chat.utils.ImageUtils;
import com.royal.chat.utils.ResourceUtils;
import com.royal.chat.utils.UiUtils;
import com.royal.chat.utils.qb.QbDialogUtils;
import com.quickblox.users.model.QBUser;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends BaseAdapter {

    protected List<QBUser> userList;
    protected QBUser currentUser;
    private Context context;
    private GetImageFileListener listener;

    public UsersAdapter(Context context, List<QBUser> users, GetImageFileListener listener) {
        this.context = context;
        this.listener = listener;
        currentUser = QBChatService.getInstance().getUser();
        userList = users;
        addCurrentUserToUserList();
    }

    private void addCurrentUserToUserList() {
        if (currentUser != null) {
            if (!userList.contains(currentUser)) {
                userList.add(currentUser);
            }
        }
    }

    public void addUserToUserList(QBUser user) {
        if (!userList.contains(user)) {
            userList.add(user);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final QBUser user = getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_user, parent, false);
            holder = new ViewHolder();
            holder.userImageView = convertView.findViewById(R.id.image_user);
            holder.loginTextView = convertView.findViewById(R.id.text_user_login);
            holder.userCheckBox = convertView.findViewById(R.id.checkbox_user);
            holder.nameAbbrView = convertView.findViewById(R.id.nameAbbr);
            holder.onlineMarkView = convertView.findViewById(R.id.viewOnlineMark);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.onlineMarkView.setImageDrawable(UiUtils.getOnlineMarkDrawable(QbDialogUtils.isOnline(user)));
        holder.nameAbbrView.setText(UiUtils.getFirstTwoCharacters(user.getFullName()));

        if (isUserMe(user)) {
            holder.loginTextView.setText(context.getString(R.string.placeholder_username_you, user.getFullName()));
            holder.onlineMarkView.setImageDrawable(UiUtils.getOnlineMarkDrawable(true));
        } else {
            holder.loginTextView.setText(user.getFullName());
        }

        if (isAvailableForSelection(user)) {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_black));
        } else {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_medium_grey));
        }

        if (user.getFileId() == null) {
            holder.userImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));
            holder.userImageView.setImageDrawable(null);
            holder.nameAbbrView.setVisibility(View.VISIBLE);
        } else {
            holder.userImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));
            holder.userImageView.setImageDrawable(null);
            holder.nameAbbrView.setVisibility(View.VISIBLE);

            File imageFile = ImageUtils.getExistImageFile(String.valueOf(user.getId()));
            if (imageFile == null) {
                Integer fileId = user.getFileId();
                Bundle params = new Bundle();
                QBContent.downloadFileById(fileId, params, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int i) {

                    }
                }).performAsync(new QBEntityCallback<InputStream>() {
                    @Override
                    public void onSuccess(InputStream inputStream, Bundle bundle) {
                        try {
                            GetImageFileTask task = new GetImageFileTask(listener, GetImageFileTask.SHOW_IMAGE);
                            task.execute(inputStream, String.valueOf(user.getId()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {
                    }
                });
            } else {
                ImageUtils.showImageFile(imageFile, holder.userImageView);
                holder.nameAbbrView.setVisibility(View.GONE);
            }
        }
        holder.userCheckBox.setVisibility(View.GONE);

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public QBUser getItem(int position) {
        return userList.get(position);
    }

    private boolean isUserMe(QBUser user) {
        return currentUser != null && currentUser.getId().equals(user.getId());
    }

    protected boolean isAvailableForSelection(QBUser user) {
        return currentUser == null || !currentUser.getId().equals(user.getId());
    }

    protected static class ViewHolder {
        CircleImageView userImageView;
        TextView loginTextView;
        CheckBox userCheckBox;
        TextView nameAbbrView;
        ImageView onlineMarkView;
    }
}