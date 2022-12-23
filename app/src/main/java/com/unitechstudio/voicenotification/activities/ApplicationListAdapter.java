package com.unitechstudio.voicenotification.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.unitechstudio.voicenotification.AppInfo;
import com.unitechstudio.voicenotification.R;
import com.unitechstudio.voicenotification.utils.image.ImageLoaderAsynTask;

import java.util.ArrayList;

public class ApplicationListAdapter extends ArrayAdapter<AppInfo> {

    private Context mContext;
    private PackageManager pm;
    private ArrayList<AppInfo> mListApplications = new ArrayList<>();
    private ArrayList<Boolean> mSeletectedApps;

    public ApplicationListAdapter(Context context, ArrayList<AppInfo> applicationList) {
        super(context, 0, applicationList);
        this.mContext = context;
        this.mListApplications = applicationList;
        pm = mContext.getPackageManager();

    }

    public class ViewHolder {
        public ImageView ivThumbnailIcon;
        public TextView tvTitle;
        public SwitchButton switchBtEnable;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {

            convertView = inflator.inflate(R.layout.app_row_layout, null);

            viewHolder = new ViewHolder();
            viewHolder.ivThumbnailIcon = (ImageView) convertView.findViewById(R.id.ivThumbnailIcon);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            viewHolder.switchBtEnable = (SwitchButton) convertView.findViewById(R.id.switchBtEnable);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AppInfo appInfo = mListApplications.get(position);
        if (appInfo != null) {

            new ImageLoaderAsynTask(mContext).execute(viewHolder.ivThumbnailIcon, appInfo);
            viewHolder.tvTitle.setText(pm.getApplicationLabel(appInfo.getAppInfo()).toString());
            viewHolder.switchBtEnable.setCheckedImmediately(appInfo.isSelected());
        }
        viewHolder.switchBtEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                appInfo.setSelected(isChecked);
                if (!isThereAnySelectedApp()) {
                    AppNotificationConfig.setCheckedWithoutDoing(false);
                } else {
                    AppNotificationConfig.setCheckedWithoutDoing(true);
                }
            }
        });

        return convertView;
    }

    @Override
    public int getCount() {
        return mListApplications != null ? mListApplications.size() : 0;
    }

    @Override
    public AppInfo getItem(int position) {
        if (mListApplications != null && mListApplications.size() > 0) {
            return mListApplications.get(position);
        }
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public ArrayList<AppInfo> getSeletectedApps() {
        ArrayList<AppInfo> listSelecteted = new ArrayList<>();

        for (int i = 0; i < mListApplications.size(); i++) {
            if (mListApplications.get(i).isSelected()) {
                listSelecteted.add(mListApplications.get(i));
            }
        }
        return listSelecteted;
    }

    public void selectAllApps(boolean isSelectAll) {
        if (mListApplications != null && !mListApplications.isEmpty()) {
            for (AppInfo app : mListApplications) {
                app.setSelected(isSelectAll);
            }
        }
        notifyDataSetChanged();
    }

    private boolean isThereAnySelectedApp() {

        if (mListApplications != null && !mListApplications.isEmpty()) {
            for (AppInfo app : mListApplications) {
                if (app.isSelected()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
