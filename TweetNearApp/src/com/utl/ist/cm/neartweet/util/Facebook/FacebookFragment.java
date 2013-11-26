package com.utl.ist.cm.neartweet.util.Facebook;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.utl.ist.cm.neartweet.R;

public class FacebookFragment extends
        Fragment {

    private static final String TAG = "Facebook";
    private Button shareButton;
    private UiLifecycleHelper uiHelper;

    private Bundle postData;
    private LoginButton authButton;
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private boolean pendingPublishReauthorization = false;


    public static FacebookFragment newInstance(Bundle postParams) {
        FacebookFragment f = new FacebookFragment();
        f.setArguments(postParams);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
                ViewGroup container,
                Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_facebook, container, false);

        authButton = (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("user_likes", "user_status"));

        postData = getArguments();

        shareButton = (Button) view.findViewById(R.id.shareButton);
        TextView text = (TextView) view.findViewById(R.id.facebook_tweet);
        // ImageView imageView = (ImageView) view.findViewById(R.id.imageFacebook);
        // Bitmap image = BitmapHelper.byteArr2Bitmap(postData.getByteArray("photo"));
        // imageView.setImageBitmap(image);

        text.setText(postData.getString("message"));
        if (savedInstanceState != null) {
            pendingPublishReauthorization = savedInstanceState.getBoolean(PENDING_PUBLISH_KEY,
                                                                          false);
        }


        Session session = Session.getActiveSession();
        if (session != null) {
            SessionState state = session.getState();
            updateSessionState(state);
        }

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishStory(postData);
            }
        });
        return view;
    }


    private void hideButtons() {
        shareButton.setVisibility(View.GONE);
        authButton.setVisibility(View.GONE);
    }

    private void updateSessionState(SessionState state) {
        if (state.isOpened()) {
            Log.i(TAG, "Loggeed in...");
            shareButton.setVisibility(View.VISIBLE);
            if (pendingPublishReauthorization
                    && state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
                pendingPublishReauthorization = false;
                publishStory(postData);
            }
        } else {
            Log.i(TAG, "Logged out...");
            shareButton.setVisibility(View.INVISIBLE);
        }
    }

    public void onSessionStateChange(
            Session session,
                SessionState state,
                Exception exception) {

        updateSessionState(state);
    }

    private void publishStory(Bundle postData) {
        hideButtons();
        Log.i(TAG, "Post Data: " + postData.toString());

        Session session = Session.getActiveSession();

        if (session != null) {
            List<String> permissions = session.getPermissions();
            if (!isSubsetOf(PERMISSIONS, permissions)) {
                pendingPublishReauthorization = true;
                Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
                        this, PERMISSIONS);
                session.requestNewPublishPermissions(newPermissionsRequest);
                return;
            }

            Callback callback = new Callback() {
                @Override
                public void onCompleted(Response response) {
                    FacebookRequestError error = response.getError();

                    if (error != null) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                       "ERROR: " + error.getErrorMessage(),
                                       Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                        return;
                    }


                    String postId = null;
                    try {
                        JSONObject graphResponse = response.getGraphObject()
                                                           .getInnerJSONObject();
                        postId = graphResponse.getString("id");
                    } catch (JSONException e) {
                        Log.i(TAG, "JSON error: " + e.getMessage());
                        return;
                    }


                    Toast.makeText(getActivity().getApplicationContext(),
                                   "Retweet successfully done on your Facebook ("
                                           + postId + ")",
                                   Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }

            };

            Log.i(TAG, "Data to post: " + postData.toString());

            Request request = new Request(session, "me/feed", postData, HttpMethod.POST,
                    callback);


            RequestAsyncTask task = new RequestAsyncTask(request);

            task.execute();
        }
    }

    private final Session.StatusCallback callback = new Session.StatusCallback() {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    // LifeCycle Methods
    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    // Save the publish Flag if appkicationStops.
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
        uiHelper.onSaveInstanceState(outState);
    }

    private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }

}
