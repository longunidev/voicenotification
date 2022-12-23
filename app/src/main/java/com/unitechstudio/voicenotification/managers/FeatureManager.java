package com.unitechstudio.voicenotification.managers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.unitechstudio.voicenotification.core.BaseManager;
import com.unitechstudio.voicenotification.core.EventDispatcher;
import com.unitechstudio.voicenotification.core.model.AppEventInfo;
import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.ResponseInfo;
import com.unitechstudio.voicenotification.core.model.SpeakoutMessage;
import com.unitechstudio.voicenotification.core.model.TTSEventInfo;
import com.unitechstudio.voicenotification.data.SharedPreferenceManager;
import com.unitechstudio.voicenotification.receivers.IncomingReceiver;
import com.unitechstudio.voicenotification.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Long Uni on 4/12/2017.
 */

public class FeatureManager extends BaseManager {

    private final static String TAG = FeatureManager.class.getSimpleName();

    private final static int SPEAK_PHONE_CALLER_FREQUENCY = 7000;

    final Handler handler = new Handler();
    private Runnable mRunnable;

    public FeatureManager(Context context, EventDispatcher eventDispatcher) {
        super(context, eventDispatcher);
    }

    @Override
    public ResponseInfo onEventHandling(EventPack event) {

        if (event == null) {
            return null;
        }

        AppEventInfo appEventInfo = (AppEventInfo) event.getEventInfo();
        if (appEventInfo == null) {
            return null;
        }

        ResponseInfo respn = null;

        switch (appEventInfo.getCommand()) {
            case ACTION_BOOT_COMPLETED:
                break;
            case ACTION_SMS_RECEIVED:
                respn = handleEventIncomingSMS(appEventInfo);
                break;
            case ACTION_PHONE_STATE:
                respn = handleEventIncomingCall(appEventInfo);
                break;
            case ACTION_NEW_NOTIFICATION_APPEAR:
                respn = handleEventNewNotificationAppear(appEventInfo);
                break;
            case ACTION_CLIPBOARD_CHANGED:
                respn = handleEventClipboard(appEventInfo);
                break;
            case ACTION_SPEAK_AGAIN_MSG:
                respn = handleSpeakAgainMessage(appEventInfo);
                break;
            case ACTION_TEST_COMMAND:

                String speak = (String) appEventInfo.getData();

                postEvent(new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, new SpeakoutMessage(SpeakoutMessage.Priority.MEDIUM, speak).setLocale(CommonUtils.getUserLocale(getContext())))));

