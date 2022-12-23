package com.unitechstudio.voicenotification.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.widgets.toast.Toasty;
import com.unitechstudio.voicenotification.activities.dialog.AlertDialogActivity;
import com.unitechstudio.voicenotification.app.BaseApplication;
import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.SpeakoutMessage;
import com.unitechstudio.voicenotification.core.model.TTSEventInfo;
import com.unitechstudio.voicenotification.gadget.clipboard.ClipboardListener;
import com.unitechstudio.voicenotification.gadget.lang.LanguageDetector;
import com.unitechstudio.voicenotification.gadget.shakedetector.ShakeDetector;
import com.unitechstudio.voicenotification.managers.FeatureManager;
import com.unitechstudio.voicenotification.managers.TTSSpeakManager;
import com.unitechstudio.voicenotification.notification.AppNotificationManager;
import com.unitechstudio.voicenotification.receivers.WakingUpReceiver;
import com.unitechstudio.voicenotification.utils.CommonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.unitechstudio.voicenotification.utils.CommonUtils.isVoiceDataLanguageInstalled;

public class TTSService extends Service implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

    private static final String TAG = TTSService.class.getSimpleName();

    private static final int TIME_DELAY_AFTER_FINISH_SPEAKING_TO_FREE_MEMORY = (int) (1000 * 60 * 3.5);

    public static final String TTS_ENGINE_DEFAULT = "com.google.android.tts";

    private Context mContext;
    private final Binder mBinder = new TTSServiceBinder();

    private SpeakoutMessage spokenMessage;

    final Handler handler = new Handler();
    long start;
    Runnable freeMemoryRunnable = new Runnable() {
        @Override
        public void run() {

            if (mTextToSpeechEngine != null && !mTextToSpeechEngine.isSpeaking()) {
                mTextToSpeechEngine.shutdown();
                //mTextToSpeechEngine = null;
                mLangHelper.deLoadProfile();

                Log.d(TAG, "deLoadProfile after: " + (System.currentTimeMillis() - start) / 1000);

                System.gc();
//                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    };

    private ShakeDetector.OnShakeListener mShakeListener = new ShakeDetector.OnShakeListener() {
        @Override
        public void OnShake() {
            if (FeatureManager.FeatureConfig.isShakeToTurnOffSpeakingEnabled(mContext)) {
                if (mTextToSpeechEngine != null && mTextToSpeechEngine.isSpeaking()) {
                    EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, SpeakoutMessage.STOP_SPEAKING_MSG));
                    BaseApplication.getInstance().postEvent(event);
                    CommonUtils.vibrate(mContext);
                }
                ShakeDetector.destroy();
            }
        }
    };

    private TextToSpeech mTextToSpeechEngine;
    private LanguageDetector mLangHelper;

    /**
     * @mExecutorService is used to execute tasks in background as a worker thread
     */
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    /**
     * @pendingTasks contains a list of submited tasks which are going to be executed
     * by mExecutorService when the language profiles are ready
     */
    private Collection<Callable<String>> pendingTasks = new ArrayList<Callable<String>>();

    public TTSService() {
    }

    public class TTSServiceBinder extends Binder {
        public TTSService getService() {
            return TTSService.this;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate()");

        mContext = this;

        ClipboardListener.registerPrimaryClipChanged(mContext, new ClipboardListener.OnClipboardChangeListener() {
            @Override
            public void onClipBoardChanged(String clipboardData) {

                if (!FeatureManager.FeatureConfig.isClipboardSpeakingEnabled(mContext.getApplicationContext())) {
                    return;
                }

                if (!TextUtils.isEmpty(clipboardData)) {

//                    EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, SpeakoutMessage.createAMessage(SpeakoutMessage.Priority.MEDIUM, clipboardData)));
                    EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, new SpeakoutMessage(SpeakoutMessage.Priority.MEDIUM, clipboardData)));
                    BaseApplication.getInstance().postEvent(event);
                }
            }
        });

        initializeLangDetection();

        mLangHelper = LanguageDetector.getInstance();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "TTSService - onStartCommand()");

        if (false && mTextToSpeechEngine == null) {

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    mTextToSpeechEngine = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                mTextToSpeechEngine.setOnUtteranceCompletedListener(TTSService.this);
                            }

                        }
                    }, TTS_ENGINE_DEFAULT);
                }
            });

        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        /**
         * Trigger an event to self-start TTSService
         * */

