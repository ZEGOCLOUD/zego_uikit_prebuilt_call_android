package com.zegocloud.uikit.prebuilt.call.invite.internal;

public class ZegoCallUser {

    private String id;
    private String name;

    public ZegoCallUser(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
