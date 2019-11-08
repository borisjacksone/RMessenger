package com.royal.chat.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;
import com.royal.chat.R;
import com.royal.chat.managers.DialogsManager;
import com.royal.chat.ui.adapter.listeners.AttachClickListener;
import com.royal.chat.ui.adapter.listeners.MessageLinkClickListener;
import com.royal.chat.utils.GetImageFileListener;
import com.royal.chat.utils.GetImageFileTask;
import com.royal.chat.utils.ImageUtils;
import com.royal.chat.utils.LinkUtils;
import com.royal.chat.utils.MessageTextClickMovement;
import com.royal.chat.utils.TimeUtils;
import com.royal.chat.utils.UiUtils;
import com.royal.chat.utils.chat.ChatHelper;
import com.royal.chat.utils.qb.PaginationHistoryListener;
import com.royal.chat.utils.qb.QbUsersHolder;
import com.quickblox.users.model.QBUser;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = ChatAdapter.class.getSimpleName();
    private final QBChatDialog chatDialog;
    private PaginationHistoryListener paginationListener;
    private int previousGetCount = 0;

    private static final int TYPE_TEXT_RIGHT = 1;
    private static final int TYPE_TEXT_LEFT = 2;
    private static final int TYPE_ATTACH_IMAGE_RIGHT = 3;
    private static final int TYPE_ATTACH_IMAGE_LEFT = 4;
    private static final int TYPE_ATTACH_AUDIO_RIGHT = 5;
    private static final int TYPE_ATTACH_AUDIO_LEFT = 6;
    private static final int TYPE_NOTIFICATION_CENTER = 7;
    private static final String WEBVIEW_BASE_URL = "file:///android_asset/";

    //Message TextView click listener
    //
    private MessageLinkClickListener messageTextViewLinkClickListener;
    private boolean overrideOnClick;

    private AttachClickListener attachImageClickListener;

    private SparseIntArray containerLayoutRes = new SparseIntArray() {
        {
            put(TYPE_TEXT_RIGHT, R.layout.list_item_text_right);
            put(TYPE_TEXT_LEFT, R.layout.list_item_text_left);
            put(TYPE_ATTACH_IMAGE_RIGHT, R.layout.list_item_attach_image_right);
            put(TYPE_ATTACH_IMAGE_LEFT, R.layout.list_item_attach_image_left);
            put(TYPE_ATTACH_AUDIO_RIGHT, R.layout.list_item_attach_audio_right);
            put(TYPE_ATTACH_AUDIO_LEFT, R.layout.list_item_attach_audio_left);
            put(TYPE_NOTIFICATION_CENTER, R.layout.list_item_notif_center);
        }
    };

    private MessageViewHolder viewHolder;
    private List<QBChatMessage> chatMessages;
    private LayoutInflater inflater;
    private GetImageFileListener listener;
    protected Context context;

    public ChatAdapter(Context context, QBChatDialog chatDialog, List<QBChatMessage> chatMessages, GetImageFileListener listener) {
        this.chatDialog = chatDialog;
        this.context = context;
        this.chatMessages = chatMessages;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void updateStatusDelivered(String messageID, Integer userId) {
        for (int position = 0; position < chatMessages.size(); position++) {
            QBChatMessage message = chatMessages.get(position);
            if (message.getId().equals(messageID)) {
                ArrayList<Integer> deliveredIds = new ArrayList<>();
                if (message.getDeliveredIds() != null) {
                    deliveredIds.addAll(message.getDeliveredIds());
                }
                deliveredIds.add(userId);
                message.setDeliveredIds(deliveredIds);
                notifyItemChanged(position);
            }
        }
    }

    public void updateStatusRead(String messageID, Integer userId) {
        for (int position = 0; position < chatMessages.size(); position++) {
            QBChatMessage message = chatMessages.get(position);
            if (message.getId().equals(messageID)) {
                ArrayList<Integer> readIds = new ArrayList<>();
                if (message.getReadIds() != null) {
                    readIds.addAll(message.getReadIds());
                }
                readIds.add(userId);
                message.setReadIds(readIds);
                notifyItemChanged(position);
            }
        }
    }

    /**
     * Sets listener for handling pressed links on message text.
     *
     * @param textViewLinkClickListener listener to set. Must to implement {@link MessageLinkClickListener}
     * @param overrideOnClick           set 'true' if have to himself manage onLinkClick event or set 'false' for delegate
     *                                  onLinkClick event to {@link android.text.util.Linkify}
     */
    public void setMessageTextViewLinkClickListener(MessageLinkClickListener textViewLinkClickListener, boolean overrideOnClick) {
        this.messageTextViewLinkClickListener = textViewLinkClickListener;
        this.overrideOnClick = overrideOnClick;
    }

    public void setAttachImageClickListener(AttachClickListener clickListener) {
        attachImageClickListener = clickListener;
    }

    public void removeAttachImageClickListener() {
        attachImageClickListener = null;
    }

    public void addToList(List<QBChatMessage> items) {
        chatMessages.addAll(0, items);
        notifyItemRangeInserted(0, items.size());
    }

    public void addList(List<QBChatMessage> items) {
        chatMessages.clear();
        chatMessages.addAll(items);
        notifyDataSetChanged();
    }

    public void add(QBChatMessage item) {
        this.chatMessages.add(item);
        this.notifyItemInserted(chatMessages.size() - 1);
    }

    public List<QBChatMessage> getList() {
        return chatMessages;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_NOTIFICATION_CENTER:
                viewHolder = new NotificationHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_text_message,
                        R.id.msg_text_time_message);
                return viewHolder;
            case TYPE_TEXT_RIGHT:
                viewHolder = new TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_text_message,
                        R.id.msg_text_time_message, R.id.msg_link_preview, R.id.msg_text_status_message);
                return viewHolder;
            case TYPE_TEXT_LEFT:
                viewHolder = new TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_text_message,
                        R.id.msg_text_time_message, R.id.msg_link_preview);
                return viewHolder;
            case TYPE_ATTACH_IMAGE_RIGHT:
                viewHolder = new ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_image_attach, R.id.msg_progressbar_attach,
                        R.id.msg_text_time_attach, R.id.msg_signs_attach, R.id.msg_attach_status_message);
                return viewHolder;
            case TYPE_ATTACH_IMAGE_LEFT:
                viewHolder = new ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_image_attach, R.id.msg_progressbar_attach,
                        R.id.msg_text_time_attach, R.id.msg_signs_attach);
                return viewHolder;
            case TYPE_ATTACH_AUDIO_RIGHT:
                viewHolder = new AudioAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_audio_attach,
                        R.id.msg_text_time_attach, R.id.msg_signs_attach, R.id.msg_attach_status_message);
                return viewHolder;
            case TYPE_ATTACH_AUDIO_LEFT:
                viewHolder = new AudioAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_audio_attach,
                        R.id.msg_text_time_attach, R.id.msg_signs_attach);
                return viewHolder;
            default:
                Log.d(TAG, "onCreateViewHolder case default");
                // resource must be set manually by creating custom adapter
                return onCreateCustomViewHolder(parent, viewType);
        }
    }

    @Override
    public void onViewRecycled(MessageViewHolder holder) {
        if (holder.getItemViewType() == TYPE_TEXT_LEFT || holder.getItemViewType() == TYPE_TEXT_RIGHT) {
            TextMessageHolder textMessageHolder = (TextMessageHolder) holder;

            if (textMessageHolder.linkPreviewLayout.getTag() != null) {
                textMessageHolder.linkPreviewLayout.setTag(null);
            }
        }

        //abort loading avatar before setting new avatar to view
        if (containerLayoutRes.get(holder.getItemViewType()) != 0 && holder.avatar != null) {
            Glide.clear(holder.avatar);
        }

        super.onViewRecycled(holder);
    }

    private MessageViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        Log.e(TAG, "You must create ViewHolder by your own");
        return null;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        downloadMore(position);
        QBChatMessage chatMessage = getItem(position);
        if (isIncoming(chatMessage) && !isReadByCurrentUser(chatMessage)) {
            readMessage(chatMessage);
        }
        int valueType = getItemViewType(position);
        Log.d(TAG, "onBindViewHolder on position " + position);
        switch (valueType) {
            case TYPE_NOTIFICATION_CENTER:
                onBindViewNotificationHolder((NotificationHolder) holder, chatMessage, position);
                break;
            case TYPE_TEXT_RIGHT:
                onBindViewMsgRightHolder((TextMessageHolder) holder, chatMessage, position);
                break;
            case TYPE_TEXT_LEFT:
                onBindViewMsgLeftHolder((TextMessageHolder) holder, chatMessage, position);
                break;
            case TYPE_ATTACH_IMAGE_RIGHT:
                onBindViewAttachRightHolder((ImageAttachHolder) holder, chatMessage, position);
                break;
            case TYPE_ATTACH_IMAGE_LEFT:
                onBindViewAttachLeftHolder((ImageAttachHolder) holder, chatMessage, position);
                break;
            case TYPE_ATTACH_AUDIO_RIGHT:
                onBindViewAttachRightHolder((AudioAttachHolder) holder, chatMessage, position);
                break;
            case TYPE_ATTACH_AUDIO_LEFT:
                onBindViewAttachLeftHolder((AudioAttachHolder) holder, chatMessage, position);
                break;
            default:
                onBindViewCustomHolder(holder, chatMessage, position);
                break;
        }
    }

    private void onBindViewNotificationHolder(NotificationHolder holder, QBChatMessage chatMessage, int position) {
        holder.messageTextView.setText(chatMessage.getBody());
        holder.messageTimeTextView.setText(getTime(chatMessage.getDateSent()));
    }

    private void onBindViewMsgRightHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        fillTextMessageHolder(holder, chatMessage, position, false);
    }

    private void onBindViewMsgLeftHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        holder.messageTimeTextView.setVisibility(View.GONE);
        setOpponentsName(holder, chatMessage, false);
        setOpponentsAvatar(holder, chatMessage);

        TextView customMessageTimeTextView = holder.itemView.findViewById(R.id.custom_msg_text_time_message);
        customMessageTimeTextView.setText(getTime(chatMessage.getDateSent()));

        fillTextMessageHolder(holder, chatMessage, position, true);
    }

    private void onBindViewAttachRightHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position) {
        fillAttachmentHolder(holder, chatMessage, position, false);
    }

    private void onBindViewAttachLeftHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position) {
        setOpponentsName(holder, chatMessage, true);
        setOpponentsAvatar(holder, chatMessage);

        fillAttachmentHolder(holder, chatMessage, position, true);
        setItemAttachClickListener(getAttachListenerByType(position), holder, getAttachment(position), position);
    }

    private void onBindViewAttachRightHolder(AudioAttachHolder holder, QBChatMessage chatMessage, int position) {
        fillAttachmentHolder(holder, chatMessage, position, false);
    }

    private void onBindViewAttachLeftHolder(AudioAttachHolder holder, QBChatMessage chatMessage, int position) {
        setOpponentsName(holder, chatMessage, true);
        setOpponentsAvatar(holder, chatMessage);

        fillAttachmentHolder(holder, chatMessage, position, true);
//        setItemAttachClickListener(getAttachListenerByType(position), holder, getAttachment(position), position);
    }

    private void onBindViewCustomHolder(MessageViewHolder holder, QBChatMessage chatMessage, int position) {
    }

    private void fillAttachmentHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position, boolean isLeftMessage) {
        setDateSentAttach(holder, chatMessage);
        displayAttachment(holder, position, isLeftMessage);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }

        setItemAttachClickListener(getAttachListenerByType(position), holder, getAttachment(position), position);

        if (!isLeftMessage) {
            boolean read = isRead(chatMessage);
            boolean delivered = isDelivered(chatMessage);
            if (read) {
                holder.attachStatusTextView.setText(R.string.statuses_read);
            } else if (delivered) {
                holder.attachStatusTextView.setText(R.string.statuses_delivered);
            } else {
                holder.attachStatusTextView.setText(R.string.statuses_sent);
            }
        }
    }

    private void fillAttachmentHolder(AudioAttachHolder holder, QBChatMessage chatMessage, int position, boolean isLeftMessage) {
        setDateSentAttach(holder, chatMessage);
        displayAttachment(holder, position, isLeftMessage);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }

