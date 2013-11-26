package com.utl.ist.cm.neartweet.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;
import android.widget.Toast;

/**
 * Allows register for system or app events.
 */
public class NTBroadcastReceiver extends
        BroadcastReceiver {

    private final WifiDirectSupport wifiDirectSupport;
    private Channel p2pChannel = null;
    private WifiP2pManager p2pManager = null;
    private static int registed = 1;

    public NTBroadcastReceiver(WifiP2pManager p2pManager,
            Channel p2pChannel,
            WifiDirectSupport wdSupport) {
        super();
        this.p2pChannel = p2pChannel;
        this.p2pManager = p2pManager;
        wifiDirectSupport = wdSupport;
    }

    /*
     * Este é o método invocado pelo android quando um evento de broadcast é recebido.
     * Vamos verificar se este é o broadcast que queremos mesmo. Apanhar a notificacao de
     * que a interface de wireless mudou ou que a lista de peers mudou
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi Direct Mode is enable or not
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                wifiDirectSupport.setIsWifiP2pEnabled(true);
                Toast.makeText(wifiDirectSupport.getContext(),
                               "WiFi Direct enabled",
                               Toast.LENGTH_SHORT).show();
                // Display on second change
                if (registed-- == 0) {
                    wifiDirectSupport.startRegistrationAndDiscovery();
                    p2pManager.requestConnectionInfo(p2pChannel, wifiDirectSupport);
                }
            } else {
                wifiDirectSupport.setIsWifiP2pEnabled(false);
                Toast.makeText(wifiDirectSupport.getContext(),
                               "WiFi Direct disabled",
                               Toast.LENGTH_SHORT).show();
                registed = 1;
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()

            if (p2pManager != null) {
                p2pManager.requestPeers(p2pChannel, wifiDirectSupport);
            }
            Log.d(WifiDirectSupport.TAG, "P2P peers updated");

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // New connection setup
            Log.d(WifiDirectSupport.TAG, "New connection changed: ");
            if (p2pManager == null) {
                Log.e(WifiDirectSupport.TAG, "Connection update but no manager available");
            }
            try {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    Log.d(WifiDirectSupport.TAG, "Im connected");
                    p2pManager.requestConnectionInfo(p2pChannel, wifiDirectSupport);
                } else {
                    // Its a disconnect
                    Log.e(WifiDirectSupport.TAG,
                          "ERROR, go is way...:WIFI_P2P_CONNECTION_CHANGED_ACTION");
                    Log.d(WifiDirectSupport.TAG, "Not connected");
                    wifiDirectSupport.restart();

                }
            } catch (RuntimeException e) {
                Log.e(WifiDirectSupport.TAG, "ERROR", e);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            switch (device.status) {
            case WifiP2pDevice.AVAILABLE:
                Log.d(WifiDirectSupport.TAG, "Device status - AVAILABLE");
                break;
            case WifiP2pDevice.CONNECTED:
                Log.d(WifiDirectSupport.TAG, "Device status - CONNECTED");
                break;
            case WifiP2pDevice.FAILED:
                Log.d(WifiDirectSupport.TAG, "Device status - FAILED");
                break;
            case WifiP2pDevice.INVITED:
                Log.d(WifiDirectSupport.TAG, "Device status - INVITED");
                break;
            case WifiP2pDevice.UNAVAILABLE:
                Log.d(WifiDirectSupport.TAG, "Device status - UNAVAILABLE");
                break;
            default:
                Log.d(WifiDirectSupport.TAG, "Device status - Unknown: error");
            }

        }
    }
}
