package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.text.TextUtils;

import com.zegocloud.uikit.internal.ZegoUIKitLanguage;

public class ZegoTranslationText {

    public String incomingVideoCallDialogTitle;
    public String incomingVideoCallDialogMessage;
    public String incomingVoiceCallDialogTitle;
    public String incomingVoiceCallDialogMessage;
    public String incomingGroupVideoCallDialogTitle;
    public String incomingGroupVideoCallDialogMessage;
    public String incomingGroupVoiceCallDialogTitle;
    public String incomingGroupVoiceCallDialogMessage;


    public String incomingVideoCallPageTitle;
    public String incomingVideoCallPageMessage;
    public String incomingVoiceCallPageTitle;
    public String incomingVoiceCallPageMessage;

    public String incomingGroupVideoCallPageTitle;
    public String incomingGroupVideoCallPageMessage;
    public String incomingGroupVoiceCallPageTitle;
    public String incomingGroupVoiceCallPageMessage;

    public String outgoingVideoCallPageTitle;
    public String outgoingVideoCallPageMessage;
    public String outgoingVoiceCallPageTitle;
    public String outgoingVoiceCallPageMessage;
    public String outgoingVoiceCallPageSmallMessage;

    public String outgoingGroupVideoCallPageTitle;
    public String outgoingGroupVideoCallPageMessage;
    public String outgoingGroupVoiceCallPageTitle;
    public String outgoingGroupVoiceCallPageMessage;

    public String incomingCallPageDeclineButton;
    public String incomingCallPageAcceptButton;

    public String sendCallButtonErrorOffLine;
    public String sendCallButtonError;


    private InvitationBaseText invatationBaseText = new InvitationTextEnglish();

    public ZegoTranslationText() {
        this(ZegoUIKitLanguage.ENGLISH);
    }
    public ZegoTranslationText(ZegoUIKitLanguage language) {
        if (language == ZegoUIKitLanguage.CHS) {
            invatationBaseText = new InvitationTextCHS();
        }
        incomingVideoCallDialogTitle = invatationBaseText.incomingVideoCallDialogTitle;
        incomingVideoCallDialogMessage = invatationBaseText.incomingVideoCallDialogMessage;
        incomingVoiceCallDialogTitle = invatationBaseText.incomingVoiceCallDialogTitle;
        incomingVoiceCallDialogMessage = invatationBaseText.incomingVoiceCallDialogMessage;
        incomingGroupVideoCallDialogTitle = invatationBaseText.incomingGroupVideoCallDialogTitle;
        incomingGroupVideoCallDialogMessage = invatationBaseText.incomingGroupVideoCallDialogMessage;
        incomingGroupVoiceCallDialogTitle = invatationBaseText.incomingGroupVoiceCallDialogTitle;
        incomingGroupVoiceCallDialogMessage = invatationBaseText.incomingGroupVoiceCallDialogMessage;
        incomingVideoCallPageTitle = invatationBaseText.incomingVideoCallPageTitle;
        incomingVideoCallPageMessage = invatationBaseText.incomingVideoCallPageMessage;
        incomingVoiceCallPageTitle = invatationBaseText.incomingVoiceCallPageTitle;
        incomingVoiceCallPageMessage = invatationBaseText.incomingVoiceCallPageMessage;
        incomingGroupVideoCallPageTitle = invatationBaseText.incomingGroupVideoCallPageTitle;
        incomingGroupVideoCallPageMessage = invatationBaseText.incomingGroupVideoCallPageMessage;
        incomingGroupVoiceCallPageTitle = invatationBaseText.incomingGroupVoiceCallPageTitle;
        incomingGroupVoiceCallPageMessage = invatationBaseText.incomingGroupVoiceCallPageMessage;
        outgoingVideoCallPageTitle = invatationBaseText.outgoingVideoCallPageTitle;
        outgoingVideoCallPageMessage = invatationBaseText.outgoingVideoCallPageMessage;
        outgoingVoiceCallPageTitle = invatationBaseText.outgoingVoiceCallPageTitle;
        outgoingVoiceCallPageMessage = invatationBaseText.outgoingVoiceCallPageMessage;
        outgoingVoiceCallPageSmallMessage = invatationBaseText.outgoingVoiceCallPageSmallMessage;
        outgoingGroupVideoCallPageTitle = invatationBaseText.outgoingGroupVideoCallPageTitle;
        outgoingGroupVideoCallPageMessage = invatationBaseText.outgoingGroupVideoCallPageMessage;
        outgoingGroupVoiceCallPageTitle = invatationBaseText.outgoingGroupVoiceCallPageTitle;
        outgoingGroupVoiceCallPageMessage = invatationBaseText.outgoingGroupVoiceCallPageMessage;
        incomingCallPageDeclineButton = invatationBaseText.incomingCallPageDeclineButton;
        incomingCallPageAcceptButton = invatationBaseText.incomingCallPageAcceptButton;

        sendCallButtonErrorOffLine = invatationBaseText.sendCallButtonErrorOffLine;
        sendCallButtonError = invatationBaseText.sendCallButtonError;
    }

