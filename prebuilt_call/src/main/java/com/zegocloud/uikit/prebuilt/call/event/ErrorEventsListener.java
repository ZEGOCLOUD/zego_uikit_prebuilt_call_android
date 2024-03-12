package com.zegocloud.uikit.prebuilt.call.event;

public interface ErrorEventsListener {

//    public static final int SUCCESS = 0;
    public static final int INIT_PARAM_ERROR = -1;
    public static final int INIT_ALREADY = -2;

    void onError(int errorCode, String message);
}
