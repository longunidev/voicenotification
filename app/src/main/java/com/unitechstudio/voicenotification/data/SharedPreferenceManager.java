package com.unitechstudio.voicenotification.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.Set;

public class SharedPreferenceManager {

    public final static String SPEAK_WHEN_SMS_INCOMING = "SPEAK_WHEN_SMS_INCOMING";
    public final static String SPEAK_WHEN_CALL_INCOMING = "SPEAK_WHEN_CALL_INCOMING";
    public final static String SPEAK_WHEN_APP_NOTIFICATIONS_APPEAR = "SPEAK_WHEN_APP_NOTIFICATIONS_APPEAR";
    public final static String SPEAK_CLIPBOARD = "SPEAK_CLIPBOARD";
    public final static String SHAKE_TO_TURN_OFF_SPEAKING = "SHAKE_TO_TURN_OFF_SPEAKING";
    public final static String ONLY_SPEAK_WHEN_EARPHONES_CONNECTED = "ONLY_SPEAK_WHEN_EARPHONES_CONNECTED";
    public final static String LIST_APP_ALLOWED_TO_SPEAK_NOTIFICATIONS = "LIST_APP_ALLOWED_TO_SPEAK_NOTIFICATIONS";
    public final static String FIRST_INSTALLATION = "FIRST_INSTALLATION";
    private static final String PERMISSION_PREFERENCE = "permission_perference";

    private static SharedPreferenceManager sprefMrgSingleton;
    private Context mContext;
    private SharedPreferences sharedPreference;
    private Editor editor;

    private SharedPreferenceManager(Context context) {
        mContext = context;
        sharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = sharedPreference.edit();
    }

    public static SharedPreferenceManager getInstance(Context context) {
        if (sprefMrgSingleton == null) {
            sprefMrgSingleton = new SharedPreferenceManager(context);
        }
        return sprefMrgSingleton;
    }

    public void putStringSet(String key, Set<String> set) {
        editor.putStringSet(key, set);
        editor.apply();
    }

    public Set<String> getStringSet(String key) {
        return sharedPreference.getStringSet(key, null);
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreference.getString(key, "");
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key) {
        return sharedPreference.getInt(key, 0);
    }

    public void putFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.apply();
    }

    public float getFloat(String key) {
        return sharedPreference.getFloat(key, 0);
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return sharedPreference.getBoolean(key, false);
    }

    //permission
    public void setBooleanAllPermission(boolean ishave) {
        putBoolean(PERMISSION_PREFERENCE, ishave);
    }

    public boolean getBooleanAllPermission() {
        return sharedPreference.getBoolean(PERMISSION_PREFERENCE, true);
    }
}
