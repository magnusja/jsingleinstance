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

import java.io.IOException;

/**
 * General interface which describes how a specific Communication
 * strategy should behave.
 * @author MJ
 *
 */
public abstract class Communication {
	
	/**
	 * Command which forces the first instance to close itself.
	 */
	/*package*/ final static String FORCE_EXIT = "FORCE_EXIT_JSINGLE";
	
	/**
	 * Init the Communication. Here you should to things like creating
	 * communication channels, which wait for incoming messages.
	 * <p>
	 * You should especially check if another instance is running, and set a
	 * flag which is returned by {@link #isAlreadyRunning}
	 * @throws IOException
	 */
	abstract /*package*/ void init() throws IOException;
	
	/**
	 * @return true if another instance is running
	 */
	abstract /*package*/ boolean isAlreadyRunning();
	
	/**
	 * Sends a command to the running instance.
	 * @param cmd the command to be send
	 * @return true if sending was successful
	 * @throws IOException if something fails
	 */
	abstract /*package*/ boolean sendCommand(String cmd) throws IOException;
	
	/**
	 * This method waits for a command received by another instance which is started
	 * later.
	 * @return The command which was received from the seconds instance
	 * @throws IOException If something fails
	 */
	abstract /*package*/ String waitForCommand() throws IOException;
	
	/**
	 * This method shuts the hole system down. You should release all
	 * resources and other instances should not recognize you now.
	 * @throws IOException If something fails
	 */
	abstract /*package*/ void shutdown() throws IOException;
}
