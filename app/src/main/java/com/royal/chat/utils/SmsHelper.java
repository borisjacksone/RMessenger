package com.royal.chat.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;

import com.quickblox.core.helper.StringUtils;
import com.royal.chat.R;

import java.util.List;

public class SmsHelper {

//    static SmsHelper instance = null;
//    private Context context = null;

//    private BroadcastReceiver sentStatusReceiver, deliveredStatusReceiver;

//    public static SmsHelper getInstance(Context context) {
//        if (instance == null) {
//            instance = new SmsHelper(context);
//        }
//        return instance;
//    }

//    private SmsHelper(Context context) {
//        this.context = context;
//
//        sentStatusReceiver = new BroadcastReceiver() {
//
//            @Override
//            public void onReceive(Context arg0, Intent arg1) {
//                int s = R.string.unknown_error;
//                switch (getResultCode()) {
//                    case Activity.RESULT_OK:
//                        s = R.string.sms_sent_success;
//                        break;
//                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//                        s = R.string.sms_generic_failure;
//                        break;
//                    case SmsManager.RESULT_ERROR_NO_SERVICE:
//                        s = R.string.sms_service_error;
//                        break;
//                    case SmsManager.RESULT_ERROR_NULL_PDU:
//                        s = R.string.sms_null_pdu;
//                        break;
//                    case SmsManager.RESULT_ERROR_RADIO_OFF:
//                        s = R.string.sms_radio_off;
//                        break;
//                    default:
//                        break;
//                }
//
//                ToastUtils.longToast(s);
//            }
//        };
//        deliveredStatusReceiver = new BroadcastReceiver() {
//
//            @Override
//            public void onReceive(Context arg0, Intent arg1) {
//                int s = R.string.msg_not_delivered;
//                switch(getResultCode()) {
//                    case Activity.RESULT_OK:
//                        s = R.string.msg_delivery_success;
//                        break;
//                    case Activity.RESULT_CANCELED:
//                        break;
//                }
//
//                ToastUtils.longToast(s);
//            }
//        };
//    }

//    public void registerReceiver() {
//        context.registerReceiver(sentStatusReceiver, new IntentFilter("SMS_SENT"));
//        context.registerReceiver(deliveredStatusReceiver, new IntentFilter("SMS_DELIVERED"));
//    }

//    public void unregisterReceiver() {
//        context.unregisterReceiver(sentStatusReceiver);
//        context.unregisterReceiver(deliveredStatusReceiver);
//    }

//    public void sendSms(String phoneNumber) {
//        if (phoneNumber == null || StringUtils.isEmpty(phoneNumber)) {
//            ToastUtils.longToast(R.string.phone_not_selected);
//            return;
//        }
//
//        String message = context.getString(R.string.text_invite_sms);
//
//        SmsManager smsManager = SmsManager.getDefault();
//        List<String> messages = smsManager.divideMessage(message);
//        for (String msg : messages) {
//            PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_SENT"), 0);
//            PendingIntent deliveredIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_DELIVERED"), 0);
//            smsManager.sendTextMessage(phoneNumber, null, msg, sentIntent, deliveredIntent);
//        }
//    }
}