//        mContext.sendBroadcast(new Intent(WakingUpReceiver.ACTION_WAKE_UP));
        Log.d(TAG, "onDestroy() : send wake_up event " + WakingUpReceiver.ACTION_WAKE_UP);

        if (mTextToSpeechEngine != null) {
            mTextToSpeechEngine.stop();
            mTextToSpeechEngine.shutdown();
        }

        super.onDestroy();

        ShakeDetector.destroy();
        ClipboardListener.unRegisterPrimaryClipChanged();
        mExecutorService.shutdown();

    }

    private void initializeLangDetection() {
        File profiles = new File(LanguageDetector.PROFILES_BASE);
        if (!profiles.exists() || profiles.listFiles().length <= 0) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    CommonUtils.copyFolder(getApplicationContext(), "profiles", null);
                    Log.d(TAG, "initializeLangDetection() done.");
                }
            }).start();

        }
    }

    private void speak(SpeakoutMessage message, Locale locale, int PLAYBACK_QUEUE_TYPE) {

        Log.d(TAG, "private void speak: speaking in threadId: " + Thread.currentThread().getId());

        if (mTextToSpeechEngine != null) {

            // Show notification here
            //AppNotificationManager.CustomNotification(mContext);
            if (!message.dontShowNotification()) {
                AppNotificationManager.createNotification(mContext, message.setLocale(locale));
            }

            mTextToSpeechEngine.setSpeechRate(TTSSpeakManager.getTTSRate(mContext.getApplicationContext()) / 100.0f);

            try {
                mTextToSpeechEngine.setLanguage(locale);

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        if (!isVoiceDataLanguageInstalled(mTextToSpeechEngine, locale.getLanguage())) {
                            Intent intent = new Intent(mContext, AlertDialogActivity.class);
                            intent.putExtra("language", locale.getDisplayLanguage());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            return;
                        }
                    } catch (NullPointerException e) {
                    }
                }

            } catch (MissingResourceException e) {
                mTextToSpeechEngine.setLanguage(Locale.ENGLISH);
                Log.d(TAG, "MissingResourceException: " + e.getMessage());
            }

            //textToSpeech can only cope with Strings with < 4000 characters
            int dividerLimit = 3900;
            String textForReading = message.getWhatToSpeakout();
            if (textForReading.length() >= dividerLimit) {

                Log.d(TAG, "textForReading large");

                int textLength = textForReading.length();
                ArrayList<String> texts = new ArrayList<String>();
                int count = textLength / dividerLimit + ((textLength % dividerLimit == 0) ? 0 : 1);
                int start = 0;
                int end = textForReading.indexOf(" ", dividerLimit);
                for (int i = 1; i <= count; i++) {
                    texts.add(textForReading.substring(start, end));
                    start = end;
                    if ((start + dividerLimit) < textLength) {
                        end = textForReading.indexOf(" ", start + dividerLimit);
                    } else {
                        end = textLength;
                    }
                }

                for (int i = 0; i < texts.size(); i++) {
                    HashMap<String, String> myHashAlarm = new HashMap();

                    String id = texts.get(i).substring(Math.max(texts.get(i).length() - 10, 0));
                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(AudioManager.STREAM_ALARM));
                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id:" + id);
                    mTextToSpeechEngine.speak(texts.get(i), PLAYBACK_QUEUE_TYPE, myHashAlarm);
//                    mTextToSpeechEngine.speak(texts.get(i), PLAYBACK_QUEUE_TYPE, null);
                }
            } else {
                Log.d(TAG, "textForReading normal");
                HashMap<String, String> myHashAlarm = new HashMap();

                String id = textForReading.substring(Math.max(textForReading.length() - 10, 0));

                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(AudioManager.STREAM_ALARM));
                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id:" + id);
                int result = mTextToSpeechEngine.speak(textForReading, PLAYBACK_QUEUE_TYPE, myHashAlarm);

                Log.d(TAG, "speak result: " + result);

//                mTextToSpeechEngine.speak(textForReading, TextToSpeech.QUEUE_ADD, myHashAlarm);

