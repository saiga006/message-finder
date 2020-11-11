# message-finder
An app to trigger **notifications** when receiving **priority messages**(*determined by user keyword*) 

## Beta Release V2.0.0

### How to use:

  1. User inputs contact number(optional) and the keyword(required), for which SOS kind of notification will be raised.
  2. Press trigger button to save the config.
  3. So that the app uses it everytime, when a sms is received and validates it, even after reboot it works.
  4. User needs to accept the READ SMS permission and READ External Storage permission(for ringtone access) or else the app wouldn't work fine as expected.
  5. To stop the SOS app functionality, press the clear config button.

### Features:
  
  - Now, it supports only multiple keyword as input to identify the text (either in CSV or space seperated format).
  - Triggers notification on when the keyword is matched with received message with vibration, colored notification along with audio playback
  - On receiving notification, ringtone is played at high volume, even if the phone is in silent mode, uses alarm stream at full volume.
  - In some phones, it works in DND mode, as it uses alarm category for notification
  - OnBoarding Screen for FTI by User
  
##### Beta 2.5.0 Changelog:
  - Added support for app settings screen, user can now see their saved config after reboots and closing the app too.
  - Displays saved app configurations, app versions and added auto suggestion as an experimental feature
  - More preference is given to contact picker as it is less error prone and doesn't require read contacts permission
    from the user like the auto suggestion feature

#### Beta 2.1.0 Changelog:
  - Added support for contact picker feature, now user can pick their contacts directly from contact app, instead of typing manually
  - Refactored and redefined permission dialog flow for auto suggestion feature
  - Bug Fix:
    1. Temp crash fix for bitmap memory consumption in FTI page for low end devices.
    
#### Beta 2.0.0 Changelog:
  - Support for Android 10 
  - Onboarding Screens for first time users
  - Error Checking func. for contact input field
  - App Size reduction by half 28MB to 13MB
  - Bug Fixes
    1. Permission Dialog dont ask again option fix
    2. Bugs related to edit text fields


#### Beta 1.3.0 Changelog:
  - Support for Android 10 
  - Onboarding Screens for first time users
  - App Size reduction by half 28MB to 13MB
  - Bug Fixes
    1. Permission Dialog dont ask again option fix
    2. Bugs related to edit text fields


#### Beta 1.2.0 Changelog:  
 
  - App permissions can be configured easily with new request permissions design
  - Bug Fixes
    1. IME popup glitches, UI glitches, landscape mode glitches
    2. Fixed runtime Permission request
    3. Fixed the ringtone issue that was occuring in some phones
    4. Snackbar related bug fixes
    

### Compatibility: 

  - Currently expected to work on phones from Android M to Android Q.

### Code Verbosity Level: 

  - Full verbose comments in code is available

### App Screenshots:

  1. <img src="https://github.com/saiga006/message-finder/blob/master/App_Notification.png" width="150" height="300">
     
  > "Notification when priority message is received"
  
  2. <img src="https://github.com/saiga006/message-finder/blob/master/Activity_Screen.png" width="150" height="300">
  
  > "Activity Screen of Message Finder, where user inputs the contact number and keyword, for which SOS has to be raised"
  
  3. <img src="https://github.com/saiga006/message-finder/blob/master/Input_Field_ActivityScreen.png" width="150" height="300">
  
  > "Example user input"

#### Resources:
  - [Android Developer Guide - Notifications](https://developer.android.com/guide/topics/ui/notifiers/notifications)
  - [Android Developer Guide - MediaPlayer](https://developer.android.com/guide/topics/media/mediaplayer)
  - [Android Developer Guide - Services](https://developer.android.com/guide/components/services)
  - [Android Developer Guide - Foreground Services](https://developer.android.com/guide/components/services#Foreground)
  - [Android Developer Guide - Toast](https://developer.android.com/guide/topics/ui/notifiers/toasts)
  - [Media Player State Machine](https://developer.android.com/images/mediaplayer_state_diagram.gif)
  - [Styles and Themes Codepath Tutorial](https://guides.codepath.com/android/Styles-and-Themes)
  - For nitty gritty details on Styles and Themes - [Android Developer Guide](https://developer.android.com/guide/topics/ui/look-and-feel/themes)
  - For Receiving SMS in Android - [Tutorial](https://google-developer-training.github.io/android-developer-phone-sms-course/Lesson%202/2_p_2_sending_sms_messages.html)
  - Requesting Runtime Permissions in Android - [Android Developer Guide](https://developer.android.com/training/permissions/requesting#perm-check)
  - Shared preferences in Android - [Android Developer Guide](https://developer.android.com/training/data-storage/shared-preferences) 
  - [Vibration effect android tutorial](https://proandroiddev.com/using-vibrate-in-android-b0e3ef5d5e07)
  - Codepath Tutorial on [Drawable](https://guides.codepath.com/android/drawables)
  - Background Service Limitations [Android Oreo](https://developer.android.com/about/versions/oreo/background#services) and [Android Q](https://developer.android.com/guide/components/activities/background-starts)
  - Good tutorial on [custom notifications](https://medium.com/hootsuite-engineering/custom-notifications-for-android-ac10ca67a08c)
  - For [controlling volume and about fixed volumes devices](https://developer.android.com/guide/topics/media-apps/volume-and-earphones) in android
  - For app [logo design](https://www.freelogodesign.org/) and adding [app icons](https://medium.com/@ujikit/add-icons-to-the-android-application-from-android-studio-ide-cd1af9348749) and [notification icons](https://developer.android.com/studio/write/image-asset-studio?hl=de#create-notification)
  - Adding Splash Screen [Tutorial](https://android.jlelse.eu/launch-screen-in-android-the-right-way-aca7e8c31f52)
  - Integration [Material Design](https://github.com/material-components/material-components-android/blob/master/docs/getting-started.md)
    and customizing [Snackbar](https://material.io/develop/android/components/snackbars)
  - App Signing [Tutorial](https://youtu.be/akDXw9n3gFY)
  - Proguard and Apk Size Reduction [Writeup](https://medium.com/androiddevelopers/practical-proguard-rules-examples-5640a3907dc9)
  - Add Onboarding Screen [Tutorial](https://www.youtube.com/watch?v=byLKoPgB7yA)
  - Add Circular ImageView [Library](https://github.com/hdodenhof/CircleImageView)
  - Obviously StackOverflow for issues during development and bug fixing :-)
  
