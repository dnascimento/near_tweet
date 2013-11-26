package dataStructures;

import java.io.Serializable;

public class TweetObject implements Serializable {

	private static final long serialVersionUID = -3318343104381430406L;
	
	protected long mTimestamp;
	
	public enum ObjType {
	    MSG, POLL_VOTE, GET, SPAM_VOTE
	}
	
	public ObjType type;
	
	protected TweetObject(ObjType t){
		this.type=t;
		this.mTimestamp = new java.util.Date().getTime();
	}
}
