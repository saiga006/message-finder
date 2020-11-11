package com.saiga.find.messagefinder;

// required for creating notification
import android.app.Notification;

import androidx.core.app.NotificationCompat;
// user controllable notification channel, required from android O
import android.app.NotificationChannel;
// required for posting notification
import android.app.NotificationManager;
// pending intent to be delivered to notification manager, so that it stops our service
import android.app.PendingIntent;
// for service handling
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

// for playing audio when notification is thrown to user
import android.media.AudioManager;
import android.media.MediaPlayer;
// for accessing user configured ringtone to play as our notification tone
//import android.media.Ringtone;
//import android.media.RingtoneManager;
import android.net.Uri;
// to decide the android api usage based on user device
import android.os.Build;

// to play vibration when notification is thrown
import android.os.VibrationEffect;
import android.os.Vibrator;

// for log and exception handling
import android.util.Log;
import java.io.IOException;
import java.util.Objects;

/**
 * Manages the Priority Notification and ringtone Playback
 */
public class SMSForegroundService extends Service implements MediaPlayer.OnPreparedListener {
    // foreground service start & stop intent actions

    private static final String startFgService = "com.start.foreground";
    private static final String stopFgService = "com.stop.foreground";

    // for playing vibration in notification
    private Vibrator vibratorService;
    // for ringtone availability and playing the ringtone
   // Uri ringUri;
 //   Ringtone ringTone;

    // manages the playback of default ringtone
    MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    // notification channel ID
    private static final String channelId = "123";
    // notification  Number to control notification func.
    private static final int notificationId = 123;

    // android resource for default app ringtone for alerts
    //private static final String priorityRingtoneId = "android.resource://com.saiga.find.messagefinder/raw/priority_ringtone";

    // stores the user configured volume
    private int current_vol_index;
    // stores the maximum supported volume by particular volume stream
    private int max_vol_index;

    private static final String TAG = "SmsService";
    public SMSForegroundService() {

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        Log.d(TAG,"Background service created!");
        //Create a media player object to initialise the player with media files
        mediaPlayer = new MediaPlayer();
        //Create a vibrator service object to control vibration and audio manager object to control volume
        vibratorService =(Vibrator) getSystemService(VIBRATOR_SERVICE);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // retrieves the max volume supported by alarm stream from devices
        if (audioManager !=null) {
            max_vol_index = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        }

        // Todo
        //mp.setWakeMode(this, PowerManager.);

        //@deprecated, now we use default ringtone instead of it
        // gets the uri of content provider that manages user ringtone values
        //ringUri = RingtoneManager.getActualDefaultRingtoneUri(this,RingtoneManager.TYPE_RINGTONE);
        //Log.d(TAG,"Print the ringtone Uri" + ringUri);
        // Notification manager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Notification Channel is needed from Android O onwards, earlier version doesn't have this new API.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // user visible notification channel name;
            CharSequence channelName = "Priority SMS";
            // so that notification has sound, headups behavior  and visible in status bar
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            // notification appears in lock screen, it can be controlled by user
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            // Todo, add bypass DND feature
            notificationChannel.setBypassDnd(true);

            // create notification channel
            try {
                if (notificationManager!=null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            } catch (NullPointerException e) {
            Log.d(TAG, "the exception" + e);
        }
    }
}
    @Override
    public void onDestroy() {
        super.onDestroy();
        // this is critical, we should clear our resources so that the audio decoder is available
        // for other media apps, when our app is finished using it.
        if (mediaPlayer !=null) {
            Log.d(TAG,"calling media player release to free the decoder resources ");
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Log.d(TAG,"Background service destroyed!");
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // start to play alarm and throw a notification to user, when the
        // user configured message is found
        if (Objects.equals(intent.getAction(), startFgService))
        {
            Log.d(TAG,"foregroundService got created");
            // get the payload, keyword, sms number
            String senderAddress = intent.getStringExtra("From");
            String messageContent = intent.getStringExtra("Payload");
            String keyword = intent.getStringExtra("Keyword");
            String personName = intent.getStringExtra("Person");
            Log.d(TAG,messageContent + "  keyword " + keyword);
            // prepare the stop intent and pack inside the pending intent and pass it to notification manager
            Intent stopIntent = new Intent(this,SMSForegroundService.class);
            stopIntent.setAction(stopFgService);
            // this stops our service when user clicks the notification, thus stopping the alarm
            PendingIntent stopPlayback = PendingIntent.getService(this,0,stopIntent,0);
            // prepare the notification with payload data, priority, pending intent, red colored,
            // with app icon and user guidance message to dismiss the notification
            // we register it category alarm, to whitelist for DND mode, so app plays
            // alarm in DND mode too.
            Notification notification = new NotificationCompat.Builder(this,channelId)
                    .setContentTitle("Priority Message Received!")
                    .setContentText("Received the text "+ keyword +" from "+ (personName!=null?personName:senderAddress))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .addAction(R.drawable.ic_block_24dp,"STOP",stopPlayback)
                    .setColor(getResources().getColor(R.color.secondaryDarkColor,null))
                    .setColorized(true)
                    .setShowWhen(false)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .setBigContentTitle("Priority Message Received!")
                            .bigText("Received the text "+messageContent+" from "+senderAddress)
                            .setSummaryText("Press STOP to cancel"))
                    .setLargeIcon(null)
                    .setSmallIcon(R.drawable.notification_mf_icon)
                    .build();

            // @deprecated gets the ringtone object from the content provider --  user configured.
            // ringTone= RingtoneManager.getRingtone(this,ringUri);

//            if(ringTone==null){
//                Log.d(TAG,"default ringtone is empty");
//            }

            // alarm playing functionality, plays alarm maximum volume with vibration,
            // restores to the user configured volume, when  the notification is dismissed
            initialiseAlarmPlayback();

            // starts the vibration, we play a custom pattern of vibration, basically we
            // can compose our own vibration.
            // there is a different api, depending upon the android version
            startVibration();
            // start the foreground service with the notification to be thrown.
            startForeground(notificationId,notification);
        }
        // called when notification is clicked by user
        else if (Objects.equals(intent.getAction(), stopFgService)) {
            // stop the media player(alarm playback) and vibration
            mediaPlayer.stop();
            vibratorService.cancel();
            // restore our user configured alarm back
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,current_vol_index,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            // stops foreground and removes notification
            stopForeground(true);
            Log.d(SMSReceiver.TAG,"stop foreground service");
            // clears the media player resources
            // this will stop the service and destroy it
            stopSelf();
        }
            // we don't want to restart our service, incase of low memory, to avoid mocking the user
            // since this is one time service, it will be invoked when there is a priority message
            return START_NOT_STICKY;
    }

