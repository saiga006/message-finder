# message-finder
An app to trigger notifications when receiving priority messages(determined by user keyword) 

Beta Release

How to use:

1. User inputs contact number(optional) and the keyword(required), for which SOS kind of notification will be raised.
2. Press trigger button to save the config.
3. So that the app uses it everytime, when a sms is received and validates it, even after reboot it works.
4. User needs to accept the READ SMS permission or else the app wouldn't work fine as expected.
5. To stop the SOS app functionality, press the clear config button.

Features:
1. For now, it supports only one keyword as input to identify the text, future versions will include more keywords.
2. Triggers notification on when the keyword is matched with received message with vibration, colored notification along with audio playback
3. On receiving notification, ringtone is played at high volume, even if the phone is in silent mode, uses alarm stream at full volume.
4. In some phones, it works in DND mode, as it uses alarm category for notification

Compatibility: Currently expected to work on phones from Android N to Android P.

Code Verbosity Level: Full verbose comments in code is available
