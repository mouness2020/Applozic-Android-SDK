package com.applozic.mobicomkit.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.listners.ApplozicUIListener;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

/**
 * Created by reytum on 5/12/17.
 */

public class ApplozicBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ApplozicUIReceiver";
    private Context context;
    private ApplozicUIListener applozicUIListener;

    public ApplozicBroadcastReceiver(Context context, ApplozicUIListener listener) {
        this.context = context;
        this.applozicUIListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Message message = null;
        String messageJson = intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);
        if (!TextUtils.isEmpty(messageJson)) {
            message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
        }
        Utils.printLog(context, TAG, "Received broadcast, action: " + action + ", message: " + message);

        if (message != null && !message.isSentToMany()) {
            if (!message.isTypeOutbox()) {
                applozicUIListener.onMessageReceived(message);
            } else {
                applozicUIListener.onMessageSent(message);
            }
        } else if (message != null && message.isSentToMany() && BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            for (String toField : message.getTo().split(",")) {
                Message singleMessage = new Message(message);
                singleMessage.setKeyString(message.getKeyString());
                singleMessage.setTo(toField);
                singleMessage.processContactIds(context);
                if (!message.isTypeOutbox()) {
                    applozicUIListener.onMessageReceived(message);
                }
            }
        }

        String keyString = intent.getStringExtra("keyString");
        String userId = message != null ? message.getContactIds() : "";

        if (BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString().equals(action)) {
            //InstructionUtil.showInstruction(context, intent.getIntExtra("resId", -1), intent.getBooleanExtra("actionable", false), R.color.instruction_color);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_CHANNEL_NAME.toString().equals(action)) {
            applozicUIListener.onChannelNameUpdated();
        } else if (BroadcastService.INTENT_ACTIONS.FIRST_TIME_SYNC_COMPLETE.toString().equals(action)) {
            //applozicUIListener.downloadConversations(true);
        } else if (BroadcastService.INTENT_ACTIONS.LOAD_MORE.toString().equals(action)) {
            applozicUIListener.onLoadMore(intent.getBooleanExtra("loadMore", true));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString().equals(action)) {
            //applozicUIListener.updateMessageKeyString(message);
        } else if (BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            applozicUIListener.onMessageSync(message, keyString);
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_MESSAGE.toString().equals(intent.getAction())) {
            userId = intent.getStringExtra("contactNumbers");
            applozicUIListener.onMessageDeleted(keyString, userId);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY.toString().equals(action) ||
                BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString().equals(action)) {
            applozicUIListener.onMessageDelivered(message, userId);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString().equals(action)) {
            applozicUIListener.onAllMessagesDelivered(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString().equals(action)) {
            applozicUIListener.onAllMessagesRead(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString().equals(action)) {
            String contactNumber = intent.getStringExtra("contactNumber");
            Integer channelKey = intent.getIntExtra("channelKey", 0);
            String response = intent.getStringExtra("response");
           /* Contact contact = null;
            if (contactNumber != null) {
                contact = baseContactService.getContactById(contactNumber);
            }*/
            applozicUIListener.onConversationDeleted(contactNumber, channelKey, response);
        } else if (BroadcastService.INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString().equals(action) && message != null) {
            //applozicUIListener.updateUploadFailedStatus(message);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_DONE.toString().equals(action) && message != null) {
            //applozicUIListener.updateDownloadStatus(message);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_FAILD.toString().equals(action) && message != null) {
            //applozicUIListener.updateDownloadFailed(message);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_TYPING_STATUS.toString().equals(action)) {
            String currentUserId = intent.getStringExtra("userId");
            String isTyping = intent.getStringExtra("isTyping");
            applozicUIListener.onUpdateTypingStatus(currentUserId, isTyping);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString().equals(action)) {
            applozicUIListener.onUpdateLastSeen(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.MQTT_DISCONNECTED.toString().equals(action)) {
            applozicUIListener.onMqttDisconnected();
        } else if (BroadcastService.INTENT_ACTIONS.CHANNEL_SYNC.toString().equals(action)) {
            applozicUIListener.onChannelSync();
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_TITLE_SUBTITLE.toString().equals(action)) {
            applozicUIListener.onChannelTitleUpdated();
        } else if (BroadcastService.INTENT_ACTIONS.CONVERSATION_READ.toString().equals(action)) {
            String currentId = intent.getStringExtra("currentId");
            boolean isGroup = intent.getBooleanExtra("isGroup", false);
            applozicUIListener.onConversationRead(currentId, isGroup);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_USER_DETAIL.toString().equals(action)) {
            applozicUIListener.onUserDetailUpdated(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_METADATA_UPDATE.toString().equals(action)) {
            applozicUIListener.onMessageMetadataUpdated(keyString);
        }
    }
}
