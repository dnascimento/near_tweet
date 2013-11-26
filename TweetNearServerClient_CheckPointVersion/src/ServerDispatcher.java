import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.UUID;
import java.util.Vector;

import dataStructures.TweetMsg;
import dataStructures.TweetObject;

public class ServerDispatcher extends Thread {
	private Vector<TweetObject> mMessageList;
	private Vector<Pair<ClientInfo, TweetObject>> mMessageQueue = new Vector<Pair<ClientInfo, TweetObject>>();
	private Vector<ClientInfo> mClients = new Vector<ClientInfo>();
	
	
	public ServerDispatcher(){
		mMessageList = loadFile("db.dat");
		System.out.println("DB size "+ mMessageList.size());
		mMessageQueue = new Vector<Pair<ClientInfo, TweetObject>>();
		mClients = new Vector<ClientInfo>();
		
	
	}
	
	
	
	private byte[] URL2ByteArr(String url){
		
		try {
			URL u = new URL(url);
			int contentLength = u.openConnection().getContentLength();
			InputStream openStream = u.openStream();
			byte[] binaryData = new byte[contentLength];
			openStream.read(binaryData);
			openStream.close();
			return binaryData;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	public void sleep(){
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Adds given client to the server's client list.
	 */
	public synchronized void addClient(ClientInfo aClientInfo) {
		mClients.add(aClientInfo);
		//updateAllMsgToClient(aClientInfo);
	}

	public void updateAllMsgToClient(ClientInfo aClientInfo) {
		for (int i = 0; i < mMessageList.size(); i++) {
			 TweetObject msg = mMessageList.get(i);
			 aClientInfo.mClientSender.sendMessage(msg);
		}		
	}

	/**
	 * Deletes given client from the server's client list if the client is in
	 * the list.
	 */
	public synchronized void deleteClient(ClientInfo aClientInfo) {
		int clientIndex = mClients.indexOf(aClientInfo);
		if (clientIndex != -1)
			mClients.removeElementAt(clientIndex);
	}

	/**
	 * Adds given message to the dispatcher's message queue and notifies this
	 * thread to wake up the message queue reader (getNextMessageFromQueue
	 * method). dispatchMessage method is called by other threads
	 * (ClientListener) when a message is arrived.
	 */
	public synchronized void dispatchMessage(ClientInfo aClientInfo,
			TweetObject aMessage) {
		
		switch (aMessage.type) {
		case MSG:
			TweetServer.logInfo(aClientInfo.mSocket.getInetAddress()+":: TweetMsg");			
			mMessageList.add(aMessage);
			//DEBUG - save image to disk!
			TweetMsg msg = (TweetMsg) aMessage;
			byte[] img = msg.getResource();
			if(img != null)
				try {
					TweetServer.writeToFile(img, msg.getMsgID()+".jpg");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			break;
			
		case POLL_VOTE:
			TweetServer.logInfo("Client "+aClientInfo.mSocket.getInetAddress()+":: TweetPollVote");
			mMessageList.add(aMessage);
			break;
			
		case SPAM_VOTE:
			TweetServer.logInfo("Client "+aClientInfo.mSocket.getInetAddress()+":: TweetSPAMVote");
			mMessageList.add(aMessage);
			break;
		
		case GET:
			TweetServer.logInfo("Client "+aClientInfo.mSocket.getInetAddress()+":: TweetGet");
			break;
			
		default:
			break;
		}
		
		//TODO delete
		//Socket socket = aClientInfo.mSocket;
		//String senderIP = socket.getInetAddress().getHostAddress();
		//String senderPort = "" + socket.getPort();
		//aMessage = senderIP + ":" + senderPort + " : " + aMessage;
		mMessageQueue.add(new Pair<ClientInfo, TweetObject>(aClientInfo,aMessage));
		notify();
	}



	/**
	 * @return and deletes the next message from the message queue. If there is
	 *         no messages in the queue, falls in sleep until notified by
	 *         dispatchMessage method.
	 */
	private synchronized Pair<ClientInfo, TweetObject> getNextMessageFromQueue()
			throws InterruptedException {
		while (mMessageQueue.size() == 0)
			wait();
		Pair<ClientInfo, TweetObject> message = (Pair<ClientInfo, TweetObject>) mMessageQueue.get(0);
		mMessageQueue.removeElementAt(0);
		return message;
	}

	/**
	 * Sends given message to all clients in the client list. Actually the
	 * message is added to the client sender thread's message queue and this
	 * client sender thread is notified.
	 */
	private synchronized void sendMessageToAllClients(TweetObject aMessage) {
		for (int i = 0; i < mClients.size(); i++) {
			ClientInfo clientInfo = (ClientInfo) mClients.get(i);
			clientInfo.mClientSender.sendMessage(aMessage);
		}
	}
	
	private synchronized void processMessage(Pair<ClientInfo, TweetObject> message){
		
		switch (message.getValue().type) {
		case MSG:
			sendMessageToAllClients(message.getValue());
			break;
			
		case POLL_VOTE:
			sendMessageToAllClients(message.getValue());
			break;
		
		case SPAM_VOTE:
			sendMessageToAllClients(message.getValue());
			break;

		case GET:
			updateAllMsgToClient(message.getKey());
			break;
			
		default:
			System.out.println("BAD LUCK!! TODO: Testar ");
			break;
		}
		
	}

	/**
	 * Infinitely reads messages from the queue and dispatch them to all clients
	 * connected to the server.
	 */
	public void run() {
		try {
			while (true) {
				Pair<ClientInfo, TweetObject> message = getNextMessageFromQueue();
				processMessage(message);
			}
		} catch (InterruptedException ie) {
			// Thread interrupted. Stop its execution
			ie.printStackTrace();
		}
	}
	
	public static Vector<TweetObject> loadFile(String filename){
	    Vector<TweetObject> arr;
		try {
	        FileInputStream fis = new FileInputStream(filename);
	        ObjectInputStream in = new ObjectInputStream(fis);
	        arr = (Vector<TweetObject>)in.readObject();
	        in.close();
	      }
	      catch (Exception e) {
	          System.out.println(e);
	          arr = new Vector<TweetObject>();
	      }
	
		return arr;
	}

}