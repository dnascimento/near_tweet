package com.utl.ist.cm.neartweet.server;


import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TweetServer {
    public static final int LISTENING_PORT = 8888;

    public static void main(String[] args) {
        // Open server socket for listening
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(LISTENING_PORT);
            System.out.println("TweetServer started on port " + LISTENING_PORT);
        } catch (IOException se) {
            System.err.println("Can not start listening on port " + LISTENING_PORT);
            se.printStackTrace();
            System.exit(-1);
        }

        // Start ServerDispatcher thread
        ServerDispatcher serverDispatcher = new ServerDispatcher();
        serverDispatcher.start();

        // Accept and handle client connections
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                logInfo("new client connected! ("
                        + socket.getInetAddress().getHostAddress() + ")");
                ClientInfo clientInfo = new ClientInfo();
                clientInfo.mSocket = socket;
                ClientListener clientListener = new ClientListener(clientInfo,
                        serverDispatcher);
                ClientSender clientSender = new ClientSender(clientInfo, serverDispatcher);
                clientInfo.mClientListener = clientListener;
                clientInfo.mClientSender = clientSender;
                clientListener.start();
                clientSender.start();
                serverDispatcher.addClient(clientInfo);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static void logInfo(String info) {// log info to console
        Date date = new Date();
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateInfo = formatter.format(date);
        String server = "TweetServer";
        System.out.println("[" + dateInfo + " @ " + server + "]:  " + info);
    }

    public static void logInfoErr(String info) {// log error to console
        Date date = new Date();
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateInfo = formatter.format(date);
        String server = "TweetServer";
        System.err.println("[" + dateInfo + " @ " + server + "]:  " + info);
    }

    static void writeToFile(byte[] img, String string) throws IOException {
        FileOutputStream fos = new FileOutputStream(string);
        try {
            fos.write(img);
        } finally {
            fos.close();
        }
    }

}
