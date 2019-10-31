package com.royal.chat.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.royal.chat.R;
import com.royal.chat.utils.ResourceUtils;
import com.royal.chat.utils.UiUtils;
import com.royal.chat.utils.qb.QbDialogUtils;

import java.util.ArrayList;
import java.util.List;

public class DialogsAdapter extends BaseAdapter {
    private Context context;
    private List<QBChatDialog> selectedItems = new ArrayList<>();
    private List<QBChatDialog> dialogs;

    public DialogsAdapter(Context context, List<QBChatDialog> dialogs) {
        this.context = context;
        this.dialogs = dialogs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_dialog, parent, false);

            holder = new ViewHolder();
            holder.rootLayout = (ViewGroup) convertView.findViewById(R.id.root);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.text_dialog_name);
            holder.lastMessageTextView = (TextView) convertView.findViewById(R.id.text_dialog_last_message);
            holder.dialogImageView = (ImageView) convertView.findViewById(R.id.image_dialog_icon);
            holder.unreadCounterTextView = (TextView) convertView.findViewById(R.id.text_dialog_unread_count);
            holder.onlineMarkView = (ImageView) convertView.findViewById(R.id.viewOnlineMark);
            holder.nameAbbrView = (TextView) convertView.findViewById(R.id.nameAbbr);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBChatDialog dialog = getItem(position);
        if (dialog.getType().equals(QBDialogType.GROUP)) {
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getGreyCircleDrawable());
            holder.dialogImageView.setImageResource(R.drawable.ic_chat_group);
            holder.onlineMarkView.setVisibility(View.GONE);
            holder.nameAbbrView.setVisibility(View.GONE);
        } else {
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));
            holder.dialogImageView.setImageDrawable(null);
            holder.onlineMarkView.setVisibility(View.VISIBLE);
            holder.nameAbbrView.setVisibility(View.VISIBLE);
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
        ImageView dialogImageView;
        ImageView onlineMarkView;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView unreadCounterTextView;
        TextView nameAbbrView;
    }
}