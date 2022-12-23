package com.unitechstudio.voicenotification.services;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.unitechstudio.voicenotification.app.BaseApplication;
import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.SpeakoutMessage;
import com.unitechstudio.voicenotification.core.model.TTSEventInfo;
import com.unitechstudio.voicenotification.managers.FeatureManager;

import java.util.ArrayList;

public class NotificationService extends NotificationListenerService {
    public NotificationService() {
    }

    private final static String TAG = "Voice " + NotificationService.class.getSimpleName();

    Context mContext;

    @Override
    public void onCreate() {

        super.onCreate();
        mContext = this;

    }

    @Override

    public void onNotificationPosted(StatusBarNotification sbn) {

        if (!FeatureManager.FeatureConfig.isAppNotificationSpeakingEnabled(mContext) || FeatureManager.isPhoneRinging()) {
            return;
        }

        if (sbn == null) {
            return;
        }

        String packageName = sbn.getPackageName();
        ArrayList<String> listSelectedAppToSpeakNotification = FeatureManager.FeatureConfig.getListAppsAllowedSpeakingNotifications(mContext);

        boolean isAllowedApp = false;
        if (listSelectedAppToSpeakNotification != null && !listSelectedAppToSpeakNotification.isEmpty()) {
            for (String pkg : listSelectedAppToSpeakNotification) {
                if (packageName.equals(pkg)) {
                    isAllowedApp = true;
                    break;
                }
            }
            //isAllowedApp = listSelectedAppToSpeakNotification.contains(packageName);
        }

        Log.d(TAG, "Notification: " + packageName + " : " + isAllowedApp);

        if (isAllowedApp) {
            Notification notification = sbn.getNotification();

            if (notification != null) {
                Bundle extras = notification.extras;

                if (extras != null) {
                    String notiTitle = extras.getString("android.title");
                    String notiContent = "";
                    try {
                        String text = (String) extras.getCharSequence("android.text");
                        if (!TextUtils.isEmpty(text)) {
                            notiContent += text + "\n";
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    try {
                        CharSequence[] textLine = (CharSequence[]) extras.get("android.textLines");
                        if (textLine != null && textLine.length > 0) {
                            for (CharSequence line : textLine) {
                                if (!TextUtils.isEmpty(line)) {
                                    notiContent += line + "\n";
                                }

                            }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    final PackageManager pm = getApplicationContext().getPackageManager();

                    String applicationName = "";

                    try {
                        ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
                        applicationName = (String) pm.getApplicationLabel(ai);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (!TextUtils.isEmpty(applicationName) && !TextUtils.isEmpty(notiTitle) && !TextUtils.isEmpty(notiContent)) {

                        // Speak application name
//                        EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, SpeakoutMessage.createAMessage(SpeakoutMessage.Priority.MEDIUM, applicationName)));
                        EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, new SpeakoutMessage(SpeakoutMessage.Priority.MEDIUM, applicationName)));
                        BaseApplication.getInstance().postEvent(event);

                        // Speak notification title
                        event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, new SpeakoutMessage(SpeakoutMessage.Priority.MEDIUM, notiTitle)));
                        BaseApplication.getInstance().postEvent(event);

                        // Speak notification content
                        event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, new SpeakoutMessage(SpeakoutMessage.Priority.MEDIUM, notiContent)));
                        BaseApplication.getInstance().postEvent(event);

                    }

                }
            }
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "Notification Removed");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public static String bundle2string(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        String string = "Bundle{";
        for (String key : bundle.keySet()) {
            string += "\n" + key + " => " + bundle.get(key) + ";";
        }
        string += " }Bundle";
        return string;
    }
}
