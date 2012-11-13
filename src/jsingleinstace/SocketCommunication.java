/**
 * JSingleInstance - allows running only one instance of any java app
 * Copyright (C) 2011-2012 MJ <mj_dv@web.de>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package jsingleinstace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * A Communication Strategy, which uses the underlying system sockets
 * for speaking with another instance.
 * <p>
 * Warning: The big problem with sockets is that a Server Socket (which is used
 * by the first instance) can cause problems with firewalls. 
 * @author MJ
 *
 */
public class SocketCommunication extends Communication {

	private class ClientThread implements Runnable {

		BufferedReader input;

		public ClientThread(Socket clientSocket) throws IOException {
			input = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
		}

		@Override
		public void run() {
			try {
				while (true) {
					final String msg = input.readLine();
					if (msg == null)
						break; // client closed connection
					
					commandQueue.put(msg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * File where the port number is saved.
	 */
	private File portFile;
	private boolean isAlreadyRunning = false;
	/**
	 * true if the system is going down. In this case we don't want exceptions
	 * which result in closing sockets
	 */
	private boolean shutdownInProgress = false;
	private int port;

	private Socket clientSocket;
	private ServerSocket serverSocket;
	private PrintWriter clientOut;
	
	private BlockingQueue<String> commandQueue;

	public SocketCommunication(String filePath) {
		this.portFile = new File(filePath);
		commandQueue = new SynchronousQueue<String>();
	}

	@Override
	/*package*/ void init() throws IOException {
		if (!portFile.exists())
			setupServerSocket();

		else {
			isAlreadyRunning = true;
			getPortFromFile();
			setupClientSocket();
		}
	}

	private void setupClientSocket() throws IOException {
		try {
			clientSocket = new Socket("localhost", port);
			clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
			clientSocket.setSoTimeout(5 * 1000);
		} catch (IOException e) {
			// connection can't be established
			// seems like the app has crashed last time
			// we just ignore that and start up normal
			isAlreadyRunning = false;
			portFile.delete();
			setupServerSocket();
		}
	}

	private void getPortFromFile() throws IOException {

		BufferedReader in = new BufferedReader(new FileReader(portFile));
		try {
			port = Integer.parseInt(in.readLine().trim());
		} catch (NumberFormatException e) {
			// NumberFormatException can only occur if we try to open a file
			// which isn't previously created by ourself
			throw new IOException(
					"File already exists but has wrong format. Possibly clash of unknown existing File!");
		} finally {
			in.close();
		}
	}

	private void setupServerSocket() throws IOException {
		serverSocket = new ServerSocket(0);
		port = serverSocket.getLocalPort();

		BufferedWriter out = new BufferedWriter(new FileWriter(portFile));
		out.write("" + port);
		out.close();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Socket clientSocket = null;
						clientSocket = serverSocket.accept();
						new Thread(new ClientThread(clientSocket)).start();
					} catch (IOException e) {
						if (!shutdownInProgress) {
							// Exception during setup of ServerSocket
							e.printStackTrace();
						}
						break; // seems like were exiting or any other error occured
					}
				}
			}

		}).start();
	}

	@Override
	/*package*/ boolean isAlreadyRunning() {
		return isAlreadyRunning;
	}

	/**
	 * sends a command to existing instance
	 * 
	 * @param cmd
	 *            the command which should be send must not contain new line
	 *            ('\n') because it is used to separate commands!
	 * @return success or not
	 */
	@Override
	/*package*/ boolean sendCommand(String cmd) {
		clientOut.println(cmd);
		return true;
	}

	/**
	 * this sets the time in second the {@link #sendCommand(String)} method
	 * shall wait for an OK zero timeout means infinite waiting for an answer
	 * 
	 * @param sec
	 *            the timeout in seconds
	 */
	public void setTimeout(int sec) {
		if (!isAlreadyRunning)
			return;

		try {
			clientSocket.setSoTimeout(sec * 1000);
		} catch (SocketException e) {
			// Error in the underlying protocol
			e.printStackTrace();
		}
	}

	@Override
	/*package*/ void shutdown() {
		if (isAlreadyRunning)
			return;
		try {
			shutdownInProgress = true;
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		portFile.delete();
	}

	@Override
	/*package*/ String waitForCommand() {
		try {
			return commandQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
