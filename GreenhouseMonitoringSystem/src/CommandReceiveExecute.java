import java.net.InetAddress;

/**
 * This Class implements Runnable and so may be run as a separate thread. Each thread is responsible for listening to a 
 * port which receives commands, and executing the command. It uses the communication protocol as defined in the design
 * document. 
 * 
 * @author Danilo Vucetic
 *
 */


public class CommandReceiveExecute implements Runnable{

	private GreenhouseData struct;
	private int commandReceivePort;
	private int serverPort;
	private InetAddress serverIP;
	
	public CommandReceiveExecute(GreenhouseData grd, int commandReceivePort, int serverPort, InetAddress serverIP){
		struct = grd;
		this.commandReceivePort = commandReceivePort;
		this.serverPort = serverPort;
		this.serverIP = serverIP;
	}
	
	public void run() {
		System.out.println("This thread is working: " + Thread.currentThread().getName());
		struct.print();
	}

	/**
	 * This method sends the JSON to the server using the communications protocol defined in the design
	 * @param JSON
	 */
	private void updateServer(String JSON){
		//TODO: code
	}
	
	/**
	 * This method send an error message to the server using the communications protocol defined in the design
	 * @param errorMessage
	 */
	private void sendErrorMessage(String errorMessage){
		//TODO: code
	}
	
	
}