//        setItemAttachClickListener(getAttachListenerByType(position), holder, getAttachment(position), position);

        if (!isLeftMessage) {
            boolean read = isRead(chatMessage);
            boolean delivered = isDelivered(chatMessage);
            if (read) {
                holder.attachStatusTextView.setText(R.string.statuses_read);
            } else if (delivered) {
                holder.attachStatusTextView.setText(R.string.statuses_delivered);
            } else {
                holder.attachStatusTextView.setText(R.string.statuses_sent);
            }
        }
    }

    private void fillTextMessageHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position, boolean isLeftMessage) {
        holder.linkPreviewLayout.setVisibility(View.GONE);
        holder.messageTextView.setText(chatMessage.getBody());
        holder.messageTimeTextView.setText(getTime(chatMessage.getDateSent()));

        setMessageTextViewLinkClickListener(holder, position);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }

        List<String> urlsList = LinkUtils.extractUrls(chatMessage.getBody());
        if (urlsList.isEmpty()) {
            holder.messageTextView.setMaxWidth(context.getResources().getDisplayMetrics().widthPixels);
        } else {
            holder.messageTextView.setMaxWidth((int) context.getResources().getDimension(R.dimen.link_preview_width));
            holder.linkPreviewLayout.setTag(chatMessage.getId());
        }

        if (!isLeftMessage) {
            boolean read = isRead(chatMessage);
            boolean delivered = isDelivered(chatMessage);
            if (read) {
                holder.messageStatusTextView.setText(R.string.statuses_read);
            } else if (delivered) {
                holder.messageStatusTextView.setText(R.string.statuses_delivered);
            } else {
                holder.messageStatusTextView.setText(R.string.statuses_sent);
            }
        }
    }

    private void setOpponentsName(MessageViewHolder holder, QBChatMessage chatMessage, boolean isAttachment) {
        int viewId = isAttachment ? R.id.opponent_name_attach_view : R.id.opponent_name_text_view;

        TextView opponentNameTextView = holder.itemView.findViewById(viewId);
        opponentNameTextView.setTextColor(UiUtils.getRandomTextColorById(chatMessage.getSenderId()));
        opponentNameTextView.setText(getSenderName(chatMessage));
    }

    private void setOpponentsAvatar(MessageViewHolder holder, final QBChatMessage chatMessage) {
        Integer fileId = getSenderFileId(chatMessage);
        final CircleImageView avatarView = holder.avatar;
        final TextView nameAbbr = holder.nameAbbr;
        if (fileId == null) {
            avatarView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(chatMessage.getSenderId()));
            avatarView.setImageDrawable(null);
            nameAbbr.setVisibility(View.VISIBLE);
            nameAbbr.setText(UiUtils.getFirstTwoCharacters(getSenderName(chatMessage)));
        } else {
            avatarView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(chatMessage.getSenderId()));
            avatarView.setImageDrawable(null);
            nameAbbr.setVisibility(View.VISIBLE);
            nameAbbr.setText(UiUtils.getFirstTwoCharacters(getSenderName(chatMessage)));

            File imageFile = ImageUtils.getExistImageFile(String.valueOf(chatMessage.getSenderId()));
            if (imageFile == null) {
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
                            task.execute(inputStream, String.valueOf(chatMessage.getSenderId()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            } else {
                ImageUtils.showImageFile(imageFile, avatarView);
                nameAbbr.setVisibility(View.GONE);
            }
        }
    }

    private String getSenderName(QBChatMessage chatMessage) {
        QBUser sender = QbUsersHolder.getInstance().getUserById(chatMessage.getSenderId());
        String fullName = "";
        if (sender != null && !TextUtils.isEmpty(sender.getFullName())) {
            fullName = sender.getFullName();
        }
        return fullName;
    }

    private Integer getSenderFileId(QBChatMessage chatMessage) {
        QBUser sender = QbUsersHolder.getInstance().getUserById(chatMessage.getSenderId());
        Integer fileId = null;
        if (sender != null) {
            fileId = sender.getFileId();
        }
        return fileId;
    }

    private void readMessage(QBChatMessage chatMessage) {
        try {
            chatDialog.readMessage(chatMessage);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            Log.w(TAG, e);
        }
    }

    private boolean isReadByCurrentUser(QBChatMessage chatMessage) {
        Integer currentUserId = ChatHelper.getCurrentUser().getId();
        return !CollectionsUtil.isEmpty(chatMessage.getReadIds()) && chatMessage.getReadIds().contains(currentUserId);
    }

    private boolean isRead(QBChatMessage chatMessage) {
        boolean read = false;
        Integer recipientId = chatMessage.getRecipientId();
        Integer currentUserId = ChatHelper.getCurrentUser().getId();
        Collection<Integer> readIds = chatMessage.getReadIds();
        if (readIds == null) {
            return false;
        }
        if (recipientId != null && !recipientId.equals(currentUserId) && readIds.contains(recipientId)) {
            read = true;
        } else if (readIds.size() == 1 && readIds.contains(currentUserId)) {
            read = false;
        } else if (readIds.size() > 0) {
            read = true;
        }
        return read;
    }

    private boolean isDelivered(QBChatMessage chatMessage) {
        boolean delivered = false;
        Integer recipientId = chatMessage.getRecipientId();
        Integer currentUserId = ChatHelper.getCurrentUser().getId();
        Collection<Integer> deliveredIds = chatMessage.getDeliveredIds();
        if (deliveredIds == null) {
            return false;
        }
        if (recipientId != null && !recipientId.equals(currentUserId) && deliveredIds.contains(recipientId)) {
            delivered = true;
        } else if (deliveredIds.size() == 1 && deliveredIds.contains(currentUserId)) {
            delivered = false;
        } else if (deliveredIds.size() > 0) {
            delivered = true;
        }
        return delivered;
    }

    public void setPaginationHistoryListener(PaginationHistoryListener paginationListener) {
        this.paginationListener = paginationListener;
    }

    private void downloadMore(int position) {
        if (position == 0) {
            if (getItemCount() != previousGetCount) {
                paginationListener.downloadMore();
                previousGetCount = getItemCount();
            }
        }
    }

    @Override
    public long getHeaderId(int position) {
        QBChatMessage chatMessage = getItem(position);
        return TimeUtils.getDateAsHeaderId(chatMessage.getDateSent() * 1000);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.view_chat_message_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        View view = holder.itemView;
        TextView dateTextView = view.findViewById(R.id.header_date_textview);

        QBChatMessage chatMessage = getItem(position);
        dateTextView.setText(getDate(chatMessage.getDateSent()));

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) dateTextView.getLayoutParams();
        lp.topMargin = 0;
        dateTextView.setLayoutParams(lp);
    }

    private void setMessageTextViewLinkClickListener(TextMessageHolder holder, int position) {
        if (messageTextViewLinkClickListener != null) {
            MessageTextClickMovement customClickMovement =
                    new MessageTextClickMovement(messageTextViewLinkClickListener, overrideOnClick, context);
            customClickMovement.setPositionInAdapter(position);

            holder.messageTextView.setMovementMethod(customClickMovement);
        }
    }

    private AttachClickListener getAttachListenerByType(int position) {
        QBAttachment attachment = getAttachment(position);
        if (QBAttachment.PHOTO_TYPE.equalsIgnoreCase(attachment.getType()) ||
            QBAttachment.IMAGE_TYPE.equalsIgnoreCase(attachment.getType())) {
            return attachImageClickListener;
        }
        return null;
    }

    private void setDateSentAttach(ImageAttachHolder holder, QBChatMessage chatMessage) {
        holder.attachTimeTextView.setText(getTime(chatMessage.getDateSent()));
    }

    private void setDateSentAttach(AudioAttachHolder holder, QBChatMessage chatMessage) {
        holder.attachTimeTextView.setText(getTime(chatMessage.getDateSent()));
    }

    @Nullable
    private String obtainAvatarUrl(int valueType, QBChatMessage chatMessage) {
        return null;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    private QBChatMessage getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        QBChatMessage chatMessage = getItem(position);

        if (chatMessage.getProperty(DialogsManager.PROPERTY_NOTIFICATION_TYPE) != null) {
            return TYPE_NOTIFICATION_CENTER;
        }

        if (hasAttachments(chatMessage)) {
            QBAttachment attachment = getAttachment(position);

            if (attachment.getType().equalsIgnoreCase(QBAttachment.AUDIO_TYPE)) {
                return isIncoming(chatMessage) ? TYPE_ATTACH_AUDIO_LEFT : TYPE_ATTACH_AUDIO_RIGHT;
            } else {
                return isIncoming(chatMessage) ? TYPE_ATTACH_IMAGE_LEFT : TYPE_ATTACH_IMAGE_RIGHT;
            }
        }

        return isIncoming(chatMessage) ? TYPE_TEXT_LEFT : TYPE_TEXT_RIGHT;
    }

    private boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = ChatHelper.getCurrentUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    private boolean hasAttachments(QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }

    /**
     * @return string in "Hours:Minutes" format, i.e. <b>10:15</b>
     */
    private String getTime(long seconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(new Date(seconds * 1000));
    }

    /**
     * @return string in "Month Day" format, i.e. <b>APRIL 25</b>
     */
    public static String getDate(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return dateFormat.format(new Date(milliseconds * 1000));
    }

    private void displayAttachment(MessageViewHolder holder, int position, boolean isLeft) {
        QBAttachment attachment = getAttachment(position);

        if (attachment.getType().equalsIgnoreCase(QBAttachment.AUDIO_TYPE)) {
            showAudioAttach(holder, position, isLeft);
        } else {
            showImageAttach(holder, position);
        }
    }

    private void showImageAttach(MessageViewHolder holder, int position) {
        String imageUrl = getFileUrl(position);
        showImageByURL(holder, imageUrl, position);
    }

    private void showAudioAttach(MessageViewHolder holder, int position, boolean isLeft) {
        String audioUrl = getFileUrl(position);
        showAudioByURL(holder, audioUrl, position, isLeft);
    }

    private String getFileUrl(int position) {
        QBAttachment attachment = getAttachment(position);
        return QBFile.getPrivateUrlForUID(attachment.getId());
    }

    private QBAttachment getAttachment(int position) {
        QBChatMessage chatMessage = getItem(position);
        return chatMessage.getAttachments().iterator().next();
    }

    private void showImageByURL(MessageViewHolder holder, String url, int position) {
        int preferredImageWidth = (int) context.getResources().getDimension(R.dimen.attach_image_width_preview);
        int preferredImageHeight = (int) context.getResources().getDimension(R.dimen.attach_image_height_preview);

        Glide.with(context)
                .load(url)
                .listener(this.<String, GlideDrawable>getRequestListener(holder, position))
                .override(preferredImageWidth, preferredImageHeight)
                .dontTransform()
                .error(R.drawable.ic_error)
                .into(((ImageAttachHolder) holder).attachImageView);
    }

    private void showAudioByURL(MessageViewHolder holder, String url, int position, boolean isLeft) {
        WebView audioPlayer = ((AudioAttachHolder) holder).attachAudioView;
        audioPlayer.getSettings().setJavaScriptEnabled(true);
        String backgroundColor = isLeft ? "#e6eef4" : "#17d655";
        String textColor = isLeft ? "#6e6e6e" : "#ffffff";
        String durationLabel = context.getString(R.string.text_duration);
        String buttonImageName = isLeft ? "other" : "self";
        String loadingStatus = context.getString(R.string.dlg_loading);

        String audioSource =
                "<html>" +
                    "<head>" +
                        "<link rel=\"stylesheet\" href=\"css/audio.css\">" +
                        "<script src=\"js/audio.js\"></script>" +
                    "</head>" +
                    "<body style=\"background-color:" + backgroundColor + ";\">\n" +
                        "<div id=\"wrapper\">\n" +
                            "<div id='player'>\n" +
                                "<div id=\"div_duration\" style=\"color:" + textColor + ";\"><span id=\"loading_status\">" + loadingStatus + "</span><span id=\"duration_label\">" + durationLabel + "</span><span id=\"duration\">00s</span></div>" +
                                "<audio id=\"music_player\">\n" +
                                    "<source src=\"" + url + "\"/>\n" +
                                "</audio>\n" +
                                "<input type=\"image\" src=\"image/play_" + buttonImageName + ".png\" onclick=\"play_aud()\" id=\"play_button\">\n" +
                                "<input type=\"image\" src=\"image/pause_" + buttonImageName + ".png\" onclick=\"pause_aud()\" id=\"pause_button\">\n" +
                            "</div>\n" +
                        "</div>\n" +
                    "</body>" +
                "</html>";
        audioPlayer.loadDataWithBaseURL(WEBVIEW_BASE_URL, audioSource, "text/html", "utf-8", null);
    }

    private RequestListener getRequestListener(MessageViewHolder holder, int position) {
        return new ImageLoadListener<>((ImageAttachHolder) holder);
    }

    private void displayAvatarImage(String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.placeholder_user)
                .dontAnimate()
                .into(imageView);
    }

    private void setItemAttachClickListener(AttachClickListener listener, MessageViewHolder holder, QBAttachment qbAttachment, int position) {
        if (listener != null) {
            holder.bubbleFrame.setOnClickListener(new ItemClickListenerFilter(listener, qbAttachment, position));
        }
    }

    private static class NotificationHolder extends MessageViewHolder {
        private TextView messageTextView;
        private TextView messageTimeTextView;

        private NotificationHolder(View itemView, @IdRes int msgId, @IdRes int timeId) {
            super(itemView);
            messageTextView = itemView.findViewById(msgId);
            messageTimeTextView = itemView.findViewById(timeId);
        }
    }

    private static class TextMessageHolder extends MessageViewHolder {
        private View linkPreviewLayout;
        private TextView messageTextView;
        private TextView messageTimeTextView;
        private TextView messageStatusTextView;

        private TextMessageHolder(View itemView, @IdRes int msgId, @IdRes int timeId, @IdRes int linkPreviewLayoutId, @IdRes int statusId) {
            super(itemView);
            messageTextView = itemView.findViewById(msgId);
            messageTimeTextView = itemView.findViewById(timeId);
            linkPreviewLayout = itemView.findViewById(linkPreviewLayoutId);
            messageStatusTextView = itemView.findViewById(statusId);
        }

        private TextMessageHolder(View itemView, @IdRes int msgId, @IdRes int timeId, @IdRes int linkPreviewLayoutId) {
            super(itemView);
            messageTextView = itemView.findViewById(msgId);
            messageTimeTextView = itemView.findViewById(timeId);
            linkPreviewLayout = itemView.findViewById(linkPreviewLayoutId);
        }
    }

    private static class ImageAttachHolder extends MessageViewHolder {
        private ImageView attachImageView;
        private ProgressBar attachProgressBar;
        private TextView attachTimeTextView;
        private TextView attachStatusTextView;

        private ImageAttachHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId, @IdRes int timeId, @IdRes int signId) {
            super(itemView);
            attachImageView = itemView.findViewById(attachId);
            attachProgressBar = itemView.findViewById(progressBarId);
            attachTimeTextView = itemView.findViewById(timeId);
        }

        private ImageAttachHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId, @IdRes int timeId, @IdRes int signId, @IdRes int statusId) {
            super(itemView);
            attachImageView = itemView.findViewById(attachId);
            attachProgressBar = itemView.findViewById(progressBarId);
            attachTimeTextView = itemView.findViewById(timeId);
            attachStatusTextView = itemView.findViewById(statusId);
        }
    }

    private static class AudioAttachHolder extends MessageViewHolder {
        private WebView attachAudioView;
        private TextView attachTimeTextView;
        private TextView attachStatusTextView;

        private AudioAttachHolder(View itemView, @IdRes int msg_audio_attach, @IdRes int msg_text_time_attach, @IdRes int msg_signs_attach) {
            super(itemView);
            attachAudioView = itemView.findViewById(msg_audio_attach);
            attachTimeTextView = itemView.findViewById(msg_text_time_attach);
        }

        private AudioAttachHolder(View itemView, @IdRes int msg_audio_attach, @IdRes int msg_text_time_attach, @IdRes int msg_signs_attach, @IdRes int msg_attach_status_message) {
            super(itemView);
            attachAudioView = itemView.findViewById(msg_audio_attach);
            attachTimeTextView = itemView.findViewById(msg_text_time_attach);
            attachStatusTextView = itemView.findViewById(msg_attach_status_message);
        }
    }

    public abstract static class MessageViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView avatar;
        public TextView nameAbbr;
        public View bubbleFrame;

        private MessageViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.msg_image_avatar);
            nameAbbr = itemView.findViewById(R.id.msg_name_abbr);
            bubbleFrame = itemView.findViewById(R.id.msg_bubble_background);
        }
    }

    protected static class ImageLoadListener<M, P> implements RequestListener<M, P> {
        private ImageAttachHolder holder;

        private ImageLoadListener(ImageAttachHolder holder) {
            this.holder = holder;
            holder.attachProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean onException(Exception e, M model, Target<P> target, boolean isFirstResource) {
            Log.e(TAG, "ImageLoadListener Exception= " + e);
            holder.attachImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.attachProgressBar.setVisibility(View.GONE);
            return false;
        }

        @Override
        public boolean onResourceReady(P resource, M model, Target<P> target, boolean isFromMemoryCache, boolean isFirstResource) {
            holder.attachImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.attachProgressBar.setVisibility(View.GONE);
            return false;
        }
    }

    private class ItemClickListenerFilter implements View.OnClickListener {
        protected int position;
        private QBAttachment attachment;
        private AttachClickListener chatAttachClickListener;

        ItemClickListenerFilter(AttachClickListener attachClickListener, QBAttachment attachment, int position) {
            this.position = position;
            this.attachment = attachment;
            this.chatAttachClickListener = attachClickListener;
        }

        @Override
        public void onClick(View view) {
            chatAttachClickListener.onLinkClicked(attachment, position);
        }
    }
}