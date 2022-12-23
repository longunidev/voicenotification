package com.unitechstudio.voicenotification.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import static android.speech.tts.TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED;

/**
 * Created by Long Uni on 4/5/2017.
 */

public class CommonUtils {
    public static void copyFolder(Context context, String name, File output) {
        // "Name" is the name of your folder!
        AssetManager assetManager = context.getAssets();
        String[] files = null;

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            // Checking file on assets subfolder
            try {
                files = assetManager.list(name);
            } catch (IOException e) {
                Log.e("ERROR", "Failed to get asset file list.", e);
            }
            // Analyzing all file on assets subfolder
            for (String filename : files) {

                InputStream in = null;
                OutputStream out = null;
                // First: checking if there is already a target folder
                String sandbox = "/data/data/" + context.getPackageName() + "/files";
                File folder = new File(sandbox + "/" + name);

                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdirs();
                }

                if (success) {
                    // Moving all the files on external SD
                    try {
                        in = assetManager.open(name + "/" + filename);
                        out = new FileOutputStream(sandbox + "/" + name + "/" + filename);
                        copyFile(in, out);

                    } catch (IOException e) {
                        Log.e("ERROR", "Failed to copy asset file: " + filename, e);
                    } finally {
                        // Edit 3 (after MMs comment)
                        try {
                            in.close();
                            in = null;
                            out.flush();
                            out.close();
                            out = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                } else {
                    // Do something else on failure
                }
            }
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            // is to know is we can neither read nor write
        }
    }

    // Method used by copyAssets() on purpose to copy a file.
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static void vibrate(Context context) {
        if (context == null) {
            return;
        }
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(300);
    }

