package com.unitechstudio.voicenotification.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.widgets.preferences.switches.SwitchPreference;
import com.unitechstudio.voicenotification.R;
import com.unitechstudio.voicenotification.app.BaseApplication;
import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.SpeakoutMessage;
import com.unitechstudio.voicenotification.core.model.TTSEventInfo;
import com.unitechstudio.voicenotification.managers.FeatureManager;
import com.unitechstudio.voicenotification.managers.TTSSpeakManager;
import com.unitechstudio.voicenotification.utils.CommonUtils;

import java.util.Locale;

import static com.unitechstudio.voicenotification.activities.AppNotificationConfig.APP_NOTIFICATION_ENABLED;
import static com.unitechstudio.voicenotification.utils.CommonUtils.launchMarket;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

/**
 * Created by LongUni on 5/4/2017.
 */

public class FeaturePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private static final String TAG = FeaturePreferenceFragment.class.getSimpleName();

    private static final String KEY_ENABLE_SMS_SPEAKING = "switch_preference_enable_sms_speaking";
    private static final String KEY_ENABLE_CALL_SPEAKING = "switch_preference_enable_call_speaking";
    private static final String KEY_ENABLE_NOTIFICATION_SPEAKING = "switch_preference_enable_notification_speaking";
    private static final String KEY_ENABLE_CLIPBOARD_SPEAKING = "switch_preference_enable_clipboard_speaking";
    private static final String KEY_ENABLE_SHAKE_TO_TURNOFF_SPEAKING = "switch_preference_enable_shake_to_turnoff_speaking";

    public static final int VOICE_DATA_CHECK = 1;
    public static final int APP_NOTIFICATION_CODE = 2;

    private Activity mActivity;

    private SwitchPreference mEnableSMSSpeaking, mEnableCallSpeaking, mEnableNotificationSpeaking, mEnableClipboardSpeaking, mEnableShakeToTurnoffSpeaking;
    private ListPreference mListSpeechRate;
    private PreferenceScreen mListenToAnExample, mRating, mPrivacyPolicy;

//    private AdView mAdView;
//    private AdRequest adRequest;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();

        mEnableSMSSpeaking = (SwitchPreference) findPreference(KEY_ENABLE_SMS_SPEAKING);
        mEnableSMSSpeaking.setChecked(FeatureManager.FeatureConfig.isSMSSpeakingEnabled(mActivity.getApplicationContext()));
        mEnableSMSSpeaking.setOnPreferenceClickListener(this);

        mEnableCallSpeaking = (SwitchPreference) findPreference(KEY_ENABLE_CALL_SPEAKING);
        mEnableCallSpeaking.setChecked(FeatureManager.FeatureConfig.isCallSpeakingEnabled(mActivity.getApplicationContext()));
        mEnableCallSpeaking.setOnPreferenceClickListener(this);

        mEnableNotificationSpeaking = (SwitchPreference) findPreference(KEY_ENABLE_NOTIFICATION_SPEAKING);
//       mEnableNotificationSpeaking.setChecked(FeatureManager.FeatureConfig.isAppNotificationSpeakingEnabled(mActivity.getApplicationContext()));
        mEnableNotificationSpeaking.setOnPreferenceClickListener(this);

        mEnableClipboardSpeaking = (SwitchPreference) findPreference(KEY_ENABLE_CLIPBOARD_SPEAKING);
        mEnableClipboardSpeaking.setChecked(FeatureManager.FeatureConfig.isClipboardSpeakingEnabled(mActivity.getApplicationContext()));
        mEnableClipboardSpeaking.setOnPreferenceClickListener(this);

        mEnableShakeToTurnoffSpeaking = (SwitchPreference) findPreference(KEY_ENABLE_SHAKE_TO_TURNOFF_SPEAKING);
        mEnableShakeToTurnoffSpeaking.setChecked(FeatureManager.FeatureConfig.isShakeToTurnOffSpeakingEnabled(mActivity.getApplicationContext()));
        mEnableShakeToTurnoffSpeaking.setOnPreferenceClickListener(this);

        mListSpeechRate = (ListPreference) findPreference("speechRate");
        mListSpeechRate.setValue(TTSSpeakManager.getTTSRate(mActivity.getApplicationContext()) + "");
        mListSpeechRate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof String) {
                    int rateValue = Integer.parseInt((String) newValue);
                    TTSSpeakManager.setTTSRate(mActivity.getApplicationContext(), rateValue);
                    mListSpeechRate.setValue((String) newValue);
                }
                return false;
            }
        });

        mListSpeechRate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return false;
            }
        });

        mListenToAnExample = (PreferenceScreen) findPreference("listenToAnExample");
        mListenToAnExample.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String text = "This is an example of speech synthesis in English";
                EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, new SpeakoutMessage(SpeakoutMessage.Priority.MEDIUM, text).setLocale(Locale.ENGLISH)));
                BaseApplication.getInstance().postEvent(event);
                return false;
            }
        });

        mRating = (PreferenceScreen) findPreference("rating");
        mRating.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                launchMarket(mActivity);

                return false;
            }
        });

        mPrivacyPolicy = (PreferenceScreen) findPreference("privacy_policy");
        mPrivacyPolicy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://unitechofficial.github.io/voice_notification_privacy_policy.html"));
                startActivity(browserIntent);

                return false;
            }
        });

        View rootView = getView();

        ListView list = (ListView) rootView.findViewById(android.R.id.list);
        list.setDivider(CommonUtils.getDrawable(mActivity.getApplicationContext(), R.drawable.divider));
