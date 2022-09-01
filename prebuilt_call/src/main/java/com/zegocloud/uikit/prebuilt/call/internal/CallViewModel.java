package com.zegocloud.uikit.prebuilt.call.internal;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;

public class CallViewModel extends ViewModel {

    private MutableLiveData<ZegoUIKitPrebuiltCallConfig> configLiveData = new MutableLiveData<>();

    public MutableLiveData<ZegoUIKitPrebuiltCallConfig> getConfigLiveData() {
        return configLiveData;
    }
}