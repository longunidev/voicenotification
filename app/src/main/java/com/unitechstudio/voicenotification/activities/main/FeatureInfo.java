package com.unitechstudio.voicenotification.activities.main;

/**
 * Created by LongUni on 4/16/2017.
 */

public class FeatureInfo {

    public enum FeatureType {
        SPEAK_INCOMING_SMS,
        SPEAK_INCOMING_CALL,
        SPEAK_APP_NOTIFICATION,
        SPEAK_CLIPBOARD,
        SHAKE_TO_TURNOFF_SPEAKING,
        ONLY_SPEAK_WHEN_EARPHONES_CONNECTED
    }

    private int mResId;
    private String mFeatureName;
    private boolean mIsEnabled;
    private FeatureType mType;

    public FeatureInfo(int resId, String featureName) {
        mResId = resId;
        mFeatureName = featureName;
    }

    public int getResId() {
        return mResId;
    }

    public FeatureInfo setResId(int mResId) {
        this.mResId = mResId;
        return this;
    }

    public String getFeatureName() {
        return mFeatureName;
    }

    public FeatureInfo setFeatureName(String mFeatureName) {
        this.mFeatureName = mFeatureName;
        return this;
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }

    public FeatureInfo setEnabled(boolean mIsEnabled) {
        this.mIsEnabled = mIsEnabled;
        return this;
    }

    public FeatureType getType() {
        return mType;
    }

    public FeatureInfo setType(FeatureType mType) {
        this.mType = mType;
        return this;
    }
}
