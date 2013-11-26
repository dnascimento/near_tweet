package com.utl.ist.cm.neartweet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.util.Log;

import com.utl.ist.cm.neartweet.dataStructures.TweetMsg;
import com.utl.ist.cm.neartweet.dataStructures.TweetObject;

public class DB {
	
	private static final String METADATAFILENAME = "metadata";

	private static DB instance = null;
	
	/*Store tweet objects IDs queue*/
	Queue<String> tweetObjQueue = null;

	private MainActivity activity;

	private DB(MainActivity activity) {
		this.activity = activity;
		// load from storage
		tweetObjQueue = readQueueMetadata();
		storeQueueMetaData(tweetObjQueue);
		if (tweetObjQueue.size() != 0)
			loadAllTweetObj();
	}

	public static synchronized DB getInstance(MainActivity activity) {
		if (instance == null) {
			instance = new DB(activity);
		}
		return instance;
	}

	/*Insert tweet object in local storage*/
	public void insertTweetObj(TweetObject obj) {
		tweetObjQueue.add(obj.getMsgID());
		try {
			storeTweetObj(obj);
			storeQueueMetaData(tweetObjQueue);
		} catch (IOException e) {
			e.printStackTrace();
			activity.showToast(e.getMessage());
		}

	}

	
	//tweetObjQueue minus tList
	public List<TweetObject> getMissingTweetObjListToOther(List<String> tList,
			MainActivity activity) {
		List<TweetObject> result = new LinkedList<TweetObject>();

		Iterator<String> it = tweetObjQueue.iterator();
		while (it.hasNext()) {
			String curr = it.next();
			if (!tList.contains(curr)) {
				result.add(readTweetObj(curr));

			}
		}
		return result;
	}

	//tList minus tweetObjQueue
	public List<String> getMissingTweetObjListToMe(List<String> tList) {
		List<String> result = new LinkedList<String>(tList);
		Iterator<String> it = tweetObjQueue.iterator();
		while (it.hasNext()) {
			String curr = it.next();
			if (tList.contains(curr)) {
				result.remove(curr);
			}
		}
		return result;
	}

	public List<String> getExistingTweetObjIDs() {
		List<String> result = new LinkedList<String>();
		Iterator<String> it = tweetObjQueue.iterator();
		while (it.hasNext()) {
			String curr = it.next();
			result.add(curr);
		}
		return result;
	}

	private synchronized void storeTweetObj(TweetObject obj) throws IOException {
		FileOutputStream fos;
		ObjectOutputStream os;
		try {
			fos = activity.openFileOutput(obj.getMsgID(), Context.MODE_PRIVATE);
			os = new ObjectOutputStream(fos);
			os.writeObject(obj);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	private TweetObject readTweetObj(String objName) {
		FileInputStream fis;
		ObjectInputStream is;
		TweetObject to = null;
		try {
			fis = activity.openFileInput(objName);
			is = new ObjectInputStream(fis);
			to = (TweetObject) is.readObject();
			is.close();
		} catch (FileNotFoundException e) {
			Log.e("exception", e.getMessage());
		} catch (StreamCorruptedException e) {
			Log.e("exception", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("exception", e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Log.e("exception", e.getMessage());
			e.printStackTrace();
		}
		
		return to;
	}

	@SuppressWarnings("unchecked")
	private Queue<String> readQueueMetadata() {
		FileInputStream fis;
		ObjectInputStream is;
		Queue<String> queue = new ConcurrentLinkedQueue<String>();
		try {
			fis = activity.openFileInput(METADATAFILENAME);
			is = new ObjectInputStream(fis);
			queue = (Queue<String>) is.readObject();
			is.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		//Welcome msg
		if(queue.size() == 0){
			TweetMsg tm = new TweetMsg("", "NearTweet@IST", null, "Welcome to NearTweet.\nThis is a Location-Aware Microblogging Service.\nEnjoy! :)");
			activity.handleTweetObj(tm, true);
		}
		
		return queue;
	}

	private synchronized void storeQueueMetaData(Queue<String> queue) {
		FileOutputStream fos;
		ObjectOutputStream os;
		try {
			fos = activity.openFileOutput(METADATAFILENAME, Context.MODE_PRIVATE);
			os = new ObjectOutputStream(fos);
			os.writeObject(queue);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public void loadAllTweetObj() {
		for (String elem : tweetObjQueue) {
			TweetObject o = readTweetObj(elem);
			if (o != null)
				activity.handleTweetObj(o, false);
		}
	}

	public boolean containsTweetObject(TweetObject to) {
		if (tweetObjQueue.contains(to.getMsgID()))
			return true;
		return false;
	}

}