    /**
     * if user not set custom translationText，then copy from innerText. Only for English.
     *
     * @param innerText
     */
    public void copyFromInnerTextIfNotCustomized(ZegoInnerText innerText) {
        if (innerText == null) {
            // innerText is null, no need to copy from
            return;
        }
        if (invatationBaseText instanceof InvitationTextEnglish) {
            InvitationTextEnglish english = new InvitationTextEnglish();
            if (english.incomingCallPageAcceptButton.equals(incomingCallPageAcceptButton)) { // not changed
                incomingCallPageAcceptButton = innerText.incomingCallPageAcceptButton;
            }
            if (english.incomingCallPageDeclineButton.equals(incomingCallPageDeclineButton)) { // not changed
                incomingCallPageDeclineButton = innerText.incomingCallPageDeclineButton;
            }
            if (english.incomingVideoCallDialogTitle.equals(incomingVideoCallDialogTitle)) { // not changed
                incomingVideoCallDialogTitle = innerText.incomingVideoCallDialogTitle;
            }
            if (english.incomingVideoCallDialogMessage.equals(incomingVideoCallDialogMessage)) { // not changed
                incomingVideoCallDialogMessage = innerText.incomingVideoCallDialogMessage;
            }
            if (english.incomingVoiceCallDialogTitle.equals(incomingVoiceCallDialogTitle)) { // not changed
                incomingVoiceCallDialogTitle = innerText.incomingVoiceCallDialogTitle;
            }
            if (english.incomingVoiceCallDialogMessage.equals(incomingVoiceCallDialogMessage)) { // not changed
                incomingVoiceCallDialogMessage = innerText.incomingVoiceCallDialogMessage;
            }
            if (english.incomingGroupVideoCallDialogTitle.equals(incomingGroupVideoCallDialogTitle)) { // not changed
                incomingGroupVideoCallDialogTitle = innerText.incomingGroupVideoCallDialogTitle;
            }
            if (english.incomingGroupVideoCallDialogMessage.equals(
                incomingGroupVideoCallDialogMessage)) { // not changed
                incomingGroupVideoCallDialogMessage = innerText.incomingGroupVideoCallDialogMessage;
            }
            if (english.incomingGroupVoiceCallDialogTitle.equals(incomingGroupVoiceCallDialogTitle)) {
                incomingGroupVoiceCallDialogTitle = innerText.incomingGroupVoiceCallDialogTitle;
            }
            if (english.incomingGroupVoiceCallDialogMessage.equals(incomingGroupVoiceCallDialogMessage)) {
                incomingGroupVoiceCallDialogMessage = innerText.incomingGroupVoiceCallDialogMessage;
            }

            if (english.incomingVideoCallPageTitle.equals(incomingVideoCallPageTitle)) {
                incomingVideoCallPageTitle = innerText.incomingVideoCallPageTitle;
            }
            if (english.incomingVideoCallPageMessage.equals(incomingVideoCallPageMessage)) {
                incomingVideoCallPageMessage = innerText.incomingVideoCallPageMessage;
            }
            if (english.incomingVoiceCallPageTitle.equals(incomingVoiceCallPageTitle)) {
                incomingVoiceCallPageTitle = innerText.incomingVoiceCallPageTitle;
            }
            if (english.incomingVoiceCallPageMessage.equals(incomingVoiceCallPageMessage)) {
                incomingVoiceCallPageMessage = innerText.incomingVoiceCallPageMessage;
            }

            if (english.incomingGroupVideoCallPageTitle.equals(incomingGroupVideoCallPageTitle)) {
                incomingGroupVideoCallPageTitle = innerText.incomingGroupVideoCallPageTitle;
            }
            if (english.incomingGroupVideoCallPageMessage.equals(incomingGroupVideoCallPageMessage)) {
                incomingGroupVideoCallPageMessage = innerText.incomingGroupVideoCallPageMessage;
            }
            if (english.incomingGroupVoiceCallPageTitle.equals(incomingGroupVoiceCallPageTitle)) {
                incomingGroupVoiceCallPageTitle = innerText.incomingGroupVoiceCallPageTitle;
            }
            if (english.incomingGroupVoiceCallPageMessage.equals(incomingGroupVoiceCallPageMessage)) {
                incomingGroupVoiceCallPageMessage = innerText.incomingGroupVoiceCallPageMessage;
            }

            if (english.outgoingVideoCallPageTitle.equals(outgoingVideoCallPageTitle)) {
                outgoingVideoCallPageTitle = innerText.outgoingVideoCallPageTitle;
            }
            if (english.outgoingVideoCallPageMessage.equals(outgoingVideoCallPageMessage)) {
                outgoingVideoCallPageMessage = innerText.outgoingVideoCallPageMessage;
            }
            if (english.outgoingVoiceCallPageTitle.equals(outgoingVoiceCallPageTitle)) {
                outgoingVoiceCallPageTitle = innerText.outgoingVoiceCallPageTitle;
            }
            if (english.outgoingVoiceCallPageMessage.equals(outgoingVoiceCallPageMessage)) {
                outgoingVoiceCallPageMessage = innerText.outgoingVoiceCallPageMessage;
            }
            if (TextUtils.equals(english.outgoingVoiceCallPageSmallMessage, outgoingVoiceCallPageSmallMessage)) {
                outgoingVoiceCallPageSmallMessage = innerText.outgoingVoiceCallPageSmallMessage;
            }

            if (english.outgoingGroupVideoCallPageTitle.equals(outgoingGroupVideoCallPageTitle)) {
                outgoingGroupVideoCallPageTitle = innerText.outgoingGroupVideoCallPageTitle;
            }
            if (english.outgoingGroupVideoCallPageMessage.equals(outgoingGroupVideoCallPageMessage)) {
                outgoingGroupVideoCallPageMessage = innerText.outgoingGroupVideoCallPageMessage;
            }
            if (english.outgoingGroupVoiceCallPageTitle.equals(outgoingGroupVoiceCallPageTitle)) {
                outgoingGroupVoiceCallPageTitle = innerText.outgoingGroupVoiceCallPageTitle;
            }
            if (english.outgoingGroupVoiceCallPageMessage.equals(outgoingGroupVoiceCallPageMessage)) {
                outgoingGroupVoiceCallPageMessage = innerText.outgoingGroupVoiceCallPageMessage;
            }
        }
    }

    public InvitationBaseText getInvitationBaseText() {
        return invatationBaseText;
    }
}
