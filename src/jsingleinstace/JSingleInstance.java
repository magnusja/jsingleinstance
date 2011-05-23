package jsingleinstace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class JSingleInstance {
	
	private File f;
	private boolean isAlreadyRunning = false;
	private int port;
	
	private Socket clientSocket = null;
	PrintWriter clientOut;
	
	public JSingleInstance(String path) throws IOException {
		this.f = new File(path);
		
		if(!f.exists())
			setupServerSocket();
		else {
			setRunning(true);
			getPortFromFile();
			setupClientSocker();
		}
	}
	
	private void setupClientSocker() throws UnknownHostException, IOException {
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
		final ServerSocket server = new ServerSocket(0);
		port = server.getLocalPort();
		
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		out.write("" + port);
		out.close();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Socket clientSocket = null;
						clientSocket = server.accept();
						new Thread(new ClientThread(clientSocket)).start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
	
	public File getF() {
		return f;
	}
	public void setF(File f) {
		this.f = f;
	}
	public boolean isRunning() {
		return isAlreadyRunning;
	}
	public void setRunning(boolean isRunning) {
		this.isAlreadyRunning = isRunning;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
}
