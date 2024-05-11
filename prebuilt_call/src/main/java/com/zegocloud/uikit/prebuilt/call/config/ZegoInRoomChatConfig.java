package com.zegocloud.uikit.prebuilt.call.config;

import com.zegocloud.uikit.components.chat.ZegoInRoomChatItemViewProvider;

public class ZegoInRoomChatConfig {

    public ZegoInRoomChatItemViewProvider inRoomChatItemViewProvider;

    /**
     * please use {@link com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallText#inRoomChatTitle} instead
     */
    @Deprecated
    public String title = "Chat";

    /**
     * please use {@link com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallText#inRoomChatInputHint} instead
     */
    @Deprecated
    public String inputHint = "Send a message to everyone";

}
