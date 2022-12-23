package com.unitechstudio.voicenotification.core;

import com.unitechstudio.voicenotification.core.model.EventPack;
import com.unitechstudio.voicenotification.core.model.ResponseInfo;

/**
 * Created by sev_user on 4/4/2017.
 */

public interface EventDispatcher {
    public ResponseInfo handleEvent(EventPack event);
}
