package com.unitechstudio.voicenotification.core.model;

/**
 * Created by Long Uni on 4/4/2017.
 */

public class EventPack {
    public enum EventType {
        GENERAL,
        APP,
        VIEW
    }

    private EventType mType;

    private BaseEventInfo mEventInfo;

    public EventPack(EventType type) {
        this.mType = type;
    }

    public EventPack(EventType type, BaseEventInfo eventInfo) {
        this.mType = type;
        this.mEventInfo = eventInfo;
    }

    public BaseEventInfo getEventInfo() {
        return mEventInfo;
    }

    public void setEventInfo(BaseEventInfo mEventInfo) {
        this.mEventInfo = mEventInfo;
    }

    public EventType getEventType() {
        return mType;
    }

    public void setEventType(EventType mType) {
        this.mType = mType;
    }

}
