package com.saiga.find.messagefinder;
// to declare broadcast receiver
import android.content.BroadcastReceiver;
import android.content.Context;
// to retrieve the broadcast intent
import android.content.Intent;
import android.content.SharedPreferences;
// to determine the api level of device
import android.os.Build;
import android.preference.PreferenceManager;
// to parse the sms packets from android telephony framework
import android.provider.Telephony;
import android.telephony.SmsMessage;

// log and util functions
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;



public class SMSReceiver extends BroadcastReceiver {

    public static final String TAG = "SMSReceiver";
    // intent action string to start the foreground service
    public static final String startFgService = "com.start.foreground";
    // intent to start our service on receiving the sms packets matching the config
    public Intent triggerIntent;
    // stores our received message
    public String messageReceived;
    // stores the contact number temporarily
    public String contactInfo;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Inside SMS Receiver!");
        // prepare the explicit intent to trigger foreground service for alarm playback
        triggerIntent = new Intent(context,SMSForegroundService.class);
        triggerIntent.setAction(startFgService);
        // flag to check, if the messages needs to be scanned for particular text
        // only true, if the user has given some config in message field of activity
        boolean msgConfigured = false;
        // get our config file
        SharedPreferences msgPref=PreferenceManager.getDefaultSharedPreferences(context);
        // if stored value is empty, assigning null
        String number = msgPref.getString("contact",null);
        String personName = msgPref.getString("contact_name",null);

        // we retrieve the keyword/content configured in an app file and convert into lower case
        //for easy comparison
        HashSet<String> keywordContents = (HashSet<String>)msgPref.getStringSet("message",null);
        if (keywordContents!=null) {
            msgConfigured =true;
        }
        Log.d(TAG,number+keywordContents);
        // if the keyword is not configured in app file we simply exit.
        if (!msgConfigured){
            return;
        }
        // gathers messages (multiple parts if exist)
        /* this api is very useful in case of multiple SMS packets
        ** it does the heavy lifting of packing sms packets together
        ** Since the sms packet(GSM standard) can old only 160 characters, rest
        ** will be delivered in another packet, this api automatically stitches for us
        ** the sms packets together into simple SmsMessage objects in the above situation.
         */
        SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for (SmsMessage msg : msgs) {
            Log.d(TAG, "message length" + msgs.length + msg.getOriginatingAddress());
            // gathering the stored value

            // if there is a message config and (if phone number of sender either configured or not configured)
            // trigger the alarm and draw the notification only if the sms payload matches with the configured keyword
            if ((number == null) || ((Objects.requireNonNull(msg.getOriginatingAddress())).contains(number))) {
                Log.d(TAG, "reading message " + number);
                // convert the msg into words and prepare for comparison with the keyword configured
                String[] words = msg.getDisplayMessageBody().toLowerCase().split("[^\\w\\d]+");
                Log.d(TAG, Arrays.toString(words));
                for (String word : words) {
                    //    Log.d(TAG, word + "*");
                    if (keywordContents.contains(word)) {
                        messageReceived = msg.getDisplayMessageBody();
                        // get the contact address
                        contactInfo = msg.getDisplayOriginatingAddress();
                        // pack inside the intent and send it to the service
                        triggerIntent.putExtra("From",contactInfo);
                        triggerIntent.putExtra("Keyword",word);
                        // pack also the received full message
                        triggerIntent.putExtra("Payload",messageReceived);
                        triggerIntent.putExtra("Person",personName);
                        Log.d(TAG, "Found the keyword " + word + " in msg " + msg.getDisplayMessageBody());
                        // inform user about the priority notification
                        // call our service and make it as foreground (visible notifications to user with an alarm)
                        // this is to background restrictions introduced in Android O, we should use foreground service
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(triggerIntent);
                        } else {
                            context.startService(triggerIntent);
                        }
                        break;
                    }
                }

            }
            // Just some logs for debugging
            // Log.d(TAG,"GOT THE MSG FROM"+msgs[i].getDisplayOriginatingAddress());
            // Log.d(TAG,"MSG CONTENT IS "+msgs[i].getDisplayMessageBody());
        }
    }
}
