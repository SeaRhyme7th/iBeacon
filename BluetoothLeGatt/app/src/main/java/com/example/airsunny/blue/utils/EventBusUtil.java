package com.example.airsunny.blue.utils;



import com.example.airsunny.blue.entity.DataEvent;

import org.greenrobot.eventbus.EventBus;

public class EventBusUtil {

    public static void sendEvent(String eventCode){
        sendEvent(eventCode, true, null);
    }

    public static void sendEvent(String eventCode, Object result){
        sendEvent(eventCode, true, result);
    }

    public static void sendErrEvent(String eventCode, String errMsg){
        DataEvent event = new DataEvent();
        event.setEventCode(eventCode);
        event.setIsSuccess(false);
        event.setErrMessage(errMsg);
        EventBus.getDefault().post(event);
    }

    public static void sendErrEvent(String eventCode, String errMsg, String errorCode){
        DataEvent event = new DataEvent();
        event.setEventCode(eventCode);
        event.setIsSuccess(false);
        event.setErrMessage(errMsg);
        event.setErrorCode(errorCode);
        EventBus.getDefault().post(event);
    }

    public static void sendEvent(String eventCode, boolean isSuccess, Object result){
        DataEvent event = new DataEvent();
        event.setEventCode(eventCode);
        event.setIsSuccess(isSuccess);
        event.setResult(result);
        EventBus.getDefault().post(event);
    }

}