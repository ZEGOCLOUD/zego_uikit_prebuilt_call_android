package com.zegocloud.uikit.prebuilt.call.core.push;

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

    @Override
    public String toString() {
        return "ZIMPushMessage{" +
            "invitationID='" + invitationID + '\'' +
            ", title='" + title + '\'' +
            ", body='" + body + '\'' +
            ", payLoad='" + payLoad + '\'' +
            '}';
    }
}
