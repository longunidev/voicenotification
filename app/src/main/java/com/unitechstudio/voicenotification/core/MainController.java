package com.unitechstudio.voicenotification.core;

import android.content.Context;
import android.util.Log;

import com.unitechstudio.voicenotification.app.BaseApplication;
import com.unitechstudio.voicenotification.core.model.AppEventInfo;
import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.ResponseInfo;
import com.unitechstudio.voicenotification.core.model.TTSEventInfo;
import com.unitechstudio.voicenotification.managers.FeatureManager;
import com.unitechstudio.voicenotification.managers.TTSSpeakManager;
import com.unitechstudio.voicenotification.notification.AppNotificationManager;

import java.util.Observable;

/**
 * Created by Long Uni on 4/4/2017.
 */

public class MainController extends Observable implements EventDispatcher {

    private final static String TAG = MainController.class.getSimpleName();

    private static MainController singleTonController;

    private Context mContext;

    private TTSSpeakManager mTTSSpeakManager;
    private AppNotificationManager mNotificationManager;
    private FeatureManager mFeatureManager;

    private MainController(Context context) {
        this.mContext = context;

        mTTSSpeakManager = new TTSSpeakManager(mContext, this);
        mNotificationManager = new AppNotificationManager(mContext, this);
        mFeatureManager = new FeatureManager(mContext, this);
    }

    public static MainController getInstance(Context context) {
        if (singleTonController == null) {
            singleTonController = new MainController(context);
        }
        return singleTonController;
    }

    @Override
    public ResponseInfo handleEvent(final EventPack event) {

        if (event == null) {
            return null;
        }

        ResponseInfo resp = null;

        Log.d(TAG, "handleEvent(): EventType=" + event.getEventType() + ", EventInfo=" + event.getEventInfo().getClass().getSimpleName() + ", Command=" + event.getEventInfo().getCommand());

        switch (event.getEventType()) {

            case GENERAL:
                resp = handleGeneralEvent(event);
                break;
            case APP:
                resp = handleAppEvent(event);
                break;
            case VIEW:
                // Notify Observers
                controllerNotifyToUIObservers(event);
                break;
        }

        return resp;
    }

    private ResponseInfo handleGeneralEvent(EventPack event) {
        ResponseInfo resp = null;
        return resp;
    }

    private ResponseInfo handleAppEvent(EventPack event) {

        if (event == null) {
            return null;
        }

        ResponseInfo resp = null;

        if (event.getEventInfo() instanceof TTSEventInfo) {
            resp = mTTSSpeakManager.onEventHandling(event);
        }

        if (event.getEventInfo() instanceof AppEventInfo) {
            resp = mFeatureManager.onEventHandling(event);
        }

        return resp;
    }

    /**
     * Notify new changes for UI observers
     */
    private void controllerNotifyToUIObservers(final EventPack event) {
        BaseApplication.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setChanged();
                notifyObservers(event);
                clearChanged();
            }
        });
    }
}
