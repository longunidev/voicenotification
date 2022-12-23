package com.unitechstudio.voicenotification.core.model;

/**
 * Created by sev_user on 4/4/2017.
 */

public interface EventPoster {
    public ResponseInfo postEvent(EventPack event);
}
