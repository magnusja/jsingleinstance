package test;

import java.io.IOException;

import jsingleinstace.JSingleInstance;
import jsingleinstace.JSingleInstance.CommandEvent;
import jsingleinstace.JSingleInstance.CommandListener;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JSingleInstance i = new JSingleInstance("/tmp/jsingle");
			i.addDataEventListener(new CommandListener() {

				@Override
				public void onCommand(CommandEvent e) {
					
				}
				
			});
			System.out.println(i.sendCommand("test123"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
