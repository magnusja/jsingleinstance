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
import java.net.UnknownHostException;
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
 * @version 0.1.1
 */
public class JSingleInstance {
	
	/**
	 * represents the version string
	 */
	public final static String VERSION = "0.1.1";
	
	/**
	 * @author MJ
	 * interface to receive commands
	 *
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
	
	public synchronized void addDataEventListener(CommandListener l) {
		commandListeners.add(l);
	}
	
	public synchronized void removeDataEventListener(CommandListener l) {
		commandListeners.remove(l);
	}
	
	private void setupClientSocket() throws UnknownHostException, IOException {
		clientSocket = new Socket("localhost", port);
		clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
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
						break; // seems like were exitting
					}
				}
			}
			
		}).start();
	}
	/**
	 * sends a command to existing instance
	 * @param cmd the command which should be send
	 * must not contain new line ('\n') because it is used to seperate commands!
	 * @return success or not
	 */
	public boolean sendCommand(String cmd) {
		if(!isAlreadyRunning) return false;
		
		clientOut.println(cmd);
		return true; //TODO wait for ok
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
	
	private synchronized void fireCommandEvent(String data) {
		CommandEvent event = new CommandEvent(this, data);
		Iterator<CommandListener> i = commandListeners.iterator();
		while(i.hasNext())
			((CommandListener) i.next()).onCommand(event);
	}
	
	
	public class ClientThread implements Runnable {
		
		Socket clientSocket;
		BufferedReader input;
		
		public ClientThread(Socket clientSocket) throws IOException {
			this.clientSocket = clientSocket;
			input = new BufferedReader(
	                new InputStreamReader(
	                    clientSocket.getInputStream()));
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					String msg = input.readLine();
					if(msg == null) break; // client closed connection
					System.out.println(msg);
					fireCommandEvent(msg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
}
