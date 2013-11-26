package com.utl.ist.cm.neartweet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.utl.ist.cm.neartweet.dataStructures.TweetMsg;
import com.utl.ist.cm.neartweet.dataStructures.TweetObject;
import com.utl.ist.cm.neartweet.dataStructures.TweetPollVote;
import com.utl.ist.cm.neartweet.dataStructures.TweetSpamVote;
import com.utl.ist.cm.neartweet.p2p.WifiDirectSupport;
import com.utl.ist.cm.neartweet.util.AboutDialog;
import com.utl.ist.cm.neartweet.util.BitmapHelper;
import com.utl.ist.cm.neartweet.util.IFragmentCommunicationListener;
import com.utl.ist.cm.neartweet.util.LocationHelper;
import com.utl.ist.cm.neartweet.util.LocationHelper.LocationResult;
import com.utl.ist.cm.neartweet.util.PollAdapter;
import com.utl.ist.cm.neartweet.util.Storage;
import com.utl.ist.cm.neartweet.util.UsernameDialog;
import com.utl.ist.cm.neartweet.util.Utils;
import com.utl.ist.cm.neartweet.util.Facebook.FacebookActivity;
import com.utl.ist.cm.neartweet.util.Listeners.BtnAddPollOptionClicked;
import com.utl.ist.cm.neartweet.util.Listeners.OnImageClick;
import com.utl.ist.cm.neartweet.util.Tasks.DownloadPicture;