    public static String getContactName(Context context, String number) {

        String name = null;

        // define the columns I want the query to return
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};

        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        // query time
        Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            } else {
            }
            cursor.close();
        }
        return name;
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    public static Drawable getDrawable(Context context, int resId) {
        if (context == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(resId, context.getTheme());
        } else {
            return context.getResources().getDrawable(resId);
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     *
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    public static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toUpperCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toUpperCase(Locale.US);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static Locale getUserLocale(Context context) {
//        String countryCode = getUserCountry(context);
//        if (!TextUtils.isEmpty(countryCode)) {
//            Log.d("LongUni", "countryCode: " + countryCode);
//            return new Locale(LANG_COUNTRY_MAP.get(countryCode));
//        }
//        return new Locale(LANG_COUNTRY_MAP.get(context.getResources().getConfiguration().locale.getCountry()));

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        return locale;
    }

    public static void launchMarket(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }

    private static boolean checkIsNetworkRequire(TextToSpeech tts, String language, String country) {
        if (tts == null || TextUtils.isEmpty(language) || TextUtils.isEmpty(country)) {
            return false;
        }
        Set<Voice> setVoice = tts.getVoices();
        for (Iterator<Voice> it = setVoice.iterator(); it.hasNext(); ) {
            Voice f = it.next();
            if (f.getLocale().getLanguage().equals(language) && f.getLocale().getCountry().equals(country)) {
                if (!f.isNetworkConnectionRequired()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkNotInstalledFlag(TextToSpeech tts, String language, String country) {
        if (tts == null || TextUtils.isEmpty(language) || TextUtils.isEmpty(country)) {
            return false;
        }
        Set<Voice> setVoice = tts.getVoices();
        for (Iterator<Voice> it = setVoice.iterator(); it.hasNext(); ) {
            Voice f = it.next();
            if (f.getLocale().getLanguage().equals(language) && f.getLocale().getCountry().equals(country)) {
                if (f.getFeatures().contains(new String(KEY_FEATURE_NOT_INSTALLED))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isVoiceDataLanguageInstalled(TextToSpeech tts, String language) {

        if (tts == null || TextUtils.isEmpty(language)) {
            return false;
        }

        for (Locale locale : LOCALE_MAP) {
            if (language.equals(locale.getLanguage())) {
                String country = locale.getCountry();
                boolean ret;
                if (checkNotInstalledFlag(tts, language, country)) {
                    ret = checkIsNetworkRequire(tts, language, country);
                    if (ret) {
                        Log.d("TAG", language + ": true");
                        return true;
                    }
                }
            }
        }
        Log.d("TAG", language + ": false");
        return false;
    }

    public static final ArrayList<Locale> LOCALE_MAP = new ArrayList<>();

    static {

        LOCALE_MAP.add(new Locale("ar", "AE"));
        LOCALE_MAP.add(new Locale("ar", "JO"));
        LOCALE_MAP.add(new Locale("ar", "SY"));
        LOCALE_MAP.add(new Locale("ar", "BH"));
        LOCALE_MAP.add(new Locale("ar", "SA"));
        LOCALE_MAP.add(new Locale("ar", "YE"));
        LOCALE_MAP.add(new Locale("ar", "EG"));
        LOCALE_MAP.add(new Locale("ar", "SD"));
        LOCALE_MAP.add(new Locale("ar", "TN"));
        LOCALE_MAP.add(new Locale("ar", "IQ"));
        LOCALE_MAP.add(new Locale("ar", "MA"));
        LOCALE_MAP.add(new Locale("ar", "QA"));
        LOCALE_MAP.add(new Locale("ar", "OM"));
        LOCALE_MAP.add(new Locale("ar", "KW"));
        LOCALE_MAP.add(new Locale("ar", "LY"));
        LOCALE_MAP.add(new Locale("ar", "DZ"));
        LOCALE_MAP.add(new Locale("ar", "LB"));
        LOCALE_MAP.add(new Locale("bg", "BG"));
        LOCALE_MAP.add(new Locale("cs", "CZ"));
        LOCALE_MAP.add(new Locale("da", "DK"));
        LOCALE_MAP.add(new Locale("de", "CH"));
        LOCALE_MAP.add(new Locale("de", "AT"));
        LOCALE_MAP.add(new Locale("de", "LU"));
        LOCALE_MAP.add(new Locale("de", "DE"));
        LOCALE_MAP.add(new Locale("de", "GR"));
        LOCALE_MAP.add(new Locale("el", "CY"));
        LOCALE_MAP.add(new Locale("el", "GR"));
        LOCALE_MAP.add(new Locale("en", "US"));
        LOCALE_MAP.add(new Locale("en", "SG"));
        LOCALE_MAP.add(new Locale("en", "MT"));
        LOCALE_MAP.add(new Locale("en", "PH"));
        LOCALE_MAP.add(new Locale("en", "NZ"));
        LOCALE_MAP.add(new Locale("en", "ZA"));
        LOCALE_MAP.add(new Locale("en", "AU"));
        LOCALE_MAP.add(new Locale("en", "IE"));
        LOCALE_MAP.add(new Locale("en", "CA"));
        LOCALE_MAP.add(new Locale("en", "IN"));
        LOCALE_MAP.add(new Locale("en", "GB"));
        LOCALE_MAP.add(new Locale("es", "PA"));
        LOCALE_MAP.add(new Locale("es", "VE"));
        LOCALE_MAP.add(new Locale("es", "PR"));
        LOCALE_MAP.add(new Locale("es", "BO"));
        LOCALE_MAP.add(new Locale("es", "AR"));
        LOCALE_MAP.add(new Locale("es", "SV"));
        LOCALE_MAP.add(new Locale("es", "ES"));
        LOCALE_MAP.add(new Locale("es", "CO"));
        LOCALE_MAP.add(new Locale("es", "PY"));
        LOCALE_MAP.add(new Locale("es", "EC"));
        LOCALE_MAP.add(new Locale("es", "US"));
        LOCALE_MAP.add(new Locale("es", "GT"));
        LOCALE_MAP.add(new Locale("es", "MX"));
        LOCALE_MAP.add(new Locale("es", "HN"));
        LOCALE_MAP.add(new Locale("es", "CL"));
        LOCALE_MAP.add(new Locale("es", "DO"));
        LOCALE_MAP.add(new Locale("es", "CU"));
        LOCALE_MAP.add(new Locale("es", "UY"));
        LOCALE_MAP.add(new Locale("es", "CR"));
        LOCALE_MAP.add(new Locale("es", "NI"));
        LOCALE_MAP.add(new Locale("es", "PE"));
        LOCALE_MAP.add(new Locale("et", "EE"));
        LOCALE_MAP.add(new Locale("fi", "FI"));
        LOCALE_MAP.add(new Locale("fr", "BE"));
        LOCALE_MAP.add(new Locale("fr", "CH"));
        LOCALE_MAP.add(new Locale("fr", "LU"));
        LOCALE_MAP.add(new Locale("fr", "FR"));
        LOCALE_MAP.add(new Locale("fr", "CA"));
        LOCALE_MAP.add(new Locale("hi", "IN"));
        LOCALE_MAP.add(new Locale("hr", "HR"));
        LOCALE_MAP.add(new Locale("hu", "HU"));
        LOCALE_MAP.add(new Locale("it", "CH"));
        LOCALE_MAP.add(new Locale("it", "IT"));
        LOCALE_MAP.add(new Locale("ja", "JP"));
        LOCALE_MAP.add(new Locale("ja", "JP"));
        LOCALE_MAP.add(new Locale("ko", "KR"));
        LOCALE_MAP.add(new Locale("lt", "LT"));
        LOCALE_MAP.add(new Locale("lv", "LV"));
        LOCALE_MAP.add(new Locale("mk", "MK"));
        LOCALE_MAP.add(new Locale("nl", "NL"));
        LOCALE_MAP.add(new Locale("nl", "BE"));
        LOCALE_MAP.add(new Locale("no", "NO"));
        LOCALE_MAP.add(new Locale("no", "NO"));
        LOCALE_MAP.add(new Locale("pl", "PL"));
        LOCALE_MAP.add(new Locale("pt", "BR"));
        LOCALE_MAP.add(new Locale("pt", "PT"));
        LOCALE_MAP.add(new Locale("ro", "RO"));
        LOCALE_MAP.add(new Locale("ru", "RU"));
        LOCALE_MAP.add(new Locale("sk", "SK"));
        LOCALE_MAP.add(new Locale("sl", "SI"));
        LOCALE_MAP.add(new Locale("sq", "AL"));
        LOCALE_MAP.add(new Locale("sv", "SE"));
        LOCALE_MAP.add(new Locale("th", "TH"));
        LOCALE_MAP.add(new Locale("th", "TH"));
        LOCALE_MAP.add(new Locale("tr", "TR"));
        LOCALE_MAP.add(new Locale("uk", "UA"));
        LOCALE_MAP.add(new Locale("vi", "VN"));
        LOCALE_MAP.add(new Locale("zh", "TW"));
        LOCALE_MAP.add(new Locale("zh", "HK"));
        LOCALE_MAP.add(new Locale("zh", "SG"));
        LOCALE_MAP.add(new Locale("zh", "CN"));
        LOCALE_MAP.add(new Locale("nb", "NO"));
    }

}
