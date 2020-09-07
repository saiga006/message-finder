// completely deprecated implementation
//package com.saiga.find.messagefinder;
//
//        import android.app.Notification;
//        import android.app.NotificationChannel;
//        import android.app.NotificationManager;
//        import android.app.PendingIntent;
//        import android.app.Service;
//        import android.content.Context;
//        import android.content.Intent;
//        import android.content.IntentFilter;
//        import android.media.AudioManager;
//        import android.media.MediaPlayer;
//        import android.media.Ringtone;
//        import android.media.RingtoneManager;
//        import android.net.Uri;
//        import android.os.Build;
//        import android.os.IBinder;
//        import android.support.v4.app.NotificationCompat;
//        import android.util.Log;
//        import android.view.View;
//        import android.widget.RemoteViews;
//
//        import java.io.IOException;
//
//public class TestService extends Service {
//    // background and foreground service start intent actions
//
//    //public static final String startBgService  = "com.sms.backgroundservice";
//    private static final String startFgService = "com.start.foreground";
//    private static final String stopFgService = "com.stop.foreground";
//    // for ringtone availability and playing the ringtone
//    Uri ringUri;
//    MediaPlayer mp;
//    Ringtone ringR;
//    private String channelId = "234";
//    //SMSReceiver testRecv;
//
//    private static final int notificationId = 123;
//    private static final String TAG = "SmsReceiver";
//    public TestService() {
//        //  testRecv = new SMSReceiver();
//    }
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//
//    @Override
//    public void onCreate() {
//
//        super.onCreate();
//        Log.d(TAG,"Background service created!");
//        ringUri = RingtoneManager.getActualDefaultRingtoneUri(this,RingtoneManager.TYPE_RINGTONE);
//
//        //    IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
//        //    registerReceiver(testRecv,filter);
//
//        // Notification manager
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence channelName = "Priority SMS";
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
//            try {
//                notificationManager.createNotificationChannel(notificationChannel);
//            } catch (NullPointerException e) {
//                Log.d(TAG, "the exception" + e);
//            }
//        }
//    }
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
////        unregisterReceiver(testRecv);
//        Log.d(TAG,"Background service destroyed!");
//
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (intent.getAction()==startFgService)
//        {
//            Log.d(TAG,"foregroundService got created");
//            Intent stopIntent = new Intent(this,SMSForegroundService.class);
//            stopIntent.setAction("com.stop.foreground");
//            //      RemoteViews collapsedView = new RemoteViews(getPackageName(),R.layout.custom_notification_view);
//            //      package com.saiga.find.messagefinder;
//            //
//            //import android.app.Notification;
//            //import android.app.NotificationChannel;
//            //import android.app.NotificationManager;
//            //import android.app.PendingIntent;
//            //import android.app.Service;
//            //import android.content.Context;
//            //import android.content.Intent;
//            //import android.media.AudioFocusRequest;
//            //import android.media.AudioManager;
//            //import android.media.MediaPlayer;
//            //import android.media.Ringtone;
//            //import android.media.RingtoneManager;
//            //import android.net.Uri;
//            //import android.os.Build;
//            //import android.os.IBinder;
//            //import android.os.VibrationEffect;
//            //import android.os.Vibrator;
//            //import android.support.annotation.RequiresApi;
//            //import android.support.v4.app.NotificationCompat;
//            //import android.util.Log;
//            //
//            //
//            //import java.io.IOException;
//            //import java.util.Objects;
//            //
//            //public class SMSForegroundService extends Service implements MediaPlayer.OnPreparedListener {
//            //    // foreground service start & stop intent actions
//            //
//            //    private static final String startFgService = "com.start.foreground";
//            //    private static final String stopFgService = "com.stop.foreground";
//            //    private AudioManager audioMan;
//            //    private AudioManager.OnAudioFocusChangeListener afListerner = new AudioManager.OnAudioFocusChangeListener() {
//            //        @Override
//            //        public void onAudioFocusChange(int focusChange) {
//            //            switch (focusChange){
//            //                case AudioManager.AUDIOFOCUS_GAIN:
//            //                    startPlaying();
//            //                    break;
//            //                case AudioManager.AUDIOFOCUS_LOSS:
//            //                     stopPlaying();
//            //                     break;
//            //                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//            //                     pausePlaying();
//            //                     break;
//            //            }
//            //        }
//            //    };
//            //    private Vibrator vibratorServ;
//            //    // for ringtone availability and playing the ringtone
//            //    Uri ringUri;
//            //    MediaPlayer mp;
//            //    Ringtone ringR;
//            //    private static final String channelId = "123";
//            //
//            //    private int current_vol_index;
//            //    private int max_vol_index;
//            //    private static final int notificationId = 123;
//            //    private static final String TAG = "SmsService";
//            //    public SMSForegroundService() {
//            //
//            //    }
//            //    @Override
//            //    public IBinder onBind(Intent intent) {
//            //        return null;
//            //    }
//            //
//            //    @Override
//            //    public void onCreate() {
//            //
//            //        super.onCreate();
//            //        Log.d(TAG,"Background service created!");
//            //        //Create a media player object to initialise the player with media files
//            //        mp = new MediaPlayer();
//            //        vibratorServ =(Vibrator) getSystemService(VIBRATOR_SERVICE);
//            //
//            //        audioMan = (AudioManager) getSystemService(AUDIO_SERVICE);
//            //
//            //        if (audioMan!=null) {
//            //            max_vol_index = audioMan.getStreamMaxVolume(AudioManager.STREAM_ALARM);
//            //        }
//            //        //mp.setWakeMode(this, PowerManager.);
//            //        ringUri = RingtoneManager.getActualDefaultRingtoneUri(this,RingtoneManager.TYPE_RINGTONE);
//            //
//            //        // Notification manager
//            //        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            //        // Notification Channel is needed from Android O onwards, earlier version doesn't have this new API.
//            //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            //            // user visible notification channel name;
//            //            CharSequence channelName = "Priority SMS";
//            //            int importance = NotificationManager.IMPORTANCE_HIGH;
//            //            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
//            //            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
//            //            // to do
//            //            notificationChannel.setBypassDnd(true);
//            //            try {
//            //                if (notificationManager!=null) {
//            //                    notificationManager.createNotificationChannel(notificationChannel);
//            //                }
//            //            } catch (NullPointerException e) {
//            //            Log.d(TAG, "the exception" + e);
//            //        }
//            //    }
//            //}
//            //    @Override
//            //    public void onDestroy() {
//            //        super.onDestroy();
//            //        if (mp!=null) {
//            //            Log.d(TAG,"calling media player release to free the decoder resources ");
//            //            mp.release();
//            //            mp = null;
//            //        }
//            //        Log.d(TAG,"Background service destroyed!");
//            //    }
//            //
//            //
//            //
//            //    @Override
//            //    public int onStartCommand(Intent intent, int flags, int startId) {
//            //        if (Objects.equals(intent.getAction(), startFgService))
//            //        {
//            //            String senderAddress = intent.getStringExtra("From");
//            //            String messageContent = intent.getStringExtra("Payload");
//            //            Log.d(TAG,"foregroundService got created");
//            //            Intent stopIntent = new Intent(this,SMSForegroundService.class);
//            //            stopIntent.setAction(stopFgService);
//            //            PendingIntent stopPlayback = PendingIntent.getService(this,0,stopIntent,0);
//            //            Notification notification = new NotificationCompat.Builder(this,channelId)
//            //                    .setContentTitle("Priority Message Received!")
//            //                    .setContentText("Received the text "+messageContent+" from "+senderAddress)
//            //                    .setPriority(NotificationCompat.PRIORITY_MAX)
//            //                    .setContentIntent(stopPlayback)
//            //                    .setColor(getResources().getColor(R.color.somewhat_red,null))
//            //                    .setColorized(true)
//            //                    .setCategory(NotificationCompat.CATEGORY_ALARM)
//            //                    .setStyle(new NotificationCompat.BigTextStyle().setSummaryText("Touch to dismiss"))
//            //                    .setSmallIcon(R.drawable.notification_mf_icon)
//            //                    .setAutoCancel(true).build();
//            //             ringR= RingtoneManager.getRingtone(this,ringUri);
//            //
//            //            if(ringR==null){
//            //                Log.d(TAG,"default ringtone is empty");
//            //            }
//            //
//            //            try {
//            //                if (mp.isPlaying()){
//            //                    mp.pause();
//            //                    mp.seekTo(0);
//            //                    mp.start();
//            //                } else {
//            //                    mp.setDataSource(this, ringUri);
//            //                    mp.setAudioStreamType(AudioManager.STREAM_ALARM);
//            //                    mp.setOnPreparedListener((MediaPlayer.OnPreparedListener) this);
//            //                    mp.prepareAsync();
//            //                    mp.setLooping(true);
//            //                    if (audioMan.isVolumeFixed()) {
//            //                        mp.setVolume((float) 1.0, (float) 1.0);
//            //                    } else {
//            //                        current_vol_index = audioMan.getStreamVolume(AudioManager.STREAM_ALARM);
//            //                        audioMan.setStreamVolume(AudioManager.STREAM_ALARM,max_vol_index,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//            //                    }
//            //                }
//            //            } catch (IOException e) {
//            //                e.printStackTrace();
//            //            }
//            //            if (vibratorServ!=null){
//            //                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
//            //                    vibratorServ.vibrate(VibrationEffect.createWaveform(new long[]{50, 1000, 400, 900, 300, 800}, new int[]{0, 255, 0, 200, 0, 150}, 2));
//            //                } else {
//            //                    vibratorServ.vibrate(new long[]{50, 1000, 400, 900, 300, 800},2);
//            //                }
//            //            }
//            //            startForeground(notificationId,notification);
//            //        }
//            //        else if (Objects.equals(intent.getAction(), stopFgService)) {
//            //            mp.stop();
//            //            vibratorServ.cancel();
//            //            audioMan.setStreamVolume(AudioManager.STREAM_ALARM,current_vol_index,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//            //            stopForeground(true);
//            //            Log.d(SMSReceiver.TAG,"stop foreground service");
//            //            // this will stop the service and destroy it
//            //            stopSelf();
//            //        }
//            //            // we dont want to restart our service, incase of low memory, to avoid mocking the user
//            //            return START_NOT_STICKY;
//            //    }
//            //
//            //
//            //    @RequiresApi(api = Build.VERSION_CODES.O)
//            //    public void manageAudioFocus(){
//            //            int mAudioResponse = audioMan.requestAudioFocus(new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
//            //                    .setOnAudioFocusChangeListener().build();
//            //
//            //    }
//            //
//            //    @Override
//            //    public void onPrepared(MediaPlayer mp) {
//            //        Log.d(TAG, "Media files are ready , starting the media player ");
//            //        mp.start();
//            //    }
//            //
//            //    public void startPlaying(){
//            //
//            //    }
//            //
//            //    public void stopPlaying(){
//            //
//            //    }
//            //
//            //    public void pausePlaying(){
//            //
//            //    }
//            //
//            //}
//            //      collapsedView.setTextViewText(R.id.notification_content_value,"Rcvd the text "+testRecv.messageRcvd+" from "+testRecv.contactInfo);
//            PendingIntent stopPlayback = PendingIntent.getService(this,0,stopIntent,0);
//            Notification notification = new NotificationCompat.Builder(this,channelId)
//                    .setContentTitle("MESSAGE FOUND!")
//                //    .setContentText("Rcvd the text "+testRecv.messageRcvd+" from "+testRecv.contactInfo)
//                    .setPriority(NotificationCompat.PRIORITY_MAX)
//                    .setContentIntent(stopPlayback)
//                //    .setSmallIcon(R.drawable.ic_launcher_background)
//                    //              .setCustomContentView(collapsedView)
//                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
//                    .setAutoCancel(true).build();
//            ringR= RingtoneManager.getRingtone(this,ringUri);
//
//            if(ringR==null)
//                Log.d(TAG,"ring is empty");
//
//            mp = new MediaPlayer();
//            try {
//                mp.setDataSource(this,ringUri);
//                mp.setAudioStreamType(AudioManager.STREAM_ALARM);
//                mp.prepare();
//                mp.setLooping(true);
//                mp.setVolume((float) 1.0,(float) 1.0);
//                mp.start();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            startForeground(notificationId,notification);
//        }
//        else if (intent.getAction().equals("com.stop.foreground")) {
//            mp.stop();
//            stopForeground(true);
//            Log.d(SMSReceiver.TAG,"stop foreground service");
//            //      stopSelf(startId);
//            //          unregisterReceiver(testRecv);
//        }
//        return START_STICKY;
//    }
//}
