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
		    	
		    	f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		    	f.addWindowListener(new WindowAdapter() {
		    		public void windowClosing(WindowEvent ev) {
		    			try {
							i.exit();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		                System.exit(0);
		            }
		    	});
			} else {
				JFrame f = new JFrame("Second instance");
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
