package com.saiga.find.messagefinder;
// for accessing manifest permission strings
import android.Manifest;
// for storing the configuration in an app only file
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
// handle to package manager system service
import android.content.pm.PackageManager;
// for controlling the shared preference
import android.database.Cursor;
import android.preference.PreferenceManager;
// for handing null and non null annotations based methods
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
// for accessing global state  of the app and system services
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.core.content.ContextCompat;
// framework independent activity class, appears & behaves same across all platform versions
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
//for persisting the state between rotation, other runtime configuration changes
import android.os.Bundle;
// for printing logs
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
// for transition animation
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.DecelerateInterpolator;
// for inflating views, buttons, edittext
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
// for disabling ime temporarily
import android.view.inputmethod.InputMethodManager;
// for displaying toast message
//import android.widget.Toast;
// for Snackbar message support
import androidx.coordinatorlayout.widget.CoordinatorLayout;
//import androidx.viewpager.widget.PagerAdapter;

//for cursor adapter and auto suggestion feature
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

//Snackbar material lib
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
//for launching app settings window to change app permissions
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
// util functions for processing multiple keywords support
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
// to launch as a seperate task
import static android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String CONFIG_MSG = "saved the config to capture SMS packets";
    private static final String triggerStatus = "Triggered!";
    private static final String clearStatus = "Cleared!";
    private static final String smsPermission = Manifest.permission.RECEIVE_SMS;
    private static final String readStoragePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String readContactsPermission = Manifest.permission.READ_CONTACTS;
    private static final String TAG = "Sms Receiver";
    private static final String ALERT_MSG = "Keyword for alert is empty!";
    private static final String READ_SMS_NOTIFICATION_MSG = "Enable READ SMS permission in App Permissions";
    private static final String READ_STORAGE_NOTIFICATION_MSG = "Enable Read Storage permission in App Permissions";
    private static final String SHOW_USER_MSG = "Some permissions needs to be enabled to work properly";
    // to uniquely identify a loader by loader manager
    //private static final int CONTACT_LOADER_ID = 123 ;
    private static final String READ_CONTACT_NOTIFICATION_MSG = "Enable Contacts permission for Auto Suggestion";
    // to uniquely identify our request to contact picker app
    private static final int RESULT_PICK_CONTACT = 125 ;
    private String contactStr = null;
    private String messageStr = null;
    private Button trigButton = null;
    private Button clearButton = null;
    private CoordinatorLayout mUserMsgLayout = null;
    private SimpleCursorAdapter contactAdapter = null;
    // just to handle runtime our runtime permission request
    private static final int permRequestCode = 123;
    private static final int contactPermRequestCode = 124;
    // launches app settings screen
    private static Handler navigateToSettings = null;

    // snackbar objects for individual permissions
    private Snackbar smsPermSnackbar = null;
    private Snackbar storagePermSnackbar = null;

    // Loader for handling cursor object from Contacts Provider
    // and updating the contact provider with the cursor

    //private LoaderManager.LoaderCallbacks<Cursor> contactsLoader = null;
    private static SharedPreferences config = null;

    // look whether read contacts permission is granted for this app.
    // serves as a check for auto suggestion popup
    // currently unused
    private static boolean isRequestDiagDisabled = false;

    // flag to mark onboard activity start and completion states
    private static boolean showOnboarding = false;
    private boolean isContactPicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // structure which controls, the flow of onboarding activity and the normal activity
        int enabled = getPackageManager().getComponentEnabledSetting(new ComponentName(this, Onboarding.class));
        Log.d(TAG, "enabled status" + enabled);
        if ((enabled == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) || (enabled == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)) {
            Log.d(TAG, "started ?");
            showOnboarding = true;
            // start onboard activity if it is the first time user installation
            startActivity(new Intent(this, Onboarding.class));
            this.finish();
        } else {
            showOnboarding = false;
        }

        // clear the splash, and set the normal app window
        setTheme(R.style.MyAppTheme);
        super.onCreate(savedInstanceState);
        // performs one time animation when entering this activity from onboarding screen
        slideAnimation();
        // inflates the contents from activity_main.xml
        setContentView(R.layout.activity_main);
        // get the handler and attach it to UI looper thread
        navigateToSettings = new Handler(getMainLooper());

        // button for saving the configuration and triggering the service in background (indirectly)
        trigButton = findViewById(R.id.trigger);
        // button for clearing the configuration and stopping the service in background (indirectly)
        clearButton = findViewById(R.id.clear);
        // snackbar co-ordinator layout
        mUserMsgLayout = findViewById(R.id.user_message_layout);
        // get the global shared preference file for this app
        config = PreferenceManager.getDefaultSharedPreferences(this);
         /* deprecated  : intent to trigger for SMS foreground Service
       // serviceIntent = new Intent(this,SMSForegroundService.class);
       // serviceIntent.setAction(startAction)
        */

        // listen to the contact number field and throw error,
        // if it is wrong, disable trigger button in such cases
        final TextInputLayout contact_number_layout = findViewById(R.id.contact_input_layout);
        contact_number_layout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // if the contact number field has more numbers or if it has any other character
                // other than number, then throw error in UI and set helper text for user
                if (s.length() > 10 || s.toString().matches("(?!^\\d+$)^.+$")) {
                    contact_number_layout.setError("Doesn't meet the input format/limit !");
                    contact_number_layout.setErrorIconDrawable(R.drawable.ic_block_24dp);
                    contact_number_layout.setErrorEnabled(true);
                    contact_number_layout.setErrorIconOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            contact_number_layout.getEditText().getText().clear();
                        }
                    });
                    trigButton.setBackgroundColor(getColor(R.color.material_on_background_disabled));
                    trigButton.setEnabled(false);
                } else {
                    contact_number_layout.setErrorEnabled(false);
                    contact_number_layout.setHelperTextEnabled(true);
                    trigButton.setBackgroundResource(R.drawable.rounded_button_gradient);
                    trigButton.setEnabled(true);
                }
                Log.d(TAG, "some on change listener : " + start + before + count);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Initialise cursor adapter with contact layout and contents to be
        // fetched from contacts provider
        setupContactAdapter();
        final AutoCompleteTextView contactTextView = (AutoCompleteTextView) findViewById(R.id.contact_value);
        // Create the loader callback object to query the contacts content provider async using a cursor loader
        // cursor loader and filter query provider doesn't work with each other very well, so better drop using loader
        //instantiateLoader();

        // we use on focus change listener for the edit text to determine, whether to throw our contact picker
        // dialog based on bunch of state variables
        // this is determined based on whether user has disabled the auto suggestion feature, or skipped
        // the auto suggestion feature or if user had successfully picked a contact using our contact picker earlier
        contactTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focusGain) {
                Log.d(TAG, "inside on focus change, is focus gained ? " + focusGain + " isrequestdialogEnabled ?" + isRequestDiagDisabled);
                // we use contact picker only in scenario's where auto suggestion is disabled, or skipped or if we haven't picked any contact earlier
                if (focusGain && (isRequestDiagDisabled || checkUserConfig()) && !isContactPicked) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                    builder.setTitle("Contact Picker");
                    builder.setIcon(R.drawable.ic_contact_phone_black_24dp);
                    builder.setMessage("Do you want to pick the contacts ?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // we use contacts provider uri as intent parameter, it will point us to the contacts app
                            Intent contactPicker = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                            startActivityForResult(contactPicker, RESULT_PICK_CONTACT);
                            Log.d(TAG, "start the contact picker activity");
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // we mark the user choice, to determine the state of this dialog popup again
                            // when the user goes back to the contact number edit text
                            isContactPicked = false;
                            Log.d(TAG, "dont start the contact picker activity");
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });


        contactTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // capture the contact name from the selected user account and display it in the notification
                TextView contactTitle = (TextView) view.findViewById(R.id.contact_title);
                insertContactName(contactTitle.getText().toString());
                Log.d(TAG,"selected phone contact" + contactTitle.getText() );
            }
        });
        // passes the text, on field selection by the cursor
        contactAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                return spliceNumber(phoneNumber);
            }
        });

        // setup the adaptor with the auto complete textview,
        contactTextView.setAdapter(contactAdapter);

        // ask contact permission before showing suggestions ....
        // check dont show again is pressed by user in earlier case
        if (!checkUserConfig()) {
            if (checkReadContactPermission()){
                //deprecated -- loader callbacks, instead FilterQueryProvider fits to our scenario
              //  getSupportLoaderManager().initLoader(CONTACT_LOADER_ID,new Bundle(),contactsLoader);
                //connect filter provider to the cursor adapter
                setFilter();
            }

        }
        //set of tasks to do on button click, registering a listener for button click
        clearButton.setOnClickListener(this);
        trigButton.setOnClickListener(this);

    }

    // called after Oncreate to inflate the settings menu item in app action bar
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     getMenuInflater().inflate(R.menu.settings_menu,menu);
        return true;
    }

    // callback invoked when the settings menu is clicked by user
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings_menu) {
            // Add link to our settings activity to display preferences such as app version,
            // stored configuration and to enable contact suggestion feature
            Intent settingsIntent = new Intent(this,SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // retrieves the content provider from the picked contact by the user, and then
    // query for the user selected contact number and contact name.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    Cursor cursor = null;
                    String phoneNumber = null;
                    String contactName = null;
                    try {
                        Uri pickedItem = data.getData();
                        cursor = getContentResolver().query(pickedItem,null,null,null,null);
                        cursor.moveToFirst();
                        // fetch the contact name and phone number and update in shared preference
                        //and  in the edit text field automatically
                        contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        EditText contact = findViewById(R.id.contact_value);
                        // removes country code and unwanted spaces between numbers if it exists
                        contact.setText(spliceNumber(phoneNumber));
                        //adds the contact name to shared preferences to display it in UI
                        insertContactName(contactName);
                        // this is a state variable to manage the dialog popup of "Do you want to pick the contacts"
                        // set this as true, when user has selected the a contact successfully
                        // this will determine whether to throw the dialog, when the user returns to our app
                        isContactPicked = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,"Result of picked contact");
                    break;
            }
        }
    }

    // inserts name into global shared preference file
    void insertContactName(String contactTitle) {
        SharedPreferences.Editor configEditor = config.edit();
        configEditor.putString("contact_name", contactTitle);
        configEditor.apply();
    }

    @Override
    protected void onResume() {
        // makes the root layout gets the focus, before it is passed to edit text
        // to prevent the ime to open automatically in landscape mode when device
        // is rotated.
        Log.d(TAG,"Inside on Resume Callback");
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

                contactStr = contactView.getText().toString().toLowerCase();
                messageStr =  messageView.getText().toString().toLowerCase();
                // if there is no message, there is no point of saving the config
                if(messageStr.isEmpty()){
                    // @deprecated display some alert message to user.
                    //Toast.makeText(this,ALERT_MSG,Toast.LENGTH_SHORT).show();
                    Snackbar.make(mUserMsgLayout,ALERT_MSG,Snackbar.LENGTH_LONG).show();
                    return;
                }
                // Don't save the config if the runtime permission is not granted by user
                if (!checkSmsPermission()){
                    // do an early return
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
                SharedPreferences.Editor savedEditor =config.edit();
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
                    smsPermSnackbar =  Snackbar.make(mUserMsgLayout, READ_SMS_NOTIFICATION_MSG, Snackbar.LENGTH_INDEFINITE)
                            .setAction("SET PERMISSIONS", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    navigateToAppPermissions();
                                }
                            })
                            .setActionTextColor(getColor(R.color.primaryTextColor));;
                    smsPermSnackbar.show();
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
                    storagePermSnackbar =  Snackbar.make(mUserMsgLayout, READ_STORAGE_NOTIFICATION_MSG, Snackbar.LENGTH_INDEFINITE)
                            .setAction("SET PERMISSIONS", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    navigateToAppPermissions();
                                }
                            })
                            .setActionTextColor(getColor(R.color.primaryTextColor));
                    storagePermSnackbar.show();
                    //Log.d(TAG,"should show rationale");
                    Log.d(TAG, "navigate/suggest user to choose the permission from settings");
                } else {
                    // show user message via snackbar about the missing permissions
                    Snackbar.make(mUserMsgLayout, SHOW_USER_MSG, Snackbar.LENGTH_LONG).show();
                }

            }
        } else if (requestCode == contactPermRequestCode) {
                if ((grantResults.length>0) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // if read contact permission is approved, initiate the cursor loader callbacks
                    //getSupportLoaderManager().initLoader(CONTACT_LOADER_ID,new Bundle(),contactsLoader);
                    // instead we use filter queryprovider
                    setFilter();
                } else if ((grantResults.length>0) && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (!shouldShowRequestPermissionRationale(readContactsPermission)) {
                        // invoked when user has rejected our requested and clicked the permission dialog to
                        // to never show again
                        // need to navigate the user to new settings app
                        smsPermSnackbar = Snackbar.make(mUserMsgLayout, READ_CONTACT_NOTIFICATION_MSG, Snackbar.LENGTH_LONG)
                                .setAction("SET PERMISSIONS", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        navigateToAppPermissions();
                                    }
                                })
                                .setActionTextColor(getColor(R.color.primaryTextColor));
                        ;
                        smsPermSnackbar.show();
                        //Log.d(TAG,"should show rationale");
                        Log.d(TAG, "navigate/suggest user to choose the permission from settings");
                    } else {
                        // show user message via snackbar about the missing permissions
                        Snackbar.make(mUserMsgLayout, SHOW_USER_MSG, Snackbar.LENGTH_SHORT).show();
                    }
                }
        }
    }

    private void setSmsConfig(){
        SharedPreferences.Editor configEditor = config.edit();
        // splits the string into words, removes duplicates, blank spaces and commas
        String[] extractedKeywords = messageStr.split("\\s*,\\s*|\\s+");
        Log.d(TAG, "keyword :" + Arrays.asList(extractedKeywords));
        Set<String> keywords = new HashSet<String>();
        Collections.addAll(keywords,extractedKeywords);
        // now accepts multiple keywords
        configEditor.putString("contact",contactStr.isEmpty()?null:contactStr);
        configEditor.putStringSet("message",keywords.isEmpty()?null:keywords);
        configEditor.apply();
        // a small temporary Ui Message to be displayed to user on successful config. save
        //@deprecated Toast.makeText(this,toastText,Toast.LENGTH_LONG).show();
        Snackbar.make(mUserMsgLayout,CONFIG_MSG,Snackbar.LENGTH_LONG).show();
        // notify the trigger status in view to the user
        trigButton.setText(triggerStatus);
        // set the clear button to initial state
        clearButton.setText(R.string.clear_button);
    }

    // one time slide animation between onboarding activity and main activity,
    // elements fly in from the end
    private void slideAnimation()
    {
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.END);
        slide.setDuration(400);
        slide.setInterpolator(new DecelerateInterpolator());
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
    }

    private void clearSoftKeyboardState (){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        // gets the current focused edit text
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            // hide the IME and make the focus to get lost for the edit text
        inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(),0);
        }
    }

    private void navigateToAppPermissions(){
        // post in the UI thread after 250 milli secs, wait till snackbar ends to start new activity in new task with no history in back stack and recents
        // when user exits
        if (smsPermSnackbar!=null){
            smsPermSnackbar.dismiss();
            smsPermSnackbar = null;
        }
        if (storagePermSnackbar!=null){
            storagePermSnackbar.dismiss();
            storagePermSnackbar = null;
        }

        // navigates the user to settings page, if he presses the dont ask again option
        // in system permission dialog
        navigateToSettings.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.fromParts("package",getPackageName(),null) );
                // we remove the activity from recents screen and also when the user presses the back button
                i.setFlags(FLAG_ACTIVITY_NEW_DOCUMENT|Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(i);
            }
        },250);
    }

    //create a simple cursor adapter to connect the contacts cursor values with our contact layout views
    private void setupContactAdapter(){
            String[] uiBindFrom = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
           int[] uiBindTo = {R.id.contact_title,R.id.contact_number };
           contactAdapter = new SimpleCursorAdapter(this, R.layout.contacts_view,null,uiBindFrom, uiBindTo,0);
    }

    //@deprecated -- not applicable for the present use case
