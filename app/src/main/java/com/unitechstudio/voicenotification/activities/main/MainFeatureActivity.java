package com.unitechstudio.voicenotification.activities.main;

import static com.unitechstudio.voicenotification.activities.AppNotificationConfig.APP_NOTIFICATION_ENABLED;
import static com.unitechstudio.voicenotification.utils.CommonUtils.launchMarket;
import static com.unitechstudio.voicenotification.utils.PermissionUtils.checkAndRequestPermissions;
import static com.unitechstudio.voicenotification.utils.PermissionUtilsNew.showDialogGoSetting;
import static com.unitechstudio.voicenotification.utils.PermissionUtilsNew.showDialogGrantPermissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.kyleduo.switchbutton.SwitchButton;
import com.unitechstudio.voicenotification.R;
import com.unitechstudio.voicenotification.activities.AppNotificationConfig;
import com.unitechstudio.voicenotification.app.BaseApplication;
import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.SpeakoutMessage;
import com.unitechstudio.voicenotification.core.model.TTSEventInfo;
import com.unitechstudio.voicenotification.managers.FeatureManager;
import com.unitechstudio.voicenotification.managers.TTSSpeakManager;
import com.unitechstudio.voicenotification.utils.CommonUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainFeatureActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private final static String TAG = "MainFeature-LongUni";

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    public static final int APP_NOTIFICATION_CODE = 2;

    private Context mContext;

    private SwitchButton mEnableSMSSpeaking, mEnableCallSpeaking, mEnableNotificationSpeaking, mEnableClipboardSpeaking, mEnableShakeToTurnoffSpeaking;

    private LinearLayout speechRate, listenToExample, vote5Stars, privacyPolicy;

    private AdManagerAdView mAdManagerAdViewFooter, mAdManagerAdViewCenter;

    private EditText mEdtSpeakText;
    private Button mBtSpeakText;
    private Button mBtStopSpeaking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_feature);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setElevation(0);

        mContext = this;

        initValues();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            checkAndRequestPermissions((Activity) mContext);
        } else {
            // do something for phones running an SDK before lollipop
        }

        setUpCenterBannerAd();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    private void setUpFooterBannerAd() {
        mAdManagerAdViewFooter = findViewById(R.id.adManagerAdViewFooter);

        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        mAdManagerAdViewFooter.loadAd(adRequest);

        mAdManagerAdViewFooter.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        });
    }

    private void setUpCenterBannerAd() {
        mAdManagerAdViewCenter = findViewById(R.id.adManagerAdViewCenter);

        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        mAdManagerAdViewCenter.loadAd(adRequest);

        mAdManagerAdViewCenter.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        });
    }

    private void initValues() {
        mEnableSMSSpeaking = (SwitchButton) findViewById(R.id.mEnableSMSSpeaking);
//        mEnableSMSSpeaking.setOnCheckedChangeListener(null);
        mEnableSMSSpeaking.setChecked(FeatureManager.FeatureConfig.isSMSSpeakingEnabled(mContext.getApplicationContext()));
        mEnableSMSSpeaking.setOnCheckedChangeListener(this);

        mEnableCallSpeaking = (SwitchButton) findViewById(R.id.mEnableCallSpeaking);
//        mEnableCallSpeaking.setOnCheckedChangeListener(null);
        mEnableCallSpeaking.setChecked(FeatureManager.FeatureConfig.isCallSpeakingEnabled(mContext.getApplicationContext()));
        mEnableCallSpeaking.setOnCheckedChangeListener(this);

        mEnableNotificationSpeaking = (SwitchButton) findViewById(R.id.mEnableNotificationSpeaking);
        mEnableNotificationSpeaking.setOnCheckedChangeListener(null);
        mEnableNotificationSpeaking.setChecked(FeatureManager.FeatureConfig.isAppNotificationSpeakingEnabled(mContext.getApplicationContext()));
        mEnableNotificationSpeaking.setOnCheckedChangeListener(this);

        mEnableClipboardSpeaking = (SwitchButton) findViewById(R.id.mEnableClipboardSpeaking);
        mEnableClipboardSpeaking.setOnCheckedChangeListener(null);
        mEnableClipboardSpeaking.setChecked(FeatureManager.FeatureConfig.isClipboardSpeakingEnabled(mContext.getApplicationContext()));
        mEnableClipboardSpeaking.setOnCheckedChangeListener(this);

        mEnableShakeToTurnoffSpeaking = (SwitchButton) findViewById(R.id.mEnableShakeToTurnoffSpeaking);
//        mEnableShakeToTurnoffSpeaking.setOnCheckedChangeListener(null);
        mEnableShakeToTurnoffSpeaking.setChecked(FeatureManager.FeatureConfig.isShakeToTurnOffSpeakingEnabled(mContext.getApplicationContext()));
        mEnableShakeToTurnoffSpeaking.setOnCheckedChangeListener(this);

        speechRate = (LinearLayout) findViewById(R.id.speechRate);
        speechRate.setOnClickListener(this);

        listenToExample = (LinearLayout) findViewById(R.id.listenToExample);
        listenToExample.setOnClickListener(this);

        vote5Stars = (LinearLayout) findViewById(R.id.vote5Stars);
        vote5Stars.setOnClickListener(this);

        privacyPolicy = (LinearLayout) findViewById(R.id.privacyPolicy);
        privacyPolicy.setOnClickListener(this);

        mEdtSpeakText = findViewById(R.id.edtSpeakText);
        mEdtSpeakText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mEdtSpeakText.hasFocus()) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_SCROLL:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });
        mEdtSpeakText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (charSequence.length() == 0) {
                    mBtSpeakText.setEnabled(false);
                    mBtSpeakText.setAlpha((float) 0.6);
                } else {
                    mBtSpeakText.setEnabled(true);
                    mBtSpeakText.setAlpha(1);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mBtSpeakText = findViewById(R.id.btSpeakText);
        mBtSpeakText.setOnClickListener(this);
        if (mEdtSpeakText.getText().toString().length() == 0) {
            mBtSpeakText.setEnabled(false);
            mBtSpeakText.setAlpha((float) 0.6);
        } else {
            mBtSpeakText.setEnabled(true);
            mBtSpeakText.setAlpha(1);
        }

        mBtStopSpeaking = findViewById(R.id.btStopSpeaking);
        mBtStopSpeaking.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();

        mEnableNotificationSpeaking.setOnCheckedChangeListener(null);
        mEnableNotificationSpeaking.setChecked(FeatureManager.FeatureConfig.isAppNotificationSpeakingEnabled(mContext.getApplicationContext()));
        mEnableNotificationSpeaking.setOnCheckedChangeListener(this);

        if (mAdManagerAdViewCenter != null) {
            mAdManagerAdViewCenter.resume();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case APP_NOTIFICATION_CODE:
                if (data != null) {
                    boolean isEnabled = data.getBooleanExtra(APP_NOTIFICATION_ENABLED, false);
                    mEnableNotificationSpeaking.setOnCheckedChangeListener(null);
                    mEnableNotificationSpeaking.setChecked(isEnabled);
                    mEnableNotificationSpeaking.setOnCheckedChangeListener(this);
                }
                break;

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.RECEIVE_SMS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        perms.put(permissions[i], grantResults[i]);
                    }

                    // Check for both permissions
                    if (perms.get(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {

                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {

                            ArrayList<String> requestPermissions = new ArrayList<>();
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
                                requestPermissions.add(Manifest.permission.RECEIVE_SMS);
                            }
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                                requestPermissions.add(Manifest.permission.READ_PHONE_STATE);
                            }
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                                requestPermissions.add(Manifest.permission.READ_CONTACTS);
                            }
                            showDialogGrantPermissions(requestPermissions.toArray(new String[0]), (Activity) mContext).show();
                        } else {

                            ArrayList<String> pendingPermissions = new ArrayList<>();
                            if (perms.get(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                                pendingPermissions.add(Manifest.permission.RECEIVE_SMS);
                            }
                            if (perms.get(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                                pendingPermissions.add(Manifest.permission.READ_PHONE_STATE);
                            }
                            if (perms.get(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                                pendingPermissions.add(Manifest.permission.READ_CONTACTS);
                            }

                            showDialogGoSetting(pendingPermissions.toArray(new String[0]), (Activity) mContext, 1000).show();

                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onPause() {
        if (mAdManagerAdViewCenter != null) {
            mAdManagerAdViewCenter.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {

        if (mAdManagerAdViewCenter != null) {
            mAdManagerAdViewCenter.removeAllViews();
            mAdManagerAdViewCenter.setAdListener(null);
            mAdManagerAdViewCenter.destroy();
            mAdManagerAdViewCenter = null;
        }

        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.mEnableSMSSpeaking:

                FeatureManager.FeatureConfig.enableSMSSpeaking(mContext.getApplicationContext(), isChecked);
                break;
            case R.id.mEnableCallSpeaking:

                FeatureManager.FeatureConfig.enableCallSpeaking(mContext.getApplicationContext(), isChecked);
                break;
            case R.id.mEnableNotificationSpeaking:

                Intent intent = new Intent(mContext, AppNotificationConfig.class);
                startActivityForResult(intent, APP_NOTIFICATION_CODE);
                break;
            case R.id.mEnableClipboardSpeaking:

                FeatureManager.FeatureConfig.enableClipboardSpeaking(mContext.getApplicationContext(), isChecked);
                break;
            case R.id.mEnableShakeToTurnoffSpeaking:

                FeatureManager.FeatureConfig.enableShakeToTurnOffSpeakingEnabled(mContext.getApplicationContext(), isChecked);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.speechRate:

                ArrayList<Map.Entry<String, Integer>> ahihi = new ArrayList<>();
                ahihi.add(new AbstractMap.SimpleEntry<String, Integer>("Very slow", 60));
                ahihi.add(new AbstractMap.SimpleEntry<String, Integer>("Slow", 80));
                ahihi.add(new AbstractMap.SimpleEntry<String, Integer>("Normal", 100));
                ahihi.add(new AbstractMap.SimpleEntry<String, Integer>("Fast", 150));
                ahihi.add(new AbstractMap.SimpleEntry<String, Integer>("Slightly faster", 200));
                ahihi.add(new AbstractMap.SimpleEntry<String, Integer>("Much faster", 250));
                ahihi.add(new AbstractMap.SimpleEntry<String, Integer>("Very fast", 300));
                ahihi.add(new AbstractMap.SimpleEntry<String, Integer>("Extremely fast", 350));
                ahihi.add(new AbstractMap.SimpleEntry<String, Integer>("Fastest", 400));

                int checkedItem = 0;
                for (Map.Entry<String, Integer> e : ahihi) {

                    if (e.getValue() == TTSSpeakManager.getTTSRate(getApplicationContext())) {
                        break;
                    }
                    checkedItem++;
                }

                new SingleChoiceDialog(mContext, "Speech Rate", ahihi, new SingleChoiceDialog.IDialogClickListener() {
                    @Override
                    public void onItemSelected(int value) {
                        TTSSpeakManager.setTTSRate(getApplicationContext(), value);
                        Log.d(TAG, "TTSRate: " + value);
                    }
                }).setCheckedItem(checkedItem).create().show();

                break;
            case R.id.listenToExample:

                String text = "This is an example of speech synthesis in English";
                EventPack event = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, new SpeakoutMessage(SpeakoutMessage.Priority.MEDIUM, text).setLocale(Locale.ENGLISH)));
                BaseApplication.getInstance().postEvent(event);
                return;
            case R.id.vote5Stars:

                launchMarket(mContext);
                break;
            case R.id.privacyPolicy:

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://unitechofficial.github.io/voice_notification_privacy_policy.html"));
                startActivity(browserIntent);
                break;
            case R.id.btSpeakText:

                String text2 = mEdtSpeakText.getText().toString();
                EventPack event2 = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, new SpeakoutMessage(SpeakoutMessage.Priority.MEDIUM, text2).setLocale(CommonUtils.getUserLocale(getApplicationContext()))));
                BaseApplication.getInstance().postEvent(event2);
                break;
            case R.id.btStopSpeaking:

                EventPack stopEvent = new EventPack(EventPack.EventType.APP, new TTSEventInfo(TTSEventInfo.TTSEventCommand.SPEAK_OUT_ONCE, SpeakoutMessage.STOP_SPEAKING_MSG));
                BaseApplication.getInstance().postEvent(stopEvent);

                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class SingleChoiceDialog {
        private Context mContext;

        private String mTitle;
        private String[] mItems;
        private Integer[] mValues;
        private ArrayList<Map.Entry<String, Integer>> mList;
        private int checkedItem = 0;

        private AlertDialog.Builder builder;
        private AlertDialog dialog;

        private IDialogClickListener mListener;

        SingleChoiceDialog(Context context, String title, ArrayList list, IDialogClickListener listener) {
            mContext = context;
            mTitle = title;
            mList = list;
            mListener = listener;

            if (mList != null) {
                mItems = new String[mList.size()];
                mValues = new Integer[mList.size()];
                int i = 0;

                for (Map.Entry<String, Integer> e : mList) {
                    mItems[i] = e.getKey();
                    mValues[i] = e.getValue();
                    i++;
                }

            }

        }

        public SingleChoiceDialog create() {
            builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mTitle);
            builder.setSingleChoiceItems(mItems, checkedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mListener != null) {
                        mListener.onItemSelected(mValues[which]);
                    }
                }
            });

            builder.setNegativeButton("OK", null);

            return this;
        }

        public SingleChoiceDialog show() {
            if (dialog == null) {
                dialog = builder.create();
                dialog.show();
            }

            return this;
        }

        public SingleChoiceDialog setCheckedItem(int position) {

            checkedItem = position;

            return this;
        }

        interface IDialogClickListener {
            void onItemSelected(int value);
        }
    }
}
