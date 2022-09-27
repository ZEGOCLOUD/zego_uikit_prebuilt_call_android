# Overview

- - -

**Call Kit** is a prebuilt feature-rich call component, which enables you to build **one-on-one and group voice/video calls** into your app with only a few lines of code.

And it includes the business logic with the UI, you can add or remove features accordingly by customizing UI components.


|One-on-one call|Group call|
|---|---|
|![One-on-one call](https://storage.zego.im/sdk-doc/Pics/ZegoUIKit/Flutter/_all_close.gif)|![Group call](https://storage.zego.im/sdk-doc/Pics/ZegoUIKit/conference/8C_little.png)|


## When do you need the Call Kit

- Build apps faster and easier
  - When you want to prototype 1-on-1 or group voice/video calls **ASAP** 

  - Consider **speed or efficiency** as the first priority

  - Call Kit allows you to integrate **in minutes**

- Customize UI and features as needed
  - When you want to customize in-call features **based on actual business needs**

  - **Less time wasted** developing basic features

  - Call Kit includes the business logic along with the UI, allows you to **customize features accordingly**


## Embedded features

- Ready-to-use one-on-one/group calls
- Customizable UI styles
- Real-time sound waves display
- Device management
- Switch views during a one-on-one call
- Extendable top/bottom menu bar
- Participant list

# Quick start

- - -


## Integrate the SDK

### Add ZegoUIKitPrebuiltCall as dependencies

1. Add the `jitpack` configuration.
- If your `gradle` version is later than **6.8**, modify your `settings.gradle` file like this:
``` groovy
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      maven { url 'https://www.jitpack.io' } // <- Add this line.
   }
}
```
- If not, modify your project-level `build.gradle` file instead:
```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }  // <- Add this line.
    }
}
```

2. Modify your app-level `build.gradle` file:
```groovy
dependencies {
    ...
    implementation 'com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:1.0.2'    // add this line in your module-level build.gradle file's dependencies, usually named [app].
}
```  

### Using the ZegoUIKitPrebuiltCallFragment in your project

- Go to [ZEGOCLOUD Admin Console\|_blank](https://console.zegocloud.com/), get the `appID` and `appSign` of your project.
- Get the `userID` and `userName` for connecting the Video Call Kit service. 
- And also get a `callID` for making a call.

<div class="mk-hint">

- `userID` and `callID` can only contain numbers, letters, and underlines (_). 
- Users that join the call with the same `callID` can talk to each other. 
</div>

<pre style="background-color: #011627; border-radius: 8px; padding: 25px; color: white"><div>
public class CallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        addCallFragment();
    }

    public void addCallFragment() {
        long appID = yourAppID;
        String appSign = yourAppSign;

        String callID = yourCallID;
        String userID = yourUserID;
        String userName = yourUserName;

        ZegoUIKitPrebuiltCallConfig config = new ZegoUIKitPrebuiltCallConfig();
        <div style="background-color:#032A4B; margin: 0px; padding: 2px;">
        ZegoUIKitPrebuiltCallFragment fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            appID, appSign, callID, userID, userName,config);
        </div>
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitNow();
    }
}
</div></pre>

Now, you can make a new call by starting your `CallActivity`.


## Run & Test

Now you have finished all the steps!

You can simply click the **Run** on Android Studio to run and test your App on your device.


## Recommended resources

[Custom prebuilt UI](https://docs.zegocloud.com/article/14766)

[Complete Sample Code](https://github.com/ZEGOCLOUD/zego_uikit_prebuilt_call_example_android)

[About Us](https://www.zegocloud.com)

If you have any questions regarding bugs and feature requests, visit the [ZEGOCLOUD community](https://discord.gg/EtNRATttyp) or email us at global_support@zegocloud.com.

