package com.unitechstudio.voicenotification.utils.image;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.unitechstudio.voicenotification.AppInfo;
import com.unitechstudio.voicenotification.activities.main.FeatureInfo;

import java.lang.ref.WeakReference;

import static com.unitechstudio.voicenotification.activities.ApplicationListAdapter.drawableToBitmap;

/**
 * Created by LongUni on 4/15/2017.
 */

public class ImageLoaderAsynTask extends AsyncTask<Object, Void, Bitmap> {

    //    private ViewHolder mViewHolder;
    private WeakReference<ImageView> mViewHolderWeakReference;
    private Context mContext;
    private PackageManager pm;

    public ImageLoaderAsynTask(Context context) {
        this.mContext = context;
        pm = mContext.getPackageManager();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(Object... params) {

        Bitmap bitmapResult = null;
        if (params != null && params.length > 0) {
            if (params[0] instanceof ImageView) {
//                mViewHolder = (ViewHolder) params[0];
                mViewHolderWeakReference = new WeakReference<ImageView>((ImageView) params[0]);
                // Load application icon
                if (params[1] instanceof AppInfo) {
                    AppInfo appInfo = (AppInfo) params[1];

                    Object appBitmap = Cache.getInstance().getLru().get(appInfo.getAppInfo().packageName);
                    if (appBitmap == null) {
                        Drawable drawableAppIcon = appInfo.getAppInfo().loadIcon(pm);
                        Bitmap bitmap = drawableToBitmap(drawableAppIcon);
                        bitmapResult = Bitmap.createScaledBitmap(bitmap, 70, 70, true);
                        Cache.getInstance().getLru().put(appInfo.getAppInfo().packageName, bitmapResult);
                    } else {
                        bitmapResult = (Bitmap) appBitmap;
                    }

                } else if (params[1] instanceof Integer) {
                    // Load image from drawable
                } else if (params[1] instanceof FeatureInfo) {
                    // Other input
                    FeatureInfo featureInfo = (FeatureInfo) params[1];
                    if (featureInfo != null) {
                        Object appBitmap = Cache.getInstance().getLru().get(featureInfo.getFeatureName());
                        if (appBitmap == null) {
                            Drawable drawableAppIcon = mContext.getResources().getDrawable(featureInfo.getResId());
                            Bitmap bitmap = drawableToBitmap(drawableAppIcon);
//                            appIconBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                            bitmapResult = bitmap;
                            Cache.getInstance().getLru().put(featureInfo.getFeatureName(), bitmapResult);
                            bitmapResult = bitmap;
                        } else {
                            bitmapResult = (Bitmap) appBitmap;
                        }
                    }
                }

            }
        }

        return bitmapResult;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (result != null && mViewHolderWeakReference != null) {
            final ImageView viewHolder = mViewHolderWeakReference.get();
            if (viewHolder != null) {
                viewHolder.setImageBitmap(result);
            }
//            mViewHolder.ivThumbnailIcon.setImageBitmap(result);
        }
    }
}
