import java.io.*;
import java.net.*;
import java.util.*;

import dataStructures.TweetObject;

public class ClientSender extends Thread {
	private Vector<TweetObject> mMessageQueue = new Vector<TweetObject>();

	private ServerDispatcher mServerDispatcher;
	private ClientInfo mClientInfo;
	private ObjectOutputStream mOut;

	public ClientSender(ClientInfo aClientInfo,
			ServerDispatcher aServerDispatcher) throws IOException {
		mClientInfo = aClientInfo;
		mServerDispatcher = aServerDispatcher;
		Socket socket = aClientInfo.mSocket;
		mOut = new ObjectOutputStream(socket.getOutputStream());
		mOut.flush();
	}

	/**
	 * Adds given message to the message queue and notifies this thread
	 * (actually getNextMessageFromQueue method) that a message is arrived.
	 * sendMessage is called by other threads (ServeDispatcher).
	 */
	public synchronized void sendMessage(TweetObject aMessage) {
		mMessageQueue.add(aMessage);
		notify();
	}

	/**
	 * @return and deletes the next message from the message queue. If the queue
	 *         is empty, falls in sleep until notified for message arrival by
	 *         sendMessage method.
	 */
	private synchronized TweetObject getNextMessageFromQueue()
			throws InterruptedException {
		while (mMessageQueue.size() == 0)
			wait();
		TweetObject message = (TweetObject) mMessageQueue.get(0);
		mMessageQueue.removeElementAt(0);
		return message;
	}

	/**
	 * Sends given message to the client's socket.
	 * 
	 * @throws IOException
	 */
	private void sendMessageToClient(TweetObject aMessage) throws IOException {
		mOut.writeObject(aMessage);
		mOut.flush();

	}

	/**
	 * Until interrupted, reads messages from the message queue and sends them
	 * to the client's socket.
	 */
	public void run() {
		try {
			while (!isInterrupted()) {
				TweetObject message = getNextMessageFromQueue();
				sendMessageToClient(message);
			}
		} catch (Exception e) {
			// Communication closed
		}

		// Communication is broken. Interrupt both listener and sender threads
		mClientInfo.mClientListener.interrupt();
		mServerDispatcher.deleteClient(mClientInfo);
		TweetServer.logInfo("Client was left!");
	}

}