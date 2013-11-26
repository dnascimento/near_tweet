package com.utl.ist.cm.neartweet.server;

import java.net.Socket;


/**
 * Client info register
 */
public class ClientInfo {
    public Socket mSocket = null;
    public ClientListener mClientListener = null;
    public ClientSender mClientSender = null;
}
