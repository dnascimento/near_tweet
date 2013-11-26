package com.utl.ist.cm.neartweet.p2p;

import java.io.Serializable;
import java.net.Socket;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Identificacao de um peer de near_tweet
 */
public class NearPeer implements Serializable {

	private static final long serialVersionUID = -1156299003181082195L;
	private int listenPort;
    private String username;
    private String uniqueId;
    private WifiP2pDevice device;
    private Socket socket;

    public NearPeer(int listenPort, String username, String uniqueId, Socket socket) {
        super();
        this.listenPort = listenPort;
        this.username = username;
        this.socket = socket;
        this.uniqueId = uniqueId;
    }

	public int getListenPort() {
		return listenPort;
	}

	public String getUsername() {
		return username;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public WifiP2pDevice getDevice() {
		return device;
	}
	
	public void setDevice(WifiP2pDevice d) {
		device = d;
	}

	public Socket getSocket() {
		return socket;
	}

}
