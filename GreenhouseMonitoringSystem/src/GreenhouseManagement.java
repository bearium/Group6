import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.json.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CRLReason;
import java.util.Arrays;



/**
 * This class should be running on the Server Pi and should be listening to it's UDP port for any data or ACKs.
 * @author Danilo Vucetic and Jacob Martin
 *
 */

public class GreenhouseManagement {

	private static int commandPort = 5510; //command port on the greenhouse pi
	private static int serverPort = 5511; //this is my port
	private static int commandUnreciprocated = 0; //keeps track of the number of unreciprocated commands. 
	private static boolean currentFanStatus = false;
	
	//TODO: JACOB make sure these are correct!
	private static String commandURL = "https://greenhousedata-cef98.firebaseio.com/users/TFSInAIyjZasPfyanDjsveMmdRH2/greenHouses/-LO6EC8taWQp_X6WGnS1/sensorData/Sensor1";
	private static String databaseURL = "https://greenhousedata-cef98.firebaseio.com/users/TFSInAIyjZasPfyanDjsveMmdRH2/greenHouses/-LO6EC8taWQp_X6WGnS1/sensorData/Sensor1";
	
	private static DatagramSocket socket;
	
	public static void main(String []args){
		//set up the socket
		try {
			socket = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			System.err.println("Could not open socket. Closing program");
			e.printStackTrace();
			System.exit(1);
		}
		
		while(true){
			//receive message
			byte[] buf = new byte[500];
			DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
			try{
				socket.receive(receivePacket);
			}catch(IOException ioe){
				ioe.printStackTrace();
			}
			
			//now that a packet has been received, we can find it's type and respond accordingly. 
			String message = new String(Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength()));
			System.out.println("Received Message: " + message);
			
			//Getting the packet type. Must loop through the packet until a null byte is found. If the packet does not contain a null byte the 
			//if-statement below will catch the error
			boolean doneTyping = false;
			String type = null;
			byte[] recData = Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength());
			for(int i = 0; i < receivePacket.getLength() && !doneTyping; i++){
				if(recData[i] == '\0'){
					//assuming that this is the first occurrence if null byte, then we know that the previous bytes are the string of type
					type = new String(Arrays.copyOfRange(receivePacket.getData(), 0, i));
					System.out.println("Received type: " + type);
					doneTyping = true;
				}
			}
			
			//We can only get ACK from commands, since we do not send data.. Also we should get data and error so this is checked
			if(type.equals("ACK")){
				System.out.println("ACK received for: " + CreateGreenhouseMessage.acknowledgeDecode(recData));
				commandUnreciprocated = 0;
				continue;
			}else if(type.equals("DATA")){
				System.out.println("DATA received: " + CreateGreenhouseMessage.dataDecode(recData));
				updateDatabase(CreateGreenhouseMessage.dataDecode(recData));
				acknowledge(receivePacket.getPort(), receivePacket.getAddress(), CreateGreenhouseMessage.MessageType.DATA);
			}else if(type.equals("ERROR")){
				System.err.println("ERROR received: " + CreateGreenhouseMessage.errorDecode(recData));
				sendErrorToDatabase("Error receieved from Greenhouse Pi: " + CreateGreenhouseMessage.errorDecode(recData));
			}else{
				System.err.println("Received packet is not of right type");
			}
			
			//Error checking, let the user know if the command they just sent did not work, or may not have reached the target. 
			if(commandUnreciprocated > 0){
				commandUnreciprocated = 0;
				System.err.println("Command has not been reciprocated! Sending error to server");
				sendErrorToDatabase("Command packet has not been reciprocated!");
			}
			
			
			Boolean newFanSTAT = pullFromDatabase();
			if(newFanSTAT == null){
				//error has occured
				sendErrorToDatabase("Command could not be recognized and/or read by SP");
			}else if(newFanSTAT != currentFanStatus){
				//send a command packet to change the fan status. 
				System.out.println("sending command! port = " + commandPort);
				sendCommand(commandPort, receivePacket.getAddress(), newFanSTAT);
			}
		}
	}
	
	
	/**
	 * This method, given the port and IP to respond to, will send an ACK packet to the machine
	 * @param port the port of the machine
	 * @param IPAddress the address of the machine on the network
	 * @param type the message type being acknowledged
	 */
	private static void acknowledge(int port, InetAddress IPAddress, CreateGreenhouseMessage.MessageType type){
		byte[] ack = CreateGreenhouseMessage.acknowledge(type);
		DatagramPacket packet = new DatagramPacket(ack, ack.length);
		try{
			socket.connect(IPAddress, port);
			socket.send(packet);
			socket.disconnect();
		} catch (IOException io) {
			io.printStackTrace();
			//This type of exception means that the system was unable to connect to the GP
		}
	}
	
	
	
	/**
	 * This method send the command to the command port of the GP as specified by port.
	 * @param port: command port of the GP
	 * @param IPAddress: address of the GP
	 * @param fanStatus: This is what the fan should be changed to. true meaning on, false meaning off. 
	 */
	private static void sendCommand(int port, InetAddress IPAddress, boolean fanStatus){
		byte[] ack = CreateGreenhouseMessage.command(fanStatus);
		DatagramPacket packet = new DatagramPacket(ack, ack.length);
		try{
			socket.connect(IPAddress, port);
			socket.send(packet);
			socket.disconnect();
		} catch (IOException io) {
			io.printStackTrace();
			//This type of exception means that the system was unable to connect to the GP 
		}
		commandUnreciprocated++;
	}
	
	/**
	 * This method checks the database for new commands. If a new command is pending, a command packet will be sent to the GP. 
	 * @return the fan status which will be sent in a command packet 
	 */
	private static Boolean pullFromDatabase(){
		//FIXME: JACOB comment these methods. 
		try {
			//URL from which to fetch the command from the database
			URL obj = new URL(commandURL);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			int responseCode = con.getResponseCode();
			System.out.println("GET Response Code :: " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				return Boolean.parseBoolean(response.toString());
			} else {
				System.err.println("GET request failed");
				return null;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * This method updates the firebase database
	 * @param JSON The data received from the GP
	 */
	private static void updateDatabase(String JSON){
		//FIXME: JACOB comment these methods. 
		try {
			//get the URL for the database JSON
			URL obj = new URL(databaseURL);
			
			//open an http connection then update the database JSON
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("PUT");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			// For POST only - START
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(JSON.getBytes());
			os.flush();
			os.close();
			// For POST only - END
			int responseCode = con.getResponseCode();
			System.out.println("POST Response Code :: " + responseCode);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method sends an error message to the database so that it may be displayed to the user as a push notification
	 * @param errorMessage the error message which will be sent to the database
	 */
	private static void sendErrorToDatabase(String errorMessage){
		//TODO: JACOB implement this functionality. I've already placed it in the code where needed. 
	}
	
	
}
