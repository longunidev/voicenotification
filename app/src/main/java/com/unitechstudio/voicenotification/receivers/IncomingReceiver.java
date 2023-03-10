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

            whatToSpeakOut = "Khi ???y, Ch??a Gi??su ph??n v???i d??n ch??ng r???ng|: \"Kh??ng ai ?????n ???????c v???i Ta, n???u Cha, l?? ?????ng sai Ta, kh??ng l??i k??o k??? ???y, v?? Ta, Ta s??? cho h??? s???ng l???i trong ng??y sau h???t. Trong s??ch c??c ti??n tri c?? ch??p r???ng: 'M???i ng?????i s??? ???????c Thi??n Ch??a d???y b???o'. Ai nghe l???i gi??o ho?? c???a Cha, th?? ?????n v???i Ta. Kh??ng m???t ai ???? xem th???y Cha, tr??? ?????ng b???i Thi??n Ch??a m?? ra, ?????ng ???y ???? th???y Cha. Th???t, Ta b???o th???t c??c ng????i: Ai tin v??o Ta th?? c?? s??? s???ng ?????i ?????i. Ta l?? b??nh ban s??? s???ng. Cha ??ng c??c ng????i ???? ??n manna trong sa m???c v?? ???? ch???t. ????y l?? b??nh b???i tr???i xu???ng, ????? ai ??n b??nh n??y th?? kh???i ch???t. Ta l?? b??nh h???ng s???ng t??? tr???i xu???ng. Ai ??n b??nh n??y, s??? s???ng ?????i ?????i. V?? b??nh Ta s??? ban, ch??nh l?? th???t Ta, ????? cho th??? gian ???????c s???ng\".  ???? l?? l???i Ch??a.";

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
