package com.unitechstudio.voicenotification.core;

import java.util.concurrent.Callable;

/**
 * Created by LongUni on 4/10/2017.
 */

public interface IBackgroundExecutor {
    void pushTask(Callable task);

    void executeTask(Callable task);

    void executeAllPendingTasks();
}
