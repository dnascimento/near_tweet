package com.utl.ist.cm.neartweet.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.utl.ist.cm.neartweet.MainActivity;
import com.utl.ist.cm.neartweet.R;
import com.utl.ist.cm.neartweet.dataStructures.TweetMsg;
import com.utl.ist.cm.neartweet.dataStructures.TweetPollVote;

public class PollAdapter extends
        BaseAdapter {

    private static final int LAYOUT_RESOURCE_ID = R.layout.poll_option;

    private final Context mContext;
    private final MainActivity activity;
    private final String[] mPollOptions;
    private final TweetMsg tweet;

    public PollAdapter(MainActivity activity, TweetMsg tweet) {

        mContext = activity;
        this.activity = activity;
        this.tweet = tweet;
        mPollOptions = tweet.getPoll()
                            .keySet()
                            .toArray(new String[tweet.getPoll().size()]);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        PollOptionHolder pollOptionsHolder = null;
        final String pollOption = mPollOptions[position];

        if (convertView == null) {

            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(LAYOUT_RESOURCE_ID, parent, false);

            pollOptionsHolder = new PollOptionHolder();
            pollOptionsHolder.pollOption = (TextView) convertView.findViewById(R.id.poll_option);
            pollOptionsHolder.pollVote = (CheckBox) convertView.findViewById(R.id.poll_vote);

            pollOptionsHolder.pollVote.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    CheckBox cb = (CheckBox) v;

                    String pollOptnExits = activity.getMyPollVotes()
                                                   .get(tweet.getMsgID());

                    if (pollOptnExits != null) {
                        activity.showToast("You have already voted in this poll");

                        if (getItem(position).equals(pollOptnExits)) {
                            cb.setChecked(true);
                        } else {
                            cb.setChecked(false);
                        }

                    } else {
                        TweetPollVote pollVote = new TweetPollVote(tweet.getMsgID(),
                                getItem(position), activity.getMyUUID());
                        activity.sendNewMsg(pollVote);
                        // activity.showToast("Voto enviado! "
                        // + getItem(position));
                        activity.getMyPollVotes()
                                .put(tweet.getMsgID(), getItem(position));
                        cb.setChecked(true);
                    }

                }
            });

            convertView.setTag(pollOptionsHolder);

        } else {
            pollOptionsHolder = (PollOptionHolder) convertView.getTag();
        }

        pollOptionsHolder.pollOption.setText(pollOption);
        pollOptionsHolder.pollVote.setText(0 + "");
        return convertView;
    }

    @Override
    public int getCount() {
        return mPollOptions.length;
    }

    @Override
    public String getItem(int position) {
        return mPollOptions[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class PollOptionHolder {

        private TextView pollOption;
        private CheckBox pollVote;
    }

}
