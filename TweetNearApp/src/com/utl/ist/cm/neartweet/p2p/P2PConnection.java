package com.utl.ist.cm.neartweet.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import android.os.Handler;
import android.util.Log;

import com.utl.ist.cm.neartweet.DB;
import com.utl.ist.cm.neartweet.MainActivity;
import com.utl.ist.cm.neartweet.dataStructures.TweetGet;
import com.utl.ist.cm.neartweet.dataStructures.TweetObject;

public class P2PConnection implements Runnable, Serializable {


	private static final long serialVersionUID = -6836130119707831219L;
	private Socket socket = null;
	private final Handler handler;
	private MainActivity activity;

	/**
	 * @param socket
	 *            : Socket received to client
	 * @param handler
	 * @param activity 
	 */
	public P2PConnection(Socket socket, Handler handler, MainActivity activity) {
		super();
		this.socket = socket;
		this.handler = handler;
		this.activity = activity;
	}

	private ObjectInputStream iStream;
	private ObjectOutputStream oStream;
	private static final String TAG = "ChatIncommingConnection";

	@Override
	public void run() {
		try {
			oStream = new ObjectOutputStream(socket.getOutputStream());
			oStream.flush();

			InputStream in = socket.getInputStream();
			iStream = new ObjectInputStream(in);
			// Send a myhandler message to destination
			handler.obtainMessage(WifiDirectSupport.MY_HANDLE, this)
					.sendToTarget();
			Object obj;
			while (true) {
				// Read
				obj = iStream.readObject();
				if (obj == null) {
					break;
				}
				// =============================================================
				// handle getRequests
				TweetObject to = (TweetObject) obj;
				if (to.type.equals(TweetObject.ObjType.GET)) {

					TweetGet get = (TweetGet) to;
					List<TweetObject> tweetsToOther = DB
							.getInstance(activity)
							.getMissingTweetObjListToOther(get.getExistingTweetIDs(), activity );
									
					
					//send all tweets to new client
					for (TweetObject tweetObj : tweetsToOther) {
						send(tweetObj);
					}
					
					//request missing tweets
					List<String> tweetsToMe = DB.getInstance(activity).getMissingTweetObjListToMe(get.getExistingTweetIDs());
					if(tweetsToMe.size() != 0){
						send(new TweetGet(tweetsToMe));
					}						
				}
				// =============================================================
				else {
					// Send data to wifiDirectSupport -> handleMessage(Message
					// msg)
					Log.d(TAG, "Data: " + obj.toString());
					handler.obtainMessage(WifiDirectSupport.NT_MESSAGE, obj)
							.sendToTarget();// signaling new message
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				handler.obtainMessage(WifiDirectSupport.REMOVE_HANDLE, this)
						.sendToTarget();
				oStream.close();
				iStream.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void send(TweetObject obj) {
		try {
			oStream.writeObject(obj);
		} catch (IOException e) {
			Log.e(TAG, "Exception during write", e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof P2PConnection))
			return false;
		P2PConnection con = (P2PConnection) obj;
		InetAddress add = con.socket.getInetAddress();
		return socket.getInetAddress().equals(add);
	}
}
