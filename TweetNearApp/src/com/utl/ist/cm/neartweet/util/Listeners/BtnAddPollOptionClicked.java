package com.utl.ist.cm.neartweet.util.Listeners;

import android.view.View;
import android.view.View.OnClickListener;

import com.utl.ist.cm.neartweet.MainActivity;

public class BtnAddPollOptionClicked
        implements OnClickListener {

    MainActivity activity;

    public BtnAddPollOptionClicked(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        activity.addPollOption();
    }

}
