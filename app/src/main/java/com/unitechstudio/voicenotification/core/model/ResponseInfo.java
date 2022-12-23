package com.unitechstudio.voicenotification.core.model;

/**
 * Created by Long Uni on 4/4/2017.
 */

public class ResponseInfo {
    public enum Result {
        OK,
        FAIL
    }

    private Result mStatus;
    private Object mData;

    public ResponseInfo(){}

    public ResponseInfo(Result status, Object data) {
        setResult(status);
        setData(data);
    }

    public Result getResult() {
        return mStatus;
    }

    public void setResult(Result mStatus) {
        this.mStatus = mStatus;
    }

    public Object getData() {
        return mData;
    }

    public void setData(Object mData) {
        this.mData = mData;
    }
}
