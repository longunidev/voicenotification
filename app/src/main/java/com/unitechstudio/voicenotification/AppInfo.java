package com.unitechstudio.voicenotification;

import android.content.pm.ApplicationInfo;

/**
 * Created by LongUni on 4/16/2017.
 */

public class AppInfo {

    private ApplicationInfo mAppInfo;
    private boolean isSelected;

    public AppInfo(ApplicationInfo appInfo) {
        mAppInfo = appInfo;
    }

    public ApplicationInfo getAppInfo() {
        return mAppInfo;
    }

    public AppInfo setAppInfo(ApplicationInfo mAppInfo) {
        this.mAppInfo = mAppInfo;
        return this;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public AppInfo setSelected(boolean selected) {
        isSelected = selected;
        return this;
    }
}
