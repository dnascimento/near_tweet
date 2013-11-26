package com.utl.ist.cm.neartweet.dataStructures;

public class TweetSpamVote extends
        TweetObject {


    private static final long serialVersionUID = 5364380678009084233L;
    private final String voteUserID;
    private final String spammerUserID;
    private final String msgID;


    public TweetSpamVote(String voteUserID, String spammerUserID, String msgID) {
        super(ObjType.SPAM_VOTE, voteUserID);
        this.voteUserID = voteUserID;
        this.spammerUserID = spammerUserID;
        this.msgID = msgID;

    }

    public String getVoteFromUserID() {
        return voteUserID;
    }

    public String getSpammerUserID() {
        return spammerUserID;
    }

    public String getRelativeMsgID() {
        return msgID;
    }



}
