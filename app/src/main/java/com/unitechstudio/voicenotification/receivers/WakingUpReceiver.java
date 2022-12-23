package com.unitechstudio.voicenotification.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.unitechstudio.voicenotification.app.BaseApplication;
import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.TTSEventInfo;

public class WakingUpReceiver extends BroadcastReceiver {

    public static final String ACTION_WAKE_UP = "com.voicenotification.action.WAKE_UP";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) {
            return;
        }

        if (ACTION_WAKE_UP.equals(intent.getAction()) || Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.STARTING));

            BaseApplication.getInstance().postEvent(event);
        }

    }
}
