

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

public class MyDiscoveryListener implements DiscoveryListener{
    
    static Object lock=new Object();
    
    public ArrayList<RemoteDevice> devices;
    
    public ArrayList<String> URLs = new ArrayList<String>();
    
    static OutputStream clientSession;
    
    public MyDiscoveryListener() {
        devices = new ArrayList<RemoteDevice>();
    }

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass arg1) {
        String name;
        try {
            name = btDevice.getFriendlyName(false);
        } catch (Exception e) {
            name = btDevice.getBluetoothAddress();
        }
        
        devices.add(btDevice);
        System.out.println("device found: " + name);
        
    }

    @Override
    public void inquiryCompleted(int arg0) {
        synchronized(lock){
            lock.notify();
        }
    }

    @Override
    public void serviceSearchCompleted(int arg0, int arg1) {
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        for (int i = 0; i < servRecord.length; i++) {
            String url = servRecord[i].getConnectionURL(ServiceRecord.AUTHENTICATE_NOENCRYPT, false);
            if (url == null) {
                continue;
            }
            URLs.add(url);
            DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
            if (serviceName != null) {
                System.out.println("service " + serviceName.getValue() + " found " + url);
                
                if(serviceName.getValue().equals("SPP")){
                    //sendMessageToDevice(url);                
                }
            } else {
                System.out.println("service found " + url);
            }
            
          
        }
    }
    
    public static void connectToDevice(String serverURL, RemoteDevice currentDevice){
    	try{
    		
    		
    		
    		System.out.println("Connecting to " + serverURL);

    		clientSession = (OutputStream) Connector.openOutputStream(serverURL);

            System.out.println("Connected " + serverURL);
    		
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    
    public static void send(){
    	
    	try {
			clientSession.write((byte)1);
			System.out.println("Success!");
		} catch (IOException e) {
			System.out.println("Fail...");
			e.printStackTrace();
		}
    	
    }
    
    static void sendMessageToDevice(String serverURL){
        try{
            System.out.println("Connecting to " + serverURL);
    
            ClientSession clientSession = (ClientSession) Connector.open(serverURL);
            
            
            HeaderSet hsConnectReply = clientSession.connect(null);
            if (hsConnectReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
                System.out.println("Failed to connect");
                return;
            }
    
            HeaderSet hsOperation = clientSession.createHeaderSet();
            hsOperation.setHeader(HeaderSet.NAME, "Hello.txt");
            hsOperation.setHeader(HeaderSet.TYPE, "text");
    
            //Create PUT Operation
            Operation putOperation = clientSession.put(hsOperation);
    
            // Send some text to server
            byte data[] = "Hello World !!!".getBytes("iso-8859-1");
            OutputStream os = putOperation.openOutputStream();
            os.write(data);
            os.close();
    
            putOperation.close();
    
            clientSession.disconnect(null);
    
            clientSession.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
