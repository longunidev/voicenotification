package com.unitechstudio.voicenotification.core.model;

import java.util.ArrayList;

/**
 * Created by LongUni on 4/5/2017.
 */

public class TTSEventInfo extends BaseEventInfo {

    public enum TTSEventCommand {
        STARTING,
        SPEAK_OUT_ONCE,
        SPEAK_OUT_REPEATEDLY,
        PAUSING,
        TERMINATING
    }

    private TTSEventCommand mCommand;

    public TTSEventInfo(TTSEventCommand command) {
        this.mCommand = command;
    }

    public TTSEventInfo(TTSEventCommand command, SpeakoutMessage singleMsg) {
        super(singleMsg);
        this.mCommand = command;
    }

    public TTSEventInfo(TTSEventCommand command, ArrayList<SpeakoutMessage> messages) {
        super(messages);
        this.mCommand = command;
    }

    @Override
    public EventPack.EventType getEventType() {
        return EventPack.EventType.APP;
    }

    @Override
    public TTSEventCommand getCommand() {
        return mCommand;
    }

    public ArrayList<SpeakoutMessage> getSpeakoutMessage() {
        return (ArrayList<SpeakoutMessage>) mData;
    }

    public SpeakoutMessage getSingleMessage() {
        return (SpeakoutMessage) mData;
    }

}
