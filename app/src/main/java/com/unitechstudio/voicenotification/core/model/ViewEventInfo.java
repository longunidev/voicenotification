package com.unitechstudio.voicenotification.core.model;

/**
 * Created by Long Uni on 4/4/2017.
 */

public class ViewEventInfo extends BaseEventInfo {

    public enum ViewEventCommand {
        UPDATE_ACTIVITY
    }

    private ViewEventCommand mCommand;
    private Object mData;

    public ViewEventInfo(ViewEventCommand command) {
        this.mCommand = command;
    }

    public ViewEventInfo(ViewEventCommand command, Object data) {
        super(data);
        this.mCommand = command;
    }

    @Override
    public EventPack.EventType getEventType() {
        return EventPack.EventType.VIEW;
    }

    @Override
    public Object getCommand() {
        return null;
    }
}
