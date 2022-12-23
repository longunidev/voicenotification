package com.unitechstudio.voicenotification.utils.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

public class PkgIconLoader {
    private static final String TAG = PkgIconLoader.class.getSimpleName();
    private static final String THREAD_NAME = "IconLoaderThread";
    private HandlerThread mHandlerThread;
    private UiUpdateHandler mUiUpdateHandler;
    private IconLoaderHandler mIconLoaderHandler;

    private Context mAppContext;
    private LruCache<String, Drawable> mCache;

    private static class IconInfo {
        String pkgName;
        ImageView imageView;
    }

    @SuppressLint("HandlerLeak")
    private class UiUpdateHandler extends Handler {

        private UiUpdateHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            IconInfo iconInfo = (IconInfo) msg.obj;
            if (iconInfo != null && (iconInfo.pkgName.equals(iconInfo.imageView.getTag()))) {
                Drawable iconDrawable = mCache.get(iconInfo.pkgName);
                if (iconDrawable != null) {
                    iconInfo.imageView.setImageDrawable(iconDrawable);
                }
            }
        }
    }

    private class IconLoaderHandler extends Handler {
        private IconLoaderHandler(Looper l) {
            super(l);
        }

        @Override
        public void handleMessage(Message msg) {
            IconInfo iconInfo = (IconInfo) msg.obj;
            if (iconInfo != null) {
                try {
                    Drawable d = mAppContext.getPackageManager().getApplicationIcon(iconInfo.pkgName);
                    if (d != null) {
                        mCache.put(iconInfo.pkgName, d);
                        mUiUpdateHandler.sendMessageAtFrontOfQueue(mUiUpdateHandler.obtainMessage(0, iconInfo));
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "icon load error:" + e);
                }
            }
        }
    }

    public PkgIconLoader(Context appContext, final int CACHE_SIZE) {
        mAppContext = appContext;
        mCache = new LruCache<>(CACHE_SIZE);
    }

    public void startIconLoaderThread() {
        mHandlerThread = new HandlerThread(THREAD_NAME);
        mHandlerThread.start();
        Looper looper = mHandlerThread.getLooper();
        if (looper != null) {
            mIconLoaderHandler = new IconLoaderHandler(looper);
        }
        mUiUpdateHandler = new UiUpdateHandler();
    }

    public void stopIconLoaderThread() {
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
        }
    }

    public void loadIcon(String pkgName, ImageView imageView) {
        if (TextUtils.isEmpty(pkgName) || imageView == null) {
            return;
        }
        imageView.setTag(pkgName);
        Drawable iconDrawable = mCache.get(pkgName);
        if (iconDrawable != null) {
            imageView.setImageDrawable(iconDrawable);
        } else {
            IconInfo iconInfo = new IconInfo();
            iconInfo.pkgName = pkgName;
            iconInfo.imageView = imageView;
            mIconLoaderHandler
                    .sendMessageAtFrontOfQueue(mIconLoaderHandler.obtainMessage(0, iconInfo));
        }
    }

    public Drawable loadIcon(String pkgName) {
        Drawable drawable;
        if (TextUtils.isEmpty(pkgName)) {
            return null;
        }
        drawable = mCache.get(pkgName);
        if (drawable == null) {
            try {
                drawable = mAppContext.getPackageManager().getApplicationIcon(pkgName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        return drawable;
    }

}
