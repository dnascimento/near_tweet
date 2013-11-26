package com.utl.ist.cm.neartweet.util.Facebook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.facebook.Session;
import com.utl.ist.cm.neartweet.R;

public class FacebookActivity extends
        FragmentActivity {

    private FacebookFragment facebookFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);


        if (savedInstanceState == null) {
            // Add fragment
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            facebookFragment = new FacebookFragment();
            Intent inta = getIntent();
            facebookFragment.setArguments(inta.getExtras());
            ft.add(android.R.id.content, facebookFragment).commit();
        } else {
            facebookFragment = (FacebookFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        }






    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.facebook, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
}
