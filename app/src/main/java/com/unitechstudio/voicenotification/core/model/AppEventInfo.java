package com.unitechstudio.voicenotification.core.model;

/**
 * Created by Long Uni on 4/4/2017.
 */

public class AppEventInfo extends BaseEventInfo {

    public enum AppEventCommand {
        ACTION_BOOT_COMPLETED,
        ACTION_SMS_RECEIVED,
        ACTION_PHONE_STATE,
        ACTION_NEW_NOTIFICATION_APPEAR,
        ACTION_CLIPBOARD_CHANGED,
        ACTION_SPEAK_AGAIN_MSG,
        ACTION_TEST_COMMAND
    }

    private AppEventCommand mCommand;

    public AppEventInfo(AppEventCommand command) {
        this.mCommand = command;
    }

    public AppEventInfo(AppEventCommand command, Object data) {
        super(data);
        this.mCommand = command;
    }

    @Override
    public EventPack.EventType getEventType() {
        return EventPack.EventType.APP;
    }

    @Override
    public AppEventCommand getCommand() {
        return mCommand;
    }
}
