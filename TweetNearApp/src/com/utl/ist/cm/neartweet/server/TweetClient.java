package com.utl.ist.cm.neartweet.server;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.utl.ist.cm.neartweet.dataStructures.TweetGet;
import com.utl.ist.cm.neartweet.dataStructures.TweetMsg;
import com.utl.ist.cm.neartweet.dataStructures.TweetObject;

/**
 * This is a dummy client to inject messages on NearTweet Server
 */
public class TweetClient {
    public static final String SERVER_HOSTNAME = "ist.dynip.sapo.pt";
    public static final int SERVER_PORT = 8888;

    public static void main(String[] args) throws ClassNotFoundException {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            // Connect to Tweet Server
            Socket socket = new Socket(SERVER_HOSTNAME, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to server " + SERVER_HOSTNAME + ":"
                    + SERVER_PORT);

        } catch (IOException ioe) {
            System.err.println("Can not establish connection to " + SERVER_HOSTNAME + ":"
                    + SERVER_PORT);
            ioe.printStackTrace();
            System.exit(-1);
        }


        // Create and start Sender thread
        Sender sender = new Sender(out);
        sender.setDaemon(true);
        sender.start();

        try {
            // Read messages from the server and print them
            TweetObject message;
            while ((message = (TweetObject) in.readObject()) != null) {

                switch (message.type) {
                case MSG:
                    TweetMsg msg = (TweetMsg) message;
                    Date date = new Date(msg.getTimestamp());
                    String uid = msg.getUserID();
                    String userName = msg.getUserName();
                    System.out.println(date + " - " + uid + userName + " - "
                            + msg.getMessage());
                    break;

                case POLL_VOTE:
                    System.out.println("pollVote");
                    break;

                case SPAM_VOTE:
                    System.out.println("spamVote");
                    break;

                default:
                    break;
                }
            }
        } catch (IOException ioe) {
            System.err.println("Connection to server broken.");
            ioe.printStackTrace();
        }

    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            throw new IOException("File is too large!");
        }
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;

        InputStream is = new FileInputStream(file);
        try {
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        return bytes;
    }
}

class Sender extends
        Thread {
    private final ObjectOutputStream mOut;

    public Sender(ObjectOutputStream out) {
        mOut = out;
    }

    /**
     * Create TweetMsgs and send to server
     */
    @Override
    public void run() {
        try {

            mOut.writeObject(new TweetGet(new ArrayList<String>()));

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (!isInterrupted()) {
                // sample tweet msg
                TweetMsg msg = new TweetMsg(UUID.randomUUID().toString(), "UserTest",
                        null, in.readLine());
                mOut.writeObject(msg);
                mOut.flush();
            }
        } catch (IOException ioe) {
            // Communication is broken
            ioe.printStackTrace();
        }
    }
}
