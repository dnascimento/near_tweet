package com.utl.ist.cm.neartweet.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import com.utl.ist.cm.neartweet.dataStructures.TweetObject;

public class ClientListener extends
        Thread {
    private final ServerDispatcher mServerDispatcher;
    private final ClientInfo mClientInfo;
    private final ObjectInputStream mIn;

    public ClientListener(ClientInfo aClientInfo, ServerDispatcher aServerDispatcher) throws IOException {
        mClientInfo = aClientInfo;
        mServerDispatcher = aServerDispatcher;
        Socket socket = aClientInfo.mSocket;
        mIn = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Until interrupted, reads messages from the client socket, forwards them to the
     * server dispatcher's queue and notifies the server dispatcher.
     */
    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                TweetObject message = (TweetObject) mIn.readObject();
                if (message == null) {
                    break;
                }
                mServerDispatcher.dispatchMessage(mClientInfo, message);
            }
        } catch (IOException ioex) {
            // Problem reading from socket (communication is broken)
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Communication is broken. Interrupt both listener and sender threads
        mClientInfo.mClientSender.interrupt();
        mServerDispatcher.deleteClient(mClientInfo);
    }

}
