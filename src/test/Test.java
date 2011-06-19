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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import jsingleinstace.JSingleInstance;
import jsingleinstace.JSingleInstance.CommandEvent;
import jsingleinstace.JSingleInstance.CommandListener;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final JSingleInstance i = new JSingleInstance("./jsingle");
			if(!i.isAlreadyRunning()) {
				JFrame f = new JFrame("First instance");
				f.setSize(250, 250);
		    	f.setLocation(300,200);
		    	final JTextArea area = new JTextArea(10, 40);
		    	f.getContentPane().add(BorderLayout.CENTER, area);
		    	f.setVisible(true);
		    	
		    	i.addDataEventListener(new CommandListener() {

					@Override
					public void onCommand(final CommandEvent e) {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								area.append(e.command + "\n");
							}
							
						});
					}
					
				});
		    	
		    	
		    	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    	/*f.addWindowListener(new WindowAdapter() {
		    		public void windowClosing(WindowEvent ev) {
		    			try {
							i.exit(); // deprecated
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		                System.exit(0);
		            }
		    	});*/
			} else {
				JFrame f = new JFrame("Second instance");
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setSize(300, 100);
		    	f.setLocation(300, 200);
		    	final JTextField field = new JTextField(20);
		    	f.getContentPane().add(BorderLayout.WEST, field);
		    	JButton button = new JButton("Send");
		    	button.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						i.sendCommand(field.getText());
					}
		    		
		    	});
		    	f.getContentPane().add(BorderLayout.EAST, button);
		    	f.setVisible(true);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