//    private void instantiateLoader(){
//        contactsLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
//            @NonNull
//            @Override
//            public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
//                String[] columnstoRetrieve = new String[] {
//                        ContactsContract.CommonDataKinds.Phone._ID,
//                  ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//                  ContactsContract.CommonDataKinds.Phone.NUMBER
//                };
//
//                CursorLoader contactsCursorLoader = new CursorLoader(MainActivity.this,
//                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                        columnstoRetrieve,
//                        null,null,null);
//                return contactsCursorLoader;
//            }
//
//            @Override
//            public void onLoadFinished(@NonNull Loader<Cursor> loader, final Cursor cursor) {
//                    contactAdapter.swapCursor(cursor);
//            }
//
//            @Override
//            public void onLoaderReset(@NonNull Loader<Cursor> loader) {
//                    contactAdapter.swapCursor(null);
//            }
//        };
//    }

    private boolean checkReadContactPermission() {
        // check if the permissions have been granted before
       boolean mPermGranted = false;
        if((ContextCompat.checkSelfPermission(getApplicationContext(),readContactsPermission)== PackageManager.PERMISSION_GRANTED)) {
            mPermGranted = true;
        }  else {
            // we defer the dialog message approx. by two seconds, so that it appears natural to user.
            // this offloads the oncreate execution time.
            if (!isRequestDiagDisabled && !showOnboarding) {
                Handler delayShowingDialog = new Handler(getMainLooper());
                delayShowingDialog.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // throw a dialog box -- seeking user approval
                        MyDialogFragment userApprovalDialog = new MyDialogFragment();
                        //userApprovalDialog.setStyle(DialogFragment.STYLE_NO_TITLE,R.style.MyAppTheme);
                        userApprovalDialog.show(getSupportFragmentManager(), "AutoSuggestion");

                    }
                },1500);

              }
        }
        return mPermGranted;

    }

    // this will be invoked on user approval from the dialog window
    void requestContactPermission(){
        Log.d(TAG,"Request Permissions is invoked");
        // throw a system dialog to request permission from user, if permission hasn't been granted before
        requestPermissions(new String[]{readContactsPermission},contactPermRequestCode);
    }


    //before spamming the user check the shared preference file for existing config
    private boolean checkUserConfig(){
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("autoSuggDisabled",false);
    }

    // setting user config of disabling auto suggestion feature
    void setUserConfig(boolean disabled){
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
        sharedPrefEditor.putBoolean("autoSuggDisabled",disabled);
        sharedPrefEditor.apply();
    }

    // temporarily save this state, so that this will be used to determine contact picker behavior,
    // which will be enabled when auto suggestion feature is disabled or skipped
    public void setPopup(boolean disabled) {
        isRequestDiagDisabled = disabled;
    }

    // makes phone number to 10 digits, removes unwanted spaces and country code
    // used for post processing after picking the number from contacts app/ contacts provider
    private String spliceNumber(String phoneNumber){
        Log.d(TAG, "before splitting numbers : " + phoneNumber);
        if (phoneNumber.length()>10){
            // parses the number if it has any characters or whitespaces and splits and merges accordingly
            String[] number_parts = phoneNumber.split("[^\\d]+");
            Log.d(TAG, "splitted nums: "+ Arrays.toString(number_parts));
            StringBuilder formattedNum = new StringBuilder();
            for (String number_part : number_parts) {
                formattedNum.append(number_part);
            }
            // if the number is greater than 10 remove the country code prefix
            int formattedLength = formattedNum.length();
            return (formattedLength>10)?formattedNum.substring(formattedLength-10): formattedNum.toString();
        } else {
            return phoneNumber;
        }
    }

    // attach the filter to our adapter, to filter out the dropdown results
    private void setFilter(){
        contactAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {

                Log.d(TAG, "Filter query "+ constraint);
                // this runs the query in the background thread
                if (constraint == null || constraint.toString().isEmpty()){
                    Log.d(TAG, "Filter query is empty "+ constraint);
                    return null;
                }
                // checks whether the user inputted number pattern matches "*1245*" in the database
                // and dynamically updates the dropdown list and updates the view as user types
                String[] projection = {ContactsContract.CommonDataKinds.Phone._ID,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
                String toBeQueried = ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?";
                String[] selectionArgs = {"%"+constraint.toString()+"%"};
                return getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,projection,toBeQueried,selectionArgs,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

            }

        });
    }

}
