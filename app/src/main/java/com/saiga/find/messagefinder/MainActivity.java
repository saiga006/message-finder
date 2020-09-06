package com.saiga.find.messagefinder;
// for accessing manifest permission strings
import android.Manifest;
// for storing the configuration in an app only file
import android.content.SharedPreferences;
// handle to package manager system service
import android.content.pm.PackageManager;
// for controlling the shared preference
import android.preference.PreferenceManager;
// for handing null and non null annotations based methods
import android.support.annotation.NonNull;
// for accessing global state  of the app and system services
import android.support.v4.content.ContextCompat;
// framework independent activity class, appears & behaves same across all platform versions
import android.support.v7.app.AppCompatActivity;
//for persisting the state between rotation, other runtime configuration changes
import android.os.Bundle;
// for printing logs
import android.util.Log;
// for inflating views, buttons, edittext
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
// for disabling ime temporarily
import android.view.inputmethod.InputMethodManager;
// for displaying toast message
//import android.widget.Toast;
// for Snackbar message support
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
//for launching app settings window to change app permissions
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
// to launch as a seperate task
import static android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String CONFIG_MSG = "saved the config to capture SMS packets";
    private static final String triggerStatus = "Triggered!";
    private static final String clearStatus = "Cleared!";
    private static final String smsPermission = Manifest.permission.RECEIVE_SMS;
    private static final String readStoragePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String TAG = "Sms Receiver";
    private static final String ALERT_MSG = "Keyword for alert is empty!";
    private static final String READ_SMS_NOTIFICATION_MSG = "Please enable the READ SMS permission in App Permissions";
    private static final String READ_STORAGE_NOTIFICATION_MSG = "Please enable the Read Storage permission in App Permissions";
    private static final String SHOW_USER_MSG = "Some permissions needs to be enabled to work properly";
    private String contactStr = null;
    private String messageStr = null;
    private Button trigButton = null;
    private Button clearButton = null;
    private CoordinatorLayout mUserMsgLayout = null;
    // just to handle runtime our runtime permission request
    private static final int permRequestCode = 123;
    // launches app settings screen
    private static Handler navigateToSettings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // clear the splash, and set the normal app window
        setTheme(R.style.MyAppTheme);
        super.onCreate(savedInstanceState);
        // inflates the contents from activity_main.xml
        setContentView(R.layout.activity_main);
        // get the handler and attach it to UI looper thread
        navigateToSettings = new Handler(getMainLooper());

        // button for saving the configuration and triggering the service in background (indirectly)
         trigButton = findViewById(R.id.trigger);
        // button for clearing the configuration and stopping the service in background (indirectly)
         clearButton = findViewById(R.id.clear);

         mUserMsgLayout = findViewById(R.id.user_message_layout);

         /* deprecated  : intent to trigger for SMS foreground Service
       // serviceIntent = new Intent(this,SMSForegroundService.class);
       // serviceIntent.setAction(startAction)
        */

        //set of tasks to do on button click, registering a listener for button click
        clearButton.setOnClickListener(this);
        trigButton.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        // makes the root layout gets the focus, before it is passed to edit text
        // to prevent the ime to open automatically in landscape mode when device
        // is rotated.
        findViewById(R.id.ui_layout).requestFocus();
        super.onResume();
    }

    // system sends us the calls our onclick function, when the button is pressed
    // based on the view, decision is taken
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.trigger :{
                // a app specific file to store the configurations (persists across reboots)
                EditText contactView = findViewById(R.id.contact_value);
                EditText messageView = findViewById(R.id.message_value);

               // clear focus to hide the IME
                clearSoftKeyboardState();
                contactView.clearFocus();
                messageView.clearFocus();

                contactStr = contactView.getText().toString();
                messageStr =  messageView.getText().toString();
                // if there is no message, there is no point of saving the config
                if(messageStr.isEmpty()){
                    // @deprecated display some alert message to user.
                    //Toast.makeText(this,ALERT_MSG,Toast.LENGTH_SHORT).show();
                    Snackbar.make(mUserMsgLayout,ALERT_MSG,Snackbar.LENGTH_LONG).show();
                    return;
                }
                // Don't save the config if the runtime permission is not granted by user
                if (!checkSmsPermission()){

                    //@deprecated Toast.makeText(this,USER_NOTIFICATION_MSG,Toast.LENGTH_LONG).show();
                    return;
                }
                // retrieve the input text from the user and save it in the persistent file
                // and inform the user through a toast message
                setSmsConfig();

                /* deprecated : this service call -- registers sms broadcast receiver
                //this.startService(serviceIntent);*/
            }
                break;
            case R.id.clear:{
                // get the edit text views to clear the text automatically
                EditText contactView = findViewById(R.id.contact_value);
                EditText messageView = findViewById(R.id.message_value);
                // clear focus to hide the IME
                clearSoftKeyboardState();
                int clear_position = 0;
                //get shared preference file specific to this app, to clear the saved config if it exists
                SharedPreferences saved = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor savedEditor =saved.edit();
                savedEditor.clear();
                savedEditor.apply();
                /* deprecated : stop the service to remove the foreground notification & deregister sms receiver
                //this.stopService(serviceIntent);
                // clear the edit text views
                //private Intent serviceIntent; */

                // moves the cursor position in edit text in message edit text and clear focus
                messageView.setSelection(clear_position);
                contactView.setSelection(clear_position);
                messageView.getText().clear();
                contactView.getText().clear();
                contactView.clearFocus();
                messageView.clearFocus();

                // button text change, to indicate configurations are cleared!
                clearButton.setText(clearStatus);
                // restore the trigger button to initial state
                trigButton.setText(R.string.trigger_button);
                }
            break;
        }
    }

    private boolean checkSmsPermission(){
        boolean mPermGranted = false;
        // check if the permissions have been granted before
        if((ContextCompat.checkSelfPermission(getApplicationContext(),smsPermission)== PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),readStoragePermission) == PackageManager.PERMISSION_GRANTED)) {
            mPermGranted = true;
        }  else {
            Log.d(TAG,"Request Permissions is invoked");
            // throw a system dialog to request permission from user, if permission hasn't been granted before
            requestPermissions(new String[]{smsPermission,readStoragePermission},permRequestCode);
        }
        return mPermGranted;
    }

    // async callback from android permission manager, when runtime permission is accepted
    // or rejected by user
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // uniquely identifies our request to sms & read external storage permission by android framework
        if (requestCode==permRequestCode){
            if ((grantResults.length>0) && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                // save the SMS configuration to trigger notification, when permission is granted
                setSmsConfig();
            } else if ((grantResults.length>0) && grantResults[0] == PackageManager.PERMISSION_DENIED)  {
                if (!shouldShowRequestPermissionRationale(smsPermission)) {
                    // invoked when user has rejected our requested and clicked the permission dialog to
                    // to never show again
                    // need to navigate the user to new settings app
                    Snackbar.make(mUserMsgLayout, READ_SMS_NOTIFICATION_MSG, Snackbar.LENGTH_SHORT).show();
                    // launch app permission screen after 3 secs;
                    navigateToAppPermissions();
                    //Log.d(TAG,"should show rationale");
                    Log.d(TAG, "navigate/suggest user to choose the permission from settings");
                } else {
                    // show user message via snackbar about the missing permissions
                    Snackbar.make(mUserMsgLayout, SHOW_USER_MSG, Snackbar.LENGTH_LONG).show();
                }


            } else if ((grantResults.length>0) && grantResults[1] == PackageManager.PERMISSION_DENIED) {

                if (!shouldShowRequestPermissionRationale(readStoragePermission)) {
                    // invoked when user has rejected our requested and clicked the permission dialog to
                    // to never show again
                    // need to navigate the user to new settings app
                    Snackbar.make(mUserMsgLayout, READ_STORAGE_NOTIFICATION_MSG, Snackbar.LENGTH_SHORT).show();
                    // launch app permission screen after 3 secs;
                    navigateToAppPermissions();
                    //Log.d(TAG,"should show rationale");
                    Log.d(TAG, "navigate/suggest user to choose the permission from settings");
                } else {
                    // show user message via snackbar about the missing permissions
                    Snackbar.make(mUserMsgLayout, SHOW_USER_MSG, Snackbar.LENGTH_LONG).show();
                }

            }
        }
    }

    private void setSmsConfig(){
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor configEditor = config.edit();
        configEditor.putString("contact",contactStr.isEmpty()?null:contactStr);
        configEditor.putString("message",messageStr.isEmpty()?null:messageStr);
        configEditor.apply();
        // a small temporary Ui Message to be displayed to user on successful config. save
        //@deprecated Toast.makeText(this,toastText,Toast.LENGTH_LONG).show();
        Snackbar.make(mUserMsgLayout,CONFIG_MSG,Snackbar.LENGTH_LONG).show();
        // notify the trigger status in view to the user
        trigButton.setText(triggerStatus);
        // set the clear button to initial state
        clearButton.setText(R.string.clear_button);
    }

    private void clearSoftKeyboardState (){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        View focussedView = getCurrentFocus();
        if (focussedView != null) {
        inputMethodManager.hideSoftInputFromWindow(focussedView.getWindowToken(),0);
        }
    }

    private void navigateToAppPermissions(){
        // post in the UI thread after 3 secs, wait till snackbar ends to start new activity in new task with no history in back stack and recents
        // when user exits
        navigateToSettings.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                // we remove the activity from recents screen and also when the user presses the back button
                i.setFlags(FLAG_ACTIVITY_NEW_DOCUMENT|Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(i);
            }
        },3000);
    }
}
