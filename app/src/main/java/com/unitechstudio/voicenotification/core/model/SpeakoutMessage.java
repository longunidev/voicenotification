package com.unitechstudio.voicenotification.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by LongUni on 4/8/2017.
 */

public class SpeakoutMessage implements Serializable {

    public enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }

    private Priority mPriority;
    private String mWhatToSpeakout;
    private boolean dontNotify;

    private Locale mLocale;

    public SpeakoutMessage(Priority priority, String whatToSpeakout) {
        this.mPriority = priority;
        this.mWhatToSpeakout = whatToSpeakout;
    }

    public Locale getLocale() {
        return mLocale;
    }

    public SpeakoutMessage setLocale(Locale locale) {
        this.mLocale = locale;
        return this;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public void setPriority(Priority mPriority) {
        this.mPriority = mPriority;
    }

    public String getWhatToSpeakout() {
        return mWhatToSpeakout;
    }

    public void setWhatToSpeakout(String mListWhatToSpeakout) {
        this.mWhatToSpeakout = mListWhatToSpeakout;
    }

    public boolean dontShowNotification() {
        return dontNotify;
    }

    public SpeakoutMessage setDontShowNotification(boolean shouldNotified) {
        this.dontNotify = shouldNotified;
        return this;
    }

    //    public static final ArrayList<SpeakoutMessage> STOP_SPEAKING_MSG = createAMessage(Priority.HIGH, "");
    public static final ArrayList<SpeakoutMessage> STOP_SPEAKING_MSG = new ArrayList<SpeakoutMessage>(Arrays.asList(new SpeakoutMessage(Priority.HIGH, "").setDontShowNotification(true)));

    public static ArrayList<SpeakoutMessage> createAMessage(Priority priority, String whatToSpeakout) {
        ArrayList<SpeakoutMessage> singleSentence = new ArrayList<>();
        SpeakoutMessage message = new SpeakoutMessage(priority, whatToSpeakout);
        singleSentence.add(message);
        return singleSentence;
    }
}
