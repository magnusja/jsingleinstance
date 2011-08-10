/**
 * JSingleInstance - allows running only one instance of any java app
 * Copyright (C) 2011 MJ <mj_dv@web.de>
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author MJ
 * a little class to allow only one instance of an application
 * {@link #JSingleInstance(String)} constructs a new object
 * {@link #isAlreadyRunning()} checks if an instance is already running
 * {@link #sendCommand(String)} send a command to the running instance
 * to receive commands just add an CommandListener
 * {@link #exit()} exits the instance, must be called when you exit your app!
 * but not if there is already an instance running
 * @version 0.2.2
 */
public class JSingleInstance {
	
	/**
	 * represents the version string
	 */
	public final static String VERSION = "0.2.2";
	private final static String FORCE_EXIT = "FORCE_EXIT_JSINGLE";
	private final static String OK = "OK_JSINGLE";
	
	/**
	 * @author MJ
	 * interface to receive commands
	 * you should return fast, because the client instance
	 * is waiting by default 5 seconds, otherwise {@link JSingleInstance#sendCommand(String)}
	 * will return false.
	 * you can change this timeout via {@link JSingleInstance#setTimeout(int)}
	 */
	public interface CommandListener {
		public void onCommand(CommandEvent e);
	}
	
	/**
	 * Represents a command event
	 * {@link CommandEvent#command} represents the command which has been sent
	 * @author MJ
	 *
	 */
	public class CommandEvent extends EventObject {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String command;
		public CommandEvent(Object source, String command) {
			super(source);
			this.command = command;
		}
	}
	
	private List<CommandListener> commandListeners = new ArrayList<CommandListener>();
	
	
	private File f;
	private boolean isAlreadyRunning = false;
	private int port;
	
	private Socket clientSocket = null;
	private ServerSocket serverSocket;
	private PrintWriter clientOut;
	private BufferedReader clientIn;
	
	/**
	 * constructs a new object
	 * @param path file where instance info (like port and pid) can be stored
	 * should be something like "USERFILES\myapp.info"
	 * @throws IOException thrown if path is not writeable or other instance
	 * has not removed the info file (eg. your app crashed)
	 */
	public JSingleInstance(String path) throws IOException {
		this.f = new File(path);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			
			public void run() {
				if(isAlreadyRunning) return;
				try {
					serverSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				f.delete();
			}
			
		});
		
		if(!f.exists())
			setupServerSocket();
		else {
			isAlreadyRunning = true;
			getPortFromFile();
			setupClientSocket();
		}
	}
	
	public synchronized void addCommandListener(CommandListener l) {
		commandListeners.add(l);
	}
	
	public synchronized void removeCommandListener(CommandListener l) {
		commandListeners.remove(l);
	}
	
	private void setupClientSocket() throws IOException {
		try {
			clientSocket = new Socket("localhost", port);
			clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
			clientIn = new BufferedReader(new InputStreamReader(
	                clientSocket.getInputStream()));
			clientSocket.setSoTimeout(5 * 1000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// seems like the app has crashed last time
			// we just ignore that and start up normal
			isAlreadyRunning = false;
			f.delete();
			setupServerSocket();
		}
	}

	private void getPortFromFile() throws FileNotFoundException {
		
		BufferedReader in = new BufferedReader(new FileReader(f));
		try {
			port = Integer.parseInt(in.readLine().trim());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setupServerSocket() throws IOException {
		serverSocket = new ServerSocket(0);
		port = serverSocket.getLocalPort();
		
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		out.write("" + port);
		out.close();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Socket clientSocket = null;
						clientSocket = serverSocket.accept();
						new Thread(new ClientThread(clientSocket)).start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break; // seems like were exiting
					}
				}
			}
			
		}).start();
	}
	
	/**
	 * this sets the time in second the {@link #sendCommand(String)} method
	 * shall wait for an OK
	 * zero timeout means infinite waiting for an answer
	 * @param sec the timeout in seconds
	 */
	public void setTimeout(int sec) {
		if(!isAlreadyRunning) return;
		
		try {
			clientSocket.setSoTimeout(sec * 1000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * sends a command to existing instance
	 * @param cmd the command which should be send
	 * must not contain new line ('\n') because it is used to separate commands!
	 * @return success or not
	 */
	public boolean sendCommand(String cmd) {
		if(!isAlreadyRunning) return false;		
		clientOut.println(cmd);
		String answer = null;
		try {
			answer = clientIn.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		if(OK.equals(answer))
			return true;
		else
			return false;
	}
	
	/**
	 * exits the instance
	 * has to be called to delete the info file and close the socket
	 * DO NOT call if there is another instance running!
	 * @throws IOException thrown if info file does not exist
	 * @deprecated since 0.1.1 this will happen automatically
	 */
	public void exit() throws IOException {
		if(isAlreadyRunning) return;
		serverSocket.close();
		f.delete();
	}
	
	/**
	 * checks if another instance is already running
	 * @return true if there is another instance running
	 */
	public boolean isAlreadyRunning() {
		return isAlreadyRunning;
	}
	
	/**
	 * this method will force the running instance
	 * to close via System.exit(-1)
	 * you can try this, if it seems like the running instance
	 * has crashed and only the jsingleinstance part is running
	 */
	public void forceExit() {
		if(!isAlreadyRunning) return;
		clientOut.println(FORCE_EXIT);
	}
	
	private synchronized void fireCommandEvent(String data) {
		CommandEvent event = new CommandEvent(this, data);
		Iterator<CommandListener> i = commandListeners.iterator();
		while(i.hasNext())
			((CommandListener) i.next()).onCommand(event);
	}
	
	
	public class ClientThread implements Runnable {
		
		Socket clientSocket;
		BufferedReader input;
		final PrintWriter output;
		
		public ClientThread(Socket clientSocket) throws IOException {
			this.clientSocket = clientSocket;
			input = new BufferedReader(
	                new InputStreamReader(
	                    clientSocket.getInputStream()));
			output = new PrintWriter(clientSocket.getOutputStream(), true);
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					final String msg = input.readLine();
					if(msg == null) break; // client closed connection
					if(FORCE_EXIT.equals(msg)) System.exit(-1);
					//System.out.println(msg);
					new Thread(new Runnable() {
						@Override
						public void run() {
							fireCommandEvent(msg);
							output.println(OK);
						}
						
					}).start();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
}
