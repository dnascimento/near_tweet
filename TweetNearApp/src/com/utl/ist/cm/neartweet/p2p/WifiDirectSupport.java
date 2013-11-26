package com.utl.ist.cm.neartweet.p2p;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.utl.ist.cm.neartweet.DB;
import com.utl.ist.cm.neartweet.MainActivity;
import com.utl.ist.cm.neartweet.dataStructures.TweetGet;
import com.utl.ist.cm.neartweet.dataStructures.TweetObject;

/** Bridge between client-server invocation and P2P mode */

public class WifiDirectSupport extends Handler implements
		ConnectionInfoListener, PeerListListener {

	public static final int WIFI_DIRECT_SERVICE_PORT = 34567;

	public static final String TAG = "NearTweet:WifiDirectSupport";

	protected static final String NEAR_TWEET_INSTANCE_NAME = "NEAR_TWEET_SERVICE";
	protected static final String NEAR_USERNAME = "near_username";
	protected static final String NEAR_PORT = "near_port";
	protected static final String NEAR_USERID = "near_user_unique_id";
	/* msg identifier */
	public static final int NT_MESSAGE = 700232;
	public static final int MY_HANDLE = 700233;
	public static final int REMOVE_HANDLE = 700234;

	private final NearPeer myNearPeer;
	private WifiP2pManager p2pManager = null;
	private Channel p2pChannel = null;
	private boolean mBound = false;
	private NTBroadcastReceiver receiver;
	private boolean wifiEnable = false;
	private P2PConnection chatConnection;

	private final ArrayList<InetAddress> addressConnectionsList = new ArrayList<InetAddress>();

	private final List<P2PConnection> chatConnectionList = new ArrayList<P2PConnection>();

	private WifiP2pDnsSdServiceRequest serviceRequest;

	// String: Username, device: actualizado na descoberta de servico
	private final HashMap<String, NearPeer> nearUsers = new HashMap<String, NearPeer>();

	private final MainActivity main;
	private final IntentFilter intentFilter;
	private final String userUniqueId;

	private WifiP2pDeviceList peersList = new WifiP2pDeviceList();

	public WifiDirectSupport(MainActivity main, String userUniqueId,
			String username) {
		this.main = main;
		this.userUniqueId = userUniqueId;

		// Init local info
		myNearPeer = new NearPeer(WIFI_DIRECT_SERVICE_PORT, username,
				userUniqueId, null);

		intentFilter = new IntentFilter();
		// Wifi Direct on/Off broadcast event
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		// Fire when discoverPeers. Peers list change, Update it
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

		// When a new connection is setup. Invoke onConnectionInfoAvailable
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		p2pManager = (WifiP2pManager) main
				.getSystemService(Context.WIFI_P2P_SERVICE);
		p2pChannel = p2pManager.initialize(main, main.getMainLooper(), null);

		// Entidade responsável por receber os broadcast
		receiver = new NTBroadcastReceiver(p2pManager, p2pChannel, this);
		main.registerReceiver(receiver, intentFilter);
	}

	public void multicastMessage(TweetObject object) {
		for (P2PConnection connection : chatConnectionList) {
			connection.send(object);
		}
		discoverService();

		// Try to connect to our known peers
		for (NearPeer peer : nearUsers.values()) {
			WifiP2pDevice device = peer.getDevice();
			connectP2p(device);
		}
	}

	public void startRegistrationAndDiscovery() {
		main.showToast("WifiDirect Registered Service");
		Map<String, String> serviceDetails = new HashMap<String, String>();
		serviceDetails.put(NEAR_USERNAME, myNearPeer.getUsername());
		serviceDetails.put(NEAR_PORT,
				String.valueOf(myNearPeer.getListenPort()));
		serviceDetails.put(NEAR_USERID, myNearPeer.getUniqueId());

		// Service information.
		WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo
				.newInstance("_neartweet", "_presence._tcp", serviceDetails);
		// Add the local service, sending the service info, network channel,
		// and listener that will be used to indicate success or failure of
		// the request.
		p2pManager.addLocalService(p2pChannel, serviceInfo,
				new ActionListener() {
					@Override
					public void onSuccess() {
						Log.d(TAG, "Service registed with success");
					}

					@Override
					public void onFailure(int arg0) {
						Log.e(TAG, "Service registed FAIL: " + arg0);
					}
				});
		Log.d(TAG, "Service added");
		discoverService();
	}

	public void discoverService() {
		// Notificar a app que há servicos disponiveis
		p2pManager.setDnsSdResponseListeners(p2pChannel,
				new DnsSdServiceResponseListener() {
					// Service has been discover
					@Override
					public void onDnsSdServiceAvailable(String instanceName,
							String registrationType, WifiP2pDevice device) {

						if (instanceName
								.equalsIgnoreCase(NEAR_TWEET_INSTANCE_NAME)) {
							Log.d(TAG, "onBonjourServiceAvailable "
									+ instanceName);
						}
						connectP2p(device);
					}
				}, new DnsSdTxtRecordListener() {

					@Override
					/*
					 * Callback includes: fullDomain: full domain name: e.g
					 * "printer._ipp._tcp.local." record: TXT record dta as a
					 * map of key/value pairs. device: The device running the
					 * advertised service.
					 */
					public void onDnsSdTxtRecordAvailable(String fullDomain,
							Map record, WifiP2pDevice device) {
						Log.d(TAG,
								"DnsSdTxtRecord available -"
										+ record.toString());
						String username = (String) record.get(NEAR_USERNAME);
						int port = Integer.parseInt((String) record
								.get(NEAR_PORT));
						String userId = (String) record.get(NEAR_USERID);
						main.showToast("New NearTweet device found: " + device.deviceName
								+ " : " + username);
						NearPeer peer = new NearPeer(port, userId, username,
								null);
						peer.setDevice(device);
						nearUsers.put(device.deviceAddress, peer);
					}
				});

		// Create a service discovery to search for Bonjour services with the
		// specified
		// service type.
		serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
		p2pManager.addServiceRequest(p2pChannel, serviceRequest,
				new ActionListener() {

					@Override
					public void onSuccess() {
						Log.d(TAG, "Service Request with success");
					}

					@Override
					public void onFailure(int reason) {
						Log.e(TAG, "Service Request Fail");
					}
				});

		p2pManager.discoverServices(p2pChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "discoverServices Request with success");
			}

			@Override
			public void onFailure(int reason) {
				Log.e(TAG, "DiscoverServices Error: " + reason);
			}
		});

	}

	private void connectP2p(WifiP2pDevice device) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;

		serviceRequest = null;

		p2pManager.connect(p2pChannel, config, new ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "Connected with success");
			}

			@Override
			public void onFailure(int reason) {
				switch (reason) {
				case WifiP2pManager.BUSY:
					Log.e(TAG, "Failed connecting to remote: busy");
					break;
				case WifiP2pManager.ERROR:
					Log.e(TAG, "Failed connecting to remote: ERROR");
					break;
				case WifiP2pManager.NO_SERVICE_REQUESTS:
					Log.e(TAG,
							"Failed connecting to remote: NO_SERVICE_REQUESTS");
					break;
				default:
					Log.e(TAG, "Failed connecting to remote: other reason");
					break;
				}
			}
		});
	}

	@Override
	/**
	 * Notifies when the stae of the connection changes.
	 * Allows one connection to each 
	 */
	public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
		Thread handlerThread = null;
		if (addressConnectionsList.contains(p2pInfo.groupOwnerAddress)) {
			main.showToast("Already connected to peer.");
			return;
		}
		if (p2pInfo.isGroupOwner && p2pInfo.groupFormed) {
			main.showToast("Group formed, Im the GO!");
			try {
				addressConnectionsList.add(p2pInfo.groupOwnerAddress);
				handlerThread = new GOSocket(this, main);
				handlerThread.start();
			} catch (IOException e) {
//				main.showToast("Failed to create a server thread - "
//						+ e.getMessage());
				e.printStackTrace();
			}
		} else if (p2pInfo.groupFormed) {
			main.showToast("Connected as peer.");
			addressConnectionsList.add(p2pInfo.groupOwnerAddress);
			handlerThread = new ClientSocket(this, p2pInfo.groupOwnerAddress,
					main);
			handlerThread.start();
		}
	}

	@Override
	public void handleMessage(Message msg) {
		Object obj = msg.obj;
		switch (msg.what) {
		case REMOVE_HANDLE:
			chatConnectionList.remove(obj);
			Log.d(TAG, "Remove old connection");
			p2pManager.removeGroup(p2pChannel, new ActionListener() {
				@Override
				public void onSuccess() {
					Log.e(TAG, "GROUP REMOVED");
				}

				@Override
				public void onFailure(int reason) {
					Log.e(TAG, "GROUP REMOVED FAIL");
				}
			});
			restart();
			break;
		case NT_MESSAGE:
			TweetObject to = (TweetObject) msg.obj;
			// Verificar se já recebeu
			if (!DB.getInstance(main).containsTweetObject(to)) {
				DB.getInstance(main).insertTweetObj(to);
				multicastObject(to);
				main.handleTweetObj(to, true);
			}
			break;
		case MY_HANDLE:
			// Send from chatConnection to itself, this will allow to send data
			// for this
			// client
			main.showToast("New connection handler.");
			addConnetion((P2PConnection) obj);
			// chatConnection.send(tweetMsg);
			break;
		default:
			Log.e(TAG, "Unknown message on handler");
		}
	}

	/*
	 * Multicast to all known sockets
	 */
	private void multicastObject(TweetObject object) {
		for (P2PConnection chat : chatConnectionList) {
			chat.send(object);
		}
	}

	/*
	 * Create intent with service connection: Turn off and On the wifi direct
	 */
	public void startWifiDirect() {
		Intent intent = new Intent(main.getApplicationContext(),
				WifiP2pManager.class);
		// Recebe os pedidos pela conneccao
		boolean status = main.bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE);
		if (status) {
			Log.d(TAG, "Wifi P2P Started with success");
		}

	}

	public void stopWifiDirect() {
		if (mBound) {
			main.unbindService(mConnection);
			Log.d(TAG, "Wifi P2P unbind");
			mBound = false;
		}
	}

	/**
	 * Defines callbacks for service binding, passed to bindService() Cria o
	 * WIFIDirect manager e inicializa-o Quando alguém se liga a um dado
	 * servico.
	 */
	private final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBound = true;
			Log.d(TAG, "onServiceConnected");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.d(TAG, "onServiceDisconnected - mete a null");

		}
	};

	public void onResume() {
		receiver = new NTBroadcastReceiver(p2pManager, p2pChannel, this);
		main.registerReceiver(receiver, intentFilter);
	}

	public void onPause() {
		main.unregisterReceiver(receiver);
	}

	public Context getContext() {
		return main.getApplicationContext();
	}

	public void setIsWifiP2pEnabled(boolean status) {
		wifiEnable = status;
	}

	public void addConnetion(P2PConnection chatConnection) {
		if (chatConnectionList.contains(chatConnection)) {
			return;
		}
		chatConnectionList.add(chatConnection);
		chatConnection.send(new TweetGet(DB.getInstance(main)
				.getExistingTweetObjIDs()));
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList newList) {
		peersList = newList;
		// Toast.makeText(getContext(),
		// "WiFi Direct: New peer list available",
		// Toast.LENGTH_SHORT).show();

		// startRegistrationAndDiscovery();
	}

	public void restart() {
		Log.e(TAG, "RESTART");
		// Clear current state
		addressConnectionsList.clear();
		// restart
		discoverService();
		p2pManager.requestConnectionInfo(p2pChannel, this);

	}

	public void pullRestart() {
		p2pManager.cancelConnect(p2pChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "PULL_RESTART: Canceled");
				startRegistrationAndDiscovery();
			}

			@Override
			public void onFailure(int reason) {
				Log.d(TAG, "PULL_RESTART: Canceled fail");
				startRegistrationAndDiscovery();
			}
		});

	}
}
