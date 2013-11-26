package com.utl.ist.cm.neartweet.util.Tasks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.os.AsyncTask;
import android.util.Log;

import com.utl.ist.cm.neartweet.DB;
import com.utl.ist.cm.neartweet.MainActivity;
import com.utl.ist.cm.neartweet.dataStructures.TweetGet;
import com.utl.ist.cm.neartweet.dataStructures.TweetObject;

public class NetworkTask extends
        AsyncTask<Void, TweetObject, Boolean> {
    MainActivity activity;
    Socket nsocket = null;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;

    public NetworkTask(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        Log.i("AsyncTask", "onPreExecute");
    }

    @Override
    protected Boolean doInBackground(Void... params) { // This runs on a
                                                       // different thread
        boolean result = false;

        while (!result) {
            try {
                Log.i("AsyncTask", "doInBackground: Creating socket");

                SocketAddress sockaddr = new InetSocketAddress(MainActivity.SERVER_IP,
                        8888);
                nsocket = new Socket();
                nsocket.connect(sockaddr, 5000); // 5 second connection
                                                 // timeout
                if (nsocket.isConnected()) {
                    activity.showToast("Connected to Server!");
                    out = new ObjectOutputStream(nsocket.getOutputStream());
                    out.flush();
                    in = new ObjectInputStream(nsocket.getInputStream());

                    Log.i("AsyncTask", "doInBackground: Socket created, streams assigned");
                    Log.i("AsyncTask", "doInBackground: Waiting for inital data...");

                    // do a GET to retrieve all messages from TweetServer
                    sendMsg(new TweetGet(DB.getInstance(null).getExistingTweetObjIDs()));

                    // Read messages from the server and print them
                    TweetObject networkObj;
                    while ((networkObj = (TweetObject) in.readObject()) != null) {
                        publishProgress(networkObj);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("AsyncTask", "doInBackground: IOException");
                Log.i("AsyncTask", "doInBackground: " + e.getLocalizedMessage());
                // result = true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("AsyncTask", "doInBackground: Exception");
                // result = true;
            } finally {
                try {
                    in.close();
                    out.close();
                    nsocket.close();
                    activity.showToast("Connection Closed");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("AsyncTask", "doInBackground: Finished");
            }
            Log.i("AsyncTask",
                  "doInBackground: Trying to connect to server within 10 seconds...");
            activity.showToast("Trying to connect to server within 10 seconds");
            try {
            	//wait 10 seconds to retry
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public void sendMsg(TweetObject msg) {
        try {
            if (nsocket.isConnected()) {
                Log.i("AsyncTask", "SendDataToNetwork: Writing message to socket");
                out.writeObject(msg);
                out.flush();
            } else {
                Log.i("AsyncTask",
                      "SendDataToNetwork: Cannot send message. Socket is closed");
            }
        } catch (Exception e) {
            Log.i("AsyncTask",
                  "SendDataToNetwork: Message send failed. Caught an exception");
        }
    }

    @Override
    protected void onProgressUpdate(TweetObject... values) {
        if (values.length > 0) {
            Log.i("AsyncTask", "onProgressUpdate: " + values[0].toString() + " received.");
            activity.handleTweetObj(values[0], true);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.i("AsyncTask", "Cancelled.");

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.i("AsyncTask", "onPostExecute: Completed with an Error.");
            // toast("Comunication Error.");
            // dialog.cancel();
        } else {
            Log.i("AsyncTask", "onPostExecute: Completed.");
            // toast("Comunication Closed");
        }
    }
}
