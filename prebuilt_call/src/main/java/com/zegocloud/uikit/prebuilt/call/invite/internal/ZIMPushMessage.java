package com.zegocloud.uikit.prebuilt.call.invite.internal;

public class ZIMPushMessage {

    public String invitationID;
    public String title;
    public String body;
    public String payLoad;

    public ZIMPushMessage(String invitationID, String title, String body, String payLoad) {
        this.invitationID = invitationID;
        this.title = title;
        this.body = body;
        this.payLoad = payLoad;
    }
}
