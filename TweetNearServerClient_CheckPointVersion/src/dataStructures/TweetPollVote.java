package dataStructures;

public class TweetPollVote extends TweetObject {

	private static final long serialVersionUID = 1832028930880336689L;

	private String parentMsgID;
	private String pollOptn;
	private String voteUserID;

	

	public TweetPollVote(String parentMsgID, String pollOptn, String userID) {
		super(ObjType.POLL_VOTE);
		this.parentMsgID = parentMsgID;
		this.pollOptn = pollOptn;
		this.voteUserID = userID; 
	}
	
	public String getVoteFromUserID() {
		return voteUserID;
	}
	
	public String getMsgID() {
		return new String(super.mTimestamp + "");
	}

	public String getPollOptn() {
		return pollOptn;
	}

	public String getParentMsgID() {
		return parentMsgID;
	}

}
