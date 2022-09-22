package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.os.Parcel;
import android.os.Parcelable;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.List;

public class CallInvitation implements Parcelable {

    public String roomID;
    public int type;
    public List<ZegoUIKitUser> invitees;
    public int timeout;
    public ZegoUIKitUser inviteUser;

    public CallInvitation(String roomID, int type, List<ZegoUIKitUser> invitees, int timeout,
        ZegoUIKitUser inviteUser) {
        this.roomID = roomID;
        this.type = type;
        this.invitees = invitees;
        this.timeout = timeout;
        this.inviteUser = inviteUser;
    }

    public static ZegoCallInvitationData convertToZegoCallInvitationData(
        CallInvitation zegoInvitation) {
        ZegoCallInvitationData invitationData = new ZegoCallInvitationData();
        invitationData.invitees = zegoInvitation.invitees;
        invitationData.inviter = zegoInvitation.inviteUser;
        invitationData.type = zegoInvitation.type;
        invitationData.callID = zegoInvitation.roomID;
        return invitationData;
    }

    public static CallInvitation getFromZegoCallInvitationData(ZegoCallInvitationData zegoInvitation) {
        CallInvitation invitationData = new CallInvitation(zegoInvitation.callID, zegoInvitation.type,
            zegoInvitation.invitees, 60, zegoInvitation.inviter);
        return invitationData;
    }


    protected CallInvitation(Parcel in) {
        roomID = in.readString();
        type = in.readInt();
        invitees = in.createTypedArrayList(ZegoUIKitUser.CREATOR);
        timeout = in.readInt();
        inviteUser = in.readParcelable(ZegoUIKitUser.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roomID);
        dest.writeInt(type);
        dest.writeTypedList(invitees);
        dest.writeInt(timeout);
        dest.writeParcelable(inviteUser, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CallInvitation> CREATOR = new Creator<CallInvitation>() {
        @Override
        public CallInvitation createFromParcel(Parcel in) {
            return new CallInvitation(in);
        }

        @Override
        public CallInvitation[] newArray(int size) {
            return new CallInvitation[size];
        }
    };
}
