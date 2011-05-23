package test;

import java.io.IOException;

import jsingleinstace.JSingleInstance;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JSingleInstance i = new JSingleInstance("/tmp/jsingle");
			System.out.println(i.sendCommand("test123"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
