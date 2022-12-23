package com.unitechstudio.voicenotification.core;

import android.content.Context;

import com.unitechstudio.voicenotification.app.BaseApplication;
import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.EventPoster;
import com.unitechstudio.voicenotification.core.model.ResponseInfo;

/**
 * Created by Long Uni on 4/4/2017.
 */

public abstract class BaseManager implements EventPoster {

    protected Context mContext;

    protected EventDispatcher mEventDispatcher;

    public BaseManager(Context context, EventDispatcher eventDispatcher) {
        this.mContext = context;
        this.mEventDispatcher = eventDispatcher;
    }

    @Override
    public ResponseInfo postEvent(EventPack event) {

        if (mEventDispatcher != null) {
            return mEventDispatcher.handleEvent(event);
        }

        return null;
    }

    protected Context getContext() {
        return mContext;
    }

    public BaseApplication getBaseApplication() {
        return (BaseApplication) mContext.getApplicationContext();
    }

    public abstract ResponseInfo onEventHandling(EventPack event);
}