package com.royal.chat.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DialogsAdapter extends BaseAdapter {
    private Context context;
    private List<QBChatDialog> selectedItems = new ArrayList<>();
    private List<QBChatDialog> dialogs;
    private GetImageFileListener listener;

    public DialogsAdapter(Context context, List<QBChatDialog> dialogs, GetImageFileListener listener) {
        this.context = context;
        this.dialogs = dialogs;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_dialog, parent, false);

            holder = new ViewHolder();
            holder.rootLayout = convertView.findViewById(R.id.root);
            holder.nameTextView = convertView.findViewById(R.id.text_dialog_name);
            holder.lastMessageTextView = convertView.findViewById(R.id.text_dialog_last_message);
            holder.dialogImageView = convertView.findViewById(R.id.image_dialog_icon);
            holder.unreadCounterTextView = convertView.findViewById(R.id.text_dialog_unread_count);
            holder.onlineMarkView = convertView.findViewById(R.id.viewOnlineMark);
            holder.nameAbbrView = convertView.findViewById(R.id.nameAbbr);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBChatDialog dialog = getItem(position);
        if (dialog.getType().equals(QBDialogType.GROUP)) {
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getGreyCircleDrawable());
            holder.dialogImageView.setImageResource(R.drawable.ic_chat_group);
            holder.onlineMarkView.setVisibility(View.GONE);
            holder.nameAbbrView.setVisibility(View.GONE);
        } else {
            if (QbDialogUtils.getDialogFileId(dialog) == null) {
                holder.dialogImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));
                holder.dialogImageView.setImageDrawable(null);
                holder.nameAbbrView.setVisibility(View.VISIBLE);
            } else {
                holder.dialogImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));
                holder.dialogImageView.setImageDrawable(null);
                holder.nameAbbrView.setVisibility(View.VISIBLE);

                File imageFile = ImageUtils.getExistImageFile(String.valueOf(dialog.getRecipientId()));
                if (imageFile == null) {
                    Integer fileId = QbDialogUtils.getDialogFileId(dialog);
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
                                task.execute(inputStream, String.valueOf(dialog.getRecipientId()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(QBResponseException e) {
                        }
                    });
                } else {
                    ImageUtils.showImageFile(imageFile, holder.dialogImageView);
                    holder.nameAbbrView.setVisibility(View.GONE);
                }
            }

            holder.onlineMarkView.setVisibility(View.VISIBLE);
        }

        holder.nameTextView.setText(QbDialogUtils.getDialogName(dialog));
        holder.nameAbbrView.setText(UiUtils.getFirstTwoCharacters(QbDialogUtils.getDialogName(dialog)));
        holder.lastMessageTextView.setText(prepareTextLastMessage(dialog));
        holder.onlineMarkView.setImageDrawable(UiUtils.getOnlineMarkDrawable(QbDialogUtils.isOnline(dialog)));

        int unreadMessagesCount = getUnreadMsgCount(dialog);
        if (unreadMessagesCount == 0) {
            holder.unreadCounterTextView.setVisibility(View.GONE);
        } else {
            holder.unreadCounterTextView.setVisibility(View.VISIBLE);
            holder.unreadCounterTextView.setText(String.valueOf(unreadMessagesCount > 99 ? "99+" : unreadMessagesCount));
        }

        holder.rootLayout.setBackgroundColor(isItemSelected(position) ? ResourceUtils.getColor(R.color.selected_list_item_color) :
                ResourceUtils.getColor(android.R.color.transparent));

        return convertView;
    }

    @Override
    public QBChatDialog getItem(int position) {
        return dialogs.get(position);
    }

    @Override
    public long getItemId(int id) {
        return (long) id;
    }

    @Override
    public int getCount() {
        return dialogs != null ? dialogs.size() : 0;
    }

    public List<QBChatDialog> getSelectedItems() {
        return selectedItems;
    }

    private boolean isItemSelected(Integer position) {
        return !selectedItems.isEmpty() && selectedItems.contains(getItem(position));
    }

    private int getUnreadMsgCount(QBChatDialog chatDialog) {
        Integer unreadMessageCount = chatDialog.getUnreadMessageCount();
        if (unreadMessageCount == null) {
            unreadMessageCount = 0;
        }
        return unreadMessageCount;
    }

    private boolean isLastMessageAttachment(QBChatDialog dialog) {
        String lastMessage = dialog.getLastMessage();
        Integer lastMessageSenderId = dialog.getLastMessageUserId();
        return TextUtils.isEmpty(lastMessage) && lastMessageSenderId != null;
    }

    private String prepareTextLastMessage(QBChatDialog chatDialog) {
        if (isLastMessageAttachment(chatDialog)) {
            return context.getString(R.string.chat_attachment);
        } else {
            return chatDialog.getLastMessage();
        }
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public void updateList(List<QBChatDialog> dialogs) {
        this.dialogs = dialogs;
        notifyDataSetChanged();
    }

    public void selectItem(QBChatDialog item) {
        if (selectedItems.contains(item)) {
            return;
        }
        selectedItems.add(item);
        notifyDataSetChanged();
    }

    public void toggleSelection(QBChatDialog item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        ViewGroup rootLayout;
        CircleImageView dialogImageView;
        ImageView onlineMarkView;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView unreadCounterTextView;
        TextView nameAbbrView;
    }
}