//        list.setDivider(mActivity.getDrawable(R.drawable.divider));
//        list.setPadding((int) mActivity.getResources().getDimension(R.dimen.activity_horizontal_margin), (int) mActivity.getResources().getDimension(R.dimen.activity_vertical_margin), (int) mActivity.getResources().getDimension(R.dimen.activity_horizontal_margin), 0);
        list.setPadding(8, 0, 8, 0);

        CommonUtils.setListViewHeightBasedOnChildren(list);

//        list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mActivity = getActivity();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Do something for lollipop and above versions
        } else {

        }

        // do something for phones running an SDK before lollipop
        final View view = inflater.inflate(R.layout.activity_preference_screen, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        toolbar.setClickable(true);
//        toolbar.setNavigationIcon(getResIdFromAttribute(mActivity, R.attr.homeAsUpIndicator));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mActivity.onBackPressed();
            }
        });

//        String android_id = Settings.Secure.getString(mActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
//        String deviceId = md5(android_id).toUpperCase();

//        MobileAds.initialize(mActivity, "ca-app-pub-9267540560139197~1479571869");

//        mAdView = (AdView) view.findViewById(R.id.adView);
//        adRequest = new AdRequest.Builder()
////                .addTestDevice(deviceId)
//                // Check the LogCat to get your test device ID
////                .addTestDevice("6021A5518F29A748388FAF1AFC010C78")
//                .build();
//        mAdView.loadAd(adRequest);

        return view;

//        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        mEnableNotificationSpeaking.setChecked(FeatureManager.FeatureConfig.isAppNotificationSpeakingEnabled(mActivity.getApplicationContext()));

//        if (mAdView != null) {
//            mAdView.resume();
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_NOTIFICATION_CODE) {
            if (data != null) {
                boolean isEnabled = data.getBooleanExtra(APP_NOTIFICATION_ENABLED, false);
                mEnableNotificationSpeaking.setChecked(isEnabled);
            }
        }
    }

    @Override
    public void onPause() {
//        if (mAdView != null) {
//            mAdView.pause();
//        }
        super.onPause();
    }

    @Override
    public void onDestroy() {

//        if (mAdView != null) {
//            adRequest = null;
//            mAdView.removeAllViews();
//            mAdView.setAdListener(null);
//            mAdView.destroy();
//            mAdView = null;
//        }

        super.onDestroy();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        boolean switched = ((SwitchPreference) preference).isChecked();

        switch (preference.getKey()) {
            case KEY_ENABLE_SMS_SPEAKING:

                FeatureManager.FeatureConfig.enableSMSSpeaking(mActivity.getApplicationContext(), switched);
                break;
            case KEY_ENABLE_CALL_SPEAKING:

                FeatureManager.FeatureConfig.enableCallSpeaking(mActivity.getApplicationContext(), switched);
                break;
            case KEY_ENABLE_NOTIFICATION_SPEAKING:
                //FeatureManager.FeatureConfig.enableAppNotificationsSpeaking(mActivity, switched);

                Intent intent = new Intent(mActivity, AppNotificationConfig.class);
                startActivityForResult(intent, APP_NOTIFICATION_CODE);

                break;
            case KEY_ENABLE_CLIPBOARD_SPEAKING:

                FeatureManager.FeatureConfig.enableClipboardSpeaking(mActivity.getApplicationContext(), switched);
                break;
            case KEY_ENABLE_SHAKE_TO_TURNOFF_SPEAKING:

                FeatureManager.FeatureConfig.enableShakeToTurnOffSpeakingEnabled(mActivity.getApplicationContext(), switched);
                break;
        }
        return false;
    }
}