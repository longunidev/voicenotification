package com.unitechstudio.voicenotification.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.unitechstudio.voicenotification.activities.MainPreferenceScreen;
import com.unitechstudio.voicenotification.app.BaseApplication;
import com.unitechstudio.voicenotification.core.model.AppEventInfo;
import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.SpeakoutMessage;
import com.unitechstudio.voicenotification.core.model.TTSEventInfo;
import com.unitechstudio.voicenotification.managers.FeatureManager;

import static com.unitechstudio.voicenotification.notification.AppNotificationManager.NOTIFICATION_ID;

public class IncomingReceiver extends BroadcastReceiver {

    private static final String TAG = IncomingReceiver.class.getSimpleName();

    private static final String ACTION_TEST_COMMAND = "com.voicenotification.action.TEST";
    public static final String ACTION_NOTI_SELECTED = "com.voicenotification.action.NOTI_SELECTED";
    public static final String ACTION_SPEAK_AGAIN_MSG = "com.voicenotification.action.SPEAK_AGAIN_MSG";
    public static final String ACTION_CLEAR_NOTIFICATION = "com.voicenotification.action.CLEAR_NOTIFICATION";
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";

    private FeatureManager featureManager;

    public IncomingReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        featureManager = new FeatureManager(context, null);

        Log.d(TAG, "onReceive() action=" + intent.getAction());

        String whatToSpeakOut = null;

        if (ACTION_SMS_RECEIVED.equals(intent.getAction())) {

            AppEventInfo appEventInfo = new AppEventInfo(AppEventInfo.AppEventCommand.ACTION_SMS_RECEIVED, intent);
            EventPack incomingSMSEvent = new EventPack(EventPack.EventType.APP, appEventInfo);
            BaseApplication.getInstance().postEvent(incomingSMSEvent);

        } else if (ACTION_PHONE_STATE.equals(intent.getAction())) {

            AppEventInfo appEventInfo = new AppEventInfo(AppEventInfo.AppEventCommand.ACTION_PHONE_STATE, intent);
            EventPack incomingCallEvent = new EventPack(EventPack.EventType.APP, appEventInfo);
            BaseApplication.getInstance().postEvent(incomingCallEvent);

        } else if (ACTION_TEST_COMMAND.equals(intent.getAction())) {

            Log.d("LongUni", "start: " + System.currentTimeMillis());

            whatToSpeakOut = "Khi ấy, Chúa Giêsu phán với dân chúng rằng|: \"Không ai đến được với Ta, nếu Cha, là Đấng sai Ta, không lôi kéo kẻ ấy, và Ta, Ta sẽ cho họ sống lại trong ngày sau hết. Trong sách các tiên tri có chép rằng: 'Mọi người sẽ được Thiên Chúa dạy bảo'. Ai nghe lời giáo hoá của Cha, thì đến với Ta. Không một ai đã xem thấy Cha, trừ Đấng bởi Thiên Chúa mà ra, Đấng ấy đã thấy Cha. Thật, Ta bảo thật các ngươi: Ai tin vào Ta thì có sự sống đời đời. Ta là bánh ban sự sống. Cha ông các ngươi đã ăn manna trong sa mạc và đã chết. Đây là bánh bởi trời xuống, để ai ăn bánh này thì khỏi chết. Ta là bánh hằng sống từ trời xuống. Ai ăn bánh này, sẽ sống đời đời. Và bánh Ta sẽ ban, chính là thịt Ta, để cho thế gian được sống\".  Đó là lời Chúa.";

            String data = intent.getStringExtra("data");
            if (!TextUtils.isEmpty(data)) {
                whatToSpeakOut = data;
            }

            AppEventInfo appEventInfo = new AppEventInfo(AppEventInfo.AppEventCommand.ACTION_TEST_COMMAND, whatToSpeakOut);
            EventPack incomingCallEvent = new EventPack(EventPack.EventType.APP, appEventInfo);
            BaseApplication.getInstance().postEvent(incomingCallEvent);

        } else if (ACTION_NOTI_SELECTED.equals(intent.getAction())) {
            Intent mainIntent = new Intent(context, MainPreferenceScreen.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainIntent);
        } else if (ACTION_SPEAK_AGAIN_MSG.equals(intent.getAction())) {
            AppEventInfo appEventInfo = new AppEventInfo(AppEventInfo.AppEventCommand.ACTION_SPEAK_AGAIN_MSG, intent);
            EventPack incomingCallEvent = new EventPack(EventPack.EventType.APP, appEventInfo);
            BaseApplication.getInstance().postEvent(incomingCallEvent);
        } else if (ACTION_CLEAR_NOTIFICATION.equals(intent.getAction())) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
            EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, SpeakoutMessage.STOP_SPEAKING_MSG));
            BaseApplication.getInstance().postEvent(event);
        }

    }

}
