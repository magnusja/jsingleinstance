package jsingleinstace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

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
			String msg = input.readLine();
			System.out.println(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
