package com.unitechstudio.voicenotification.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.kyleduo.switchbutton.SwitchButton;
import com.unitechstudio.voicenotification.AppInfo;
import com.unitechstudio.voicenotification.R;
import com.unitechstudio.voicenotification.managers.FeatureManager;
import com.unitechstudio.voicenotification.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppNotificationConfig extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String APP_NOTIFICATION_ENABLED = "app_notification_speaking_enabled";

    private static Context mContext;

    private static SwitchButton mSwitchBtManageApps;

    private ListView mLvApplications;
    private ApplicationListAdapter mAppAdapter;
    private ArrayList<AppInfo> listApplications = new ArrayList<>();

//    private AdView mAdView;
//    private AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_notification_config);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setElevation(0);

        mContext = this;

        initViews();

        if (listApplications == null || listApplications.size() == 0) {
            new AsyncTask<Void, Void, Void>() {

                private ProgressDialog dialog;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    dialog = new ProgressDialog(mContext);
                    dialog.setMessage("Loading applications...");
                    dialog.show();

                }

                @Override
                protected Void doInBackground(Void... params) {

                    listApplications = getLauncherApps(mContext);
                    mAppAdapter = new ApplicationListAdapter(mContext, listApplications);

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    mLvApplications.setAdapter(mAppAdapter);
                    mAppAdapter.notifyDataSetChanged();
                    CommonUtils.setListViewHeightBasedOnChildren(mLvApplications);
                    dialog.dismiss();
                }
            }.execute();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

//        if (mAdView != null) {
//            mAdView.pause();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (mAdView != null) {
//            mAdView.resume();
//        }

        if (!TextUtils.isEmpty(Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners")) && Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
            //service is enabled do something

        } else {
            //service is not enabled try to enabled by calling...
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (mAdView != null) {
//            adRequest = null;
//            mAdView.removeAllViews();
//            mAdView.setAdListener(null);
//            mAdView.destroy();
//            mAdView = null;
//        }

        mContext = null;
        mSwitchBtManageApps = null;
        listApplications.clear();
        listApplications = null;
        mAppAdapter = null;
        System.gc();
    }

    private void initViews() {
        mSwitchBtManageApps = (SwitchButton) findViewById(R.id.switchBtManageApps);
        mSwitchBtManageApps.setChecked(FeatureManager.FeatureConfig.isAppNotificationSpeakingEnabled(mContext));
        mSwitchBtManageApps.setOnCheckedChangeListener(this);

        mLvApplications = (ListView) findViewById(R.id.lvApplications);
        mLvApplications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

//        mAdView = (AdView) findViewById(R.id.adView);
//        adRequest = new AdRequest.Builder()
////                .addTestDevice(deviceId)
//                // Check the LogCat to get your test device ID
////                .addTestDevice("6021A5518F29A748388FAF1AFC010C78")
//                .build();
//        mAdView.loadAd(adRequest);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.saveSelectedApps:
                ArrayList<String> listSelectedPkgs = getListSelectedApps();

                boolean isEnabled = (listSelectedPkgs != null && !listSelectedPkgs.isEmpty());
                FeatureManager.FeatureConfig.storeListAppsAllowedSpeakingNotifications(mContext, listSelectedPkgs);
                FeatureManager.FeatureConfig.enableAppNotificationsSpeaking(mContext, isEnabled);

                if (mSwitchBtManageApps != null) {
                    FeatureManager.FeatureConfig.enableAppNotificationsSpeaking(mContext, mSwitchBtManageApps.isChecked());
                }

                Intent data = new Intent();
                data.putExtra(APP_NOTIFICATION_ENABLED, isEnabled);
                setResult(-1, data);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_selector_menu, menu);

        MenuItem colorPicker = menu.findItem(R.id.saveSelectedApps);
        colorPicker.setIcon(R.drawable.ic_save);

        return true;
    }

    private ArrayList<String> getListSelectedApps() {

        ArrayList<String> listSelectedPackages = new ArrayList<>();

        ArrayList<AppInfo> listSeletectedApps = mAppAdapter.getSeletectedApps();
        if (listSeletectedApps != null && !listSeletectedApps.isEmpty()) {
            for (AppInfo app : listSeletectedApps) {
                listSelectedPackages.add(app.getAppInfo().packageName);
            }
        }

        return listSelectedPackages;
    }

    private ArrayList<AppInfo> getLauncherApps(Context context) {

        PackageManager packageManager = context.getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(packageManager));
        ArrayList<String> listSelectedPkgs = FeatureManager.FeatureConfig.getListAppsAllowedSpeakingNotifications(mContext);

        if (apps != null) {
            final int count = apps.size();

            ArrayList<AppInfo> listApps = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                try {
                    if (!apps.get(i).activityInfo.packageName.equals(mContext.getPackageName())) {
                        if (listSelectedPkgs != null && !listSelectedPkgs.isEmpty() && listSelectedPkgs.contains(apps.get(i).activityInfo.packageName)) {
                            listApps.add(new AppInfo(this.getPackageManager().getApplicationInfo(apps.get(i).activityInfo.packageName, 0)).setSelected(true));
                        } else {
                            listApps.add(new AppInfo(this.getPackageManager().getApplicationInfo(apps.get(i).activityInfo.packageName, 0)).setSelected(false));
                        }
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return listApps;
        }

        return null;
    }

    public static void setCheckedWithoutDoing(boolean isChecked) {
        mSwitchBtManageApps.setOnCheckedChangeListener(null);
        mSwitchBtManageApps.setChecked(isChecked);
        mSwitchBtManageApps.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) mContext);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        FeatureManager.FeatureConfig.enableAppNotificationsSpeaking(mContext, isChecked);
        mAppAdapter.selectAllApps(isChecked);
    }
}
