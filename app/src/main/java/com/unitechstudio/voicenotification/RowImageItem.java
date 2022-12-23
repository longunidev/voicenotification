package com.unitechstudio.voicenotification;

import android.graphics.drawable.Drawable;

/**
 * Created by LongUni on 4/15/2017.
 */

public class RowImageItem {
    private Drawable mThumbnail;
    private String keyName;
    private String mTitle;
    private boolean isEnabled;

    public RowImageItem() {

    }

    public RowImageItem(Drawable thumbnail, String title, boolean isEnabled) {
        this.mThumbnail = thumbnail;
        this.mTitle = title;
        this.isEnabled = isEnabled;
    }

    public Drawable getThumbnail() {
        return mThumbnail;
    }

    public RowImageItem setThumbnail(Drawable mThumbnail) {
        this.mThumbnail = mThumbnail;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public RowImageItem setTitle(String mTitle) {
        this.mTitle = mTitle;
        return this;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public RowImageItem setEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }

    public String getKeyName() {
        return keyName;
    }

    public RowImageItem setKeyName(String keyName) {
        this.keyName = keyName;
        return this;
    }
}