                break;
        }

        return respn;
    }

    private ResponseInfo handleEventIncomingSMS(AppEventInfo appEventInfo) {

        if (!FeatureConfig.isSMSSpeakingEnabled(getContext())) {
            Log.i(TAG, "Speaking incoming SMS is not enabled");
            return null;
        }

        ResponseInfo resp = null;

        Intent smsIntent = (Intent) appEventInfo.getData();
        if (smsIntent != null) {

            String whatToSpeakOut = null;
            // Get the SMS message received
            final Bundle bundle = smsIntent.getExtras();
            try {
                if (bundle != null) {
                    // A PDU is a "protocol data unit". This is the industrial standard for SMS message
                    final Object[] pdusObj = (Object[]) bundle.get("pdus");
                    for (int i = 0; i < pdusObj.length; i++) {
                        // This will create an SmsMessage object from the received pdu
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                        // Get sender phone number
                        String phoneNumber = sms.getDisplayOriginatingAddress();
                        String message = sms.getDisplayMessageBody();

                        if (!TextUtils.isEmpty(message)) {
                            // Retrieve contact name based phone number saved in contact list
                            String contactName = CommonUtils.getContactName(getContext(), phoneNumber);

                            if (!TextUtils.isEmpty(contactName)) {
                                whatToSpeakOut = contactName + "\n" + message;
                            } else {
                                whatToSpeakOut = phoneNumber + "\n" + message;
                            }

//                            EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, SpeakoutMessage.createAMessage(SpeakoutMessage.Priority.MEDIUM, whatToSpeakOut)));
                            EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, new SpeakoutMessage(SpeakoutMessage.Priority.MEDIUM, whatToSpeakOut).setLocale(CommonUtils.getUserLocale(getContext()))));
                            postEvent(event);

                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resp;
    }

    private ResponseInfo handleEventIncomingCall(AppEventInfo appEventInfo) {

        if (!FeatureConfig.isCallSpeakingEnabled(getContext())) {
            Log.i(TAG, "Speaking incoming call is not enabled");
            return null;
        }

        ResponseInfo resp = null;

        Intent incomingCallIntent = (Intent) appEventInfo.getData();
        if (incomingCallIntent != null) {
            try {

                String whatToSpeakOut = null;

                String state = incomingCallIntent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String incomingNumber = incomingCallIntent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    isRinging = true;
                    if (!TextUtils.isEmpty(incomingNumber)) {
                        // Retrieve contact name based phone number saved in contact list
                        String contactName = CommonUtils.getContactName(getContext(), incomingNumber);

                        if (!TextUtils.isEmpty(contactName)) {
                            whatToSpeakOut = contactName;
                        } else {
                            whatToSpeakOut = incomingNumber;
                        }

                        final SpeakoutMessage message = new SpeakoutMessage(SpeakoutMessage.Priority.MEDIUM, whatToSpeakOut).setLocale(CommonUtils.getUserLocale(getContext()));

                        mRunnable = new Runnable() {
                            @Override
                            public void run() {

//                                mTTs = new TextToSpeech(getContext(), FeatureManager.this, TTS_ENGINE_DEFAULT);
                                EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, message));
                                postEvent(event);

                                handler.postDelayed(mRunnable, SPEAK_PHONE_CALLER_FREQUENCY);
                            }
                        };

                        handler.post(mRunnable);
                    }

                } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    handler.removeCallbacks(mRunnable);
                    isRinging = false;
                } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    isRinging = false;
                    handler.removeCallbacks(mRunnable);
                    /**
                     * When the call is accepted or rejected, we must stop speaking the callee's name or the incoming phone number
                     * */

//                    EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, SpeakoutMessage.STOP_SPEAKING_MSG));
//                    BaseApplication.getInstance().postEvent(event);
                }

            } catch (Exception e) {

            }
        }

        return resp;
    }

    private ResponseInfo handleEventNewNotificationAppear(AppEventInfo appEventInfo) {
        return null;
    }

    private ResponseInfo handleEventClipboard(AppEventInfo appEventInfo) {
        return null;
    }

    private ResponseInfo handleSpeakAgainMessage(AppEventInfo appEventInfo) {

        Intent intent = (Intent) appEventInfo.getData();
        if (intent != null && IncomingReceiver.ACTION_SPEAK_AGAIN_MSG.equals(intent.getAction())) {
            SpeakoutMessage message = (SpeakoutMessage) intent.getSerializableExtra("message");
            if (message != null) {
//                EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, SpeakoutMessage.createAMessage(SpeakoutMessage.Priority.MEDIUM, message.getWhatToSpeakout())));
                EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, new ArrayList<>(Arrays.asList(message.setDontShowNotification(true)))));
                postEvent(event);
            }
        }

        return null;
    }

    private static boolean isRinging = false;

    public static boolean isPhoneRinging() {
        return isRinging;
    }

    public static class FeatureConfig {

        public static void enableSMSSpeaking(Context context, boolean isEnabled) {
            if (context != null) {
                SharedPreferenceManager.getInstance(context).putBoolean(SharedPreferenceManager.SPEAK_WHEN_SMS_INCOMING, isEnabled);
            }
        }

        public static boolean isSMSSpeakingEnabled(Context context) {
            if (context != null) {
                return SharedPreferenceManager.getInstance(context).getBoolean(SharedPreferenceManager.SPEAK_WHEN_SMS_INCOMING);
            }
            return false;
        }

        public static void enableCallSpeaking(Context context, boolean isEnabled) {
            if (context != null) {
                SharedPreferenceManager.getInstance(context).putBoolean(SharedPreferenceManager.SPEAK_WHEN_CALL_INCOMING, isEnabled);
            }

        }

        public static boolean isCallSpeakingEnabled(Context context) {
            if (context != null) {
                return SharedPreferenceManager.getInstance(context).getBoolean(SharedPreferenceManager.SPEAK_WHEN_CALL_INCOMING);
            }
            return false;
        }

        public static void enableAppNotificationsSpeaking(Context context, boolean isEnabled) {
            if (context != null) {
                SharedPreferenceManager.getInstance(context).putBoolean(SharedPreferenceManager.SPEAK_WHEN_APP_NOTIFICATIONS_APPEAR, isEnabled);
            }
        }

        public static boolean isAppNotificationSpeakingEnabled(Context context) {
            if (context != null) {
                return SharedPreferenceManager.getInstance(context).getBoolean(SharedPreferenceManager.SPEAK_WHEN_APP_NOTIFICATIONS_APPEAR);
            }
            return false;
        }

        public static void enableClipboardSpeaking(Context context, boolean isEnabled) {
            if (context != null) {
                SharedPreferenceManager.getInstance(context).putBoolean(SharedPreferenceManager.SPEAK_CLIPBOARD, isEnabled);
            }

        }

        public static boolean isClipboardSpeakingEnabled(Context context) {
            if (context != null) {
                return SharedPreferenceManager.getInstance(context).getBoolean(SharedPreferenceManager.SPEAK_CLIPBOARD);
            }
            return false;
        }

        public static void enableShakeToTurnOffSpeakingEnabled(Context context, boolean isEnabled) {
            if (context != null) {
                SharedPreferenceManager.getInstance(context).putBoolean(SharedPreferenceManager.SHAKE_TO_TURN_OFF_SPEAKING, isEnabled);
            }

        }

        public static boolean isShakeToTurnOffSpeakingEnabled(Context context) {
            if (context != null) {
                return SharedPreferenceManager.getInstance(context).getBoolean(SharedPreferenceManager.SHAKE_TO_TURN_OFF_SPEAKING);
            }
            return false;
        }

        public static void enableSpeakOnlyWhenEarphonesConnected(Context context, boolean isEnabled) {
            if (context != null) {
                SharedPreferenceManager.getInstance(context).putBoolean(SharedPreferenceManager.ONLY_SPEAK_WHEN_EARPHONES_CONNECTED, isEnabled);
            }

        }

        public static boolean isSpeakOnlyWhenEarphonesConnected(Context context) {
            if (context != null) {
                return SharedPreferenceManager.getInstance(context).getBoolean(SharedPreferenceManager.ONLY_SPEAK_WHEN_EARPHONES_CONNECTED);
            }
            return false;
        }

        public static void storeListAppsAllowedSpeakingNotifications(Context context, ArrayList<String> listPackageNames) {

            Set<String> set = new HashSet<String>();
            set.addAll(listPackageNames);
            SharedPreferenceManager.getInstance(context).putStringSet(SharedPreferenceManager.LIST_APP_ALLOWED_TO_SPEAK_NOTIFICATIONS, set);
        }

        public static ArrayList<String> getListAppsAllowedSpeakingNotifications(Context context) {
            Set<String> set = SharedPreferenceManager.getInstance(context).getStringSet(SharedPreferenceManager.LIST_APP_ALLOWED_TO_SPEAK_NOTIFICATIONS);
            if (set != null && !set.isEmpty()) {
                ArrayList<String> listApp = new ArrayList<String>(set);
                return listApp;
            }
            return null;
        }
    }

}
