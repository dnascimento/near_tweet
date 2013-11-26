package com.utl.ist.cm.neartweet.p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.util.Log;

import com.utl.ist.cm.neartweet.MainActivity;

public class GOSocket extends Thread {

	ServerSocket socket = null;
	private final int THREAD_COUNT = 10;
	private Handler handler;
	private static final String TAG = "GO_socket";
	private MainActivity activity;

	public GOSocket(Handler handler, MainActivity main) throws IOException {
		try {
			socket = new ServerSocket(
					WifiDirectSupport.WIFI_DIRECT_SERVICE_PORT);
			this.handler = handler;
			Log.d(TAG, "Socket Started");
			this.activity = main;
		} catch (IOException e) {
			e.printStackTrace();
			pool.shutdownNow();
			throw e;
		}
	}

	private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
			THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>());

	@Override
	public void run() {
		while (true) {
			try {
				// Notificar de uma nova conneccao
				pool.execute(new P2PConnection(socket.accept(), handler, activity));
				Log.d(TAG, "Lauching new ChatIncommingConnection");
			} catch (IOException e) {
				try {
					if (socket != null && !socket.isClosed()) {
						socket.close();
					}
				} catch (IOException ioe) {

				}
				e.printStackTrace();
				pool.shutdownNow();
				break;
			}
		}
	}

//	public interface MessageTarget {
//		public Handler getHandler();
//	}
}
