package com.unitechstudio.voicenotification.managers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.unitechstudio.voicenotification.core.BaseManager;
import com.unitechstudio.voicenotification.core.EventDispatcher;
import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.ResponseInfo;
import com.unitechstudio.voicenotification.core.model.SpeakoutMessage;
import com.unitechstudio.voicenotification.core.model.TTSEventInfo;
import com.unitechstudio.voicenotification.data.SharedPreferenceManager;
import com.unitechstudio.voicenotification.services.TTSService;

import java.util.ArrayList;

/**
 * Created by LongUni on 4/5/2017.
 */

public class TTSSpeakManager extends BaseManager {

    private final static boolean USING_BOUND_TTSSERVICE_CONNECTION = true;
    private final static int REPEATED_SPEAKING_LIMIT = 20;

    private TTSService mBoundTTSService;
    private boolean isBound = false;

    private ServiceConnection mTTSServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TTSService.TTSServiceBinder binder = (TTSService.TTSServiceBinder) service;
            mBoundTTSService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    public TTSSpeakManager(Context context, EventDispatcher eventDispatcher) {
        super(context, eventDispatcher);

        if (USING_BOUND_TTSSERVICE_CONNECTION) {
            Intent intent = new Intent(getContext(), TTSService.class);
            getContext().bindService(intent, mTTSServiceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    public ResponseInfo onEventHandling(EventPack event) {

        ResponseInfo resp = null;

        TTSEventInfo eventInfo = (TTSEventInfo) event.getEventInfo();

        if (eventInfo == null) {
            return resp;
        }

        switch (eventInfo.getCommand()) {
            case STARTING:
                resp = connectTTSService();
                break;
            case SPEAK_OUT_ONCE:

                ArrayList<SpeakoutMessage> listWhatToSpeak = null;

                if (eventInfo.getData() instanceof SpeakoutMessage) {
                    listWhatToSpeak = new ArrayList<>();
                    listWhatToSpeak.add((SpeakoutMessage) eventInfo.getData());
                } else if (eventInfo.getData() instanceof ArrayList<?>) {
                    listWhatToSpeak = eventInfo.getSpeakoutMessage();
                }

                resp = speakOut(listWhatToSpeak);
                break;
            case SPEAK_OUT_REPEATEDLY:
                speakOutRepeatedly(eventInfo.getSpeakoutMessage());
                break;
            case PAUSING:
                break;
            case TERMINATING:
                resp = terminate();
                break;
        }

        return resp;
    }

    private ResponseInfo connectTTSService() {

        ResponseInfo resp = new ResponseInfo();

        if (USING_BOUND_TTSSERVICE_CONNECTION) {
            Intent intent = new Intent(getContext(), TTSService.class);
            getContext().bindService(intent, mTTSServiceConnection, Context.BIND_AUTO_CREATE);
        } else {

            resp = new ResponseInfo();

            Intent intent = new Intent(getContext(), TTSService.class);
            getContext().startService(intent);

        }

        resp.setResult(ResponseInfo.Result.OK);

        return resp;
    }

    private ResponseInfo speakOut(ArrayList<SpeakoutMessage> messages) {

        final ResponseInfo resp = new ResponseInfo();
        if (USING_BOUND_TTSSERVICE_CONNECTION) {
            if (mBoundTTSService != null) {
                mBoundTTSService.speakOutAPI(messages, new TTSService.ITTSServiceCallback() {
                    @Override
                    public void onSpeakingComplete(boolean isSpeakingComplete) {
                        resp.setData(isSpeakingComplete);
                    }
                });
            }

        } else {
            //@TODO: Implement later
        }

        return resp;
    }

    private ResponseInfo speakOutRepeatedly(ArrayList<SpeakoutMessage> messages) {

        final ResponseInfo resp = new ResponseInfo();

        if (messages != null && !messages.isEmpty()) {

            String concatenatedContent = "";
            for (SpeakoutMessage msg : messages) {
                concatenatedContent += msg.getWhatToSpeakout() + "\n";
            }

            for (int i = 0; i < REPEATED_SPEAKING_LIMIT; i++) {
                concatenatedContent += concatenatedContent + "\n";
            }

            speakOut(SpeakoutMessage.createAMessage(SpeakoutMessage.Priority.MEDIUM, concatenatedContent));
        }

        return resp;
    }

    private ResponseInfo terminate() {

        ResponseInfo resp = new ResponseInfo();
        if (USING_BOUND_TTSSERVICE_CONNECTION) {
            getContext().unbindService(mTTSServiceConnection);
        } else {
            Intent intent = new Intent(getContext(), TTSService.class);
            getContext().stopService(intent);
        }

        resp.setResult(ResponseInfo.Result.OK);

        return resp;
    }

    private void requestDownloadMissingTTSData() {

    }

    private void checkForInstalledLanguageData() {
    }

    public static void setTTSRate(Context context, int value) {
        SharedPreferenceManager.getInstance(context).putInt("tts_default_rate", value);
    }

    public static int getTTSRate(Context context) {
        int value = SharedPreferenceManager.getInstance(context).getInt("tts_default_rate");
        return (value < 60 || value > 400) ? 100 : value;
    }

}
