import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

import javax.bluetooth.*;

/**
 * An example of a good GUI.  All code to update the display is executed in the swing
 * "event dispatch thread".  All other code is executed in a different thread.  As a result,
 * the GUI will respond properly even if there is a lot of stuff being done by the program.
 */
public class MainFrame {
	
	private JFrame frame;
	private JPanel sortPanel;
	private static final int SORTSIZE = 50000;
	private int clickCount = 0;
	private int sortCount = 0;
	

	/**
	 * Create a frame with two buttons.  One records how often it is clicked.  The other lauches
	 * a really slow sort routine and a progress bar that tracks the sort.
	 */
	public MainFrame() {
		// the constructor is not running in the event dispatch thread
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// code here is scheduled to be run by the event dispatch thread
				launchDisplay();
			}
		});
	}

	/**
	 * Create the display.  This code is the same as the constructor in BadGUI, but it now will
	 * be called by the invokeLater above to be run by the event dispatch thread.
	 */
	private void launchDisplay() {
		
		//bluetooth elements//
		final bluetoothConnector bluetoothConnector = new bluetoothConnector();
		
		
		/*Ui Elements*/
		JButton but1 = new JButton("Find Devices");
		JButton but2 = new JButton("Connect Device");
		frame = new JFrame();
		String[] colHeadings = {"COLUMN1","COLUMN2"};
		int numRows = 5 ;
		final DefaultTableModel model = new DefaultTableModel(numRows, colHeadings.length) ;
		model.setColumnIdentifiers(colHeadings);
		final JTable list = new JTable(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sortPanel = new JPanel(new GridLayout(0,2));
		frame.getContentPane().add(sortPanel, "North");
		sortPanel.add(but1);
		sortPanel.add(but2);
		frame.getContentPane().add(list, "South");
		

		// button 1 will update itself with the number of times it is clicked
		but1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// this code is run by the event dispatch thread
				
				model.setRowCount(0);
				ArrayList<String> newRow = new ArrayList<String>();
				
				RemoteDevice[] ConnectableList = bluetoothConnector.refreshList();
				for(int i = 0; i < ConnectableList.length; i++) {
					
					newRow.clear();
					try {
						newRow.add(ConnectableList[i].getFriendlyName(true));
					} catch (IOException e1) {
						newRow.add("Unknown...");
						
					}
					newRow.add(ConnectableList[i].toString());
					model.addRow(newRow.toArray());
				}
				
				
			}
		});
		
		// button 2 will connect to selected device
				but2.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						list.getSelectedRow();
						
						
					}
				});

		frame.setSize(300,100);
		frame.setVisible(true);
	}

	//offers methods to connect bluetooth with
	public class bluetoothConnector{
		
		RemoteDevice[] connectableDevices;
		final MyDiscoveryListener bluetoothListener = new MyDiscoveryListener();
		
		//returns a list of connectable bluetooth devices
		public RemoteDevice[] refreshList(){
			try{
		        LocalDevice localDevice = LocalDevice.getLocalDevice();
		        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
		        agent.startInquiry(DiscoveryAgent.GIAC, bluetoothListener);
		        
		        try {
		            synchronized(bluetoothListener.lock){
		            	bluetoothListener.lock.wait();
		            }
		        }
		        catch (InterruptedException e1) {
		            e1.printStackTrace();
		            return null;
		        }
		        
		        
		        System.out.println("Device Inquiry Completed. ");
		        
		   
		        UUID[] uuidSet = new UUID[1];
		        uuidSet[0]=new UUID(0x1105); //OBEX Object Push service
		        
		        int[] attrIDs =  new int[] {
		                0x0100 // Service name
		        };
		        
		        for (RemoteDevice device : bluetoothListener.devices) {
		            agent.searchServices(attrIDs,uuidSet,device,bluetoothListener);
		            
		            
		            try {
		                synchronized(bluetoothListener.lock){
		                	bluetoothListener.lock.wait();
		                }
		            }
		            catch (InterruptedException e1) {
		                e1.printStackTrace();
		                return null;
		            }
		            
		            
		            System.out.println("Service search finished.");
		        }
		       
		        return connectableDevices = agent.retrieveDevices(0);
		        
		    } catch (Exception e1) {
		        e1.printStackTrace();
		    }
			return null;
		}
		
		public void connectDevice(){
			
		}
	}
	
	
	
	/** 
	 * A class used in the event dispatch thread to run code in a different thread
	 * Used for any code that will take a long time so it does not prevent the GUI from working.
	 */
	public class MyWorker extends SwingWorker<Void, Integer> {
		
		private JProgressBar bar;

		/** Store the progress bar for the sort */
		public MyWorker(JProgressBar bar) {
			this.bar = bar;
		}

		/** This method is used to run code in a different thread from the event dispatch thread */
		public Void doInBackground() {
			return null;
			// this code is not run by the event dispatch thread, instead it is run by a different thread
			
		}
	}

	/** launches the GUI */
	public static void main(String[] args) {
		new MainFrame();
	}

}
