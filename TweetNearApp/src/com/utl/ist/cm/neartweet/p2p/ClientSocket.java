package com.utl.ist.cm.neartweet.p2p;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;

import com.utl.ist.cm.neartweet.MainActivity;

public class ClientSocket extends
        Thread {

    private static final String TAG = "ClientSocket";
    private Handler handler;
    private InetAddress goAddress;
    private P2PConnection chat;
    private MainActivity activity;

    public ClientSocket(Handler handler, InetAddress groupOwnerAddress, MainActivity activity) {
        this.handler = handler;
        goAddress = groupOwnerAddress;
        this.activity = activity;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(goAddress.getHostAddress(),
                    WifiDirectSupport.WIFI_DIRECT_SERVICE_PORT));
            Log.d(TAG, "Lauching the I/O handler");
            chat = new P2PConnection(socket, handler, activity);
            new Thread(chat).start();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }
}
