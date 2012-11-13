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

package jsingleinstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

/**
 * A little class to allow only one instance of an application.
 * <UL>
 * <LI> {@link #JSingleInstance(Communication)} constructs a new object.
 * <LI> {@link #isAlreadyRunning()} checks if an instance is already running.
 * <LI> {@link #sendCommand(String)} send a command to the running instance.
 * <LI> To receive commands just add an CommandListener via {@link #addCommandListener(CommandListener)}.
 * </UL>
 * @version 0.2.2
 * @author MJ
 */
public class JSingleInstance {
	
	private class ReceiveThread implements Runnable {
		
		@Override
		public void run() {
			try {
				while(true) {
					final String msg = communication.waitForCommand();
					if(msg == null) break;
					if(Communication.FORCE_EXIT.equals(msg)) System.exit(-1);
					
					new Thread(new Runnable() {
						@Override
						public void run() {
							fireCommandEvent(msg);							
						}
						
					}).start();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * represents the version string
	 */
	public final static String VERSION = "0.2.2";
	
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
	
	private Communication communication;
	
	/**
	 * constructs a new object
	 * @param communication {@link Communication} Object which shall be used for
	 * the communication between the instances.
	 * @throws IOException thrown if path is not writeable, other instance
	 * has not removed the info file (eg. your app crashed) or the file 
	 * exists but was not previously created by our own
	 */
	public JSingleInstance(final Communication communication) throws IOException {
		this.communication = communication;
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			
			public void run() {
				try {
					communication.shutdown();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		communication.init();
		
		if(!communication.isAlreadyRunning())
			new Thread(new ReceiveThread()).start();
	}
	
	public synchronized void addCommandListener(CommandListener l) {
		commandListeners.add(l);
	}
	
	public synchronized void removeCommandListener(CommandListener l) {
		commandListeners.remove(l);
	}
		
	/**
	 * Checks if another instance is already running.
	 * @return true if there is another instance running
	 */
	public boolean isAlreadyRunning() {
		return communication.isAlreadyRunning();
	}
	
	/**
	 * This method will force the running instance to close via System.exit(-1).
	 * You can try this, if it seems like the running instance has crashed and 
	 * only the jsingleinstance part is running.
	 * @throws IOException 
	 */
	public void forceExit() throws IOException {
		communication.sendCommand(Communication.FORCE_EXIT);
	}
	
	/**
	 * Sends a command to existing instance.
	 * @param cmd the command which should be send
	 * @return true if success
	 * @throws IOException If something fails
	 */
	public boolean sendCommand(String cmd) throws IOException {
		return communication.sendCommand(cmd);
	}
	
	private synchronized void fireCommandEvent(String data) {
		CommandEvent event = new CommandEvent(this, data);
		Iterator<CommandListener> i = commandListeners.iterator();
		while(i.hasNext())
			((CommandListener) i.next()).onCommand(event);
	}
}

