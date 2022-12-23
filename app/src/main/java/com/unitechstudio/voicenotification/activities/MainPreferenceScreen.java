package com.unitechstudio.voicenotification.activities;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.unitechstudio.voicenotification.data.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.unitechstudio.voicenotification.managers.FeatureManager.FeatureConfig.enableCallSpeaking;
import static com.unitechstudio.voicenotification.managers.FeatureManager.FeatureConfig.enableClipboardSpeaking;
import static com.unitechstudio.voicenotification.managers.FeatureManager.FeatureConfig.enableSMSSpeaking;
import static com.unitechstudio.voicenotification.managers.FeatureManager.FeatureConfig.enableShakeToTurnOffSpeakingEnabled;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainPreferenceScreen extends PreferenceActivity {

    private static final int CHECK_TTS_AVAILABILITY = 100;

    private static final String TAG = MainPreferenceScreen.class.getSimpleName();

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = this;

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            try {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setElevation(0);
            } catch (NoSuchMethodError e) {
                e.printStackTrace();
            }

        }

        if (!SharedPreferenceManager.getInstance(mContext.getApplicationContext()).getBoolean(SharedPreferenceManager.FIRST_INSTALLATION)) {
            SharedPreferenceManager.getInstance(mContext.getApplicationContext()).putBoolean(SharedPreferenceManager.FIRST_INSTALLATION, true);

            enableSMSSpeaking(mContext.getApplicationContext(), true);
            enableCallSpeaking(mContext.getApplicationContext(), true);
            enableClipboardSpeaking(mContext.getApplicationContext(), true);
            enableShakeToTurnOffSpeakingEnabled(mContext.getApplicationContext(), true);
        }

        getFragmentManager().beginTransaction().replace(android.R.id.content, new FeaturePreferenceFragment()).commit();

        Log.d("MainPreferenceScreen", "main thread: " + Thread.currentThread().getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            checkAndRequestPermissions();
        } else {
            // do something for phones running an SDK before lollipop
        }

        Intent intent = new Intent();
        intent.setAction("android.speech.tts.engine.CHECK_TTS_DATA");
        startActivityForResult(intent, CHECK_TTS_AVAILABILITY);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.RECEIVE_SMS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "RECEIVE_SMS & READ_PHONE_STATE & READ_CONTACTS services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showDialogOK("Receive SMS, read Phone state and read Contact services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
                break;
            }
            case CHECK_TTS_AVAILABILITY:

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

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private boolean checkAndRequestPermissions() {
        int permissionReceiveSMS = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS);
        int readPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        int readContactPermisison = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (readPhonePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (permissionReceiveSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
        }

        if (readContactPermisison != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
}
