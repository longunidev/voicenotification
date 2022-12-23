package com.unitechstudio.voicenotification.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unitechstudio.voicenotification.R;
import com.unitechstudio.voicenotification.data.SharedPreferenceManager;

import java.util.ArrayList;

import static com.unitechstudio.voicenotification.utils.PermissionUtils.checkAndRequestPermissions;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtilsNew {

    private static final String TAG = "PermissionUtils";

    public static class RequestCode {
        public static final int CONTACTS = 1;
        public static final int PHONE = 3;
        public static final int SMS = 5;
        public static final int ALL_PERMISSION = 7;
        public static final int PERMISSION_RUN_APP = 8;
    }

    private static final int PERMISSION_ALL = 1000;
    private static final int OTHER_PERMISSION = 1001;

    private static ArrayList<String> getPermissionByRequestCode(int requestCode) {
        ArrayList<String> permission = new ArrayList<>();
        switch (requestCode) {

            case RequestCode.PHONE:
                permission.add(Manifest.permission.READ_PHONE_STATE);
                break;
            case RequestCode.SMS:
                permission.add(Manifest.permission.SEND_SMS);
                permission.add(Manifest.permission.RECEIVE_SMS);
                break;
            case RequestCode.CONTACTS:
                permission.add(Manifest.permission.GET_ACCOUNTS);
                permission.add(Manifest.permission.READ_CONTACTS);
                break;
            default:
                break;
        }

        return permission;
    }

    static AlertDialog mDialogSetting;

    public static boolean checkPermission(Activity activity, int requestCodePermission) {
        if (activity == null) {
            Log.e(TAG, "checkPermission activity is null");
            return false;
        }
        if (mDialogSetting != null) {
            if (mDialogSetting.isShowing()) {
                mDialogSetting.cancel();
            }
        }
        ArrayList<Integer> codeList = new ArrayList<>();
        ArrayList<Integer> requestCodes = new ArrayList<>();
        ArrayList<String> requestPermission = new ArrayList<>();
        boolean needAppDialog = false;

        // add critical permission for run application
        if (RequestCode.ALL_PERMISSION == requestCodePermission) {
            codeList.add(RequestCode.CONTACTS);
            codeList.add(RequestCode.SMS);
            codeList.add(RequestCode.PHONE);
        } else if (RequestCode.PERMISSION_RUN_APP == requestCodePermission) {
            codeList.add(RequestCode.CONTACTS);
            codeList.add(RequestCode.SMS);
            codeList.add(RequestCode.PHONE);
        } else {
            codeList.add(requestCodePermission);
        }
        for (Integer requestCode : codeList) {
            ArrayList<String> permissions = getPermissionByRequestCode(requestCode);

            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "checkSelfPermission permission : " + permission + " ret : " + "PERMISSION_DENIED");

                    if (requestPermission.contains(permission)) {
                        continue;
                    }

                    requestPermission.add(permission);
                    requestCodes.add(requestCode);
                    if (!needAppDialog) {
                        needAppDialog = !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
                    }
                }
            }
        }

        if (requestPermission.isEmpty()) {
            Log.v(TAG, "checkPermission requested permission size is zero ");
            return true;
        } else {
            if (requestCodePermission == RequestCode.ALL_PERMISSION || requestCodePermission == RequestCode.PERMISSION_RUN_APP) {
                boolean isFirstEnter = SharedPreferenceManager.getInstance(activity).getBooleanAllPermission();
                Log.i(TAG, "checkPermission - needAppDialog : " + needAppDialog + " isFirstEnter : " + isFirstEnter);
                if (needAppDialog && !isFirstEnter) {
                    mDialogSetting = showDialogGoSetting(requestPermission.toArray(new String[requestPermission.size()]), activity, PERMISSION_ALL);
                    mDialogSetting.setCanceledOnTouchOutside(false);
                    mDialogSetting.show();
                } else {
                    ActivityCompat.requestPermissions(activity, requestPermission.toArray(new String[requestPermission.size()]), requestCodePermission);
                }
                return false;
            } else {
                Log.i(TAG, "checkPermission - needAppDialog : " + needAppDialog);
                if (needAppDialog) {
                    mDialogSetting = showDialogGoSetting(requestPermission.toArray(new String[requestPermission.size()]), activity, OTHER_PERMISSION);
                    mDialogSetting.setCanceledOnTouchOutside(false);
                    mDialogSetting.show();
                } else {
                    ActivityCompat.requestPermissions(activity, requestPermission.toArray(new String[requestPermission.size()]), requestCodePermission);
                }
                return false;
            }
        }
    }

    public static View initView(View rootView, Context context, String[] permissions, String body) {

        TextView mainText = (TextView) rootView.findViewById(R.id.dialog_main_text);
//        String body = context.getString(R.string.runtime_permission_go_settings_msg);
        Spanned functionBold = Html.fromHtml(body);
        mainText.setText(functionBold);

        LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.permission_dialog);
        ArrayList<String> permLabels = new ArrayList<>();
        for (String permission : permissions) {
            View permissionItemView = getPermissionItemView(permission, context);
            if (permissionItemView != null) {
                String permLabel = ((TextView) permissionItemView
                        .findViewById(R.id.permission_name)).getText().toString();
                if (!permLabels.contains(permLabel)) {
                    permLabels.add(permLabel);
                    ll.addView(permissionItemView);
                }
            }
        }
        return rootView;
    }

    @SuppressLint("NewApi")
    public static View getPermissionItemView(String permission, Context context) {
        View permissionItemView = LayoutInflater.from(context).inflate(
                R.layout.permission_dialog_list_item, new LinearLayout(context));
        TextView nameView = (TextView) permissionItemView.findViewById(R.id.permission_name);
        ImageView iconView = (ImageView) permissionItemView.findViewById(R.id.permission_icon);

        String label;
        Drawable icon = null;
        try {
            PackageManager pm = context.getPackageManager();
            if (pm == null) {
                return null;
            }
            PermissionInfo pi = pm.getPermissionInfo(permission, PackageManager.GET_PERMISSIONS);
            PermissionGroupInfo pgi = pm.getPermissionGroupInfo(pi.group, PackageManager.GET_PERMISSIONS);
            label = context.getResources().getString(pgi.labelRes);
            if (pgi.icon != 0) {
                icon = pgi.loadIcon(pm);
                icon.setTint(context.getColor(R.color.dialog_permission_item_icon));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        permissionItemView.setContentDescription(label);
        nameView.setText(label);
        iconView.setImageDrawable(icon);
        return permissionItemView;
    }

    public static AlertDialog showDialogGoSetting(String[] permissions, final Activity activity, final int type) {
        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        View rootView = LayoutInflater.from(activity).inflate(R.layout.permission_dialog, null);
        b.setView(rootView);
        b.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    // Open the specific App Info page:
                    Intent intent = new Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                    activity.finish();
                } catch (ActivityNotFoundException e) {
                    // e.printStackTrace();
                    // Open the generic Apps page:
                    Intent intent = new Intent(
                            android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                    activity.finish();
                }
            }
        });
        b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (type == PERMISSION_ALL) {
                    activity.finish();
                } else {
                    dialog.cancel();
                }
            }
        });
        b.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (type == PERMISSION_ALL) {
                        activity.finish();
                        dialog.cancel();
                    } else {
                        dialog.cancel();
                    }
                    return true;
                }
                return false;
            }
        });

        AlertDialog dialog = b.create();
        dialog.setView(PermissionUtilsNew.initView(rootView, activity, permissions, activity.getString(R.string.runtime_permission_go_settings_msg)));
        return dialog;
    }

    public static AlertDialog showDialogGrantPermissions(String[] permissions, final Activity activity) {
        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        View rootView = LayoutInflater.from(activity).inflate(R.layout.permission_dialog, null);
        b.setView(rootView);
        b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                checkAndRequestPermissions(activity);
            }
        });
        b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                dialog.cancel();
            }
        });

        AlertDialog dialog = b.create();
        dialog.setView(PermissionUtilsNew.initView(rootView, activity, permissions, activity.getString(R.string.runtime_permission_request_msg)));
        return dialog;
    }
}