    // async callback delivered by android framework, when media player is finished configuring
    // audio decoder with our ringtone, starts the alarm playback
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "Media files are ready , starting the media player ");
        // starts the media player
        mp.start();
    }

    /*
    **  handles media player state machine and volume state of alarm playback
     */
    private void initialiseAlarmPlayback(){
        // default ringtone tied to this app resource, we use this to avoid the unwanted permission
        // that we requested from the user in earlier scenario
        String priorityRingtoneId = "android.resource://" + getPackageName() + "/raw/priority_ringtone";
        Log.d(TAG," Priority URI " + priorityRingtoneId);
        try {
            // if media player is currently playing and there is new notification
            // then pause the playback and start from beginning
            if (mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            } else {
                // @deprecated sets the media player audio source to ringtone URI(user configured)
                // use default app ringtone
                //mediaPlayer.setDataSource(this, ringUri);

                //we instead select the data source from the resource file, to avoid the excess
                // permission we added to read user storage earlier
                mediaPlayer.setDataSource(this,Uri.parse(priorityRingtoneId));

                // sets the stream, in which audio has to be played, in our case, it is alarm stream
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                //registers for media player callback, because it will take long time, if
                // we do a blocking call. So its better to run in another thread
                // so that we can prevent unexpected crashes, we give this task to android framework.
                // and register our app for the callback when it is done
                mediaPlayer.setOnPreparedListener((MediaPlayer.OnPreparedListener) this);
                // prepares the audio decoder with our resource and notify us, when it is ready
                mediaPlayer.prepareAsync();
                // if the audio playback is to be looped after the track is completed
                mediaPlayer.setLooping(true);

                // if we use fixed volume devices, i.e whose volume can't be controlled through
                // app such as from AudioManager apis, we use mediaplayer api for setting volume programmatically
                // fixed volume devices - some times no control for mute, decreasing volume.
                // This method works only for fixed volume devices
                if (audioManager.isVolumeFixed()) {
                    mediaPlayer.setVolume((float) 1.0, (float) 1.0);
                } else {
                    // get the current volume , in case of normal devices and update the alarm stream
                    // to maximum volume
                    current_vol_index = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM,max_vol_index,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startVibration(){
        if (vibratorService!=null){
            // api change based on build
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                // this api is added to control amplitude also along with timing interval
                // repeat the vibration from index 2 of timing array, until the notification is dismissed by user
                vibratorService.vibrate(VibrationEffect.createWaveform(new long[]{50, 1000, 400, 900, 300, 800}, new int[]{0, 255, 0, 200, 0, 150}, 2));
            } else {
                vibratorService.vibrate(new long[]{50, 1000, 400, 900, 300, 800},2);
            }
        }
    }

}