//                mTextToSpeechEngine.speak(textForReading, PLAYBACK_QUEUE_TYPE, null);
            }

            //mTextToSpeechEngine.speak(message.getWhatToSpeakout(), PLAYBACK_QUEUE_TYPE, null);
        }
    }

    /**
     * This is the only method which speaks out by @mTextToSpeechEngine
     * Do not use mTextToSpeechEngine to speak out in anywhere else
     *
     * @param message             The message is going to speak.
     * @param PLAYBACK_QUEUE_TYPE Refer #TextToSpeech.QUEUE_FLUSH or #TextToSpeech.QUEUE_ADD
     */
    private boolean speakOut(final SpeakoutMessage message, final int PLAYBACK_QUEUE_TYPE) {

        Log.d(TAG, "private boolean speakOut: speaking in threadId: " + Thread.currentThread().getId());

        if (message == null || TextUtils.isEmpty(message.getWhatToSpeakout())) {
            return false;
        }

        final String whatToSpeakout = message.getWhatToSpeakout();

        if (message.getLocale() != null) {
            Log.d(TAG, "Locale was determined: " + message.getLocale());
            speak(message, message.getLocale(), PLAYBACK_QUEUE_TYPE);
        } else {

            String langPattern = whatToSpeakout.substring(0, Math.min(message.getWhatToSpeakout().length(), 1000));

            Log.d(TAG, "private speakOut msg: " + whatToSpeakout.length());

            mLangHelper.detectLanguage(langPattern, new LanguageDetector.ILangDetectListener() {
                @Override
                public void onLangDetected(String detectedLang) {
                    if (!TextUtils.isEmpty(detectedLang)) {
                        Locale locale;
                        if ("zh-cn".equals(detectedLang)) {
                            locale = Locale.CHINESE;
                        } else if ("zh-tw".equals(detectedLang)) {
                            locale = Locale.TAIWAN;
                        } else if ("unknown".equals(detectedLang)) {
                            locale = Locale.ENGLISH;
                        } else {
                            locale = new Locale(detectedLang);
                        }
                        speak(message, locale, PLAYBACK_QUEUE_TYPE);

                    } else {
                    }
                    Log.d(TAG, "speakOut() - onLangDetected: " + detectedLang);
                }
            });
        }
        return true;
    }

    private boolean speakOutByPriority(final SpeakoutMessage message) {

        if (message == null) {
            return false;
        }

        boolean ret = false;

        Log.d(TAG, "speakOutByPriority() - " + "Priority= " + message.getPriority() + ", Message=" + message.getWhatToSpeakout().substring(0, Math.min(message.getWhatToSpeakout().length(), 10)));

        Log.d(TAG, "private boolean speakOutByPriority: speaking in threadId: " + Thread.currentThread().getId());

        switch (message.getPriority()) {
            case HIGH:
                if (message == SpeakoutMessage.STOP_SPEAKING_MSG.get(0)) {
                    if (mTextToSpeechEngine != null && mTextToSpeechEngine.isSpeaking()) {
                        mTextToSpeechEngine.stop();
                    }
//                    mTextToSpeechEngine.setLanguage(Locale.ENGLISH);
//                    mTextToSpeechEngine.speak(message.getWhatToSpeakout(), TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    ret = speakOut(message, TextToSpeech.QUEUE_FLUSH);
                }
                break;
            case MEDIUM:
            case LOW:
                ret = speakOut(message, TextToSpeech.QUEUE_ADD);
                break;
        }

        return ret;
    }

    public void speakOutAPI(final ArrayList<SpeakoutMessage> listWhatToSpeak, final ITTSServiceCallback callback) {

        // Adding requests into the task list;
        if (listWhatToSpeak != null && !listWhatToSpeak.isEmpty()) {
            for (SpeakoutMessage message : listWhatToSpeak) {
                //speakOutByPriority(whatToSpeak);
//                pendingTasks.add(new SpeakingCallable(whatToSpeak));

                if (message != null) {

                    if (message == SpeakoutMessage.STOP_SPEAKING_MSG.get(0)) {
                        mExecutorService.submit(new SpeakingCallable(message));
                        return;
                    }

                    if (message.getLocale() != null) {
                        // Speak instantly because language is determined
                        mExecutorService.submit(new SpeakingCallable(message));
                        return;
                    } else {
                        // Add to queue
                        pushTask(new SpeakingCallable(message));
                    }
                }

            }
        }

        if (!mLangHelper.isProfileReady()) {
            Log.d(TAG, "begin loading profiles");
            final long startLoading = System.currentTimeMillis();
            Handler handler = new Handler(Looper.getMainLooper());

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toasty.info(mContext, "Voice Notification is processing text...", Toast.LENGTH_SHORT, true).show();
                }
            });

            // Request loading the profiles
            mLangHelper.loadProfile(new LanguageDetector.LoadingLanguageProfileThread.ILangProfileListener() {
                @Override
                public void onLoadingComplete(boolean isComplete) {

                    // When the profiles are loaded successfully. Execute all pending tasks immediately
                    if (isComplete) {
                        executeAllPendingTasks();
                    }

                    Log.d(TAG, "onLoadingComplete(): " + isComplete + " in " + (System.currentTimeMillis() - startLoading));
                }
            });
        } else {
            Log.d(TAG, "Profiles loaded");
            // When the profiles are loaded successfully. Execute all pending tasks immediately
            if (mTextToSpeechEngine != null) {
                executeAllPendingTasks();
            }
        }

    }

    public void synthesizeToFileAPI() {
        HashMap<String, String> myHashRender = new HashMap();
        String wakeUpText = "Are you up yet?";
        String destFileName = "/sdcard/tts.wav";
        myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, wakeUpText);
        mTextToSpeechEngine.synthesizeToFile(wakeUpText, myHashRender, destFileName);
        //TODO: Implement this later
    }

    private synchronized void pushTask(Callable task) {
        pendingTasks.add(task);
    }

    private synchronized void executeTask(Callable task) {
        mExecutorService.submit(task);
    }

    private synchronized void executeAllPendingTasks() {

//        handler.removeCallbacks(myRunnable);

        try {
            if (pendingTasks != null && !pendingTasks.isEmpty() && mExecutorService != null && !mExecutorService.isShutdown()) {
                mExecutorService.invokeAll(pendingTasks);
                pendingTasks.clear();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Toast.makeText(mContext, "deLoadProfile()!!!", Toast.LENGTH_SHORT).show();

//        start = System.currentTimeMillis();
//        handler.postDelayed(myRunnable, TIME_DELAY_AFTER_FINISH_SPEAKING_TO_FREE_MEMORY);

        Log.d(TAG, "executeAllPendingTasks() done");
    }

    private class SpeakingCallable implements Callable {

        private SpeakoutMessage mMessage;

        public SpeakingCallable(SpeakoutMessage message) {
            mMessage = message;
        }

        @Override
        public Object call() throws Exception {

            ShakeDetector.create(mContext, mShakeListener);

            spokenMessage = mMessage;

            if (mTextToSpeechEngine == null) {
                mTextToSpeechEngine = new TextToSpeech(mContext, TTSService.this, TTS_ENGINE_DEFAULT);
                Log.d(TAG, "TextToSpeech initialize");
            } else {
                speakOutByPriority(mMessage);
            }

            return null;
        }
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.ERROR) {

            Log.d(TAG, "onInit: speaking in threadId: " + Thread.currentThread().getId());

            speakOutByPriority(spokenMessage);
//                            mTextToSpeechEngine.setOnUtteranceCompletedListener((TextToSpeech.OnUtteranceCompletedListener) mContext);
            mTextToSpeechEngine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.d(TAG, "UtteranceProgressListener - onStart");
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.d(TAG, "UtteranceProgressListener - onDone");
                }

                @Override
                public void onError(String utteranceId) {
                    Log.d(TAG, "UtteranceProgressListener - onError");
                }
            });

            mTextToSpeechEngine.setOnUtteranceCompletedListener(TTSService.this);

        }
    }

    @Override
    public void onUtteranceCompleted(String utteranceId) {
        Log.d(TAG, "id: " + utteranceId);

        handler.removeCallbacks(freeMemoryRunnable);

        ShakeDetector.destroy();

        start = System.currentTimeMillis();
        handler.postDelayed(freeMemoryRunnable, TIME_DELAY_AFTER_FINISH_SPEAKING_TO_FREE_MEMORY);

        Log.d(TAG, "OnUtteranceCompletedListener: " + utteranceId);
    }

    public interface ITTSServiceCallback {
        void onSpeakingComplete(boolean isSpeakingComplete);
    }

}
