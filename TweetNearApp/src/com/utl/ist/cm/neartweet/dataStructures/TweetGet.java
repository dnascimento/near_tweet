package com.utl.ist.cm.neartweet.dataStructures;

import java.util.List;


public class TweetGet extends
        TweetObject {


    private static final long serialVersionUID = 2148060782841880121L;
    private List<String> existingTweetIDs;
    

	public TweetGet(List<String> existingIDs) {
        super(ObjType.GET, null);
        this.existingTweetIDs = existingIDs;
    }
	
    public List<String> getExistingTweetIDs() {
		return existingTweetIDs;
	}

    

}
