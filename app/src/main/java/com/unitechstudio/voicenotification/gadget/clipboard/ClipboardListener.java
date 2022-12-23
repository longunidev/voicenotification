package com.unitechstudio.voicenotification.gadget.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by LongUni on 5/5/2017.
 */

public class ClipboardListener {

    private static final String TAG = ClipboardListener.class.getSimpleName();

    private static boolean isRegistered = false;
    private static OnClipboardChangeListener mListener;
    private static ClipboardListener mClipboardListener;

    private static ClipboardManager mClipboardManager;

    private static String mPreviousText = "";

    private ClipboardListener(OnClipboardChangeListener listener) {
        mListener = listener;
    }

    public static void registerPrimaryClipChanged(Context context, OnClipboardChangeListener listener) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null");
        }

        if (!isRegistered) {

            isRegistered = true;

            mClipboardListener = new ClipboardListener(listener);
            mClipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);

            Log.d(TAG, "registerPrimaryClipChanged");
        }

    }

    public static void unRegisterPrimaryClipChanged() {

        if (isRegistered) {
            if (mClipboardManager != null) {
                mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
                mClipboardListener = null;
            }

            mListener = null;
            isRegistered = false;
        }

    }

    public interface OnClipboardChangeListener {
        void onClipBoardChanged(String clipboardData);
    }

    private static ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {

            if (mClipboardManager != null) {
                ClipData clip = mClipboardManager.getPrimaryClip();
                if (clip != null) {
                    ClipData.Item item = clip.getItemAt(0);
                    if (item != null) {
                        if (item.getText() != null) {
                            String clipboardData = (String) item.getText().toString();

                            if (mPreviousText.equals(clipboardData)) {
                                return;
                            } else {
                                /// do something
                                mPreviousText = clipboardData;
                                mListener.onClipBoardChanged(clipboardData);
                                Log.i(TAG, "Text is copied to clipboard: " + clipboardData.length());
                            }
                        }
                    } else {
                        Log.i(TAG, "ClipData.Item clip.getItemAt(0) = null");
                    }
                } else {
                    Log.i(TAG, "ClipData getPrimaryClip = null");
                }

            }
        }
    };
}