public class MainActivity extends
        FragmentActivity
        implements IFragmentCommunicationListener {

    /** Constants **/
    public static String SERVER_IP;
    private static final int MAX_NUM_CHARS = 160;
    private static final int FM_NOTIFICATION_ID = 0;
    public static final String MAIN_TWEET = "MainTweetId";
    private static final int MAX_SPAMS_TO_BAN = 3;

    /** Intent Results **/
    protected static final int INTENT_GALLERY_PIC_REQ = 1;
    protected static final int INTENT_TAKE_PHOTO_REQ = 2;
    protected static final int FACEBOOK_REQUEST = 3;

    public static final String MIME_TYPE_IMAGE = "image/*";

    /** Control variables **/
    private boolean mSendPicture = false;
    private boolean mSendLocation = false;
    private boolean mSendPoll = false;

    private LocationHelper mLocationHelper;
    // private NetworkTask networktask;

    private Location mLocation;
    private String mLocationName = "";
    private byte[] mPicture;

    /** Notification vars **/
    private NotificationCompat.Builder notificationBuilder;
    private int unreadMsgsCounter;
    private boolean isForegrounded;

    private String myUUID = "";
    private String myUsername = "";

    private WifiDirectSupport p2p;

    /**
     * mainTweets - guarda os tweets principais sobre a forma de apontadores para
     * identificadores. Para obter os tweets em questao, e necessario recorrer a ED
     * allTweet.
     */

    /** <timestamp,msgid> **/
    private Map<Long, TweetMsg> mainTweets;

    /** <msgID, userSpammerID> **/
    private Map<String, String> mySpamVotes;

    /** <userSpammerID, voteCounter> **/
    private Map<String, Integer> spammerBlackList;

    /** msgID, adapter **/
    private Map<String, View> tweetViews;

    /** myvotes **/
    private Map<String, String> myPollVotes;

    /** emoticons mapping **/
    private static final HashMap<String, Integer> emoticons = new HashMap<String, Integer>();
    static {
        emoticons.put(":'(", R.drawable.emo_im_crying);
        emoticons.put(":$", R.drawable.emo_im_embarrassed);
        emoticons.put(":)", R.drawable.emo_im_happy);
        emoticons.put(":D", R.drawable.emo_im_laughing);
        emoticons.put(":(", R.drawable.emo_im_sad);
        emoticons.put(":O", R.drawable.emo_im_surprised);
        emoticons.put(":p", R.drawable.emo_im_tongue_sticking_out);
        emoticons.put(":\\", R.drawable.emo_im_undecided);
        emoticons.put(";)", R.drawable.emo_im_winking);
    }

    /** INTERFACE **/
    private Dialog mTweetDialog;
    private TextView mTweetLeftChars;
    private EditText mTweetInput;
    private CheckBox mBtnPicture, mBtnLocation, mBtnPoll;
    private ViewGroup mPollContainer;
    private ImageButton mBtnAddPollOption;
    private ViewGroup mTweetRoot;

    /**
     * ########################## Activity Methods ##########################
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        final PullToRefreshScrollView mPullRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
        mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {


                p2p.pullRestart();

                mTweetRoot.removeAllViews();

                mainTweets = new TreeMap<Long, TweetMsg>();
                mySpamVotes = new HashMap<String, String>();
                spammerBlackList = new HashMap<String, Integer>();

                tweetViews = new HashMap<String, View>();
                myPollVotes = new HashMap<String, String>();

                DB.getInstance(getActivity()).loadAllTweetObj();

                mPullRefreshScrollView.onRefreshComplete();


            }
        });

        Log.i("", "onCreate");

        SERVER_IP = Utils.getStringFromSharedPrefs(this, R.string.server_ip_key);
        if (SERVER_IP.isEmpty()) {
            SERVER_IP = "ist.dynip.sapo.pt";
            Utils.commitStringToSharedPrefs(this, R.string.server_ip_key, SERVER_IP);
        }
        myUUID = Utils.getStringFromSharedPrefs(this, R.string.user_uuid_key);
        if (myUUID.isEmpty()) {
            myUUID = UUID.randomUUID().toString();
            Utils.commitStringToSharedPrefs(this, R.string.user_uuid_key, myUUID);
        }

        myUsername = Utils.getStringFromSharedPrefs(this, R.string.username_key);
        if (myUsername.isEmpty()) {

            int random = (int) (Math.random() * 10000 + 1);
            myUsername = "User" + random;

            Utils.commitStringToSharedPrefs(this, R.string.username_key, myUsername);
            showUsernameDialog(getString(R.string.set_username_dialog_title));
        }

        // init smap DSs
        mainTweets = new TreeMap<Long, TweetMsg>();
        mTweetRoot = (ViewGroup) findViewById(R.id.tweet_root);

        mySpamVotes = new HashMap<String, String>();
        spammerBlackList = new HashMap<String, Integer>();

        tweetViews = new HashMap<String, View>();
        myPollVotes = new HashMap<String, String>();

        // turn WIFI on
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled())
        	wifiManager.setWifiEnabled(true);

        // load messages
        DB.getInstance(this);

        p2p = new WifiDirectSupport(this, myUUID, myUsername);
        p2p.startWifiDirect();
        // p2p.startRegistrationAndDiscovery();



    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("", "onResume");
        isForegrounded = true;
        unreadMsgsCounter = 0;
        removeNotification();
        p2p.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("", "onPause");
        isForegrounded = false;
        p2p.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("", "onDestroy -> System.exit()!");
        super.finish();
        finish();

        System.exit(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        Log.d("", "onBackPressed Called");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit NeatTweet?");
        builder.setMessage("Are you sure you want to quit? You will not receive future tweets.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                onBackPressedDefault();
                dialog.dismiss();

            }

        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.tweet:

            // lock if is spammer
            if (spammerBlackList.containsKey(myUUID)) {
                if (spammerBlackList.get(myUUID) > MAX_SPAMS_TO_BAN) {
                    showToast("Sorry, you can send more Tweets!");
                    return true;
                }

            }

            showTweetDialog(null, false);
            return true;

        case R.id.about:
            AboutDialog about = new AboutDialog(this);
            about.setTitle("About NearTweet");
            about.show();
            return true;

        case R.id.change_username:
            showUsernameDialog(getString(R.string.change_username_dialog_title));
            return true;

        /*
        case R.id.change_server_ip:
            DialogFragment serverIPDialog = new ServerIPDialog();
            serverIPDialog.show(getSupportFragmentManager(), "change_server_ip");
            return true;
		*/
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * ########################## Private Methods ##########################
     */
    private void showTweetDialog(final String parentTweetId, final boolean isPrivate) {

        LocationResult locationResult = new LocationResult() {
            @Override
            public void setLocation(Location location) {
                mLocation = location;
            }
        };

        mLocationHelper = new LocationHelper();
        mLocationHelper.start(this, locationResult);

        View view = getLayoutInflater().inflate(R.layout.tweet_dialog, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(view);

        dialogBuilder.setPositiveButton(R.string.tweet,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                        int id) {
                                                mLocationHelper.stop();

                                                String tweetmsg = mTweetInput.getText()
                                                                             .toString();
                                                TweetMsg msg = new TweetMsg(myUUID,
                                                        myUsername, parentTweetId,
                                                        tweetmsg);

                                                msg.setPrivacy(isPrivate);

                                                if (mSendPicture) {
                                                    msg.setResource(mPicture);
                                                }

                                                if (mSendLocation) {
                                                    msg.setLocation_Lat(mLocation.getLatitude());
                                                    msg.setLocation_Lon(mLocation.getLongitude());
                                                    msg.setLocationName(mLocationName);
                                                }

                                                if (mSendPoll) {
                                                    for (int i = 0; i < mPollContainer.getChildCount() - 1; i++) {

                                                        String pollOption = ((EditText) mPollContainer.getChildAt(i)).getText()
                                                                                                                     .toString();
                                                        if (!pollOption.isEmpty()) {
                                                            msg.insertPollOptn(pollOption);
                                                        }
                                                    }
                                                    mSendPoll = false;
                                                }

                                                sendNewMsg(msg);


                                                Log.i("", "Novo tweet enviado");

                                                mPicture = null;
                                                mSendPicture = false;
                                                mSendLocation = false;
                                                mSendPoll = false;
                                                mLocationName = "";

                                            }


                                        });
        dialogBuilder.setNegativeButton(android.R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                        int id) {
                                                mLocationHelper.stop();
                                            }
                                        });

        mTweetDialog = dialogBuilder.create();
        mImageView = (ImageView) view.findViewById(R.id.imageViewerTweet);

        mTweetLeftChars = (TextView) view.findViewById(R.id.tweet_left_chars);
        mTweetInput = (EditText) view.findViewById(R.id.tweet_input);
        mTweetInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTweetLeftChars();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mBtnPicture = (CheckBox) view.findViewById(R.id.btn_picture);
        mBtnPicture.setOnCheckedChangeListener(btnPictureCheckChanged);

        mBtnLocation = (CheckBox) view.findViewById(R.id.btn_location);
        mBtnLocation.setOnCheckedChangeListener(btnLocationCheckChanged);

        // mLocationLabel = (TextView) findViewById(R.id.location_label);

        mBtnPoll = (CheckBox) view.findViewById(R.id.btn_poll);
        mBtnPoll.setOnCheckedChangeListener(btnPollCheckChanged);

        mPollContainer = (ViewGroup) view.findViewById(R.id.poll_container);

        mBtnAddPollOption = (ImageButton) view.findViewById(R.id.btn_add_poll_option);
        mBtnAddPollOption.setOnClickListener(new BtnAddPollOptionClicked(this));

        mTweetDialog.show();

        updateTweetLeftChars();
    }

    private void addNotification(String notificationMsg) {
        if (notificationBuilder == null) {
            notificationBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher_small)
                                                                      .setContentTitle("NearTweet")
                                                                      .setContentText(notificationMsg)
                                                                      .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            notificationBuilder.setOnlyAlertOnce(true);

        } else {
            notificationBuilder.setContentText(notificationMsg);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                                                                0,
                                                                notificationIntent,
                                                                PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(FM_NOTIFICATION_ID, notificationBuilder.build());
    }

    private void onBackPressedDefault() {
        super.onBackPressed();
        Log.d("", "onBackPressed Super Called");
    }

    // Remove notification
    private void removeNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(FM_NOTIFICATION_ID);
        notificationBuilder = null;
    }

    private void showUsernameDialog(String title) {
        DialogFragment usernameDialog = new UsernameDialog();

        Bundle args = new Bundle();
        args.putString("dialog_title", title);
        usernameDialog.setArguments(args);

        usernameDialog.show(getSupportFragmentManager(), "change_username");
    }

    private void updateTweetLeftChars() {
        mTweetLeftChars.setText(String.valueOf(MAX_NUM_CHARS - mTweetInput.length()));
        enableTweet();
    }

    private void enableTweet() {

        if (mTweetInput.length() > 0) {
            ((AlertDialog) mTweetDialog).getButton(DialogInterface.BUTTON_POSITIVE)
                                        .setEnabled(true);
        } else {
            ((AlertDialog) mTweetDialog).getButton(DialogInterface.BUTTON_POSITIVE)
                                        .setEnabled(false);
        }
    }

    private void showImagePickerDialog() {
        imagePickerDialogView = getLayoutInflater().inflate(R.layout.image_picker, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(imagePickerDialogView);
        dialogBuilder.setTitle(R.string.image_picker_title);

        imagePickerDialog = dialogBuilder.create();
        imagePickerDialog.show();

        // Handlers de botoes da popup

        ImageButton from_camera = (ImageButton) imagePickerDialogView.findViewById(R.id.from_camera);
        from_camera.setOnClickListener(btnCamaraPicture);

        ImageButton from_gallery = (ImageButton) imagePickerDialogView.findViewById(R.id.from_gallery);
        from_gallery.setOnClickListener(btnGalleryPicture);

        ImageButton from_url = (ImageButton) imagePickerDialogView.findViewById(R.id.from_url);
        from_url.setOnClickListener(btnFromUrl);

    }

    private TweetMsg insertReply(TweetMsg tweet, Collection<TweetMsg> tweets) {

        TweetMsg parentTweet = null;
        for (TweetMsg t : tweets) {
            if (t.getMsgID().equals(tweet.getParentMsgID())) {
                t.addReply(tweet);
                parentTweet = t;
                break;
            } else if (t.getReplies().size() > 0) {
                parentTweet = insertReply(tweet, t.getReplies().values());
            }
        }

        return parentTweet;
    }

    private TweetMsg getTweet(String tweetId, Collection<TweetMsg> tweets) {

        TweetMsg tweet = null;
        for (TweetMsg t : tweets) {
            if (t.getMsgID().equals(tweetId)) {
                tweet = t;
                break;
            } else if (t.getReplies().size() > 0) {
                tweet = getTweet(tweetId, t.getReplies().values());
            }
        }

        return tweet;
    }

    private void galleryAddPic(String photoPath) {
        Intent mediaScanIntent = new Intent(
                "android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private synchronized void updateTweetThread(final TweetMsg tweet, TweetMsg parentTweet) {

        // test if userID is a spammer
        if (spammerBlackList.containsKey(tweet.getUserID())
                && !tweet.getUserID().equals(myUUID)) {
            showToast("" + spammerBlackList.get(tweet.getUserID()));
            if (spammerBlackList.get(tweet.getUserID()) > MAX_SPAMS_TO_BAN) {
                return;
            }
        }

        View tweetView;
        final ViewGroup tweetContainer;

        if (tweet.isRootMsg()) {
            tweetContainer = mTweetRoot;
        } else {

            View parentTweetView = mTweetRoot.findViewWithTag(tweet.getParentMsgID());

            tweetContainer = (ViewGroup) parentTweetView.findViewById(R.id.tweet_replies);
            if (tweet.getUserID() == myUUID)
            {
            	tweetContainer.setVisibility(View.VISIBLE);
            	// tweetContainer.requestFocus();
            }
            
            // Update parent replies
            TextView numberOfReplies = (TextView) parentTweetView.findViewById(R.id.number_of_replies);

            parentTweetView.findViewById(R.id.main_tweet)
                           .setOnClickListener(new OnClickListener() {

                               @Override
                               public void onClick(View v) {
                                   if (tweetContainer.getVisibility() == View.VISIBLE) {
                                       tweetContainer.setVisibility(View.GONE);
                                   } else {
                                       tweetContainer.setVisibility(View.VISIBLE);
                                   }
                               }
                           });

            numberOfReplies.setText(parentTweet.getReplies().size()
                    + ((parentTweet.getReplies().size() > 1) ? this.getString(R.string.replies)
                            : this.getString(R.string.reply)));
            numberOfReplies.setVisibility(View.VISIBLE);
        }

        tweetView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.tweet,
                                                                                                 tweetContainer,
                                                                                                 false);
        tweetView.setTag(tweet.getMsgID());

        tweetView.findViewById(R.id.btn_private_reply)
                 .setOnClickListener(new OnClickListener() {

                     @Override
                     public void onClick(View v) {

                         // lock if is spammer
                         if (spammerBlackList.containsKey(myUUID)) {
                             if (spammerBlackList.get(myUUID) > MAX_SPAMS_TO_BAN) {
                                 showToast("Sorry, you can send more Tweets!");
                                 return;
                             }
                         }

                         showTweetDialog(tweet.getMsgID(), true);
                     }
                 });

        tweetView.findViewById(R.id.btn_public_reply)
                 .setOnClickListener(new OnClickListener() {

                     @Override
                     public void onClick(View v) {

                         // lock if is spammer
                         if (spammerBlackList.containsKey(myUUID)) {
                             if (spammerBlackList.get(myUUID) > MAX_SPAMS_TO_BAN) {
                                 showToast("Sorry, you can send more Tweets!");
                                 return;
                             }
                         }

                         showTweetDialog(tweet.getMsgID(), false);
                     }
                 });

        tweetView.findViewById(R.id.btn_retweet)
                 .setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         Intent intent = new Intent(getApplicationContext(),
                                 FacebookActivity.class);

                         Bundle postParams = new Bundle();

                         postParams.putString("name", "NearTweet Android App");
                         postParams.putString("caption",
                                              "Project of Mobile Computing - IST");
                         postParams.putString("description", "" + tweet.getUserName()
                                 + " say: " + tweet.getMessage());

                         postParams.putString("message", tweet.getUserName() + " says: "
                                 + tweet.getMessage());

                         // Bitmap imageBitmap =
                         // BitmapFactory.decodeByteArray(tweet.getResource(),
                         // 0,
                         // tweet.getResource().length);
                         // ByteArrayOutputStream baos = new
                         // ByteArrayOutputStream();
                         // imageBitmap.compre2s(Bitmap.CompressFormat.JPEG, 100,
                         // baos);
                         // byte[] data = baos.toByteArray();
                         //
                         // postParams.putByteArray("photo", data);

                         // postParams.putByteArray("photo", data);

                         postParams.putString("link", "http://www.ist.utl.pt/");
                         postParams.putString("picture",
                                              "http://web.ist.utl.pt/ist176590/neartweet_logofb.png");

                         if (tweet.getLocation_Lat() != 0.0) {
                             double lat = tweet.getLocation_Lat();
                             double lon = tweet.getLocation_Lon();
                             String localityName = tweet.getLocationName();

                             String url = "https://maps.google.com/maps?q=" + lat + ","
                                     + lon + "+(" + localityName + ")&z=14&ll=" + lat
                                     + "," + lon;

                             postParams.putString("link", url);

                             postParams.putString("description", "Send from "
                                     + localityName);

                         }

                         intent.putExtras(postParams);
                         startActivityForResult(intent, FACEBOOK_REQUEST);
                     }
                 });

        final AlertDialog.Builder spamDialogConfirm = new AlertDialog.Builder(this);

        if (tweet.getUserID().equals(myUUID)) {
            tweetView.findViewById(R.id.btn_report_spam).setVisibility(View.GONE);
        } else {
            tweetView.findViewById(R.id.btn_report_spam)
                     .setOnClickListener(new OnClickListener() {

                         @Override
                         public void onClick(View v) {
                             final String spammerID = tweet.getUserID();

                             if (mySpamVotes.containsKey(tweet.getMsgID())) {
                                 showToast("You already marked this tweet as spam!");
                                 return;
                             }

                             spamDialogConfirm.setTitle(getString(R.string.spam_dialog_title));
                             spamDialogConfirm.setIcon(R.drawable.warning);
                             spamDialogConfirm.setMessage(getString(R.string.spammer1)
                                     + tweet.getUserName() + getString(R.string.spammer2));

                             spamDialogConfirm.setPositiveButton(android.R.string.ok,
                                                                 new DialogInterface.OnClickListener() {

                                                                     @Override
                                                                     public void onClick(
                                                                             DialogInterface dialog,
                                                                                 int which) {

                                                                         TweetSpamVote spamVote = new TweetSpamVote(
                                                                                 myUUID,
                                                                                 spammerID,
                                                                                 tweet.getMsgID());
                                                                         sendNewMsg(spamVote);
                                                                     }

                                                                 });

                             spamDialogConfirm.setNegativeButton(android.R.string.cancel,
                                                                 new DialogInterface.OnClickListener() {

                                                                     @Override
                                                                     public void onClick(
                                                                             DialogInterface dialog,
                                                                                 int which) {
                                                                         // Do nothing
                                                                         dialog.dismiss();
                                                                     }
                                                                 });

                             AlertDialog dialog = spamDialogConfirm.create();
                             dialog.show();

                         }
                     });
        }

        if (tweet.isPrivate()) {

            tweetView.findViewById(R.id.private_tweet).setVisibility(View.VISIBLE);
            tweetView.findViewById(R.id.btn_public_reply).setEnabled(false);
        }

        ((TextView) tweetView.findViewById(R.id.tweet_timestamp)).setText(Utils.getElapsedTime(new Date(
                tweet.getTimestamp())) + getString(R.string.ago));
        ((TextView) tweetView.findViewById(R.id.tweet_author)).setText(tweet.getUserName());

        ((TextView) tweetView.findViewById(R.id.tweet_message)).setText(getSmiledText(getApplicationContext(),
                                                                                      tweet.getMessage()));

        ImageView tweetImage = (ImageView) tweetView.findViewById(R.id.tweet_image);
        if (tweet.getResource() != null) {
            Bitmap image = BitmapHelper.byteArr2BitmapThumb(tweet.getResource());
            tweetImage.setImageBitmap(image);
            tweetImage.setVisibility(View.VISIBLE);

            // Save image
            try {
                Uri imageURL = Storage.saveImageToGallery(image,
                                                          getApplicationContext(),
                                                          tweet.getUserID() + "_"
                                                                  + tweet.getTimestamp());
                galleryAddPic(imageURL.getPath());

                tweetImage.setOnClickListener(new OnImageClick(imageURL, this));
            } catch (Exception e) {
                Log.e("STORAGE", "Cannot save File:" + e.getMessage());
            }
        } else {
            tweetImage.setVisibility(View.GONE);
        }

        if (tweet.getLocation_Lat() != 0.0 && tweet.getLocation_Lon() != 0.0) {

            LinearLayout btn_location = (LinearLayout) tweetView.findViewById(R.id.btn_location);
            btn_location.setVisibility(View.VISIBLE);

            final ImageView btn_locationImg = (ImageView) tweetView.findViewById(R.id.btn_gpslocationimg);

            final TextView btn_locationName = (TextView) tweetView.findViewById(R.id.btn_gpslocationname);
            btn_locationName.setText("Sent from " + tweet.getLocationName());

            btn_location.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        btn_locationImg.setImageDrawable(getResources().getDrawable(R.drawable.location_on));
                        btn_locationName.setTextColor(Color.parseColor("#33b5e5"));
                        break;

                    case MotionEvent.ACTION_OUTSIDE:

                        btn_locationImg.setImageDrawable(getResources().getDrawable(R.drawable.location_off));
                        btn_locationName.setTextColor(Color.BLACK);
                        break;

                    case MotionEvent.ACTION_UP:

                        btn_locationImg.setImageDrawable(getResources().getDrawable(R.drawable.location_off));
                        btn_locationName.setTextColor(Color.BLACK);

                        try {
                            String uri = String.format(Locale.ENGLISH,
                                                       "geo:%f,%f",
                                                       tweet.getLocation_Lat(),
                                                       tweet.getLocation_Lon());
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                        } catch (Exception e) {
                            showToast("You need Google Maps!");
                        }

                        break;
                    }
                    return true;
                }
            });

            tweetViews.put(tweet.getMsgID(), tweetView);
        }

        ListView tweetPoll = (ListView) tweetView.findViewById(R.id.tweet_poll);
        if (tweet.getPoll().size() > 0) {

            // String[] pollOptions = tweet.getPoll().keySet()
            // .toArray(new String[tweet.getPoll().size()]);

            PollAdapter tweetPollAdapter = new PollAdapter(this, tweet);
            tweetPoll.setAdapter(tweetPollAdapter);

            int totalHeight = 0;
            for (int i = 0; i < tweetPollAdapter.getCount(); i++) {
                View listItem = tweetPollAdapter.getView(i, null, tweetPoll);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = tweetPoll.getLayoutParams();
            params.height = totalHeight
                    + (tweetPoll.getDividerHeight() * (tweetPoll.getCount() - 1));
            tweetPoll.setLayoutParams(params);
            tweetPoll.requestLayout();
            tweetPoll.setVisibility(View.VISIBLE);
        } else {
            tweetPoll.setVisibility(View.GONE);
        }

        TextView numberOfReplies = (TextView) tweetView.findViewById(R.id.number_of_replies);

        if (tweet.getReplies().size() > 0) {

            final ViewGroup replies = (ViewGroup) tweetView.findViewById(R.id.tweet_replies);

            tweetView.findViewById(R.id.main_tweet)
                     .setOnClickListener(new OnClickListener() {

                         @Override
                         public void onClick(View v) {
                             if (replies.getVisibility() == View.VISIBLE) {
                                 replies.setVisibility(View.GONE);
                             } else {
                                 replies.setVisibility(View.VISIBLE);
                             }
                         }
                     });

            numberOfReplies.setText(tweet.getReplies().size()
                    + ((tweet.getReplies().size() > 1) ? this.getString(R.string.replies)
                            : this.getString(R.string.reply)));
            numberOfReplies.setVisibility(View.VISIBLE);
        } else {
            numberOfReplies.setVisibility(View.GONE);
        }

        if (tweet.isRootMsg()) {
            tweetContainer.addView(tweetView, 0);
        } else {
            tweetContainer.addView(tweetView);
        }

        tweetViews.put(tweet.getMsgID(), tweetView);
    }

    private AlertDialog imagePickerDialog;
    private ImageView mImageView;
    private View imagePickerDialogView;

    /**
     * ########################## Event Listeners ##########################
     */
    private final OnCheckedChangeListener btnPictureCheckChanged = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (!mSendPicture) {
                    mBtnPicture.setChecked(false);
                    showImagePickerDialog();
                }
            } else {
                removeThumbnailImage();
            }
        }
    };
    private final OnClickListener btnFromUrl = new OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

            alert.setTitle("Get picture from URL");

            // Set an EditText view to get user input
            final EditText inputName = new EditText(v.getContext());
            inputName.setHint("Paste a URL here...");
            alert.setView(inputName);

            alert.setPositiveButton("Insert picture",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                    int whichButton) {
                                            imagePickerDialog.dismiss();
                                            String url = inputName.getText().toString();
                                            setImageFromURL(url);
                                        }
                                    });

            alert.show();

        }
    };

    private final OnClickListener btnGalleryPicture = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent getFromGalleryIntent = new Intent(Intent.ACTION_PICK);
            getFromGalleryIntent.setType("image/*");
            removeThumbnailImage();
            startActivityForResult(getFromGalleryIntent, INTENT_GALLERY_PIC_REQ);

        }
    };

    private final OnClickListener btnCamaraPicture = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            removeThumbnailImage();
            startActivityForResult(takePictureIntent, INTENT_TAKE_PHOTO_REQ);
        }
    };

    private final OnCheckedChangeListener btnLocationCheckChanged = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                mBtnLocation.setText("Getting location...");
                // mLocationLabel.setText("Getting location...");
                showToast("Getting location...");

                if (mLocation != null) {

                    String locationCoords = Location.convert(mLocation.getLatitude(),
                                                             Location.FORMAT_DEGREES)
                            + "; "
                            + Location.convert(mLocation.getLongitude(),
                                               Location.FORMAT_DEGREES);

                    showToast(locationCoords);

                    Geocoder geocoder = new Geocoder(getApplicationContext(),
                            Locale.getDefault());
                    try {
                        List<Address> listAddresses = geocoder.getFromLocation(mLocation.getLatitude(),
                                                                               mLocation.getLongitude(),
                                                                               1);

                        if (null != listAddresses && listAddresses.size() > 0) {
                            String locationName = listAddresses.get(0).getLocality();

                            if (locationName == null) {
                                locationName = listAddresses.get(0).getThoroughfare();
                            }

                            if (locationName == null) {
                                locationName = listAddresses.get(0).getCountryName();
                            } else {
                                locationName += ", "
                                        + listAddresses.get(0).getCountryName();
                            }

                            // showToast(""+listAddresses.size());
                            // showToast("admin area "+listAddresses.get(0).getAdminArea());
                            // showToast("country  "+listAddresses.get(0).getCountryName());
                            // showToast("subadmin area "+listAddresses.get(0).getSubAdminArea());
                            // showToast("sub locality "+listAddresses.get(0).getSubLocality());
                            // showToast("thou... "+listAddresses.get(0).getThoroughfare());

                            if (locationName == null) {
                                mBtnLocation.setText(locationCoords);
                                // mLocationLabel.setText(locationCoords);
                                mLocationName = locationCoords;
                            } else {
                                mBtnLocation.setText(locationName);
                                // mLocationLabel.setText(locationName);
                                mLocationName = locationName;
                            }

                            mSendLocation = true;

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                mSendLocation = false;
                mLocationName = "";
                mBtnLocation.setText("");
                // mLocationLabel.setText("");

            }
        }
    };

    private final OnCheckedChangeListener btnPollCheckChanged = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                mSendPoll = true;

                addPollOption();
                mPollContainer.setVisibility(View.VISIBLE);
            } else {
                mSendPoll = false;

                mPollContainer.setVisibility(View.GONE);
                removePollOptions();
            }
        }
    };

    /**
     * ########################## Public Methods ##########################
     */
    // public NetworkTask getNetworktask() {
    // return networktask;
    // }

    public String getMyUUID() {
        return myUUID;
    }

    public Map<String, String> getMyPollVotes() {
        return myPollVotes;
    }

    public final void setImageFromURL(String urlString) {

        Log.v("URL Download", "Start");
        DownloadPicture downloadThread = new DownloadPicture(mImageView, this);
        downloadThread.execute(urlString);
    }

    public void addPollOption() {
        EditText pollOptionInput = new EditText(this);
        pollOptionInput.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        pollOptionInput.setHint(R.string.poll_option_hint);

        pollOptionInput.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
                MAX_NUM_CHARS) });

        pollOptionInput.setFocusableInTouchMode(true);
        pollOptionInput.requestFocus();

        pollOptionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mBtnAddPollOption.setEnabled(true);
                } else {
                    mBtnAddPollOption.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mBtnAddPollOption.setEnabled(false);
        mPollContainer.addView(pollOptionInput, mPollContainer.getChildCount() - 1);
    }

    public void removePollOptions() {
        int optionCount = mPollContainer.getChildCount() - 1;
        for (int i = optionCount; i > 0; i--) {
            mPollContainer.removeViewAt(0);
        }
    }

    // Invoked by NetworTask during retrieval
    public synchronized void handleTweetObj(TweetObject tweetObj, boolean notify) {


        switch (tweetObj.type) {

        case MSG:
            TweetMsg tweetMsg = (TweetMsg) tweetObj;
            TweetMsg parentTweetMsg = null;

            // Store tweet
            if (tweetMsg.isPrivate()) {

                String tweetUserID = tweetMsg.getUserID();
                String parentTweetUserID = getTweet(tweetMsg.getParentMsgID(),
                                                    mainTweets.values()).getUserID();

                // Privacy
                if (!(myUUID.equals(tweetUserID) || myUUID.equals(parentTweetUserID))) {
                    return;
                }
            }

            if (tweetMsg.isRootMsg()) {
                mainTweets.put(tweetMsg.getTimestamp(), tweetMsg);
            } else {
                parentTweetMsg = insertReply(tweetMsg, mainTweets.values());
            }

            Log.i("HandleMsg", tweetMsg.getMsgID() + tweetMsg.getMessage());

            // Update interface
            updateTweetThread(tweetMsg, parentTweetMsg);
            // vibrate(50);

            // Notifications
            if (!isForegrounded && notify) {
                if (unreadMsgsCounter == 0) {
                    addNotification("You have a new tweet!");
                    unreadMsgsCounter++;
                } else {
                    addNotification("You have " + ++unreadMsgsCounter + " new tweets!");
                }
            }
            break;

        case POLL_VOTE:
            updateTweetVote((TweetPollVote) tweetObj);

            break;

        case SPAM_VOTE:
            TweetSpamVote tweetSmapVote = (TweetSpamVote) tweetObj;

            if (tweetSmapVote.getVoteFromUserID().equals(myUUID)) {
                mySpamVotes.put(tweetSmapVote.getRelativeMsgID(),
                                tweetSmapVote.getSpammerUserID());
            }

            if (spammerBlackList.containsKey(tweetSmapVote.getSpammerUserID())) {
                Integer spamCounter = spammerBlackList.get(tweetSmapVote.getSpammerUserID());
                spammerBlackList.put(tweetSmapVote.getSpammerUserID(), ++spamCounter);
            } else {
                spammerBlackList.put(tweetSmapVote.getSpammerUserID(), 1);
            }
            break;

        default:
            break;
        }
    }

    void updateTweetVote(final TweetPollVote vote) {
        View v = tweetViews.get(vote.getParentMsgID());
        ListView poll = (ListView) v.findViewById(R.id.tweet_poll);

        int i = 0;
        LinearLayout pollOption = (LinearLayout) poll.getChildAt(i++);

        if (pollOption == null) {
            mTweetRoot.post(new Runnable() {
                @Override
                public void run() {
                    updateTweetVote(vote);
                }
            });
            return;
        } else {
            while (pollOption != null) {
                TextView pollOptnText = (TextView) pollOption.findViewById(R.id.poll_option);

                if (pollOptnText.getText().toString().equals(vote.getPollOptn())) {
                    CheckBox cb = (CheckBox) pollOption.findViewById(R.id.poll_vote);
                    int counter = Integer.parseInt(cb.getText().toString());
                    cb.setText(++counter + "");

                    if (vote.getVoteFromUserID().equals(myUUID)) {
                        myPollVotes.put(vote.getParentMsgID(), vote.getPollOptn());
                        cb.setChecked(true);
                    }

                    break;
                }
                pollOption = (LinearLayout) poll.getChildAt(i++);
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.v("Activity Intent return", "Result Received");
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
            case INTENT_TAKE_PHOTO_REQ:
                imagePickerDialog.dismiss();
                Bitmap mImageBitmap = (Bitmap) intent.getExtras().get("data");
                String timeStamp = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
                /* String url = */
                MediaStore.Images.Media.insertImage(getContentResolver(),
                                                    mImageBitmap,
                                                    "NearTweet_" + timeStamp,
                                                    "NearTweet Photo");
                setThumbnailImage(mImageBitmap);
                break;
            case INTENT_GALLERY_PIC_REQ:
                imagePickerDialog.dismiss();
                Uri photoUri = intent.getData();
                if (photoUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                                                                          photoUri);
                        setThumbnailImage(bitmap);
                    } catch (FileNotFoundException e) {
                        Log.e("ERROR", "Import from gallery: File not found");
                    } catch (IOException e) {
                        Log.e("ERROR", "Import from gallery: IO Error");
                    }
                }
            default:
                Log.e("ERROR", "Invalid activity result");
                break;
            }
        }
    }

    public void removeThumbnailImage() {
        mImageView.setImageBitmap(null);
        mImageView.setVisibility(View.INVISIBLE);
        mPicture = null;
        mSendPicture = false;
        mBtnPicture.setChecked(false);
    }

    public void setThumbnailImage(Bitmap image) {
        mImageView.setImageBitmap(image);
        mImageView.setVisibility(View.VISIBLE);
        mPicture = BitmapHelper.bitmap2ByteArr(image);
        mSendPicture = true;
        mBtnPicture.setChecked(true);
    }

    public void vibrate(int ms) {
        // Get instance of Vibrator from current Context
        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (ms != -1) {
            mVibrator.vibrate(ms);
        } else {
            long[] pattern = { 0, 100, 50, 100, 50, 100 };
            mVibrator.vibrate(pattern, -1);
        }
    }

    // call this toast from anywhere
    public void showToast(final String msg) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onUsernameChange(String newUsername) {

        myUsername = newUsername;
        Utils.commitStringToSharedPrefs(this, R.string.username_key, myUsername);
    }

    public static Spannable getSmiledText(Context context, String text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        int index;
        for (index = 0; index < builder.length(); index++) {
            for (Entry<String, Integer> entry : emoticons.entrySet()) {
                int length = entry.getKey().length();
                if (index + length > builder.length()) {
                    continue;
                }
                if (builder.subSequence(index, index + length)
                           .toString()
                           .equals(entry.getKey())) {
                    builder.setSpan(new ImageSpan(context, entry.getValue()),
                                    index,
                                    index + length,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    index += length - 1;
                    break;
                }
            }
        }
        return builder;
    }

    @Override
    public void onServerIPChange(String newServerIP) {
        Utils.commitStringToSharedPrefs(this, R.string.server_ip_key, newServerIP);
        System.exit(0);
    }


    public void sendNewMsg(TweetObject msg) {
        DB.getInstance(this).insertTweetObj(msg);
        p2p.multicastMessage(msg);
        handleTweetObj(msg, false);
    }

    public MainActivity getActivity() {
        return this;
    }
}
