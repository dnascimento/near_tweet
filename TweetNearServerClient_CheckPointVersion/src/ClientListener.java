import java.io.*;
import java.net.*;

import dataStructures.TweetObject;

public class ClientListener extends Thread {
	private ServerDispatcher mServerDispatcher;
	private ClientInfo mClientInfo;
	private ObjectInputStream mIn;

	public ClientListener(ClientInfo aClientInfo,
			ServerDispatcher aServerDispatcher) throws IOException {
		mClientInfo = aClientInfo;
		mServerDispatcher = aServerDispatcher;
		Socket socket = aClientInfo.mSocket;
		mIn = new ObjectInputStream(socket.getInputStream());
	}

	/**
	 * Until interrupted, reads messages from the client socket, forwards them
	 * to the server dispatcher's queue and notifies the server dispatcher.
	 */
	public void run() {
		try {
			while (!isInterrupted()) {
				TweetObject message = (TweetObject) mIn.readObject();
				if (message == null)
					break;
				mServerDispatcher.dispatchMessage(mClientInfo, message);
			}
		} catch (IOException ioex) {
			// Problem reading from socket (communication is broken)
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Communication is broken. Interrupt both listener and sender threads
		mClientInfo.mClientSender.interrupt();
		mServerDispatcher.deleteClient(mClientInfo);
	}

}