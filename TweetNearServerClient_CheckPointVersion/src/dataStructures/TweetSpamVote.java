package dataStructures;

public class TweetSpamVote extends TweetObject {

	
	private static final long serialVersionUID = 5364380678009084233L;
	private String voteUserID;
	private String spammerUserID;	
	private String msgID;

	
	public TweetSpamVote(String voteUserID, String spammerUserID, String msgID) {
		super(ObjType.SPAM_VOTE);
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
	
	public String getMsgID() {
		return msgID;
	}



}
