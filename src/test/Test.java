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

package test;

import java.io.IOException;

import jsingleinstace.Communication;
import jsingleinstace.JSingleInstance;
import jsingleinstace.SocketCommunication;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Communication c = new SocketCommunication("./jsingle");
			
			final JSingleInstance i = new JSingleInstance(c);
			if(i.isAlreadyRunning()) {
				SecondInstance si = new SecondInstance(i);
				si.setVisible(true);
			} else {
				FirstInstance fi = new FirstInstance(i);
				fi.setVisible(true);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
