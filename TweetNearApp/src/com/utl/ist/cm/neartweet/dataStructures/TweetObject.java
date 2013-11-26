package com.utl.ist.cm.neartweet.dataStructures;

import java.io.Serializable;

public abstract class TweetObject
        implements Serializable {

    private static final long serialVersionUID = -3318343104381430406L;

    protected long mTimestamp;
    protected final String mUserID;

    public enum ObjType {
        MSG, POLL_VOTE, GET, SPAM_VOTE
    }

    public ObjType type;

    protected TweetObject(ObjType t, String mUserID) {
        type = t;
        mTimestamp = new java.util.Date().getTime();
        this.mUserID = mUserID;
    }

    public String getMsgID() {
        return mTimestamp + mUserID.toString();
    }
    
    public String getUserID() {
        return mUserID;
    }
}