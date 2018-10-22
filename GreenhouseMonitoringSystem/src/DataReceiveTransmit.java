import java.net.InetAddress;

/**
 * This Class implements Runnable and so may be run as a separate thread. Each thread is responsible for listening to a 
 * USB serial port which receives data from the Arduino. It uses the communication protocol as defined in the design
 * document to send the data to the server.
 * 
 * @author Danilo Vucetic
 *
 */
public class DataReceiveTransmit implements Runnable{

	private GreenhouseData struct;
	private int receiveTransmitPort;
	private int serverPort;
	private InetAddress serverIP;
	
	public DataReceiveTransmit(GreenhouseData grd, int receiveTransmitPort, int serverPort, InetAddress serverIP){
		struct = grd;
		this.receiveTransmitPort = receiveTransmitPort;
		this.serverPort = serverPort;
		this.serverIP = serverIP;
	}
	
	public void run() {
		System.out.println("This thread is working: " + Thread.currentThread().getName());
		struct.print();
	}

	
	private void turnFanOn(){
		//TODO: code, make sure to change the data structure
	}
	
	
	private void turnFanOff(){
		//TODO: code, make sure to change the data structure
	}
	
	
	private void acknowledge(){
		//TODO code
	}
	
}
