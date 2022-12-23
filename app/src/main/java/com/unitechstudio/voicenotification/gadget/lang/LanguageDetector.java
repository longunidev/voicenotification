package com.unitechstudio.voicenotification.gadget.lang;

import android.text.TextUtils;
import android.util.Log;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Long Uni on 4/5/2017.
 */
public class LanguageDetector {

    private static String TAG = LanguageDetector.class.getSimpleName();

    public static String PROFILES_BASE = "/data/data/com.unitechstudio.voicenotification/files/profiles/";

    /**
     * @isLoadingProfile true: if there is a background thread working to load the profile
     * false: otherwise
     */
    private boolean isLoadingProfile = false;

    /**
     * @isProfileLoaded true: If the language profiles are already loaded
     * false: otherwise
     */
    private boolean isProfileLoaded = false;

    private static LanguageDetector ourInstance;

    private Detector mDetector = null;

    public static LanguageDetector getInstance() {

        if (ourInstance == null) {
            ourInstance = new LanguageDetector();
        }
        return ourInstance;
    }

    private LanguageDetector() {

        // This is supposed to load language profiles at first

    }

    public boolean isProfileReady() {
        return isProfileLoaded;
    }

    public void detectLanguage(final String patternLang, final ILangDetectListener listener) {

        /**
         * The language profiles were previously loaded
         * */
        if (isProfileLoaded) {
            try {
                mDetector = DetectorFactory.create();
                if (!TextUtils.isEmpty(patternLang)) {
                    mDetector.append(patternLang);
                    String langDetected = mDetector.detect();
                    if (listener != null) {
                        listener.onLangDetected(langDetected);
                    }
                }
            } catch (LangDetectException e) {
                /**
                 * If no features in text occurs, select the current language of the device
                 * */
                if (listener != null) {
                    listener.onLangDetected(Locale.getDefault().toString());
                }
                Log.e(TAG, "detectLanguage(): " + e.getMessage());
            }
        } else {

            /**
             * The language profiles are not loaded, we must create new background thread to load it
             * */
            if (!isLoadingProfile) {
                isLoadingProfile = true;
                Log.d(TAG, "detectLanguage(): profiles not available. new thread working now...");
                loadProfile(new LoadingLanguageProfileThread.ILangProfileListener() {
                    @Override
                    public void onLoadingComplete(boolean isComplete) {
                        if (isComplete) {
                            isProfileLoaded = isComplete;
                            try {
                                mDetector = DetectorFactory.create();

                                if (!TextUtils.isEmpty(patternLang)) {
                                    mDetector.append(patternLang);
                                    String langDetected = mDetector.detect();
                                    if (listener != null) {
                                        listener.onLangDetected(langDetected);
                                    }
                                }

                            } catch (LangDetectException e) {
                                /**
                                 * If no features in text occurs, select the current language of the device
                                 * */
                                if (listener != null) {
                                    listener.onLangDetected(Locale.getDefault().toString());
                                }
                                Log.e(TAG, "onLoadingComplete(): " + e.getMessage());
                            }
                            isLoadingProfile = false;
                        }

                        Log.d(TAG, "onLoadingComplete(): loading profile = " + isComplete);
                    }
                });
            } else {
                /**
                 * The language profiles are being loaded by a thread triggered by a preivous request.
                 * So, the request here will probably not be done because the profiles are not ready
                 * */
//                if (listener != null) {
//                    listener.onLangDetected(null);
//                }
            }
        }
    }

    public void loadProfile(final LoadingLanguageProfileThread.ILangProfileListener listener) {

        Log.d(TAG, "loadProfile(), isLoadingProfile=" + isLoadingProfile);
        if (!isLoadingProfile) {

            isLoadingProfile = true;
            Log.d(TAG, "loadProfile(): a new thread loading the profiles now...");
            new LoadingLanguageProfileThread(new LoadingLanguageProfileThread.ILangProfileListener() {
                @Override
                public void onLoadingComplete(boolean isComplete) {
                    isProfileLoaded = isComplete;
                    isLoadingProfile = false;
                    listener.onLoadingComplete(isComplete);
                }
            }).start();
        }

    }

    synchronized public void deLoadProfile() {
        isProfileLoaded = false;
        isLoadingProfile = false;
        DetectorFactory.clear();
        DetectorFactory.instance_.wordLangProbMap = new HashMap<>();
        DetectorFactory.instance_.langlist.clear();
//        DetectorFactory.instance_.langlist = null;
    }

    public interface ILangLoadListener {
        void onLangProfileLoaded(boolean isLoaded);
    }

    public interface ILangDetectListener {
        void onLangDetected(String detectedLang);
    }

    public static class LoadingLanguageProfileThread extends Thread {

        private ILangProfileListener mListener;

        LoadingLanguageProfileThread(ILangProfileListener listener) {
            this.mListener = listener;
        }

        @Override
        public void run() {
            Log.d(TAG, "LoadingLanguageProfileThread(): running, tid=" + Thread.currentThread().getId());
            try {
                DetectorFactory.loadProfile(new File(PROFILES_BASE));
                mListener.onLoadingComplete(true);
            } catch (LangDetectException e) {
                mListener.onLoadingComplete(false);
                Log.e(TAG, "LoadingLanguageProfileThread(): " + e.getMessage());
            }
        }

        public interface ILangProfileListener {
            void onLoadingComplete(boolean isComplete);
        }
    }
}
