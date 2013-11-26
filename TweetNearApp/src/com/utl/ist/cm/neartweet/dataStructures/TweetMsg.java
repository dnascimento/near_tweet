package com.utl.ist.cm.neartweet.dataStructures;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;


public class TweetMsg extends
        TweetObject
        implements Serializable {

    private static final long serialVersionUID = 797962396965349779L;
    private final String mUserName;
    private final String parentMsgID;

    private final String mMessage;
    private double mLocation_Lat;
    private double mLocation_Lon;
    private String mLocationName;


    private boolean isPrivate;
    private final Map<Long, TweetMsg> replies;
    private final Map<String, Integer> poll;
    private byte[] resource;

    public TweetMsg(String myUUID, String userName, String parentMsgID, String msg) {
        super(ObjType.MSG, myUUID);
        mUserName = userName;
        this.parentMsgID = parentMsgID;
        mMessage = msg;
        replies = new TreeMap<Long, TweetMsg>();
        poll = new TreeMap<String, Integer>();
        isPrivate = false;
    }


    public String getParentMsgID() {
        return parentMsgID;
    }

    public boolean isRootMsg() {
        return parentMsgID == null;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivacy(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

//    public void setTimestamp(long mTimestamp) {
//        this.mTimestamp = mTimestamp;
//    }

    public String getMessage() {
        return mMessage;
    }

    public double getLocation_Lat() {
        return mLocation_Lat;
    }

    public void setLocation_Lat(double mLocation_Lat) {
        this.mLocation_Lat = mLocation_Lat;
    }

    public double getLocation_Lon() {
        return mLocation_Lon;
    }

    public void setLocation_Lon(double mLocation_Lon) {
        this.mLocation_Lon = mLocation_Lon;
    }

    public byte[] getResource() {
        return resource;
    }

    public void setResource(byte[] resource) {
        this.resource = resource;
    }

    public Map<Long, TweetMsg> getReplies() {
        return replies;
    }

    public void addReply(TweetMsg reply) {
        replies.put(reply.getTimestamp(), reply);
    }

    public Map<String, Integer> getPoll() {
        return poll;
    }

    public void insertPollOptn(String polloptn) {
        poll.put(polloptn, 0);
    }

//    public Boolean pollVote(String vote) {
//        if (poll.containsKey(vote)) {
//            Integer counter = poll.get(vote);
//            poll.put(vote, counter + 1);
//            return true;
//        }
//        return false;
//    }

    public String getUserName() {
        return mUserName;
    }

    public String getLocationName() {
        return mLocationName;
    }

    public void setLocationName(String mLocationName) {
        this.mLocationName = mLocationName;
    }
}
