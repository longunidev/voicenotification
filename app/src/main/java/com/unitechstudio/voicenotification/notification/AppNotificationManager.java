package com.unitechstudio.voicenotification.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.unitechstudio.voicenotification.R;
import com.unitechstudio.voicenotification.core.BaseManager;
import com.unitechstudio.voicenotification.core.EventDispatcher;
import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.ResponseInfo;
import com.unitechstudio.voicenotification.core.model.SpeakoutMessage;
import com.unitechstudio.voicenotification.receivers.IncomingReceiver;

import static android.app.PendingIntent.FLAG_MUTABLE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.unitechstudio.voicenotification.receivers.IncomingReceiver.ACTION_CLEAR_NOTIFICATION;

import androidx.core.app.NotificationCompat;

/**
 * Created by Long Uni on 4/11/2017.
 */

public class AppNotificationManager extends BaseManager {

    public static final int NOTIFICATION_ID = 0;

    public AppNotificationManager(Context context, EventDispatcher eventDispatcher) {
        super(context, eventDispatcher);
    }

    @Override
    public ResponseInfo onEventHandling(EventPack event) {
        return null;
    }

    public static void createNotification(Context context, SpeakoutMessage message) {

        if (context == null || message == null) {
            return;
        }

        // Prepare intent which is triggered if the
        // notification is selected

        // When the user touches on Notification
        Intent notiIntent = new Intent(context, IncomingReceiver.class);
        notiIntent.setAction(IncomingReceiver.ACTION_NOTI_SELECTED);
        PendingIntent notiPendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), notiIntent, FLAG_MUTABLE);

        // When the user selects speak again
        Intent speakingAgainIntent = new Intent(context, IncomingReceiver.class);
        speakingAgainIntent.setAction(IncomingReceiver.ACTION_SPEAK_AGAIN_MSG);
        speakingAgainIntent.putExtra("message", message);
        PendingIntent speakingAgainPendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), speakingAgainIntent, FLAG_MUTABLE);

        // When the user selects close notification
        Intent clearNotiIntent = new Intent(context, IncomingReceiver.class);
        clearNotiIntent.setAction(ACTION_CLEAR_NOTIFICATION);
        PendingIntent clearNotiPendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), clearNotiIntent, FLAG_MUTABLE);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(context)
                .setContentTitle("New message")
                .setContentText(message.getWhatToSpeakout())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(notiPendingIntent)
                .addAction(R.drawable.ic_noti_speaker, "Speak", speakingAgainPendingIntent)
                .addAction(R.drawable.ic_noti_close, "Close", clearNotiPendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        notificationManager.notify(NOTIFICATION_ID, noti);

    }

    public static void createCustomNotification(Context context, SpeakoutMessage message) {
        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.customnotification);

        // Set Notification Title
        String strtitle = "New message";
        // Set Notification Text
        String strtext = "Do cây thị đã cao tuổi và rất linh thiêng nên người dân làng Chờ (Phú Mẫn, Yên Phong, Bắc Ninh) không ai dám mạo phạm.\n";

        // Open NotificationView Class on Notification Click
        Intent intent = new Intent(context, IncomingReceiver.class);
        // Send data to NotificationView Class
        intent.putExtra("title", strtitle);
        intent.putExtra("text", strtext);
        // Open NotificationView.java Activity
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                // Set Icon
                .setSmallIcon(R.mipmap.ic_launcher)
                // Set Ticker Message
                //.setTicker(getString(R.string.customnotificationticker))
                // Dismiss Notification
                .setAutoCancel(true)
                // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                // Set RemoteViews into Notification
                .setContent(remoteViews);

        // Locate and set the Image into customnotificationtext.xml ImageViews
        remoteViews.setImageViewResource(R.id.notiIcon, R.mipmap.ic_launcher);

        // Locate and set the Text into customnotificationtext.xml TextViews
        remoteViews.setTextViewText(R.id.notiTitle, "New Message");
        remoteViews.setTextViewText(R.id.notiContent, "Do cây thị đã cao tuổi và rất linh thiêng nên người dân làng Chờ (Phú Mẫn, Yên Phong, Bắc Ninh) không ai dám mạo phạm.Do cây thị đã cao tuổi và rất linh thiêng nên người dân làng Chờ (Phú Mẫn, Yên Phong, Bắc Ninh) không ai dám mạo phạm.Do cây thị đã cao tuổi và rất linh thiêng nên người dân làng Chờ (Phú Mẫn, Yên Phong, Bắc Ninh) không ai dám mạo phạm.Do cây thị đã cao tuổi và rất linh thiêng nên người dân làng Chờ (Phú Mẫn, Yên Phong, Bắc Ninh) không ai dám mạo phạm.");
        remoteViews.setTextViewText(R.id.tvSpeakagain, "Speak again");

        remoteViews.setTextViewText(R.id.tvCloseNoti, "Close");

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(0, builder.build());
    }

    public static void CustomNotification(Context context) {

        Intent i = new Intent(context, IncomingReceiver.class);

        PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        android.app.Notification.Builder builder = (new android.app.Notification.Builder(context)).setSmallIcon(R.mipmap.ic_launcher).setContentIntent(pendingintent).setAutoCancel(true).setTicker("New message").setContentTitle("New message").setContentText("Notifications in the notification drawer appear in two main visual styles, normal view and big view");

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(0, builder.build());

    }
}
