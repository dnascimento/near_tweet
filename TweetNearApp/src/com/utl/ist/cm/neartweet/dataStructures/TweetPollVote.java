package com.utl.ist.cm.neartweet.dataStructures;

public class TweetPollVote extends
        TweetObject {

    private static final long serialVersionUID = 1832028930880336689L;

    private final String parentMsgID;
    private final String pollOptn;
    private final String voteUserID;



    public TweetPollVote(String parentMsgID, String pollOptn, String userID) {
        super(ObjType.POLL_VOTE, userID);
        this.parentMsgID = parentMsgID;
        this.pollOptn = pollOptn;
        voteUserID = userID;
    }

    public String getVoteFromUserID() {
        return voteUserID;
    }


    public String getPollOptn() {
        return pollOptn;
    }

    public String getParentMsgID() {
        return parentMsgID;
    }

}
