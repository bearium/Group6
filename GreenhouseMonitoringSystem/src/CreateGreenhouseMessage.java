
/**
 * This class is meant to standardize the creation of messages between the greenhouse system. It should make 
 * messaging consistent. 
 * @author Danilo Vucetic
 *
 */
public abstract class CreateGreenhouseMessage {

	public enum MessageType{
		ACK,
		COMMAND,
		DATA,
		ERROR;
	}
	
	public byte[] acknowledge(MessageType ackType){
		byte[] shit = null;
		
		
		return shit;
	}
	
	
	public byte[] command(boolean newFanStatus){
		byte[] shit = null;
		
		
		return shit;
	}
	
	
	public byte[] data(String JSON){
		byte[] shit = null;
		
		
		return shit;
	}
	
	
	public byte[] error(String errorMessage){
		byte[] shit = null;
		
		
		return shit;
	}
	
}
