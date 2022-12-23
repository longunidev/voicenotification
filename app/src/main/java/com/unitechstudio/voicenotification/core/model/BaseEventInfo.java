package com.unitechstudio.voicenotification.core.model;

/**
 * Created by Long Uni on 4/4/2017.
 */

public abstract class BaseEventInfo {

    protected Object mData;

    public BaseEventInfo() {
    }

    public BaseEventInfo(Object data) {
        setData(data);
    }

    public Object getData() {
        return mData;
    }

    public void setData(Object data) {
        this.mData = data;
    }

    public abstract EventPack.EventType getEventType();
    public abstract Object getCommand();
}
