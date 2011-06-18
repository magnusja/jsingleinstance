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

public class JSingleInstance {
	
	public interface CommandListener {
		public void onCommand(CommandEvent e);
	}
	
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
	
	List<CommandListener> commandListeners = new ArrayList<CommandListener>();
	
	
	private File f;
	private boolean isAlreadyRunning = false;
	private int port;
	
	private Socket clientSocket = null;
	private ServerSocket serverSocket;
	private PrintWriter clientOut;
	
	public JSingleInstance(String path) throws IOException {
		this.f = new File(path);
		
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
	
	public boolean sendCommand(String cmd) {
		if(!isAlreadyRunning) return false;
		
		clientOut.println(cmd);
		return true; //TODO wait for ok
	}
	
	public void exit() throws IOException {
		serverSocket.close();
		f.delete();
	}
	
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
