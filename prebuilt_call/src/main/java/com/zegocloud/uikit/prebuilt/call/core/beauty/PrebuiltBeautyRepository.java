package com.zegocloud.uikit.prebuilt.call.core.beauty;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.internal.ZegoUIKitLanguage;
import com.zegocloud.uikit.plugin.adapter.plugins.beauty.ZegoBeautyPluginEffectsType;
import com.zegocloud.uikit.plugin.adapter.plugins.beauty.ZegoBeautyPluginInnerTextCHS;
import com.zegocloud.uikit.plugin.adapter.plugins.beauty.ZegoBeautyPluginInnerTextEnglish;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;

public class PrebuiltBeautyRepository {

    public void init(Application application, long appID, String appSign) {
        ZegoUIKit.getBeautyPlugin().init(application, appID, appSign);
    }

    public void updateLanguageSettingsAndApply(ZegoUIKitPrebuiltCallConfig callConfig) {
        if (callConfig.zegoCallText != null) {
            ZegoUIKitLanguage language = callConfig.zegoCallText.getLanguage();
            if (language == ZegoUIKitLanguage.CHS) {
                callConfig.beautyConfig.innerText = new ZegoBeautyPluginInnerTextCHS();
            } else {
                callConfig.beautyConfig.innerText = new ZegoBeautyPluginInnerTextEnglish();
            }
        }
        ZegoUIKit.getBeautyPlugin().setZegoBeautyPluginConfig(callConfig.beautyConfig);
    }

    public void resetBeautyValueToDefault(ZegoBeautyPluginEffectsType beautyType) {
        ZegoUIKit.getBeautyPlugin().resetBeautyValueToDefault(beautyType);
    }

    public Dialog getBeautyDialog(Context context) {
        return ZegoUIKit.getBeautyPlugin().getBeautyDialog(context);
    }

    public boolean isPluginExited() {
        return ZegoUIKit.getBeautyPlugin().isPluginExited();
    }
}
